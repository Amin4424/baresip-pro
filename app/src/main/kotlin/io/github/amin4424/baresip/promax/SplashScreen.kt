package io.github.amin4424.baresip.promax

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    durationMillis: Long = 2300L,
    onFinish: () -> Unit
) {
    var isVisible by remember { mutableStateOf(true) }

    // Animations
    val logoScale = remember { Animatable(0.6f) }
    val logoAlpha = remember { Animatable(0f) }

    val baresipAlpha = remember { Animatable(0f) }
    val baresipOffset = remember { Animatable(-30f) }

    val proScale = remember { Animatable(0f) }
    val proAlpha = remember { Animatable(0f) }

    val maxScale = remember { Animatable(0f) }
    val maxAlpha = remember { Animatable(0f) }

    val subtitleAlpha = remember { Animatable(0f) }
    val progressBarWidth = remember { Animatable(0f) }

    // Pulsing background aura
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    LaunchedEffect(Unit) {
        // 1. Icon appears
        launch {
            logoAlpha.animateTo(1f, tween(400))
        }
        launch {
            logoScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
        }
        delay(200)

        // 2. "Baresip" slides in
        launch {
            baresipAlpha.animateTo(1f, tween(400))
        }
        launch {
            baresipOffset.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
        delay(250)

        // 3. "Pro" pops in
        launch {
            proAlpha.animateTo(1f, tween(250))
        }
        launch {
            proScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
        }
        delay(200)

        // 4. "max" bursts in
        launch {
            maxAlpha.animateTo(1f, tween(200))
        }
        launch {
            maxScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium))
        }
        delay(150)

        // 5. Subtitle fades in
        launch {
            subtitleAlpha.animateTo(1f, tween(400))
        }

        // 6. Progress bar fills
        launch {
            progressBarWidth.animateTo(1f, tween((durationMillis - 600).toInt(), easing = LinearEasing))
        }

        delay(durationMillis - 300)
        isVisible = false
        delay(300)
        onFinish()
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(350)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF080E1E),
                            Color(0xFF0B1426),
                            Color(0xFF030712)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Ambient Aura Glow behind logo
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .scale(pulseScale)
                    .alpha(pulseAlpha)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00B0FF),
                                Color(0xFFFF9100),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Main Logo Shield / Icon
                Box(
                    modifier = Modifier
                        .scale(logoScale.value)
                        .alpha(logoAlpha.value)
                        .size(108.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF131F37),
                                    Color(0xFF0F172A)
                                )
                            ),
                            RoundedCornerShape(28.dp)
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                listOf(
                                    Color(0xFF00B0FF).copy(alpha = 0.8f),
                                    Color(0xFFFF9100).copy(alpha = 0.8f)
                                )
                            ),
                            shape = RoundedCornerShape(28.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "Baresip Pro Max",
                        modifier = Modifier
                            .size(108.dp)
                            .clip(RoundedCornerShape(28.dp))
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Animated Brand Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // "Baresip "
                    Text(
                        text = "Baresip ",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .alpha(baresipAlpha.value)
                            .offset(x = baresipOffset.value.dp)
                    )

                    // "Pro"
                    Text(
                        text = "Pro",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF38BDF8),
                        modifier = Modifier
                            .scale(proScale.value)
                            .alpha(proAlpha.value)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // "max"
                    Text(
                        text = "max",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFF9100),
                        modifier = Modifier
                            .scale(maxScale.value)
                            .alpha(maxAlpha.value)
                            .offset(y = (-11).dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tagline / Subtitle
                Text(
                    text = "SIP & High Definition Voice Studio",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.65f),
                    letterSpacing = 1.sp,
                    modifier = Modifier.alpha(subtitleAlpha.value)
                )
            }

            // Bottom loading bar
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp)
                    .width(160.dp)
                    .height(3.dp)
                    .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(2.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressBarWidth.value)
                        .height(3.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF00B0FF),
                                    Color(0xFFFF9100)
                                )
                            ),
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}
