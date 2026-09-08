package com.zincstate.playmatics.ui.theme

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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PlaymaticsLightScheme = lightColorScheme(
    primary = AccentBlue,
    onPrimary = LightSurface,
    primaryContainer = AccentBlueLight,
    onPrimaryContainer = AccentBlue,
    secondary = CellUserCorrect,
    onSecondary = LightSurface,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVar,
    outline = LightOutline,
    error = CellUserIncorrect,
    onError = LightSurface
)

private val PlaymaticsDarkScheme = darkColorScheme(
    primary = LogoGreen,
    onPrimary = DarkBackground,
    primaryContainer = LogoGreen.copy(alpha = 0.3f),
    onPrimaryContainer = LogoGreen,
    secondary = LogoGreen,
    onSecondary = DarkBackground,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVar,
    outline = DarkOutline,
    error = CellUserIncorrect,
    onError = DarkSurface
)

@Composable
fun PlaymaticsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // off by default — our palette is intentional
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> PlaymaticsDarkScheme
        else -> PlaymaticsLightScheme
    }

    // Status bar styling
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PlaymaticsTypography,
        content = content
    )
}