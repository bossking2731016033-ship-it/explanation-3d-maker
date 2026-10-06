package com.example.ui.screens.loading

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.GenerationState
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.GlowGreen
import com.example.ui.theme.GlowRed
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.VividBlue
import com.example.ui.viewmodel.ExplainerViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LoadingScreen(
    viewModel: ExplainerViewModel,
    onNavigateToResult: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.generationState.collectAsState()

    LaunchedEffect(state) {
        if (state is GenerationState.Success) {
            onNavigateToResult()
        }
    }

    val steps = listOf(
        "Searching internet...",
        "Writing script...",
        "Generating voiceover...",
        "Building 3D scene...",
        "Rendering video...",
        "Almost done..."
    )

    val currentStep = (state as? GenerationState.Generating)?.stepIndex ?: 0
    val rawProgress = (state as? GenerationState.Generating)?.progress ?: 0.05f
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 400),
        label = "genProgress"
    )

    // Infinite 3D rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "loading3d")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate3d"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(24.dp)
            .testTag("loading_screen")
    ) {
        if (state is GenerationState.Error) {
            val errorMsg = (state as GenerationState.Error).message
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = "Error",
                    tint = GlowRed,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Generation Error",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMsg,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(28.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.cancelGeneration(onNavigateBack) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Back to Home")
                    }
                    Button(
                        onClick = { viewModel.startGeneration {} },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Retry")
                    }
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Crafting 3D Explainer",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = (state as? GenerationState.Generating)?.stepTitle ?: "Processing request...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CyanAccent,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 3D Isometric Holographic Progress Orb
                Box(
                    modifier = Modifier.size(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val r = size.width * 0.38f

                        // Pulsing ambient glow
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(NeonPurple.copy(alpha = 0.4f), Color.Transparent),
                                center = Offset(cx, cy),
                                radius = r * 1.6f
                            ),
                            radius = r * 1.6f,
                            center = Offset(cx, cy)
                        )

                        // 3D Isometric Wireframe Cube Projection
                        rotate(rotationAngle, pivot = Offset(cx, cy)) {
                            // Ring 1 (Cyan orbit)
                            drawOval(
                                color = CyanAccent,
                                topLeft = Offset(cx - r * 1.25f, cy - r * 0.45f),
                                size = Size(r * 2.5f, r * 0.9f),
                                style = Stroke(width = 3.dp.toPx())
                            )
                            val rad = Math.toRadians(rotationAngle.toDouble())
                            val orbX = cx + (r * 1.25f) * cos(rad).toFloat()
                            val orbY = cy + (r * 0.45f) * sin(rad).toFloat()
                            drawCircle(color = Color.White, radius = 5.dp.toPx(), center = Offset(orbX, orbY))
                        }

                        rotate(-rotationAngle * 1.2f, pivot = Offset(cx, cy)) {
                            // Ring 2 (Purple orbit)
                            drawOval(
                                color = NeonPurple,
                                topLeft = Offset(cx - r * 1.1f, cy - r * 0.4f),
                                size = Size(r * 2.2f, r * 0.8f),
                                style = Stroke(width = 2.5.dp.toPx())
                            )
                        }

                        // Central Holographic core
                        drawCircle(
                            brush = Brush.linearGradient(
                                colors = listOf(ElectricViolet, VividBlue),
                                start = Offset(cx - r * 0.6f, cy - r * 0.6f),
                                end = Offset(cx + r * 0.6f, cy + r * 0.6f)
                            ),
                            radius = r * 0.6f,
                            center = Offset(cx, cy)
                        )

                        // Latitudinal glowing grid
                        drawOval(
                            color = Color.White.copy(alpha = 0.6f),
                            topLeft = Offset(cx - r * 0.6f, cy - r * 0.2f),
                            size = Size(r * 1.2f, r * 0.4f),
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }

                    // Numeric percentage badge in center
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(DarkBg.copy(alpha = 0.85f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${(animatedProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Linear Progress Bar
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    color = CyanAccent,
                    trackColor = DarkCardBorder,
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Step-by-Step Status List
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        steps.forEachIndexed { index, stepText ->
                            val isCompleted = index < currentStep
                            val isCurrent = index == currentStep

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isCompleted -> GlowGreen.copy(alpha = 0.2f)
                                                isCurrent -> CyanAccent.copy(alpha = 0.2f)
                                                else -> DarkCardBorder
                                            }
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = when {
                                                isCompleted -> GlowGreen
                                                isCurrent -> CyanAccent
                                                else -> Color.Transparent
                                            },
                                            shape = CircleShape
                                        )
                                ) {
                                    if (isCompleted) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Completed",
                                            tint = GlowGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else if (isCurrent) {
                                        CircularProgressIndicator(
                                            strokeWidth = 2.dp,
                                            color = CyanAccent,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "${index + 1}",
                                            fontSize = 11.sp,
                                            color = TextTertiary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Text(
                                    text = stepText,
                                    fontSize = 14.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isCompleted -> TextPrimary
                                        isCurrent -> CyanAccent
                                        else -> TextTertiary
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Cancel Button
                OutlinedButton(
                    onClick = { viewModel.cancelGeneration(onNavigateBack) },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(48.dp)
                        .testTag("cancel_generation_button")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cancel")
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
