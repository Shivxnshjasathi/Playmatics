package com.zincstate.playmatics.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zincstate.playmatics.data.local.entity.SavedPuzzleEntity
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface PuzzleDao {

    @Query("SELECT * FROM saved_puzzles WHERE isCompleted = 0 ORDER BY updatedAt DESC LIMIT 1")
    fun getActivePuzzle(): Flow<SavedPuzzleEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPuzzle(puzzle: SavedPuzzleEntity): Long

    @Update
    suspend fun updatePuzzle(puzzle: SavedPuzzleEntity): Int

    @Query("DELETE FROM saved_puzzles WHERE id = :id")
    suspend fun deletePuzzle(id: Long): Int

    @Query("DELETE FROM saved_puzzles WHERE isCompleted = 1")
    suspend fun deleteCompletedPuzzles(): Int
}
