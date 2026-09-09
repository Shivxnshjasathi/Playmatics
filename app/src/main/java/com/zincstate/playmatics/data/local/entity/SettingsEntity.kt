package com.zincstate.playmatics.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    /** null = follow system, true = dark, false = light */
    val isDarkTheme: Boolean? = null,
    val themePalette: String = "Sunset",
    val haptics: Boolean = true,
    val highlightMistakes: Boolean = true,
    val mistakeLimitEnabled: Boolean = false,
    val musicEnabled: Boolean = false,
    val sfxEnabled: Boolean = true
)
