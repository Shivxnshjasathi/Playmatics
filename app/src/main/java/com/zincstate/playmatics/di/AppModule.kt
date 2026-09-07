package com.zincstate.playmatics.di

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
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
        return Room.databaseBuilder(
            context,
            SudokuDatabase::class.java,
            "sudoku_db"
        )
            .setDriver(BundledSQLiteDriver())
            .build()
    }

    @Provides
    fun providePuzzleDao(db: SudokuDatabase): PuzzleDao = db.puzzleDao()

    @Provides
    fun provideStatsDao(db: SudokuDatabase): StatsDao = db.statsDao()

    @Provides
    fun provideSettingsDao(db: SudokuDatabase): SettingsDao = db.settingsDao()
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
