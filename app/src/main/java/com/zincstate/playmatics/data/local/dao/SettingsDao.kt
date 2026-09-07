package com.zincstate.playmatics.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.zincstate.playmatics.data.local.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {

    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettings(): Flow<SettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefault(settings: SettingsEntity = SettingsEntity())

    @Query("UPDATE settings SET isDarkTheme = :isDark WHERE id = 1")
    suspend fun setThemeMode(isDark: Boolean?)

    @Query("UPDATE settings SET haptics = :enabled WHERE id = 1")
    suspend fun setHaptics(enabled: Boolean)

    @Query("UPDATE settings SET highlightMistakes = :enabled WHERE id = 1")
    suspend fun setHighlightMistakes(enabled: Boolean)
}
