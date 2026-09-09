package com.zincstate.playmatics.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PureWhite = Color(0xFFFFFFFF)

// ============================================================================
// SUNSET MINIMALIST
// ============================================================================
val SunsetOrange = Color(0xFFFF7E67)
val SunsetPeach = Color(0xFFFFA27A)
val SunsetRed = Color(0xFFE05D5D)
val SunsetGreen = Color(0xFF60A561)
val SunsetDarkText = Color(0xFF2D2825)
val SunsetDarkSurface = Color(0xFF3D3733)
val SunsetBrownMuted = Color(0xFF756A63)
val SunsetBgLight = Color(0xFFFFFBF7)
val SunsetSurfaceVarLight = Color(0xFFF6EFE9)
val SunsetOutlineLight = Color(0xFFE8DFD8)
val SunsetGridLineLight = Color(0xFFD6C8BC)
val SunsetSurfaceVarDark = Color(0xFF4A433E)
val SunsetOutlineDark = Color(0xFF5C544E)

// Removed Neon and Classic themes

// ============================================================================
// DYNAMIC THEME BINDINGS (Legacy Aliases)
// Maps old hardcoded colors dynamically to MaterialTheme
// ============================================================================
val LogoGreen @Composable get() = MaterialTheme.colorScheme.primary
val LogoNavy @Composable get() = MaterialTheme.colorScheme.surface
val LogoCream @Composable get() = MaterialTheme.colorScheme.onBackground
val AccentBlue @Composable get() = MaterialTheme.colorScheme.primary
val AccentBlueLight @Composable get() = MaterialTheme.colorScheme.primaryContainer

val DeepSpace @Composable get() = MaterialTheme.colorScheme.onBackground
val SlateSurface @Composable get() = MaterialTheme.colorScheme.surface
val NeonCyan @Composable get() = MaterialTheme.colorScheme.primary
val NeonPink @Composable get() = MaterialTheme.colorScheme.error

val CellGivenLight       @Composable get() = MaterialTheme.colorScheme.surfaceVariant
val CellGivenDark        @Composable get() = MaterialTheme.colorScheme.surface
val CellUserCorrect      @Composable get() = MaterialTheme.colorScheme.primary
val CellUserIncorrect    @Composable get() = MaterialTheme.colorScheme.error
val CellSelected         @Composable get() = MaterialTheme.colorScheme.primaryContainer
val CellHighlightSame    @Composable get() = MaterialTheme.colorScheme.secondaryContainer
val CellConflict         @Composable get() = MaterialTheme.colorScheme.errorContainer
val WinGreen             @Composable get() = MaterialTheme.colorScheme.secondary
val LossRed              @Composable get() = MaterialTheme.colorScheme.error
val ProgressYou          @Composable get() = MaterialTheme.colorScheme.primary
val ProgressOpponent     @Composable get() = MaterialTheme.colorScheme.tertiary