package com.tutpro.baresip.plus

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.telecom.TelecomManager
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.tutpro.baresip.plus.BaresipService.Companion.uas
import kotlinx.coroutines.delay

private const val CALL_SCREEN_TAG = "CallScreen"

fun NavGraphBuilder.callScreenRoute(navController: NavController, viewModel: ViewModel) {
    composable("call") {
        CallScreen(navController, viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CallScreen(navController: NavController, viewModel: ViewModel) {
    val ctx = LocalContext.current
    val selectedAor by viewModel.selectedAor.collectAsState()
    val ua = uas.value.find { it.account.aor == selectedAor }
    val focusedCall by viewModel.focusedCall.collectAsState()
    val calls by viewModel.calls.collectAsState()

    // Active call resolution
    val call = ua?.currentCall() ?: focusedCall ?: calls.lastOrNull() ?: BaresipService.calls.lastOrNull()
    val status = call?.status?.value ?: "idle"

    val isSpeakerOn by viewModel.isSpeakerOn.collectAsState()
    val micIcon by viewModel.micIcon.collectAsState()
    var isMicMuted by remember { mutableStateOf(BaresipService.isMicMuted) }
    var isRecording by remember { mutableStateOf(BaresipService.isRecOn) }
    var showDialpad by remember { mutableStateOf(false) }

    var showTransferDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    val isConnected = status == "connected" || status == "transferring"
    val isIncoming = status == "incoming"
    val isOutgoing = status == "outgoing" || status == "calling" || status == "ringing"
    val isHold = call?.onhold == true || (call?.callOnHold?.value == true && call?.showOnHoldNotice?.value != true)
    val isHeldByPeer = (call?.showOnHoldNotice?.value == true || call?.held == true) && call?.onhold != true
    val isOnHold = isHold || isHeldByPeer || call?.callOnHold?.value == true
    val isCalling = status == "calling"
    val isRinging = status == "ringing"
    val isIncomingVideo = isIncoming && (call?.hasVideo() == true || call?.videoCall == true)
    val isVideoCall = call?.videoCall == true || call?.hasVideo() == true

    var userClosedVideo by remember(call?.callp) { mutableStateOf(false) }
    var showVideoLayout by remember(call?.callp) { mutableStateOf(isVideoCall) }
    var isAnsweringVideo by remember(call?.callp) { mutableStateOf(false) }
    var isCameraMuted by remember { mutableStateOf(Camera2.isCameraMuted) }

    LaunchedEffect(isConnected, status, call?.hasVideo(), call?.videoCall) {
        if ((isConnected || status == "answered") && (call?.hasVideo() == true || call?.videoCall == true)) {
            showVideoLayout = true
        }
    }

    LaunchedEffect(micIcon) {
        isMicMuted = micIcon == Icons.Filled.MicOff || BaresipService.isMicMuted
    }

    // Keep screen on during active call
    DisposableEffect(Unit) {
        val window = (ctx as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Auto-return to main if call is terminated or no calls are active
    LaunchedEffect(call?.terminated?.value, call?.rejected, calls.size, status) {
        if (call == null || call.terminated.value || call.rejected || calls.isEmpty() || status == "disconnected" || status == "idle") {
            Log.d(CALL_SCREEN_TAG, "Call terminated or no active calls, returning to main")
            navController.navigate("main") {
                popUpTo("main") { inclusive = true }
            }
        }
    }

    // Intercept back button to return safely to main without killing call
    BackHandler(enabled = true) {
        navController.navigate("main") {
            popUpTo("main") { inclusive = true }
        }
    }

    val greenColor = Color(0xFF2ABB86)
    val redColor = Color(0xFFEA4335)
    val accentGreen = Color(0xFF00C853)
    val yellowColor = Color(0xFFF9A825)

    val isDark = isSystemInDarkTheme() || BaresipService.darkTheme.value

    val bgGradientColors = if (isDark) {
        listOf(Color(0xFF070B14), Color(0xFF0D1527), Color(0xFF060910))
    } else {
        listOf(Color(0xFFFFFFFF), Color(0xFFF1F5F9), Color(0xFFE2E8F0))
    }
    val titleTextColor = if (isDark) Color.White else Color(0xFF0F172A)
    val subtitleTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    val iconButtonBg = if (isDark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.05f)
    val iconButtonTint = if (isDark) Color.White else Color(0xFF0F172A)

    val statusBadgeBg = if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.85f)
    val statusBadgeBorder = BorderStroke(1.dp, if (isOnHold) yellowColor.copy(alpha = 0.5f) else accentGreen.copy(alpha = 0.5f))

    val avatarSurfaceBg = if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF)
    val numberBadgeBg = if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.85f)
    val numberBadgeBorder = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFE2E8F0))

    // Resolve Contact for Avatar & Name
    val contact = remember(call?.peerUri) {
        if (call != null) Contact.findContact(call.peerUri) else null
    }

    // Guard: If there is no call, or the call has been terminated/rejected/disconnected, do not render any in-call UI!
    val isTerminated = call == null || call.terminated.value || call.rejected || status == "disconnected" || status == "idle"
    if (isTerminated) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) Color(0xFF070B14) else Color(0xFFFFFFFF))
        )
        return
    }

    // 1. Direct Video Screen: launch immediately if connected, answered, or answering with video!
    val shouldShowVideo = (showVideoLayout || isVideoCall || isAnsweringVideo) && !userClosedVideo
    if ((isConnected || status == "answered" || isAnsweringVideo) && shouldShowVideo) {
        VideoCallScreen(
            ctx = ctx,
            viewModel = viewModel,
            call = call,
            onCloseVideo = {
                userClosedVideo = true
                showVideoLayout = false
                isAnsweringVideo = false
            },
            onHangup = {
                call.terminated.value = true
                call.hangup(0, "")
                navController.navigate("main") {
                    popUpTo("main") { inclusive = true }
                }
            }
        )
        return
    }

    // 2. Incoming Call Screen: luxury full-screen experience
    if (isIncoming && !isAnsweringVideo) {
        IncomingCallScreen(
            ctx = ctx,
            call = call,
            contact = contact,
            isVideoCall = isIncomingVideo,
            onAnswerVideo = { cameraOn ->
                isCameraMuted = !cameraOn
                Camera2.isCameraMuted = !cameraOn
                isAnsweringVideo = true
                showVideoLayout = true
                call.videoCall = true
                answerCall(ctx, call, video = true)
            },
            onAnswerAudio = { answerCall(ctx, call, video = false) },
            onDecline = {
                call.terminated.value = true
                rejectCall(call)
                navController.navigate("main") {
                    popUpTo("main") { inclusive = true }
                }
            }
        )
        return
    }

    // 3. Outgoing Call Screen: luxury full-screen experience
    if (isOutgoing && !isConnected) {
        OutgoingCallScreen(
            ctx = ctx,
            call = call,
            contact = contact,
            status = status,
            isVideoCall = isVideoCall,
            isMicMuted = isMicMuted,
            isSpeakerOn = isSpeakerOn,
            isRecording = isRecording,
            isMobileAccount = ua?.account?.isMobile ?: false,
            isCameraMuted = isCameraMuted,
            onToggleCamera = {
                val targetMuted = !isCameraMuted
                isCameraMuted = targetMuted
                Camera2.isCameraMuted = targetMuted
            },
            onToggleMute = {
                val newMute = !BaresipService.isMicMuted
                BaresipService.setMicMute(newMute)
                if (newMute) viewModel.updateMicIcon(Icons.Filled.MicOff) else viewModel.updateMicIcon(Icons.Filled.Mic)
                isMicMuted = newMute
            },
            onToggleSpeaker = {
                BaresipService.instance?.toggleSpeakerphone()
            },
            onToggleRecord = {
                val nextRec = !BaresipService.isRecOn
                BaresipService.isRecOn = nextRec
                if (nextRec) {
                    Api.module_load("sndfile")
                    Toast.makeText(ctx, ctx.getString(R.string.recording_started), Toast.LENGTH_SHORT).show()
                } else {
                    Api.module_unload("sndfile")
                    Toast.makeText(ctx, ctx.getString(R.string.recording_stopped), Toast.LENGTH_SHORT).show()
                }
                isRecording = nextRec
            },
            onHangup = {
                call.terminated.value = true
                call.hangup(0, "")
                navController.navigate("main") {
                    popUpTo("main") { inclusive = true }
                }
            }
        )
        return
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = if (isDark) Color(0xFF070B14) else Color(0xFFFFFFFF)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colors = bgGradientColors))
                .padding(padding)
        ) {
            // Ambient glowing aura in center
            Box(
                modifier = Modifier
                    .size(340.dp)
                    .align(Alignment.Center)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00B0FF).copy(alpha = if (isDark) 0.12f else 0.10f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Navigation & Security Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    // Minimize Call Screen Button
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(iconButtonBg, CircleShape)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    navController.navigate("main") {
                                        popUpTo("main") { inclusive = true }
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Minimize",
                            tint = iconButtonTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Center Status Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = statusBadgeBg,
                        border = statusBadgeBorder
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(if (isOnHold) yellowColor else accentGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isOnHold) stringResource(R.string.on_hold).uppercase() else stringResource(R.string.connected).uppercase(),
                                color = if (isOnHold) yellowColor else accentGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // ZRTP Security Lock Icon
                    if (call != null && call.securityIconTint.value != -1) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(iconButtonBg, CircleShape)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { showSecurityDialog = true }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (call.securityIconTint.value == R.color.colorTrafficRed)
                                    Icons.Filled.LockOpen
                                else
                                    Icons.Filled.Lock,
                                contentDescription = "Security",
                                tint = colorResource(call.securityIconTint.value),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(40.dp))
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Contact Avatar Container with Gradient Border and Shadow
                Box(contentAlignment = Alignment.BottomEnd) {
                    Surface(
                        modifier = Modifier
                            .size(136.dp)
                            .shadow(24.dp, CircleShape, spotColor = if (isDark) Color(0xFF00B0FF) else Color(0xFF00B0FF).copy(alpha = 0.35f)),
                    shape = CircleShape,
                    color = avatarSurfaceBg,
                    border = BorderStroke(
                        2.5.dp,
                        Brush.linearGradient(listOf(Color(0xFF00B0FF), Color(0xFF38BDF8)))
                    )
                ) {
                    when (contact) {
                        is Contact.BaresipContact -> {
                            val avatarBitmap = contact.avatarImage
                            if (avatarBitmap != null) {
                                Image(
                                    bitmap = avatarBitmap.asImageBitmap(),
                                    contentDescription = "Avatar",
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
                                        fontSize = 52.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        is Contact.AndroidContact -> {
                            val thumbUri = contact.thumbnailUri
                            if (thumbUri != null) {
                                coil.compose.AsyncImage(
                                    model = thumbUri,
                                    contentDescription = "Avatar",
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
                                        fontSize = 52.sp,
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
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(68.dp)
                                )
                            }
                        }
                    }
                }

                // Call Type Indicator Badge (Bottom End)
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .padding(3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (isIncomingVideo) greenColor else if (isConnected) accentGreen else if (isIncoming) greenColor else MaterialTheme.colorScheme.primary,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isIncomingVideo) {
                            Icon(
                                imageVector = Icons.Filled.Videocam,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Caller Name & Number
            val callerName = if (call != null) Utils.friendlyUri(ctx, call.peerUri, call.ua.account) else stringResource(R.string.unknown)
            Text(
                text = callerName,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = titleTextColor,
                textAlign = TextAlign.Center
            )
            val peerNumber = call?.peerUri?.substringAfter(":") ?: ""
            if (peerNumber.isNotEmpty() && peerNumber != callerName) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = numberBadgeBg,
                    border = numberBadgeBorder
                ) {
                    Text(
                        text = peerNumber,
                        fontSize = 14.sp,
                        color = subtitleTextColor,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Live Call Duration Timer
            if (call != null && isConnected) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = numberBadgeBg,
                    border = BorderStroke(1.dp, accentGreen.copy(alpha = 0.5f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(accentGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        CallTimerDisplay(
                            initialDurationSeconds = call.duration().toLong(),
                            textColor = titleTextColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Actions Section based on Call State
            when {
                isIncoming -> {
                    IncomingCallContent(
                        isVideoCall = isIncomingVideo,
                        onAnswer = { call?.let { answerCall(ctx, it, video = isIncomingVideo) } },
                        onDecline = { call?.let { rejectCall(it) } },
                        onAnswerAudioOnly = null
                    )
                }
                isOutgoing -> {
                    OutgoingCallContent(
                        isMicMuted = isMicMuted,
                        isSpeakerOn = isSpeakerOn,
                        isRecording = isRecording,
                        isMobileAccount = ua?.account?.isMobile ?: false,
                        onToggleMute = {
                            val newMute = !BaresipService.isMicMuted
                            BaresipService.setMicMute(newMute)
                            if (newMute) viewModel.updateMicIcon(Icons.Filled.MicOff) else viewModel.updateMicIcon(Icons.Filled.Mic)
                            isMicMuted = newMute
                        },
                        onToggleSpeaker = {
                            BaresipService.instance?.toggleSpeakerphone()
                        },
                        onToggleRecord = {
                            val nextRec = !BaresipService.isRecOn
                            BaresipService.isRecOn = nextRec
                            if (nextRec) {
                                Api.module_load("sndfile")
                                Toast.makeText(ctx, ctx.getString(R.string.recording_started), Toast.LENGTH_SHORT).show()
                            } else {
                                Api.module_unload("sndfile")
                                val savedNotice = "${ctx.getString(R.string.recording_stopped)}\n${ctx.getString(R.string.recording_saved)} (${ctx.getString(R.string.call_history)})"
                                Toast.makeText(ctx, savedNotice, Toast.LENGTH_LONG).show()
                                call?.let { c ->
                                    val rxName = c.dumpfiles[0]
                                    val fileName = if (rxName.isNotEmpty()) java.io.File(rxName).name else null
                                    BaresipService.instance?.showRecordingSavedNotification(c.dumpfiles[0], fileName)
                                }
                            }
                            isRecording = nextRec
                        },
                        onHangup = {
                            call?.let {
                                Log.d(CALL_SCREEN_TAG, "Hanging up outgoing call ${it.callp}")
                                it.terminated.value = true
                                it.hangup(0, "")
                            }
                        }
                    )
                }
                else -> {
                    val hasVideo = call != null && (call.hasVideo() || call.videoIcon.value != Video.NONE)
                    val isVideoOn = call?.hasVideo() == true
                    InCallContent(
                        isMicMuted = isMicMuted,
                        isSpeakerOn = isSpeakerOn,
                        isRecording = isRecording,
                        isHold = isHold,
                        isHeldByPeer = isHeldByPeer,
                        isMobileAccount = ua?.account?.isMobile ?: false,
                        hasVideo = hasVideo,
                        isVideoOn = isVideoOn,
                        onToggleMute = {
                            val newMute = !BaresipService.isMicMuted
                            BaresipService.setMicMute(newMute)
                            if (newMute) viewModel.updateMicIcon(Icons.Filled.MicOff) else viewModel.updateMicIcon(Icons.Filled.Mic)
                            isMicMuted = newMute
                        },
                        onToggleSpeaker = {
                            BaresipService.instance?.toggleSpeakerphone()
                        },
                        onToggleRecord = {
                            val nextRec = !BaresipService.isRecOn
                            BaresipService.isRecOn = nextRec
                            if (nextRec) {
                                Api.module_load("sndfile")
                                Toast.makeText(ctx, ctx.getString(R.string.recording_started), Toast.LENGTH_SHORT).show()
                            } else {
                                Api.module_unload("sndfile")
                                val savedNotice = "${ctx.getString(R.string.recording_stopped)}\n${ctx.getString(R.string.recording_saved)} (${ctx.getString(R.string.call_history)})"
                                Toast.makeText(ctx, savedNotice, Toast.LENGTH_LONG).show()
                                call?.let { c ->
                                    val rxName = c.dumpfiles[0]
                                    val fileName = if (rxName.isNotEmpty()) java.io.File(rxName).name else null
                                    BaresipService.instance?.showRecordingSavedNotification(c.dumpfiles[0], fileName)
                                }
                            }
                            isRecording = nextRec
                        },
                        onToggleHold = {
                            call?.let {
                                if (it.onhold || (it.callOnHold.value && !it.held)) {
                                    Log.d(CALL_SCREEN_TAG, "User requested resume for ${it.callp}")
                                    it.resume()
                                } else if (!it.held && !it.showOnHoldNotice.value) {
                                    Log.d(CALL_SCREEN_TAG, "User requested hold for ${it.callp}")
                                    it.hold()
                                }
                            }
                        },
                        onToggleDialpad = {
                            showDialpad = true
                        },
                        onToggleVideo = if (hasVideo) {
                            {
                                userClosedVideo = false
                                showVideoLayout = true
                            }
                        } else null,
                        onTransfer = {
                            call?.let {
                                if (it.onHoldCall != null) {
                                    if (!Api.call_supported(it.callp, Api.REPLACES)) {
                                        Toast.makeText(ctx, R.string.replaces_not_supported, Toast.LENGTH_SHORT).show()
                                    } else {
                                        it.hold()
                                        if (!it.executeTransfer()) {
                                            Toast.makeText(ctx, R.string.transfer_failed, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    showTransferDialog = true
                                }
                            }
                        },
                        onInfo = {
                            showInfoDialog = true
                        },
                        onHangup = {
                            call?.let {
                                Log.d(CALL_SCREEN_TAG, "Hanging up connected call ${it.callp}")
                                it.terminated.value = true
                                it.hangup(0, "")
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

    // In-Call DTMF Dialpad Bottom Sheet
    if (showDialpad && !isHeldByPeer && call != null) {
        ModalBottomSheet(
            onDismissRequest = { showDialpad = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = if (isDark) Color(0xFF090E1A) else Color(0xFFFFFFFF),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .size(width = 44.dp, height = 4.dp)
                        .background(
                            if (isDark) Color.White.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.18f),
                            RoundedCornerShape(2.dp)
                        )
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

    // Call Transfer Dialog
    if (showTransferDialog && call != null) {
        CallTransferDialog(
            ctx = ctx,
            call = call,
            onDismiss = { showTransferDialog = false }
        )
    }

    // Call Audio Stats Info Dialog
    if (showInfoDialog && call != null) {
        CallInfoDialog(
            ctx = ctx,
            call = call,
            onDismiss = { showInfoDialog = false }
        )
    }

    // ZRTP Security Dialog
    if (showSecurityDialog && call != null) {
        CallSecurityDialog(
            ctx = ctx,
            call = call,
            onDismiss = { showSecurityDialog = false }
        )
    }
}

private fun answerCall(ctx: Context, call: Call, video: Boolean = call.hasVideo()) {
    Log.d(CALL_SCREEN_TAG, "AoR ${call.ua.account.aor} answering call ${call.callp} (video=$video)")
    call.videoCall = video
    val intent = Intent(ctx, BaresipService::class.java)
    intent.action = "Call Answer"
    intent.putExtra("uap", call.ua.uap)
    intent.putExtra("callp", call.callp)
    intent.putExtra("video", video)
    ContextCompat.startForegroundService(ctx, intent)
}

private fun rejectCall(call: Call) {
    Log.d(CALL_SCREEN_TAG, "AoR ${call.ua.account.aor} rejecting call ${call.callp}")
    call.reject()
}

@Composable
internal fun CallTimerDisplay(
    initialDurationSeconds: Long,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Unspecified
) {
    val startTime = remember(initialDurationSeconds) {
        SystemClock.elapsedRealtime() - (initialDurationSeconds * 1000L)
    }

    var timeText by remember { mutableStateOf("") }

    LaunchedEffect(startTime) {
        while (true) {
            val now = SystemClock.elapsedRealtime()
            val elapsedMillis = now - startTime
            val seconds = if (elapsedMillis > 0) elapsedMillis / 1000 else 0
            timeText = android.text.format.DateUtils.formatElapsedTime(seconds)
            delay(1000)
        }
    }

    Text(
        text = timeText,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = if (textColor != Color.Unspecified) textColor else Color.White,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CallTransferDialog(
    ctx: Context,
    call: Call,
    onDismiss: () -> Unit
) {
    val blindChecked = remember { mutableStateOf(true) }
    var transferUri by remember { mutableStateOf("") }
    var filteredSuggestions by remember { mutableStateOf<List<Triple<Contact, AnnotatedString, Contact.ContactUri?>>>(emptyList()) }
    val focusRequester = remember { FocusRequester() }
    val lazyListState = rememberLazyListState()

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.call_transfer),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = transferUri,
                    singleLine = true,
                    onValueChange = { input ->
                        transferUri = input
                        if (input.length > 1) {
                            val normalizedInput = Utils.unaccent(input)
                            val numericInput = input.filter { c -> c.isDigit() || c == '+' }
                            val currentAor = call.ua.account.aor
                            filteredSuggestions = BaresipService.contacts.flatMap { contact ->
                                val nameMatch = Utils.unaccent(contact.name()).contains(normalizedInput, ignoreCase = true)
                                val uris = contact.uris().filter { !Utils.uriMatch(it.uri, currentAor) }
                                val matchingUris = uris.filter { u ->
                                    (u.uri.startsWith("tel:") && numericInput.isNotEmpty() && u.uri.substring(4).contains(numericInput)) ||
                                            (u.uri.startsWith("sip:") && Utils.uriUserPart(u.uri).contains(normalizedInput, ignoreCase = true))
                                }
                                if (nameMatch) {
                                    val annotatedName = Utils.buildAnnotatedStringWithHighlight(contact.name(), input)
                                    if (uris.isEmpty())
                                        listOf(Triple(contact, annotatedName, null))
                                    else
                                        uris.map { Triple(contact, annotatedName, it) }
                                } else if (matchingUris.isNotEmpty()) {
                                    matchingUris.map { Triple(contact, AnnotatedString(contact.name()), it) }
                                } else {
                                    emptyList()
                                }
                            }
                        } else {
                            filteredSuggestions = emptyList()
                        }
                    },
                    trailingIcon = {
                        if (transferUri.isNotEmpty()) {
                            Icon(
                                Icons.Outlined.Clear,
                                contentDescription = null,
                                modifier = Modifier.clickable { transferUri = "" },
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    label = { Text(stringResource(R.string.transfer_destination)) },
                    textStyle = TextStyle(fontSize = 16.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Suggestions List
                if (filteredSuggestions.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().heightIn(max = 140.dp)) {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            state = lazyListState
                        ) {
                            itemsIndexed(
                                items = filteredSuggestions,
                                key = { index, item -> "${item.first.id()}:${item.third?.uri ?: ""}:$index" }
                            ) { _, (contactItem, annotatedName, matchingUri) ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val uri = matchingUri?.uri ?: contactItem.uris().firstOrNull()?.uri ?: contactItem.name()
                                            transferUri = Utils.friendlyUri(ctx, uri, call.ua.account, unique = true)
                                            filteredSuggestions = emptyList()
                                        }
                                        .padding(vertical = 8.dp, horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = annotatedName,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                if (call.replaces()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { blindChecked.value = true }
                                .padding(end = 16.dp)
                        ) {
                            RadioButton(
                                selected = blindChecked.value,
                                onClick = { blindChecked.value = true }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.blind),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { blindChecked.value = false }
                        ) {
                            RadioButton(
                                selected = !blindChecked.value,
                                onClick = { blindChecked.value = false }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.attended),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val cleanUri = transferUri.trim()
                            if (cleanUri.isNotEmpty()) {
                                val uri = if (Utils.isTelNumber(cleanUri))
                                    Utils.telToSip("tel:$cleanUri", call.ua.account)
                                else
                                    Utils.uriComplete(cleanUri, call.ua.account.aor)

                                val success = if (!blindChecked.value && call.replaces()) {
                                    val tm = ctx.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
                                    val accountId = if (call.ua.account.isMobile) BaresipService.PSTN_ACCOUNT_ID else BaresipService.SIP_ACCOUNT_ID
                                    val handle = BaresipService.getPhoneAccountHandle(ctx, accountId)
                                    val extras = Bundle().apply {
                                        putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, handle)
                                    }
                                    val callExtras = Bundle().apply {
                                        putBoolean("conferenceCall", false)
                                        putLong("uap", call.ua.uap)
                                        putLong("onHoldCallp", call.callp)
                                        if (call.ua.account.isMobile) {
                                            putBoolean("pstnCall", true)
                                            putString("aor", call.ua.account.aor)
                                        }
                                    }
                                    extras.putBundle(TelecomManager.EXTRA_OUTGOING_CALL_EXTRAS, callExtras)
                                    val telecomUri = if (uri.startsWith("tel:"))
                                        Uri.fromParts("tel", uri.substring(4), null)
                                    else
                                        uri.toUri()
                                    try {
                                        tm?.placeCall(telecomUri, extras)
                                        true
                                    } catch (e: Exception) {
                                        Log.e(CALL_SCREEN_TAG, "Telecom placeCall for attended transfer failed: ${e.message}")
                                        val intent = Intent(ctx, BaresipService::class.java).apply {
                                            action = "Start Call"
                                            putExtra("uap", call.ua.uap)
                                            putExtra("uri", uri)
                                            putExtra("onHoldCallp", call.callp)
                                        }
                                        ContextCompat.startForegroundService(ctx, intent)
                                        true
                                    }
                                } else {
                                    call.hold()
                                    call.transfer(uri)
                                }
                                if (!success) {
                                    Toast.makeText(ctx, R.string.transfer_failed, Toast.LENGTH_SHORT).show()
                                }
                            }
                            onDismiss()
                        },
                        enabled = transferUri.isNotBlank()
                    ) {
                        Text(stringResource(R.string.transfer))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CallInfoDialog(
    ctx: Context,
    call: Call,
    onDismiss: () -> Unit
) {
    val stats = call.stats("audio")
    val infoMessage = if (stats.isNotEmpty() && call.startTime != null) {
        val parts = ArrayList(stats.split(","))
        if (parts.size >= 5 && parts[2] == "0/0") {
            parts[2] = "?/?"
            parts[3] = "?/?"
            parts[4] = "?/?"
        }
        val codecs = call.audioCodecs().split(',')
        val duration = call.duration()
        val txCodec = codecs.getOrNull(0)?.split("/") ?: listOf("?", "?", "?")
        val rxCodec = codecs.getOrNull(1)?.split("/") ?: listOf("?", "?", "?")
        "${String.format(ctx.getString(R.string.duration), duration)}\n" +
                "${ctx.getString(R.string.codecs)}: \u2192 ${txCodec.getOrElse(0) { "?" }} ${txCodec.getOrElse(1) { "?" }}Hz ${txCodec.getOrElse(2) { "?" }}ch /\n " +
                "    \u2190 ${rxCodec.getOrElse(0) { "?" }} ${rxCodec.getOrElse(1) { "?" }}Hz ${rxCodec.getOrElse(2) { "?" }}ch\n" +
                "${String.format(ctx.getString(R.string.rate), parts.getOrElse(0) { "?" })}\n" +
                "${String.format(ctx.getString(R.string.average_rate), parts.getOrElse(1) { "?" })}\n" +
                "${ctx.getString(R.string.packets)}: ${parts.getOrElse(2) { "?" }}\n" +
                "${ctx.getString(R.string.lost)}: ${parts.getOrElse(3) { "?" }}\n" +
                String.format(ctx.getString(R.string.jitter), parts.getOrElse(4) { "?" })
    } else {
        ctx.getString(R.string.call_info_not_available)
    }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.call_info),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = infoMessage,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.ok))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CallSecurityDialog(
    ctx: Context,
    call: Call,
    onDismiss: () -> Unit
) {
    val tint = call.securityIconTint.value
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (tint == R.color.colorTrafficGreen) stringResource(R.string.info) else stringResource(R.string.notice),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = when (tint) {
                        R.color.colorTrafficRed -> stringResource(R.string.call_not_secure)
                        R.color.colorTrafficYellow -> stringResource(R.string.peer_not_verified)
                        else -> stringResource(R.string.call_is_secure)
                    },
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    if (tint == R.color.colorTrafficGreen) {
                        TextButton(onClick = {
                            if (Api.cmd_exec("zrtp_unverify " + call.zid) != 0) {
                                Log.e(CALL_SCREEN_TAG, "Command 'zrtp_unverify ${call.zid}' failed")
                            } else {
                                call.securityIconTint.value = R.color.colorTrafficYellow
                            }
                            onDismiss()
                        }) {
                            Text(stringResource(R.string.unverify), color = MaterialTheme.colorScheme.error)
                        }
                    }
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.ok))
                    }
                }
            }
        }
    }
}
