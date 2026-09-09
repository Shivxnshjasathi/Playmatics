package com.zincstate.playmatics.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * User preferences / settings.
 */
interface SettingsRepository {

    /** null = follow system, true = dark, false = light */
    fun observeThemeMode(): Flow<Boolean?>

    fun observeThemePalette(): Flow<String>

    fun observeHapticsEnabled(): Flow<Boolean>

    fun observeHighlightMistakes(): Flow<Boolean>

    fun observeMusicEnabled(): Flow<Boolean>

    fun observeSfxEnabled(): Flow<Boolean>

    suspend fun setThemeMode(isDark: Boolean?)

    suspend fun setThemePalette(palette: String)

    suspend fun setHapticsEnabled(enabled: Boolean)

    suspend fun setHighlightMistakes(enabled: Boolean)

    suspend fun setMusicEnabled(enabled: Boolean)

    suspend fun setSfxEnabled(enabled: Boolean)

    fun observeMistakeLimitEnabled(): Flow<Boolean>
    suspend fun setMistakeLimitEnabled(enabled: Boolean)
}
