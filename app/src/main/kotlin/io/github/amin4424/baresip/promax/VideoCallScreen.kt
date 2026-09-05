package io.github.amin4424.baresip.promax

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideoCameraBack
import androidx.compose.material.icons.filled.VideoCameraFront
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay

private const val VIDEO_CALL_TAG = "VideoCallScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoCallScreen(
    ctx: Context,
    viewModel: ViewModel,
    call: Call,
    onCloseVideo: () -> Unit,
    onHangup: () -> Unit = {
        call.terminated.value = true
        call.hangup(0, "")
        onCloseVideo()
    }
) {
    var isFrontCamera by remember { mutableStateOf(true) }
    val isSpeakerOn by viewModel.isSpeakerOn.collectAsState()
    val micIcon by viewModel.micIcon.collectAsState()
    var isMicMuted by remember { mutableStateOf(BaresipService.isMicMuted) }
    var isCameraMuted by remember { mutableStateOf(Camera2.isCameraMuted) }

    var controlsVisible by remember { mutableStateOf(true) }
    var showDialpad by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    LaunchedEffect(micIcon) {
        isMicMuted = micIcon == Icons.Filled.MicOff || BaresipService.isMicMuted
    }

    LaunchedEffect(call.callp, call.hasVideo(), call.videoCall) {
        if (call.hasVideo() || call.videoCall) {
            call.startVideoDisplay()
        }
    }

    // Keep screen on during video call
    DisposableEffect(Unit) {
        val window = (ctx as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Handle back button to exit video view
    BackHandler(enabled = true) {
        onCloseVideo()
    }

    // Auto-hide controls after 5 seconds of inactivity
    LaunchedEffect(controlsVisible) {
        if (controlsVisible) {
            delay(5000)
            controlsVisible = false
        }
    }

    val contact = remember(call.peerUri) {
        Contact.findContact(call.peerUri)
    }
    val callerName = Utils.friendlyUri(ctx, call.peerUri, call.ua.account)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                controlsVisible = !controlsVisible
            }
    ) {
        // 1. Fullscreen Native Video Surface
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                val videoView = VideoView(context)
                videoView.surfaceView.apply {
                    setZOrderMediaOverlay(true)
                }
                videoView.surfaceView
            }
        )

        // Remote Camera Off Overlay
        if (!call.remoteVideo.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        modifier = Modifier.size(108.dp),
                        shape = CircleShape,
                        color = Color(0xFF1E293B),
                        border = BorderStroke(2.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        when (contact) {
                            is Contact.BaresipContact -> {
                                val bmp = contact.avatarImage
                                if (bmp != null) {
                                    Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(contact.colorInt())),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = contact.name.firstOrNull()?.uppercase() ?: "",
                                            color = Color.White,
                                            fontSize = 44.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            is Contact.AndroidContact -> {
                                val uri = contact.thumbnailUri
                                if (uri != null) {
                                    coil.compose.AsyncImage(
                                        model = uri,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(contact.colorInt())),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = contact.name.firstOrNull()?.uppercase() ?: "",
                                            color = Color.White,
                                            fontSize = 44.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            null -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(54.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = callerName,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VideocamOff,
                                contentDescription = null,
                                tint = Color(0xFFEA4335),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.camera_off),
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Central On-Hold Banner
        val isOnHold = call.callOnHold.value || call.showOnHoldNotice.value || call.held || call.onhold
        if (isOnHold) {
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .shadow(16.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xDD0F172A),
                border = BorderStroke(1.5.dp, Color(0xFFF59E0B))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PauseCircle,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.call_is_on_hold),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 2. Top Scrim Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.75f), Color.Transparent)
                    )
                )
        )

        // 3. Bottom Scrim Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
        )

        // 4. Top Floating Glassmorphic Header (HUD)
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(animationSpec = tween(300)) + slideInVertically(initialOffsetY = { -it / 2 }),
            exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(targetOffsetY = { -it / 2 }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                color = Color(0x991E1E24),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Minimize / Audio Switch Button
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.12f), CircleShape)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onCloseVideo
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Minimize Video",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Remote Peer Details & Timer
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    ) {
                        // Contact Mini Avatar
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            when (contact) {
                                is Contact.BaresipContact -> {
                                    val bmp = contact.avatarImage
                                    if (bmp != null) {
                                        Image(
                                            bitmap = bmp.asImageBitmap(),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color(contact.colorInt())),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = contact.name.firstOrNull()?.uppercase() ?: "",
                                                color = Color.White,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                is Contact.AndroidContact -> {
                                    val uri = contact.thumbnailUri
                                    if (uri != null) {
                                        coil.compose.AsyncImage(
                                            model = uri,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color(contact.colorInt())),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = contact.name.firstOrNull()?.uppercase() ?: "",
                                                color = Color.White,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                null -> {
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = callerName,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CallTimerDisplay(
                                    initialDurationSeconds = call.duration().toLong(),
                                    modifier = Modifier
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                // HD Badge
                                Surface(
                                    color = Color(0x3300E676),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, Color(0x6600E676))
                                ) {
                                    Text(
                                        text = "HD",
                                        color = Color(0xFF00E676),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                                if (isOnHold) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = Color(0x33F59E0B),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, Color(0xFFF59E0B))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(Color(0xFFF59E0B), CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = stringResource(R.string.on_hold).uppercase(),
                                                color = Color(0xFFF59E0B),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Flip Camera Button
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.12f), CircleShape)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    val next = !isFrontCamera
                                    isFrontCamera = next
                                    try {
                                        Log.d(VIDEO_CALL_TAG, "Switching video source front=$next")
                                        call.setVideoSource(next)
                                    } catch (e: Exception) {
                                        Log.e(VIDEO_CALL_TAG, "Failed to switch video source: ${e.message}")
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFrontCamera) Icons.Filled.VideoCameraFront else Icons.Filled.VideoCameraBack,
                            contentDescription = "Switch Camera",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // 5. Bottom Floating Action Dock (HUD)
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(animationSpec = tween(300)) + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(targetOffsetY = { it / 2 }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(36.dp)),
                shape = RoundedCornerShape(36.dp),
                color = Color(0xB21A1A22),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Camera Mute / Video Transmission Toggle
                    VideoActionButton(
                        icon = if (!isCameraMuted) Icons.Filled.Videocam else Icons.Filled.VideocamOff,
                        label = stringResource(R.string.video),
                        isActive = !isCameraMuted,
                        activeColor = Color(0xFF2ABB86),
                        onClick = {
                            val targetMuted = !isCameraMuted
                            isCameraMuted = targetMuted
                            Camera2.isCameraMuted = targetMuted
                        }
                    )

                    // Microphone Toggle
                    VideoActionButton(
                        icon = if (isMicMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                        label = if (isMicMuted) stringResource(R.string.unmute) else stringResource(R.string.mute),
                        isActive = isMicMuted,
                        activeColor = Color(0xFFEA4335),
                        onClick = {
                            val targetMute = !isMicMuted
                            isMicMuted = targetMute
                            BaresipService.setMicMute(targetMute)
                            if (targetMute) viewModel.updateMicIcon(Icons.Filled.MicOff) else viewModel.updateMicIcon(Icons.Filled.Mic)
                        }
                    )

                    // Speakerphone Toggle
                    VideoActionButton(
                        icon = if (isSpeakerOn) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                        label = stringResource(R.string.speaker),
                        isActive = isSpeakerOn,
                        activeColor = Color(0xFF2ABB86),
                        onClick = {
                            BaresipService.instance?.toggleSpeakerphone()
                        }
                    )

                    // Keypad Toggle
                    VideoActionButton(
                        icon = Icons.Filled.Dialpad,
                        label = stringResource(R.string.keypad),
                        isActive = false,
                        activeColor = MaterialTheme.colorScheme.primary,
                        onClick = { showDialpad = true }
                    )

                    // Info / Audio & Video Stats
                    VideoActionButton(
                        icon = Icons.Outlined.Info,
                        label = stringResource(R.string.info),
                        isActive = false,
                        activeColor = MaterialTheme.colorScheme.primary,
                        onClick = { showInfoDialog = true }
                    )

                    // End Call (Hang up) Button
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .shadow(8.dp, CircleShape)
                            .background(Color(0xFFEA4335), CircleShape)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                Log.d(VIDEO_CALL_TAG, "Hanging up call ${call.callp} from video screen")
                                onHangup()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CallEnd,
                            contentDescription = stringResource(R.string.hangup),
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
        }
    }

    // DTMF Dialpad Bottom Sheet
    if (showDialpad) {
        ModalBottomSheet(
            onDismissRequest = { showDialpad = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color(0xFF090E1A),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .size(width = 44.dp, height = 4.dp)
                        .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(2.dp))
                )
            }
        ) {
            InCallDialpadSheet(
                dtmfText = call.dtmfText.value,
                onSendDtmf = { digit ->
                    call.sendDigit(digit)
                    call.dtmfText.value += digit
                },
                onDismiss = { showDialpad = false }
            )
        }
    }

    // Call Stats & Codecs Dialog
    if (showInfoDialog) {
        CallInfoDialog(
            ctx = ctx,
            call = call,
            onDismiss = { showInfoDialog = false }
        )
    }
}

@Composable
private fun VideoActionButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        if (isActive) activeColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.12f),
        label = "videoBtnBg"
    )
    val iconTint by animateColorAsState(
        if (isActive) activeColor else Color.White,
        label = "videoBtnTint"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(backgroundColor, CircleShape)
                .border(
                    if (isActive) BorderStroke(1.5.dp, activeColor) else BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    CircleShape
                )
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) activeColor else Color.White.copy(alpha = 0.85f),
            maxLines = 1
        )
    }
}
