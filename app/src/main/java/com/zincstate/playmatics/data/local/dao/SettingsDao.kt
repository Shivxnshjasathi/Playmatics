package com.zincstate.playmatics.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zincstate.playmatics.data.local.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface SettingsDao {

    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettings(): Flow<SettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefault(settings: SettingsEntity = SettingsEntity()): Long

    @Query("UPDATE settings SET isDarkTheme = :isDark WHERE id = 1")
    suspend fun setThemeMode(isDark: Boolean?): Int

    @Query("UPDATE settings SET haptics = :enabled WHERE id = 1")
    suspend fun setHaptics(enabled: Boolean): Int

    @Query("UPDATE settings SET highlightMistakes = :enabled WHERE id = 1")
    suspend fun setHighlightMistakes(enabled: Boolean): Int
}
