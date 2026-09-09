package com.zincstate.playmatics.data.repository

import com.zincstate.playmatics.data.remote.ConnectivityObserver
import com.zincstate.playmatics.data.remote.SupabaseMatchManager
import com.zincstate.playmatics.data.remote.dto.MatchDto
import com.zincstate.playmatics.domain.engine.Difficulty
import com.zincstate.playmatics.domain.model.Match
import com.zincstate.playmatics.domain.model.MatchStatus
import com.zincstate.playmatics.domain.model.PlayerProgress
import com.zincstate.playmatics.domain.repository.MatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchRepositoryImpl @Inject constructor(
    private val matchManager: SupabaseMatchManager,
    private val connectivityObserver: ConnectivityObserver
) : MatchRepository {

    override suspend fun ensureAuthenticated(): String {
        return matchManager.ensureAuthenticated()
    }

    override suspend fun createRoom(difficulty: Difficulty, seed: Long, roomCode: String, gameType: String): Match {
        val userId = matchManager.ensureAuthenticated()
        require(userId.isNotEmpty()) { "Authentication failed — cannot create room." }
        val dto = matchManager.createRoom(
            MatchDto(
                roomCode = roomCode,
                seed = seed,
                difficulty = difficulty.name.lowercase(),
                gameType = gameType,
                hostId = userId
            )
        )
        return dto.toDomain()
    }

    override suspend fun joinRoom(roomCode: String): Match {
        val userId = matchManager.getCurrentUserId()
        val dto = matchManager.joinMatch(roomCode.uppercase(), userId)
        return dto.toDomain()
    }

    override suspend fun findRoomByCode(roomCode: String): Match? {
        return matchManager.findRoomByCode(roomCode.uppercase())?.toDomain()
    }

    override suspend fun deleteRoom(matchId: String) {
        matchManager.deleteRoom(matchId)
    }

    override fun observeMatch(matchId: String): Flow<Match> {
        return matchManager.observeMatchChanges(matchId).map { it.toDomain() }
    }

    override suspend fun joinMatchChannel(matchId: String) {
        matchManager.joinMatchChannel(matchId)
        val userId = matchManager.getCurrentUserId()
        matchManager.trackPresence(userId, "Player")
    }

    override suspend fun leaveMatchChannel() {
        matchManager.leaveMatchChannel()
    }

    override suspend fun sendProgress(solvedCount: Int) {
        val userId = matchManager.getCurrentUserId()
        matchManager.sendProgress(userId, solvedCount)
    }

    override fun observeOpponentProgress(): Flow<PlayerProgress> {
        val userId = matchManager.getCurrentUserId()
        return matchManager.observeProgress()
            ?.map { event ->
                PlayerProgress(
                    playerId = event.playerId,
                    solvedCount = event.solvedCount
                )
            }
            ?.let { flow ->
                // Filter out own progress events
                flow.map { progress ->
                    if (progress.playerId != userId) progress
                    else PlayerProgress("", 0)
                }
            } ?: emptyFlow()
    }

    override suspend fun sendForfeitBroadcast() {
        val userId = matchManager.getCurrentUserId()
        matchManager.sendForfeitBroadcast(userId)
    }

    override fun observeForfeit(): Flow<String> {
        val userId = matchManager.getCurrentUserId()
        return matchManager.observeForfeit()
            ?.mapNotNull { event ->
                if (event.playerId != userId) event.playerId else null
            } ?: emptyFlow()
    }

    override suspend fun completeMatch(matchId: String): Match? {
        val userId = matchManager.getCurrentUserId()
        return matchManager.completeMatch(matchId, userId)?.toDomain()
    }

    override suspend fun forfeitMatch(matchId: String, forfeitingPlayerId: String): Match? {
        return matchManager.forfeitMatch(matchId, forfeitingPlayerId)?.toDomain()
    }

    override fun observeOpponentPresence(): Flow<Boolean> {
        return matchManager.observePresenceChanges() ?: emptyFlow()
    }

    
    override suspend fun sendDrawStroke(stroke: List<Float>) {
        val userId = matchManager.getCurrentUserId() ?: return
        matchManager.sendDrawStroke(userId, stroke)
    }

    override suspend fun sendGuess(guess: String) {
        val userId = matchManager.getCurrentUserId() ?: return
        matchManager.sendGuess(userId, guess)
    }

    override suspend fun sendClearBoard() {
        val userId = matchManager.getCurrentUserId() ?: return
        matchManager.sendClearBoard(userId)
    }

    override fun observeDrawStrokes(): Flow<List<Float>> {
        val userId = matchManager.getCurrentUserId()
        return matchManager.observeDrawStrokes()?.mapNotNull {
            if (it.playerId != userId) it.stroke else null
        } ?: kotlinx.coroutines.flow.emptyFlow()
    }

    override fun observeGuesses(): Flow<String> {
        val userId = matchManager.getCurrentUserId()
        return matchManager.observeGuesses()?.mapNotNull {
            if (it.playerId != userId) it.guess else null
        } ?: kotlinx.coroutines.flow.emptyFlow()
    }

    override fun observeClearBoard(): Flow<Unit> {
        val userId = matchManager.getCurrentUserId()
        return matchManager.observeClearBoard()?.mapNotNull {
            if (it.playerId != userId) Unit else null
        } ?: kotlinx.coroutines.flow.emptyFlow()
    }

    override fun observeConnectivity(): Flow<Boolean> {
        return connectivityObserver.observeConnectivity()
    }

    // ------------------------------------------------------------------ //
    //  Mapper                                                             //
    // ------------------------------------------------------------------ //

    private fun MatchDto.toDomain() = Match(
        id = id,
        roomCode = roomCode,
        seed = seed,
        difficulty = try {
            Difficulty.valueOf(difficulty.uppercase())
        } catch (_: Exception) { Difficulty.NORMAL },
        gameType = gameType,
        status = when (status) {
            "waiting" -> MatchStatus.WAITING
            "in_progress" -> MatchStatus.IN_PROGRESS
            "completed" -> MatchStatus.COMPLETED
            else -> MatchStatus.WAITING
        },
        hostId = hostId,
        guestId = guestId,
        winnerId = winnerId
    )
}
