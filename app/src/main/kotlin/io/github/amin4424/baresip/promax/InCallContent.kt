package io.github.amin4424.baresip.promax

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.outlined.ArrowCircleRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InCallContent(
    isMicMuted: Boolean,
    isSpeakerOn: Boolean,
    isRecording: Boolean,
    isHold: Boolean,
    isHeldByPeer: Boolean,
    isMobileAccount: Boolean,
    hasVideo: Boolean = false,
    isVideoOn: Boolean = false,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleRecord: () -> Unit,
    onToggleHold: () -> Unit,
    onToggleDialpad: () -> Unit,
    onToggleVideo: (() -> Unit)? = null,
    onTransfer: () -> Unit,
    onInfo: () -> Unit,
    onHangup: () -> Unit
) {
    val isDark = isSystemInDarkTheme() || BaresipService.darkTheme.value

    val redColor = Color(0xFFFF3B30)
    val crimsonDeep = Color(0xFFC62828)
    val greenColor = Color(0xFF00E676)
    val yellowColor = Color(0xFFF59E0B)
    val primaryCyan = Color(0xFF00B0FF)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Connected In-Call Floating Frosted Glass Deck
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp, horizontal = 6.dp)
            ) {
                // Row 1: Mute, Keypad, Speaker, Hold/Resume
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InCallControlButton(
                        modifier = Modifier.weight(1f),
                        icon = if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        label = if (isMicMuted) stringResource(R.string.unmute) else stringResource(R.string.mute),
                        isActive = isMicMuted,
                        activeColor = redColor,
                        isDark = isDark,
                        onClick = onToggleMute
                    )

                    InCallControlButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Dialpad,
                        label = stringResource(R.string.keypad),
                        isActive = false,
                        activeColor = primaryCyan,
                        enabled = !isHeldByPeer,
                        isDark = isDark,
                        onClick = onToggleDialpad
                    )

                    InCallControlButton(
                        modifier = Modifier.weight(1f),
                        icon = if (isSpeakerOn) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                        label = stringResource(R.string.speaker),
                        isActive = isSpeakerOn,
                        activeColor = greenColor,
                        isDark = isDark,
                        onClick = onToggleSpeaker
                    )

                    InCallControlButton(
                        modifier = Modifier.weight(1f),
                        icon = if (isHeldByPeer) Icons.Outlined.PauseCircle else if (isHold) Icons.Default.PlayArrow else Icons.Outlined.PauseCircle,
                        label = if (isHeldByPeer) stringResource(R.string.on_hold) else if (isHold) stringResource(R.string.resume) else stringResource(R.string.hold),
                        isActive = isHold || isHeldByPeer,
                        activeColor = yellowColor,
                        enabled = !isHeldByPeer,
                        isDark = isDark,
                        onClick = onToggleHold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 2: Record, Transfer, Info, and Video
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isMobileAccount) {
                        InCallControlButton(
                            modifier = Modifier.weight(1f),
                            icon = if (isRecording) Icons.Filled.RadioButtonChecked else Icons.Filled.FiberManualRecord,
                            label = if (isRecording) stringResource(R.string.recording) else stringResource(R.string.record),
                            isActive = isRecording,
                            activeColor = redColor,
                            isDark = isDark,
                            onClick = onToggleRecord
                        )

                        InCallControlButton(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.ArrowCircleRight,
                            label = stringResource(R.string.transfer),
                            isActive = false,
                            activeColor = primaryCyan,
                            enabled = !isHeldByPeer,
                            isDark = isDark,
                            onClick = onTransfer
                        )
                    }

                    InCallControlButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.Info,
                        label = stringResource(R.string.info),
                        isActive = false,
                        activeColor = primaryCyan,
                        isDark = isDark,
                        onClick = onInfo
                    )

                    if (hasVideo && onToggleVideo != null) {
                        InCallControlButton(
                            modifier = Modifier.weight(1f),
                            icon = if (isVideoOn || hasVideo) Icons.Filled.Videocam else Icons.Filled.VideocamOff,
                            label = stringResource(R.string.video),
                            isActive = isVideoOn || hasVideo,
                            activeColor = greenColor,
                            isDark = isDark,
                            onClick = onToggleVideo
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // End Call Button
        Box(
            modifier = Modifier
                .size(76.dp)
                .shadow(18.dp, CircleShape, spotColor = redColor)
                .background(
                    Brush.linearGradient(listOf(redColor, crimsonDeep)),
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
                imageVector = Icons.Default.CallEnd,
                contentDescription = stringResource(R.string.hangup),
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
fun InCallDialpadSheet(
    dtmfText: String,
    onSendDtmf: (Char) -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = isSystemInDarkTheme() || BaresipService.darkTheme.value

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // DTMF Header Display Box
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFF1F5F9),
            border = BorderStroke(1.dp, if (isDark) Color(0xFF00B0FF).copy(alpha = 0.35f) else Color(0xFF00B0FF).copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val scrollState = rememberScrollState()
                LaunchedEffect(dtmfText) {
                    scrollState.animateScrollTo(scrollState.maxValue)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dtmfText.ifEmpty { stringResource(R.string.keypad) },
                        fontSize = if (dtmfText.isEmpty()) 16.sp else 30.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = if (dtmfText.isEmpty()) 0.sp else 4.sp,
                        color = if (dtmfText.isEmpty()) (if (isDark) Color.White.copy(alpha = 0.45f) else Color(0xFF64748B)) else (if (isDark) Color.White else Color(0xFF0F172A)),
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3x4 DTMF Dialpad Grid
        val dialpadKeys = listOf(
            listOf("1" to "", "2" to "ABC", "3" to "DEF"),
            listOf("4" to "GHI", "5" to "JKL", "6" to "MNO"),
            listOf("7" to "PQRS", "8" to "TUV", "9" to "WXYZ"),
            listOf("*" to "", "0" to "+", "#" to "")
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            for (row in dialpadKeys) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for ((digit, subtext) in row) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .shadow(if (isDark) 6.dp else 4.dp, CircleShape, spotColor = if (isDark) Color(0xFF00B0FF).copy(alpha = 0.3f) else Color(0x14000000))
                                .background(
                                    if (isDark) {
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.12f),
                                                Color.White.copy(alpha = 0.05f)
                                            )
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.White,
                                                Color(0xFFF8FAFC)
                                            )
                                        )
                                    },
                                    CircleShape
                                )
                                .border(1.2.dp, if (isDark) Color.White.copy(alpha = 0.18f) else Color(0xFFCBD5E1), CircleShape)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onSendDtmf(digit[0]) }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = digit,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else Color(0xFF0F172A)
                                )
                                if (subtext.isNotEmpty()) {
                                    Text(
                                        text = subtext,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Close / Dismiss Sheet Button
        Box(
            modifier = Modifier
                .size(width = 120.dp, height = 44.dp)
                .background(if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.06f), RoundedCornerShape(22.dp))
                .clip(RoundedCornerShape(22.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.cancel),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color.White else Color(0xFF0F172A)
            )
        }
    }
}

@Composable
private fun InCallControlButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    enabled: Boolean = true,
    isDark: Boolean = true,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        when {
            !enabled -> if (isDark) Color.White.copy(alpha = 0.03f) else Color.Black.copy(alpha = 0.02f)
            isActive -> activeColor.copy(alpha = if (isDark) 0.25f else 0.18f)
            else -> if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFF1F5F9)
        },
        label = "inCallBtnBg"
    )
    val iconTint by animateColorAsState(
        when {
            !enabled -> if (isDark) Color.White.copy(alpha = 0.25f) else Color(0xFF94A3B8)
            isActive -> activeColor
            else -> if (isDark) Color.White else Color(0xFF334155)
        },
        label = "inCallBtnTint"
    )
    val textTint by animateColorAsState(
        when {
            !enabled -> if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
            isActive -> activeColor
            else -> if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
        },
        label = "inCallBtnTextTint"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(vertical = 4.dp, horizontal = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(backgroundColor, CircleShape)
                .border(
                    width = 1.dp,
                    color = if (isActive && enabled) activeColor.copy(alpha = 0.6f) else if (isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFCBD5E1),
                    shape = CircleShape
                )
                .clip(CircleShape)
                .clickable(
                    enabled = enabled,
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
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isActive && enabled) FontWeight.Bold else FontWeight.Normal,
            color = textTint,
            maxLines = 1
        )
    }
}
