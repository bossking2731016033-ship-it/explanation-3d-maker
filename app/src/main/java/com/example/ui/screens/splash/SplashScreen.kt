package com.example.ui.screens.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBg
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividBlue
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }

    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "splashAlpha"
    )

    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.8f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "splashScale"
    )

    // Infinite 3D orbit rotation
    val infiniteTransition = rememberInfiniteTransition(label = "splashOrbits")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitRotation"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2000)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .alpha(alphaAnim)
                .scale(scaleAnim)
        ) {
            // Animated 3D holographic emblem
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val radius = size.width * 0.35f

                    // Radial glow background
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(NeonPurple.copy(alpha = 0.5f), ElectricViolet.copy(alpha = 0.2f), Color.Transparent),
                            center = Offset(cx, cy),
                            radius = radius * 1.5f
                        ),
                        radius = radius * 1.5f,
                        center = Offset(cx, cy)
                    )

                    // Rotating Orbital Rings
                    rotate(rotationAngle, pivot = Offset(cx, cy)) {
                        drawOval(
                            color = CyanAccent,
                            topLeft = Offset(cx - radius * 1.2f, cy - radius * 0.45f),
                            size = Size(radius * 2.4f, radius * 0.9f),
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // Satellite node on ring 1
                        val rad = Math.toRadians(rotationAngle.toDouble())
                        val nx = cx + (radius * 1.2f) * cos(rad).toFloat()
                        val ny = cy + (radius * 0.45f) * sin(rad).toFloat()
                        drawCircle(color = CyanAccent, radius = 5.dp.toPx(), center = Offset(nx, ny))
                    }

                    rotate(-rotationAngle * 0.8f, pivot = Offset(cx, cy)) {
                        drawOval(
                            color = NeonPurple,
                            topLeft = Offset(cx - radius * 1.1f, cy - radius * 0.4f),
                            size = Size(radius * 2.2f, radius * 0.8f),
                            style = Stroke(width = 2.5.dp.toPx())
                        )
                    }

                    // 3D Core Sphere
                    drawCircle(
                        brush = Brush.linearGradient(
                            colors = listOf(ElectricViolet, VividBlue),
                            start = Offset(cx - radius * 0.6f, cy - radius * 0.6f),
                            end = Offset(cx + radius * 0.6f, cy + radius * 0.6f)
                        ),
                        radius = radius * 0.65f,
                        center = Offset(cx, cy)
                    )

                    // Core Wireframe latitude
                    drawOval(
                        color = Color.White.copy(alpha = 0.7f),
                        topLeft = Offset(cx - radius * 0.65f, cy - radius * 0.2f),
                        size = Size(radius * 1.3f, radius * 0.4f),
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // Core center pulse
                    drawCircle(
                        color = Color.White,
                        radius = 8.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Explainer3D",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 34.sp,
                    letterSpacing = 1.2.sp
                ),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Turn Any Topic Into 3D Explainer Videos",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                ),
                color = TextSecondary
            )
        }
    }
}
