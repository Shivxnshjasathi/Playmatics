package com.zincstate.playmatics.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * User preferences / settings.
 */
interface SettingsRepository {

    /** null = follow system, true = dark, false = light */
    fun observeThemeMode(): Flow<Boolean?>

    fun observeHapticsEnabled(): Flow<Boolean>

    fun observeHighlightMistakes(): Flow<Boolean>

    suspend fun setThemeMode(isDark: Boolean?)

    suspend fun setHapticsEnabled(enabled: Boolean)

    suspend fun setHighlightMistakes(enabled: Boolean)
}
