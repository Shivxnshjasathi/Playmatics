package com.zincstate.playmatics.ui.theme

import androidx.compose.ui.graphics.Color

// ── Brand / Accent ──────────────────────────────────────────────────────
val AccentBlue       = Color(0xFF2962FF)
val AccentBlueDark   = Color(0xFF448AFF)
val AccentBlueLight  = Color(0xFFE3F2FD)

// ── Board cell colors ───────────────────────────────────────────────────
val CellGivenLight       = Color(0xFF1A1A2E)
val CellGivenDark        = Color(0xFFE8E8F0)
val CellUserCorrect      = Color(0xFF00897B)  // teal-green
val CellUserIncorrect    = Color(0xFFE53935)  // coral-red
val CellSelected         = Color(0x332962FF)  // accent with 20% alpha
val CellHighlightSame    = Color(0x1A2962FF)  // accent with 10% alpha
val CellConflict         = Color(0x33E53935)  // red with 20% alpha

// ── Light theme ─────────────────────────────────────────────────────────
val LightBackground      = Color(0xFFF8F9FD)
val LightSurface         = Color(0xFFFFFFFF)
val LightSurfaceVariant  = Color(0xFFF0F1F5)
val LightOnBackground    = Color(0xFF1A1A2E)
val LightOnSurface       = Color(0xFF1A1A2E)
val LightOnSurfaceVar    = Color(0xFF6B7280)
val LightOutline         = Color(0xFFD1D5DB)
val LightGridLine        = Color(0xFFBBBFC6)
val LightGridBlock       = Color(0xFF374151)

// ── Dark theme ──────────────────────────────────────────────────────────
val DarkBackground       = Color(0xFF0F1117)
val DarkSurface          = Color(0xFF1A1C25)
val DarkSurfaceVariant   = Color(0xFF252830)
val DarkOnBackground     = Color(0xFFE8E8F0)
val DarkOnSurface        = Color(0xFFE8E8F0)
val DarkOnSurfaceVar     = Color(0xFF9CA3AF)
val DarkOutline          = Color(0xFF374151)
val DarkGridLine         = Color(0xFF4B5563)
val DarkGridBlock        = Color(0xFFD1D5DB)

// ── Progress / Status ───────────────────────────────────────────────────
val ProgressYou          = Color(0xFF2962FF)
val ProgressOpponent     = Color(0xFFEF6C00)
val WinGreen             = Color(0xFF43A047)
val LossRed              = Color(0xFFE53935)