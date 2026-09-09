package com.zincstate.playmatics.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// ============================================================================
// SUNSET MINIMALIST
// ============================================================================
private val SunsetLightScheme = lightColorScheme(
    primary = SunsetOrange,
    onPrimary = PureWhite,
    primaryContainer = SunsetOrange.copy(alpha = 0.3f),
    onPrimaryContainer = SunsetDarkText,
    secondary = SunsetGreen,
    onSecondary = PureWhite,
    secondaryContainer = SunsetSurfaceVarLight,
    onSecondaryContainer = SunsetDarkText,
    tertiary = SunsetPeach,
    onTertiary = SunsetDarkText,
    background = SunsetBgLight,
    onBackground = SunsetDarkText,
    surface = PureWhite,
    onSurface = SunsetDarkText,
    surfaceVariant = SunsetSurfaceVarLight,
    onSurfaceVariant = SunsetBrownMuted,
    outline = SunsetOutlineLight,
    error = SunsetRed,
    onError = PureWhite,
    errorContainer = SunsetRed.copy(alpha = 0.2f),
    onErrorContainer = SunsetRed
)

private val SunsetDarkScheme = darkColorScheme(
    primary = SunsetOrange,
    onPrimary = SunsetDarkText,
    primaryContainer = SunsetOrange.copy(alpha = 0.3f),
    onPrimaryContainer = SunsetOrange,
    secondary = SunsetGreen,
    onSecondary = SunsetDarkText,
    secondaryContainer = SunsetSurfaceVarDark,
    onSecondaryContainer = PureWhite,
    tertiary = SunsetPeach,
    onTertiary = SunsetDarkText,
    background = SunsetDarkText,
    onBackground = SunsetBgLight,
    surface = SunsetDarkSurface,
    onSurface = SunsetBgLight,
    surfaceVariant = SunsetSurfaceVarDark,
    onSurfaceVariant = SunsetBrownMuted,
    outline = SunsetOutlineDark,
    error = SunsetRed,
    onError = SunsetDarkSurface,
    errorContainer = SunsetRed.copy(alpha = 0.2f),
    onErrorContainer = SunsetRed
)

// Removed Neon and Classic Schemes

@Composable
fun PlaymaticsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SunsetDarkScheme else SunsetLightScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PlaymaticsTypography,
        content = content
    )
}