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

    override fun observeHapticsEnabled(): Flow<Boolean> {
        return settingsDao.getSettings().map { it?.haptics ?: true }
    }

    override fun observeHighlightMistakes(): Flow<Boolean> {
        return settingsDao.getSettings().map { it?.highlightMistakes ?: true }
    }

    override suspend fun setThemeMode(isDark: Boolean?) {
        settingsDao.insertDefault()
        settingsDao.setThemeMode(isDark)
    }

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        settingsDao.insertDefault()
        settingsDao.setHaptics(enabled)
    }

    override suspend fun setHighlightMistakes(enabled: Boolean) {
        settingsDao.insertDefault()
        settingsDao.setHighlightMistakes(enabled)
    }
}
