package com.zincstate.playmatics.di

import android.content.Context
import androidx.room.Room
import com.zincstate.playmatics.data.local.SudokuDatabase
import com.zincstate.playmatics.data.local.dao.PuzzleDao
import com.zincstate.playmatics.data.local.dao.SettingsDao
import com.zincstate.playmatics.data.local.dao.StatsDao
import com.zincstate.playmatics.data.repository.MatchRepositoryImpl
import com.zincstate.playmatics.data.repository.PuzzleRepositoryImpl
import com.zincstate.playmatics.data.repository.SettingsRepositoryImpl
import com.zincstate.playmatics.domain.repository.MatchRepository
import com.zincstate.playmatics.domain.repository.PuzzleRepository
import com.zincstate.playmatics.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SudokuDatabase {
        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE settings ADD COLUMN themePalette TEXT NOT NULL DEFAULT 'Sunset'")
            }
        }
        
        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE settings ADD COLUMN mistakeLimitEnabled INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        return Room.databaseBuilder(
            context,
            SudokuDatabase::class.java,
            "sudoku_db"
        )
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun providePuzzleDao(db: SudokuDatabase): PuzzleDao = db.puzzleDao()

    @Provides
    fun provideStatsDao(db: SudokuDatabase): StatsDao = db.statsDao()

    @Provides
    fun provideSettingsDao(db: SudokuDatabase): SettingsDao = db.settingsDao()

    @Provides
    @Singleton
    fun provideAudioPlayer(@ApplicationContext context: Context): com.zincstate.playmatics.presentation.audio.AudioPlayer {
        return com.zincstate.playmatics.presentation.audio.AudioPlayer(context).apply { init() }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPuzzleRepository(impl: PuzzleRepositoryImpl): PuzzleRepository

    @Binds
    @Singleton
    abstract fun bindMatchRepository(impl: MatchRepositoryImpl): MatchRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
