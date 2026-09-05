package io.github.amin4424.baresip.promax

import android.content.Context
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun OutgoingCallScreen(
    ctx: Context,
    call: Call,
    contact: Contact?,
    status: String,
    isVideoCall: Boolean,
    isMicMuted: Boolean,
    isSpeakerOn: Boolean,
    isRecording: Boolean,
    isMobileAccount: Boolean,
    isCameraMuted: Boolean = false,
    onToggleCamera: () -> Unit = {},
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleRecord: () -> Unit,
    onHangup: () -> Unit
) {
    val callerName = Utils.friendlyUri(ctx, call.peerUri, call.ua.account)
    val peerNumber = call.peerUri.substringAfter(":")

    val isDark = isSystemInDarkTheme() || BaresipService.darkTheme.value

    // Colors
    val primaryCyan = Color(0xFF00B0FF)
    val emeraldNeon = Color(0xFF00E676)
    val emeraldDeep = Color(0xFF00A86B)
    val coralRed = Color(0xFFFF3B30)
    val crimsonDeep = Color(0xFFC62828)
    val themeAccent = if (isVideoCall) emeraldNeon else primaryCyan

    val bgGradientColors = if (isDark) {
        listOf(Color(0xFF070B14), Color(0xFF0D1527), Color(0xFF060910))
    } else {
        listOf(Color(0xFFFFFFFF), Color(0xFFF1F5F9), Color(0xFFE2E8F0))
    }
    val titleTextColor = if (isDark) Color.White else Color(0xFF0F172A)
    val subtitleTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    val pillBgColor = themeAccent.copy(alpha = if (isDark) 0.15f else 0.12f)
    val pillBorderColor = themeAccent.copy(alpha = if (isDark) 0.45f else 0.40f)
    val pillTextColor = if (isDark) themeAccent else (if (isVideoCall) Color(0xFF059669) else Color(0xFF0284C7))

    val numberBadgeBg = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.85f)
    val numberBadgeBorder = if (isDark) Color.White.copy(alpha = 0.10f) else Color(0xFFCBD5E1)

    val avatarSurfaceBg = if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF)

    // Animated dots for "Calling..." / "Ringing..."
    var dots by remember { mutableStateOf(".") }
    LaunchedEffect(Unit) {
        while (true) {
            dots = when (dots) {
                "." -> ".."
                ".." -> "..."
                else -> "."
            }
            delay(450)
        }
    }

    val isRinging = status == "ringing"
    val statusLabel = if (isRinging) stringResource(R.string.ringing) else stringResource(R.string.calling)

    // Pulse animations for radar waves
    val transition = rememberInfiniteTransition(label = "outgoingPulse")

    val rippleScale1 by transition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outgoingRipple1"
    )

    val rippleAlpha1 by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outgoingAlpha1"
    )

    val rippleScale2 by transition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, delayMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outgoingRipple2"
    )

    val rippleAlpha2 by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, delayMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outgoingAlpha2"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = bgGradientColors))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Central ambient glow
        Box(
            modifier = Modifier
                .size(340.dp)
                .align(Alignment.Center)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            themeAccent.copy(alpha = if (isDark) 0.12f else 0.10f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. Call Type Pill Banner
            Surface(
                shape = RoundedCornerShape(30.dp),
                color = pillBgColor,
                border = BorderStroke(1.dp, pillBorderColor)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(themeAccent, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isVideoCall) stringResource(R.string.outgoing_call).uppercase() + " (VIDEO)" else stringResource(R.string.outgoing_call).uppercase(),
                        color = pillTextColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // 2. Caller Name & Number
            Text(
                text = callerName,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = titleTextColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (peerNumber.isNotEmpty() && peerNumber != callerName) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = numberBadgeBg,
                    border = BorderStroke(1.dp, numberBadgeBorder)
                ) {
                    Text(
                        text = peerNumber,
                        fontSize = 14.sp,
                        color = subtitleTextColor,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Live status: "Calling..." / "Ringing..."
            Text(
                text = "$statusLabel$dots",
                fontSize = 18.sp,
                color = if (isRinging) (if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)) else (if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)),
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.weight(1f))

            // 3. Central Animated Avatar with Outgoing Radar Waves
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                // Expanding wave 2
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(rippleScale2)
                        .border(
                            width = 1.5.dp,
                            color = themeAccent.copy(alpha = rippleAlpha2),
                            shape = CircleShape
                        )
                )

                // Expanding wave 1
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(rippleScale1)
                        .border(
                            width = 2.dp,
                            color = themeAccent.copy(alpha = rippleAlpha1),
                            shape = CircleShape
                        )
                )

                // Main Avatar Surface
                Box(contentAlignment = Alignment.BottomEnd) {
                    Surface(
                        modifier = Modifier
                            .size(136.dp)
                            .shadow(24.dp, CircleShape, spotColor = if (isDark) themeAccent else themeAccent.copy(alpha = 0.35f)),
                        shape = CircleShape,
                        color = avatarSurfaceBg,
                        border = BorderStroke(
                            2.5.dp,
                            Brush.linearGradient(
                                colors = if (isVideoCall)
                                    listOf(emeraldNeon, Color(0xFF00B0FF))
                                else
                                    listOf(primaryCyan, Color(0xFF38BDF8))
                            )
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

                    // Indicator Badge at Bottom Right of Avatar
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(8.dp, CircleShape)
                            .background(
                                Brush.linearGradient(
                                    if (isVideoCall) listOf(emeraldNeon, emeraldDeep) else listOf(primaryCyan, Color(0xFF0284C7))
                                ),
                                CircleShape
                            )
                            .border(2.dp, if (isDark) Color(0xFF070B14) else Color(0xFFFFFFFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isVideoCall) Icons.Filled.Videocam else Icons.Filled.Call,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 4. Frosted Glass Controls Dock (Mute, Speaker, Record)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        if (isDark) 16.dp else 10.dp,
                        RoundedCornerShape(28.dp),
                        spotColor = if (isDark) Color.Black else Color(0x14000000)
                    ),
                shape = RoundedCornerShape(28.dp),
                color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.90f),
                border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.15f) else Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Camera Toggle (only for video calls)
                    if (isVideoCall) {
                        OutgoingControlButton(
                            icon = if (!isCameraMuted) Icons.Filled.Videocam else Icons.Filled.VideocamOff,
                            label = if (!isCameraMuted) stringResource(R.string.camera_on) else stringResource(R.string.camera_off),
                            isActive = !isCameraMuted,
                            activeColor = emeraldNeon,
                            isDark = isDark,
                            onClick = onToggleCamera
                        )
                    }

                    // Mute
                    OutgoingControlButton(
                        icon = if (isMicMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                        label = if (isMicMuted) stringResource(R.string.unmute) else stringResource(R.string.mute),
                        isActive = isMicMuted,
                        activeColor = coralRed,
                        isDark = isDark,
                        onClick = onToggleMute
                    )

                    // Speaker
                    OutgoingControlButton(
                        icon = if (isSpeakerOn) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                        label = stringResource(R.string.speaker),
                        isActive = isSpeakerOn,
                        activeColor = Color(0xFF2ABB86),
                        isDark = isDark,
                        onClick = onToggleSpeaker
                    )

                    // Record
                    if (!isMobileAccount) {
                        OutgoingControlButton(
                            icon = if (isRecording) Icons.Filled.RadioButtonChecked else Icons.Filled.FiberManualRecord,
                            label = if (isRecording) stringResource(R.string.recording) else stringResource(R.string.record),
                            isActive = isRecording,
                            activeColor = coralRed,
                            isDark = isDark,
                            onClick = onToggleRecord
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 5. End Call Button
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .shadow(18.dp, CircleShape, spotColor = coralRed)
                    .background(
                        Brush.linearGradient(listOf(coralRed, crimsonDeep)),
                        CircleShape
                    )
                    .border(1.5.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onHangup
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CallEnd,
                    contentDescription = stringResource(R.string.hangup),
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun OutgoingControlButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(
                    if (isActive) activeColor.copy(alpha = if (isDark) 0.25f else 0.18f) else if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFF1F5F9),
                    CircleShape
                )
                .border(
                    width = 1.dp,
                    color = if (isActive) activeColor.copy(alpha = 0.6f) else if (isDark) Color.White.copy(alpha = 0.15f) else Color(0xFFCBD5E1),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) activeColor else if (isDark) Color.White else Color(0xFF334155),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isActive) activeColor else if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
            fontWeight = FontWeight.Medium
        )
    }
}
