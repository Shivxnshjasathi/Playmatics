package com.zincstate.playmatics.data.repository

import com.zincstate.playmatics.data.local.dao.PuzzleDao
import com.zincstate.playmatics.data.local.dao.StatsDao
import com.zincstate.playmatics.data.local.entity.SavedPuzzleEntity
import com.zincstate.playmatics.data.local.entity.StatsEntity
import com.zincstate.playmatics.domain.engine.Difficulty
import com.zincstate.playmatics.domain.repository.PlayerStats
import com.zincstate.playmatics.domain.repository.PuzzleRepository
import com.zincstate.playmatics.domain.repository.SavedPuzzle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PuzzleRepositoryImpl @Inject constructor(
    private val puzzleDao: PuzzleDao,
    private val statsDao: StatsDao
) : PuzzleRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun observeActivePuzzle(): Flow<SavedPuzzle?> {
        return puzzleDao.getActivePuzzle().map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun savePuzzle(puzzle: SavedPuzzle): Long {
        val entity = puzzle.toEntity()
        return if (puzzle.id == 0L) {
            puzzleDao.insertPuzzle(entity)
        } else {
            puzzleDao.updatePuzzle(entity)
            puzzle.id
        }
    }

    override suspend fun deletePuzzle(id: Long) {
        puzzleDao.deletePuzzle(id)
    }

    override fun observeStats(): Flow<PlayerStats> {
        return statsDao.getStats().map { entity ->
            entity?.toDomain() ?: PlayerStats()
        }
    }

    override suspend fun recordSinglePlayerCompletion(difficulty: Difficulty, timeMs: Long) {
        statsDao.insertDefault() // no-op if already exists
        statsDao.recordCompletion(difficulty.name, timeMs)
    }

    override suspend fun recordMultiplayerResult(won: Boolean) {
        statsDao.insertDefault()
        statsDao.recordMultiplayerResult(won)
    }

    // ------------------------------------------------------------------ //
    //  Mappers                                                            //
    // ------------------------------------------------------------------ //

    private fun SavedPuzzleEntity.toDomain() = SavedPuzzle(
        id = id,
        seed = seed,
        difficulty = Difficulty.valueOf(difficulty),
        givenCellsSnapshot = givenCellsSnapshot,
        currentBoardSnapshot = currentBoardSnapshot,
        pencilNotes = try {
            json.decodeFromString<Map<Int, Set<Int>>>(pencilNotes)
        } catch (_: Exception) { emptyMap() },
        elapsedSeconds = elapsedSeconds,
        isCompleted = isCompleted,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun SavedPuzzle.toEntity() = SavedPuzzleEntity(
        id = id,
        seed = seed,
        difficulty = difficulty.name,
        givenCellsSnapshot = givenCellsSnapshot,
        currentBoardSnapshot = currentBoardSnapshot,
        pencilNotes = json.encodeToString(pencilNotes),
        elapsedSeconds = elapsedSeconds,
        isCompleted = isCompleted,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun StatsEntity.toDomain() = PlayerStats(
        gamesPlayed = gamesPlayed,
        gamesWon = gamesWon,
        bestTimeEasyMs = bestTimeEasyMs,
        bestTimeNormalMs = bestTimeNormalMs,
        bestTimeHardMs = bestTimeHardMs,
        currentWinStreak = currentWinStreak,
        longestWinStreak = longestWinStreak,
        multiplayerMatchesPlayed = multiplayerMatchesPlayed,
        multiplayerWins = multiplayerWins
    )
}
