package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ExplainerDarkColorScheme = darkColorScheme(
    primary = NeonPurple,
    onPrimary = Color.White,
    primaryContainer = ElectricViolet,
    onPrimaryContainer = Color.White,
    secondary = CyanAccent,
    onSecondary = DarkBg,
    secondaryContainer = VividBlue,
    onSecondaryContainer = Color.White,
    tertiary = GlowGreen,
    onTertiary = DarkBg,
    background = DarkBg,
    onBackground = TextPrimary,
    surface = DarkCard,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DarkCardBorder,
    outlineVariant = Color(0xFF3B3166)
)

private val ExplainerLightColorScheme = lightColorScheme(
    primary = ElectricViolet,
    onPrimary = Color.White,
    secondary = VividBlue,
    onSecondary = Color.White,
    background = Color(0xFFF7F5FC),
    onBackground = Color(0xFF1B1629),
    surface = Color.White,
    onSurface = Color(0xFF1B1629),
    surfaceVariant = Color(0xFFEBE7F6),
    onSurfaceVariant = Color(0xFF554D6B),
    outline = Color(0xFFD2CCE2)
)

@Composable
fun Explainer3DTheme(
    darkTheme: Boolean = true, // Default to sleek modern dark theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> ExplainerDarkColorScheme
        else -> ExplainerLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = colorScheme.background.toArgb()
                it.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(it, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
