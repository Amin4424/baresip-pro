package io.github.amin4424.baresip.promax

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun IncomingCallContent(
    isVideoCall: Boolean = false,
    onAnswer: () -> Unit,
    onDecline: () -> Unit,
    onAnswerAudioOnly: (() -> Unit)? = null
) {
    val greenColor = Color(0xFF2ABB86)
    val redColor = Color(0xFFEA4335)
    val neutralColor = Color.White.copy(alpha = 0.15f)

    // Pulse animation for incoming video call
    val infiniteTransition = rememberInfiniteTransition(label = "videoCallPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Decline Button
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .shadow(8.dp, CircleShape)
                    .background(redColor, CircleShape)
                    .clip(CircleShape)
                    .clickable(onClick = onDecline),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = stringResource(R.string.decline),
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.decline),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Optional Audio-Only Answer Button (when incoming call has video)
        if (isVideoCall && onAnswerAudioOnly != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        .clip(CircleShape)
                        .clickable(onClick = onAnswerAudioOnly),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = stringResource(R.string.answer_audio),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.answer_audio),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Answer / Video Answer Button
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .scale(if (isVideoCall) pulseScale else 1f)
                    .size(74.dp)
                    .shadow(if (isVideoCall) 12.dp else 8.dp, CircleShape, spotColor = greenColor)
                    .background(greenColor, CircleShape)
                    .border(
                        width = if (isVideoCall) 2.dp else 0.dp,
                        color = if (isVideoCall) Color.White.copy(alpha = 0.8f) else Color.Transparent,
                        shape = CircleShape
                    )
                    .clip(CircleShape)
                    .clickable(onClick = onAnswer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isVideoCall) Icons.Filled.Videocam else Icons.Default.Call,
                    contentDescription = stringResource(if (isVideoCall) R.string.answer_video else R.string.answer),
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(if (isVideoCall) R.string.answer_video else R.string.answer),
                fontSize = 14.sp,
                fontWeight = if (isVideoCall) FontWeight.Bold else FontWeight.Medium,
                color = if (isVideoCall) greenColor else MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
