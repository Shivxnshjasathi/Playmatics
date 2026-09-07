package com.zincstate.playmatics.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zincstate.playmatics.data.local.converter.Converters
import com.zincstate.playmatics.data.local.dao.PuzzleDao
import com.zincstate.playmatics.data.local.dao.SettingsDao
import com.zincstate.playmatics.data.local.dao.StatsDao
import com.zincstate.playmatics.data.local.entity.SavedPuzzleEntity
import com.zincstate.playmatics.data.local.entity.SettingsEntity
import com.zincstate.playmatics.data.local.entity.StatsEntity

@Database(
    entities = [
        SavedPuzzleEntity::class,
        StatsEntity::class,
        SettingsEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class SudokuDatabase : RoomDatabase() {
    abstract fun puzzleDao(): PuzzleDao
    abstract fun statsDao(): StatsDao
    abstract fun settingsDao(): SettingsDao
}
