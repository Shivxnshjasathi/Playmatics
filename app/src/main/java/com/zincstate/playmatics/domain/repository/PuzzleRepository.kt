package com.zincstate.playmatics.domain.repository

import com.zincstate.playmatics.domain.engine.Difficulty
import kotlinx.coroutines.flow.Flow

/**
 * Persistence operations for offline single-player puzzles.
 */
interface PuzzleRepository {

    /** Observe the currently active (in-progress) puzzle, or null. */
    fun observeActivePuzzle(): Flow<SavedPuzzle?>

    /** Save or update a puzzle. Returns the row ID. */
    suspend fun savePuzzle(puzzle: SavedPuzzle): Long

    /** Delete a puzzle by ID. */
    suspend fun deletePuzzle(id: Long)

    /** Observe player stats. */
    fun observeStats(): Flow<PlayerStats>

    /** Record a completed single-player game. */
    suspend fun recordSinglePlayerCompletion(difficulty: Difficulty, timeMs: Long)

    /** Record a multiplayer match result. */
    suspend fun recordMultiplayerResult(won: Boolean)
}

/** Domain representation of a saved puzzle. */
data class SavedPuzzle(
    val id: Long = 0L,
    val seed: Long,
    val difficulty: Difficulty,
    val givenCellsSnapshot: String,   // 81-char string
    val currentBoardSnapshot: String, // 81-char string
    val pencilNotes: Map<Int, Set<Int>>,
    val elapsedSeconds: Long,
    val isCompleted: Boolean,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/** Domain representation of player stats. */
data class PlayerStats(
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val bestTimeEasyMs: Long = 0L,
    val bestTimeNormalMs: Long = 0L,
    val bestTimeHardMs: Long = 0L,
    val currentWinStreak: Int = 0,
    val longestWinStreak: Int = 0,
    val multiplayerMatchesPlayed: Int = 0,
    val multiplayerWins: Int = 0
)
