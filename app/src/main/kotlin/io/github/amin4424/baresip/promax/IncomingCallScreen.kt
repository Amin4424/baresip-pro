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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomingCallScreen(
    ctx: Context,
    call: Call,
    contact: Contact?,
    isVideoCall: Boolean,
    onAnswerVideo: (cameraOn: Boolean) -> Unit,
    onAnswerAudio: () -> Unit,
    onDecline: () -> Unit
) {
    val callerName = Utils.friendlyUri(ctx, call.peerUri, call.ua.account)
    val peerNumber = call.peerUri.substringAfter(":")
    var showAnswerOptionsSheet by remember { mutableStateOf(false) }

    val isDark = isSystemInDarkTheme() || BaresipService.darkTheme.value

    // Dynamic Color Palette for Dark & Light Themes
    val emeraldNeon = Color(0xFF00E676)
    val emeraldDeep = Color(0xFF00A86B)
    val coralRed = Color(0xFFFF3B30)
    val crimsonDeep = Color(0xFFC62828)

    val bgGradientColors = if (isDark) {
        listOf(Color(0xFF070B14), Color(0xFF0D1527), Color(0xFF060910))
    } else {
        listOf(Color(0xFFFFFFFF), Color(0xFFF1F5F9), Color(0xFFE2E8F0))
    }
    val titleTextColor = if (isDark) Color.White else Color(0xFF0F172A)
    val subtitleTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    val pillBgColor = if (isVideoCall) {
        emeraldNeon.copy(alpha = if (isDark) 0.15f else 0.12f)
    } else {
        if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.85f)
    }
    val pillBorderColor = if (isVideoCall) {
        emeraldNeon.copy(alpha = if (isDark) 0.45f else 0.40f)
    } else {
        if (isDark) Color.White.copy(alpha = 0.15f) else Color(0xFFCBD5E1)
    }
    val pillTextColor = if (isVideoCall) emeraldNeon else if (isDark) Color.White else Color(0xFF1E293B)

    val numberBadgeBg = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.85f)
    val numberBadgeBorder = if (isDark) Color.White.copy(alpha = 0.10f) else Color(0xFFCBD5E1)

    val avatarSurfaceBg = if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF)

    // Pulse animations for background aura and ripples
    val transition = rememberInfiniteTransition(label = "incomingPulse")

    val rippleScale1 by transition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple1"
    )

    val rippleAlpha1 by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha1"
    )

    val rippleScale2 by transition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, delayMillis = 700, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple2"
    )

    val rippleAlpha2 by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, delayMillis = 700, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha2"
    )

    val buttonPulse by transition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "buttonScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = bgGradientColors))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Ambient glow backdrop in center
        Box(
            modifier = Modifier
                .size(340.dp)
                .align(Alignment.Center)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            if (isVideoCall) emeraldNeon.copy(alpha = if (isDark) 0.12f else 0.10f) else Color(0xFF2ABB86).copy(alpha = if (isDark) 0.09f else 0.08f),
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

            // 1. Incoming Call Type Pill Banner
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
                            .background(
                                if (isVideoCall) emeraldNeon else Color(0xFF2ABB86),
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isVideoCall) stringResource(R.string.incoming_video_call).uppercase() else stringResource(R.string.incoming_call).uppercase(),
                        color = pillTextColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

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

            Spacer(modifier = Modifier.weight(1f))

            // 3. Central Animated Avatar with Expanding Ripple Waves
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
                            color = (if (isVideoCall) emeraldNeon else Color(0xFF2ABB86)).copy(alpha = rippleAlpha2),
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
                            color = (if (isVideoCall) emeraldNeon else Color(0xFF2ABB86)).copy(alpha = rippleAlpha1),
                            shape = CircleShape
                        )
                )

                // Main Avatar Surface
                Box(contentAlignment = Alignment.BottomEnd) {
                    Surface(
                        modifier = Modifier
                            .size(136.dp)
                            .shadow(24.dp, CircleShape, spotColor = if (isVideoCall) emeraldNeon else Color(0xFF2ABB86)),
                        shape = CircleShape,
                        color = avatarSurfaceBg,
                        border = BorderStroke(
                            2.5.dp,
                            Brush.linearGradient(
                                colors = if (isVideoCall)
                                    listOf(emeraldNeon, Color(0xFF00B0FF))
                                else
                                    listOf(Color(0xFF2ABB86), Color(0xFF10B981))
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

                    // Video Camera Overlay Badge at Bottom Right of Avatar
                    if (isVideoCall) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .shadow(8.dp, CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(emeraldNeon, emeraldDeep)),
                                    CircleShape
                                )
                                .border(2.dp, if (isDark) Color(0xFF070B14) else Color(0xFFFFFFFF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Videocam,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1.2f))

            // 4. Modern Interactive Action Deck
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Decline Call Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .shadow(16.dp, CircleShape, spotColor = coralRed)
                            .background(
                                Brush.linearGradient(listOf(coralRed, crimsonDeep)),
                                CircleShape
                            )
                            .border(1.5.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onDecline
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CallEnd,
                            contentDescription = stringResource(R.string.decline),
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.decline),
                        color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Answer Video (or Answer Voice) Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .scale(if (isVideoCall) buttonPulse else 1f)
                            .size(76.dp)
                            .shadow(
                                elevation = if (isVideoCall) 22.dp else 16.dp,
                                shape = CircleShape,
                                spotColor = emeraldNeon
                            )
                            .background(
                                Brush.linearGradient(
                                    colors = if (isVideoCall)
                                        listOf(emeraldNeon, emeraldDeep)
                                    else
                                        listOf(Color(0xFF2ABB86), Color(0xFF10B981))
                                ),
                                CircleShape
                            )
                            .border(
                                width = 2.dp,
                                color = Color.White.copy(alpha = 0.4f),
                                shape = CircleShape
                            )
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    if (isVideoCall) {
                                        showAnswerOptionsSheet = true
                                    } else {
                                        onAnswerAudio()
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isVideoCall) Icons.Filled.Videocam else Icons.Filled.Call,
                            contentDescription = stringResource(if (isVideoCall) R.string.answer_video else R.string.answer),
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(if (isVideoCall) R.string.answer_video else R.string.answer),
                        color = if (isVideoCall) emeraldNeon else if (isDark) Color(0xFF2ABB86) else Color(0xFF059669),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Modal Bottom Sheet for Video Call Answer Options (Camera On vs Off)
    if (showAnswerOptionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAnswerOptionsSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = if (isDark) Color(0xFF0F172A) else Color(0xFFFFFFFF),
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.answer_video_call),
                    color = titleTextColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(R.string.choose_answer_mode),
                    color = subtitleTextColor,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Option 1: Answer with Camera ON
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            showAnswerOptionsSheet = false
                            onAnswerVideo(true)
                        },
                    shape = RoundedCornerShape(20.dp),
                    color = emeraldNeon.copy(alpha = if (isDark) 0.12f else 0.10f),
                    border = BorderStroke(1.5.dp, emeraldNeon.copy(alpha = if (isDark) 0.5f else 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(emeraldNeon, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Videocam,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.answer_camera_on),
                                color = if (isDark) Color.White else Color(0xFF065F46),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.answer_camera_on_desc),
                                color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF047857),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Option 2: Answer with Camera OFF
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            showAnswerOptionsSheet = false
                            onAnswerVideo(false)
                        },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.2f) else Color(0xFFCBD5E1))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VideocamOff,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.answer_camera_off),
                                color = titleTextColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.answer_camera_off_desc),
                                color = subtitleTextColor,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Cancel Button
                Text(
                    text = stringResource(R.string.cancel),
                    color = subtitleTextColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { showAnswerOptionsSheet = false }
                        .padding(8.dp)
                )
            }
        }
    }
}
