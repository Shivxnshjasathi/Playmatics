package com.zincstate.playmatics.ui.theme

import androidx.compose.ui.graphics.Color

// ── Logo Colors ─────────────────────────────────────────────────────────
val LogoCream = Color(0xFFF1E39B)
val LogoCreamLight = Color(0xFFFDFBF0) // Very light cream for backgrounds
val LogoGreen = Color(0xFFA2B163)
val LogoNavy = Color(0xFF434D62)
val LogoNavyDark = Color(0xFF2C3445) // Darker navy for dark mode backgrounds

// ── Brand / Accent ──────────────────────────────────────────────────────
val AccentBlue = LogoNavy
val AccentBlueLight = LogoGreen.copy(alpha = 0.3f)

// ── Board cell colors (Semantic) ────────────────────────────────────────
val CellGivenLight       = LogoNavy
val CellGivenDark        = LogoCream
val CellUserCorrect      = LogoGreen
val CellUserIncorrect    = Color(0xFFEF4444) // Keep standard red for errors
val CellSelected         = LogoGreen.copy(alpha = 0.4f)
val CellHighlightSame    = LogoCream
val CellConflict         = Color(0xFFFEE2E2)

// ── Light theme ─────────────────────────────────────────────────────────
val LightBackground      = LogoCreamLight
val LightSurface         = LogoCream
val LightSurfaceVariant  = LogoCreamLight
val LightOnBackground    = LogoNavy
val LightOnSurface       = LogoNavy
val LightOnSurfaceVar    = LogoNavy.copy(alpha = 0.7f)
val LightOutline         = LogoNavy.copy(alpha = 0.3f)
val LightGridLine        = LogoNavy.copy(alpha = 0.5f)
val LightGridBlock       = LogoNavy

// ── Dark theme ──────────────────────────────────────────────────────────
val DarkBackground       = LogoNavyDark
val DarkSurface          = LogoNavy
val DarkSurfaceVariant   = LogoNavyDark
val DarkOnBackground     = LogoCreamLight
val DarkOnSurface        = LogoCreamLight
val DarkOnSurfaceVar     = LogoCreamLight.copy(alpha = 0.7f)
val DarkOutline          = LogoCream.copy(alpha = 0.3f)
val DarkGridLine         = LogoCream.copy(alpha = 0.5f)
val DarkGridBlock        = LogoCream

// ── Progress / Status ───────────────────────────────────────────────────
val ProgressYou          = LogoGreen
val ProgressOpponent     = Color(0xFFF59E0B)
val WinGreen             = LogoGreen
val LossRed              = Color(0xFFEF4444)