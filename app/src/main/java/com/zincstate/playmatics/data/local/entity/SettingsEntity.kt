package com.zincstate.playmatics.data.local.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    /** null = follow system, true = dark, false = light */
    val isDarkTheme: Boolean? = null,
    val haptics: Boolean = true,
    val highlightMistakes: Boolean = true
)
