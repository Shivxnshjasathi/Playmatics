package com.zincstate.playmatics.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zincstate.playmatics.data.local.entity.StatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface StatsDao {

    @Query("SELECT * FROM stats WHERE id = 1")
    fun getStats(): Flow<StatsEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefault(stats: StatsEntity = StatsEntity()): Long

    @Query("""
        UPDATE stats SET 
            gamesPlayed = gamesPlayed + 1,
            bestTimeEasyMs = CASE 
                WHEN :difficulty = 'EASY' AND (bestTimeEasyMs = 0 OR :timeMs < bestTimeEasyMs) THEN :timeMs 
                ELSE bestTimeEasyMs 
            END,
            bestTimeNormalMs = CASE 
                WHEN :difficulty = 'NORMAL' AND (bestTimeNormalMs = 0 OR :timeMs < bestTimeNormalMs) THEN :timeMs 
                ELSE bestTimeNormalMs 
            END,
            bestTimeHardMs = CASE 
                WHEN :difficulty = 'HARD' AND (bestTimeHardMs = 0 OR :timeMs < bestTimeHardMs) THEN :timeMs 
                ELSE bestTimeHardMs 
            END
        WHERE id = 1
    """)
    suspend fun recordCompletion(difficulty: String, timeMs: Long): Int

    @Query("""
        UPDATE stats SET 
            multiplayerMatchesPlayed = multiplayerMatchesPlayed + 1,
            multiplayerWins = multiplayerWins + CASE WHEN :won THEN 1 ELSE 0 END,
            gamesWon = gamesWon + CASE WHEN :won THEN 1 ELSE 0 END,
            currentWinStreak = CASE WHEN :won THEN currentWinStreak + 1 ELSE 0 END,
            longestWinStreak = CASE 
                WHEN :won AND currentWinStreak + 1 > longestWinStreak THEN currentWinStreak + 1 
                ELSE longestWinStreak 
            END
        WHERE id = 1
    """)
    suspend fun recordMultiplayerResult(won: Boolean): Int
}
