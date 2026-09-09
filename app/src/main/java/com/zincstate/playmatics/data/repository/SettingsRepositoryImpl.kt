package com.zincstate.playmatics.data.repository

import com.zincstate.playmatics.data.local.dao.SettingsDao
import com.zincstate.playmatics.data.local.entity.SettingsEntity
import com.zincstate.playmatics.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDao: SettingsDao
) : SettingsRepository {

    override fun observeThemeMode(): Flow<Boolean?> {
        return settingsDao.getSettings().map { it?.isDarkTheme }
    }

    override fun observeThemePalette(): Flow<String> {
        return settingsDao.getSettings().map { it?.themePalette ?: "Sunset Minimalist" }
    }

    override fun observeHapticsEnabled(): Flow<Boolean> {
        return settingsDao.getSettings().map { it?.haptics ?: true }
    }

    override fun observeHighlightMistakes(): Flow<Boolean> {
        return settingsDao.getSettings().map { it?.highlightMistakes ?: true }
    }

    override fun observeMusicEnabled(): Flow<Boolean> {
        return settingsDao.getSettings().map { it?.musicEnabled ?: false }
    }

    override fun observeSfxEnabled(): Flow<Boolean> {
        return settingsDao.getSettings().map { it?.sfxEnabled ?: true }
    }

    override suspend fun setThemeMode(isDark: Boolean?) {
        settingsDao.insertDefault()
        settingsDao.setThemeMode(isDark)
    }

    override suspend fun setThemePalette(palette: String) {
        settingsDao.insertDefault()
        settingsDao.setThemePalette(palette)
    }

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        settingsDao.insertDefault()
        settingsDao.setHaptics(enabled)
    }

    override suspend fun setHighlightMistakes(enabled: Boolean) {
        settingsDao.insertDefault()
        settingsDao.setHighlightMistakes(enabled)
    }

    override suspend fun setMusicEnabled(enabled: Boolean) {
        settingsDao.insertDefault()
        settingsDao.setMusicEnabled(enabled)
    }

    override suspend fun setSfxEnabled(enabled: Boolean) {
        settingsDao.insertDefault()
        settingsDao.setSfxEnabled(enabled)
    }

    override fun observeMistakeLimitEnabled(): Flow<Boolean> {
        return settingsDao.getSettings().map { it?.mistakeLimitEnabled ?: false }
    }

    override suspend fun setMistakeLimitEnabled(enabled: Boolean) {
        settingsDao.insertDefault()
        settingsDao.setMistakeLimitEnabled(enabled)
    }
}
