package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val NeonPurple = Color(0xFFB5179E)
val ElectricViolet = Color(0xFF7209B7)
val VividBlue = Color(0xFF4361EE)
val CyanAccent = Color(0xFF4CC9F0)
val DarkBg = Color(0xFF0B0814)
val DarkCard = Color(0xFF151128)
val DarkCardBorder = Color(0xFF2C244F)
val DarkSurfaceElevated = Color(0xFF1F183C)
val TextPrimary = Color(0xFFF3F0FF)
val TextSecondary = Color(0xFFA59EBF)
val TextTertiary = Color(0xFF766E96)
val GlowGreen = Color(0xFF06D6A0)
val GlowRed = Color(0xFFEF476F)
val GlowAmber = Color(0xFFFFD166)

val GradientPurpleBlue = Brush.horizontalGradient(
    colors = listOf(NeonPurple, VividBlue, CyanAccent)
)

val Gradient3DCard = Brush.verticalGradient(
    colors = listOf(Color(0xFF201844), Color(0xFF120E28))
)

val GradientGlowSphere = Brush.radialGradient(
    colors = listOf(CyanAccent, ElectricViolet, Color.Transparent)
)
