package com.tutpro.baresip.plus

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
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

@Composable
private fun CallScreen(navController: NavController, viewModel: ViewModel) {
    val ctx = LocalContext.current
    val selectedAor by viewModel.selectedAor.collectAsState()
    val ua = uas.value.find { it.account.aor == selectedAor }
    val focusedCall by viewModel.focusedCall.collectAsState()
    val calls by viewModel.calls.collectAsState()

    // Active call resolution
    val call = ua?.currentCall() ?: focusedCall ?: calls.lastOrNull()
    val status = call?.status?.value ?: "idle"

    val isSpeakerOn by viewModel.isSpeakerOn.collectAsState()
    val micIcon by viewModel.micIcon.collectAsState()
    var isMicMuted by remember { mutableStateOf(BaresipService.isMicMuted) }
    var isRecording by remember { mutableStateOf(BaresipService.isRecOn) }

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

    // Auto-return to main if no calls are active
    LaunchedEffect(calls) {
        if (calls.isEmpty()) {
            Log.d(CALL_SCREEN_TAG, "No active calls, returning to main")
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

    val isConnected = status == "connected" || status == "transferring"
    val isIncoming = status == "incoming"
    val isOutgoing = status == "outgoing" || status == "answered"

    val greenColor = Color(0xFF2ABB86)
    val redColor = Color(0xFFEA4335)
    val accentGreen = Color(0xFF00C853)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Status Header (Dot + Text + Recording Badge)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                val statusText = when {
                    isIncoming -> stringResource(R.string.incoming_call)
                    isOutgoing -> stringResource(R.string.outgoing_call)
                    isConnected -> stringResource(R.string.connected)
                    else -> stringResource(R.string.call)
                }

                val statusColor = when {
                    isIncoming -> greenColor
                    isOutgoing -> MaterialTheme.colorScheme.primary
                    else -> greenColor
                }

                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(statusColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = statusText,
                    color = statusColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                if (isRecording) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        color = redColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, redColor.copy(alpha = 0.5f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(redColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "REC",
                                color = redColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Avatar Container
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .shadow(8.dp, CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                when (val contact = if (call != null) Contact.findContact(call.peerUri) else null) {
                    is Contact.BaresipContact -> {
                        val avatarImage = contact.avatarImage
                        if (avatarImage != null)
                            Image(
                                bitmap = avatarImage.asImageBitmap(),
                                contentDescription = "Contact Avatar",
                                modifier = Modifier
                                    .size(130.dp)
                                    .clip(CircleShape),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        else
                            CustomElements.TextAvatar(name = contact.name, color = contact.color)
                    }
                    is Contact.AndroidContact -> {
                        val thumbNailUri = contact.thumbnailUri
                        if (thumbNailUri != null)
                            coil.compose.AsyncImage(
                                model = thumbNailUri,
                                contentDescription = "Contact Avatar",
                                modifier = Modifier
                                    .size(130.dp)
                                    .clip(CircleShape),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        else
                            CustomElements.TextAvatar(name = contact.name, color = contact.color)
                    }
                    null -> {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(72.dp)
                        )
                    }
                }

                // Call Type Indicator Badge (Bottom End)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .size(28.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .padding(3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (isConnected) accentGreen else if (isIncoming) greenColor else MaterialTheme.colorScheme.primary,
                                CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Caller Name & Number
            val callerName = if (call != null) Utils.friendlyUri(ctx, call.peerUri, call.ua.account) else stringResource(R.string.unknown)
            Text(
                text = callerName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            val peerNumber = call?.peerUri?.substringAfter(":") ?: ""
            if (peerNumber.isNotEmpty() && peerNumber != callerName) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = peerNumber,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Live Call Duration Timer / Ringing Animation
            if (call != null) {
                if (isOutgoing) {
                    var dots by remember { mutableStateOf(".") }
                    LaunchedEffect(Unit) {
                        while (true) {
                            dots = when (dots) {
                                "." -> ".."
                                ".." -> "..."
                                else -> "."
                            }
                            delay(500)
                        }
                    }
                    Text(
                        text = "Ringing$dots",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                } else if (isConnected) {
                    CallTimerDisplay(initialDurationSeconds = call.duration().toLong())
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Actions Section based on Call State
            when {
                isIncoming -> {
                    IncomingCallContent(
                        onAnswer = { call?.let { answerCall(ctx, it) } },
                        onDecline = { call?.let { rejectCall(it) } }
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
                                Toast.makeText(ctx, ctx.getString(R.string.recording_stopped), Toast.LENGTH_SHORT).show()
                            }
                            isRecording = nextRec
                        },
                        onHangup = {
                            call?.let {
                                Log.d(CALL_SCREEN_TAG, "Hanging up outgoing call ${it.callp}")
                                it.terminated.value = true
                                it.hangup(487, "Request Terminated")
                            }
                        }
                    )
                }
                else -> {
                    InCallContent(
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
                                Toast.makeText(ctx, ctx.getString(R.string.recording_stopped), Toast.LENGTH_SHORT).show()
                            }
                            isRecording = nextRec
                        },
                        onHangup = {
                            call?.let {
                                Log.d(CALL_SCREEN_TAG, "Hanging up connected call ${it.callp}")
                                it.terminated.value = true
                                it.hangup(487, "Request Terminated")
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun answerCall(ctx: Context, call: Call) {
    Log.d(CALL_SCREEN_TAG, "AoR ${call.ua.account.aor} answering call ${call.callp}")
    val intent = Intent(ctx, BaresipService::class.java)
    intent.action = "Call Answer"
    intent.putExtra("uap", call.ua.uap)
    intent.putExtra("callp", call.callp)
    ContextCompat.startForegroundService(ctx, intent)
}

private fun rejectCall(call: Call) {
    Log.d(CALL_SCREEN_TAG, "AoR ${call.ua.account.aor} rejecting call ${call.callp}")
    call.reject()
}

@Composable
private fun CallTimerDisplay(initialDurationSeconds: Long, modifier: Modifier = Modifier) {
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
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
    )
}
