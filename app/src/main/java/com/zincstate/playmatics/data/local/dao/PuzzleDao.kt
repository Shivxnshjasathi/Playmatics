package com.zincstate.playmatics.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import com.zincstate.playmatics.data.local.entity.SavedPuzzleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PuzzleDao {

    @Query("SELECT * FROM saved_puzzles WHERE isCompleted = 0 ORDER BY updatedAt DESC LIMIT 1")
    fun getActivePuzzle(): Flow<SavedPuzzleEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPuzzle(puzzle: SavedPuzzleEntity): Long

    @Update
    suspend fun updatePuzzle(puzzle: SavedPuzzleEntity)

    @Query("DELETE FROM saved_puzzles WHERE id = :id")
    suspend fun deletePuzzle(id: Long)

    @Query("DELETE FROM saved_puzzles WHERE isCompleted = 1")
    suspend fun deleteCompletedPuzzles()
}
