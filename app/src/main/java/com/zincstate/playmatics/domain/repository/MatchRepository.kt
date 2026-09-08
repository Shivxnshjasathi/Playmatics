package com.zincstate.playmatics.domain.repository

import com.zincstate.playmatics.domain.engine.Difficulty
import com.zincstate.playmatics.domain.model.Match
import com.zincstate.playmatics.domain.model.PlayerProgress
import kotlinx.coroutines.flow.Flow

/**
 * Multiplayer match operations (Supabase-backed).
 */
interface MatchRepository {

    /** Ensure the user has an anonymous auth session. Returns the user ID. */
    suspend fun ensureAuthenticated(): String

    /** Create a new room. Returns the Match. */
    suspend fun createRoom(difficulty: Difficulty, seed: Long, roomCode: String, gameType: String): Match

    /** Join an existing room by code. Returns the updated Match. */
    suspend fun joinRoom(roomCode: String): Match

    /** Look up a match by room code (for the confirm step). */
    suspend fun findRoomByCode(roomCode: String): Match?

    /** Delete a waiting room (host cancel). */
    suspend fun deleteRoom(matchId: String)

    /** Observe match row changes (Postgres Changes). */
    fun observeMatch(matchId: String): Flow<Match>

    /** Join the Realtime channel for a match. */
    suspend fun joinMatchChannel(matchId: String)

    /** Leave/unsubscribe from the Realtime channel. */
    suspend fun leaveMatchChannel()

    /** Send own progress broadcast. */
    suspend fun sendProgress(solvedCount: Int)

    /** Observe opponent progress broadcasts. */
    fun observeOpponentProgress(): Flow<PlayerProgress>

    /** Complete the match (first to 81 wins). Returns the final Match, or null if someone already won. */
    suspend fun completeMatch(matchId: String): Match?

    /** Forfeit the match (disconnect grace period expired). */
    suspend fun forfeitMatch(matchId: String, forfeitingPlayerId: String): Match?

    /** Observe opponent presence (join/leave). */
    fun observeOpponentPresence(): Flow<Boolean>

    /** Check network connectivity. */
    fun observeConnectivity(): Flow<Boolean>
}
