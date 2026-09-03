package com.tutpro.baresip.plus

import android.Manifest
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Range
import android.view.OrientationEventListener
import android.view.Surface
import androidx.annotation.RequiresPermission
import java.nio.ByteBuffer
import java.util.concurrent.Executor

class Camera2(
    private val w: Int,
    private val h: Int,
    private val fps: Int,
    private val userData: Long
) {

    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var bgThread: HandlerThread? = null
    private var bgHandler: Handler? = null
    private var imageReader: ImageReader? = null
    private var previewSurface: Surface? = null
    private var isRunning = false
    private var orientationListener: OrientationEventListener? = null
    private var sensorOrientation = 0
    private var isFrontFacing = false

    fun startBackground() {
        bgThread = HandlerThread("CameraBg").apply { start() }
        bgHandler = Handler(bgThread!!.looper)
    }

    fun stopBackground() {
        bgThread?.let {
            it.quitSafely()
            try {
                it.join()
            } catch (_: InterruptedException) {
            }
        }
        bgThread = null
        bgHandler = null
        Log.d("Camera2", "stopBackground")
    }

    private var lastPreviewSurface: Surface? = null
    private var lastFacing: Int = 0

    fun muteCamera(mute: Boolean) {
        Log.d(TAG, "muteCamera($mute), currently isRunning=$isRunning")
        try {
            setMute(userData, mute)
        } catch (_: Exception) {}
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    @Suppress("unused")
    fun startCamera(previewSurface: Surface?, facing: Int) {
        this.lastPreviewSurface = previewSurface
        this.lastFacing = facing
        this.previewSurface = previewSurface
        isFrontFacing = (facing == CameraCharacteristics.LENS_FACING_FRONT)
        startBackground()
        val width = if (w > 0) w else (BaresipService.videoSize.width.takeIf { it > 0 } ?: 640)
        val height = if (h > 0) h else (BaresipService.videoSize.height.takeIf { it > 0 } ?: 480)
        try {
            imageReader = ImageReader.newInstance(width, height, ImageFormat.YUV_420_888, 3).apply {
                setOnImageAvailableListener(imageAvailListener, bgHandler)
            }
        } catch (e: Exception) {
            Log.e("Camera2", "Failed to create ImageReader: ${e.message}")
            return
        }
        isRunning = true
        try {
            val cameraId = getCameraId(facing) ?: cameraManager?.cameraIdList?.firstOrNull()
            if (cameraId == null) {
                Log.e(TAG, "No camera found for facing $facing")
                return
            }
            Log.d(TAG, "Starting camera id=$cameraId facing=$facing")
            val characteristics = cameraManager?.getCameraCharacteristics(cameraId)
            sensorOrientation = characteristics?.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 0
            currentInstance = this
            try {
                setMute(userData, isCameraMuted)
            } catch (_: Exception) {}
            cameraManager?.openCamera(cameraId, camStateCallback, bgHandler)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open camera: ${e.message}")
            e.printStackTrace()
            bgHandler?.postDelayed({
                if (isRunning && cameraDevice == null) {
                    try {
                        val retryId = getCameraId(facing) ?: cameraManager?.cameraIdList?.firstOrNull()
                        if (retryId != null) {
                            cameraManager?.openCamera(retryId, camStateCallback, bgHandler)
                        }
                    } catch (ex: Exception) {
                        Log.e(TAG, "Retry openCamera failed: ${ex.message}")
                    }
                }
            }, 300)
        }

        if (orientationListener == null && appContext != null) {
            orientationListener = object : OrientationEventListener(appContext) {
                override fun onOrientationChanged(orientation: Int) {
                    if (orientation == ORIENTATION_UNKNOWN) return
                    val rotation = when (orientation) {
                        in 45 until 135 -> 270
                        in 135 until 225 -> 180
                        in 225 until 315 -> 90
                        else -> 0
                    }
                    val degrees = if (isFrontFacing)
                        (sensorOrientation + rotation) % 360
                    else
                        (sensorOrientation - rotation + 360) % 360
                    setRotation(userData, degrees)
                }
            }
        }
        orientationListener?.enable()
    }

    @Throws(CameraAccessException::class)
    private fun getCameraId(facing: Int): String? {
        cameraManager?.cameraIdList?.forEach { id ->
            val c = cameraManager!!.getCameraCharacteristics(id)
            val lensFacing = c.get(CameraCharacteristics.LENS_FACING)
            if (lensFacing != null && lensFacing == facing) return id
        }
        return null
    }

    @Suppress("unused")
    fun stopCamera() {
        Log.d(TAG, "stopCamera")
        if (!isRunning) return
        isRunning = false
        if (currentInstance == this) {
            currentInstance = null
        }
        orientationListener?.disable()
        try {
            captureSession?.stopRepeating()
            captureSession?.abortCaptures()
        } catch (_: Exception) {}
        captureSession?.close()
        captureSession = null
        cameraDevice?.close()
        cameraDevice = null
        imageReader?.close()
        imageReader = null
    }

    private val imageAvailListener = ImageReader.OnImageAvailableListener { reader ->
        if (!isRunning) return@OnImageAvailableListener
        val image = reader.acquireLatestImage() ?: return@OnImageAvailableListener

        val planes = image.planes
        val plane0: ByteBuffer = planes[0].buffer
        val plane1: ByteBuffer? = if (planes.size > 1) planes[1].buffer else null
        val plane2: ByteBuffer? = if (planes.size > 2) planes[2].buffer else null

        pushFrame(
            userData,
            plane0,
            planes[0].rowStride,
            planes[0].pixelStride,
            plane1,
            plane1?.let { planes[1].rowStride } ?: 0,
            plane1?.let { planes[1].pixelStride } ?: 0,
            plane2,
            plane2?.let { planes[2].rowStride } ?: 0,
            plane2?.let { planes[2].pixelStride } ?: 0
        )
        image.close()
    }

    private val camStateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            Log.d(TAG, "Camera ${camera.id} onOpened")
            cameraDevice = camera

            try {
                val builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                val targets = mutableListOf<Surface>()

                previewSurface?.let {
                    builder.addTarget(it)
                    targets.add(it)
                }

                imageReader?.surface?.let {
                    builder.addTarget(it)
                    targets.add(it)
                }

                try {
                    builder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range(fps, fps))
                } catch (e: Exception) {
                    Log.w(TAG, "Could not set AE target fps $fps: ${e.message}")
                }

                val outputConfigs = targets.map { OutputConfiguration(it) }
                val executor = Executor { command -> bgHandler?.post(command) }
                val sessionConfig = SessionConfiguration(
                    SessionConfiguration.SESSION_REGULAR,
                    outputConfigs,
                    executor,
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(session: CameraCaptureSession) {
                            Log.d(TAG, "CaptureSession onConfigured for camera ${camera.id}")
                            captureSession = session
                            try {
                                session.setRepeatingRequest(builder.build(), null, bgHandler)
                            } catch (e: CameraAccessException) {
                                Log.e(TAG, "setRepeatingRequest failed: ${e.message}")
                                e.printStackTrace()
                            }
                        }
                        override fun onConfigureFailed(session: CameraCaptureSession) {
                            Log.e(TAG, "CaptureSession onConfigureFailed for camera ${camera.id}")
                        }
                    }
                )

                camera.createCaptureSession(sessionConfig)

            } catch (e: Exception) {
                Log.e(TAG, "Error in onOpened: ${e.message}")
                e.printStackTrace()
            }
        }

        override fun onDisconnected(camera: CameraDevice) {
            Log.d(TAG, "Camera ${camera.id} onDisconnected")
            camera.close()
            if (cameraDevice == camera) {
                cameraDevice = null
            }
        }

        override fun onError(camera: CameraDevice, error: Int) {
            Log.e(TAG, "CameraDevice ${camera.id} onError code=$error")
            try {
                camera.close()
            } catch (_: Exception) {}
            if (cameraDevice == camera) {
                cameraDevice = null
            }
            if (isRunning && (error == ERROR_CAMERA_IN_USE || error == ERROR_MAX_CAMERAS_IN_USE || error == ERROR_CAMERA_DEVICE)) {
                Log.w(TAG, "Camera in use or error $error, retrying openCamera in 250ms...")
                bgHandler?.postDelayed({
                    if (isRunning && cameraDevice == null) {
                        try {
                            val retryId = getCameraId(lastFacing) ?: cameraManager?.cameraIdList?.firstOrNull()
                            if (retryId != null) {
                                cameraManager?.openCamera(retryId, this, bgHandler)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Retry openCamera failed: ${e.message}")
                        }
                    }
                }, 250)
            }
        }

        override fun onClosed(camera: CameraDevice) {
            Log.d(TAG, "CameraDevice ${camera.id} onClosed")
            if (!isRunning) {
                stopBackground()
            }
        }
    }

    @Suppress("KotlinJniMissingFunction")
    external fun pushFrame(
        userData: Long,
        plane0: ByteBuffer, rowStride0: Int, pixStride0: Int,
        plane1: ByteBuffer?, rowStride1: Int, pixStride1: Int,
        plane2: ByteBuffer?, rowStride2: Int, pixStride2: Int
    )

    @Suppress("KotlinJniMissingFunction")
    external fun setRotation(userData: Long, degrees: Int)

    @Suppress("KotlinJniMissingFunction")
    external fun setMute(userData: Long, muted: Boolean)

    companion object {
        private var cameraManager: CameraManager? = null
        private var appContext: Context? = null
        private var currentInstance: Camera2? = null

        var isCameraMuted: Boolean = false
            set(value) {
                field = value
                currentInstance?.muteCamera(value)
            }

        @JvmStatic
        fun setCameraManager(cm: CameraManager) {
            cameraManager = cm
        }

        @JvmStatic
        fun setContext(ctx: Context) {
            appContext = ctx.applicationContext
        }
    }
}
