package com.pulse.player.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = RedPrimary,
    onPrimary = LightTextPrimary,
    secondary = CyanAccent,
    onSecondary = DarkBackground,
    background = DarkBackground,
    onBackground = LightTextPrimary,
    surface = DarkSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = LightTextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = RedPrimary,
    onPrimary = LightTextPrimary,
    secondary = CyanAccent,
    background = LightTextPrimary,
    surface = LightTextPrimary
)

@Composable
fun PulsePlayerTheme(
    darkTheme: Boolean = true, // Default to sleek dark mode
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
