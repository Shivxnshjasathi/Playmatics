package com.zincstate.playmatics.presentation.multiplayer.lobby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zincstate.playmatics.domain.engine.Difficulty
import com.zincstate.playmatics.domain.model.Match
import com.zincstate.playmatics.domain.model.MatchStatus
import com.zincstate.playmatics.domain.repository.MatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

data class CreateRoomUiState(
    val difficulty: Difficulty = Difficulty.NORMAL,
    val mistakeLimitEnabled: Boolean = false,
    val isCreating: Boolean = false,
    val error: String? = null
)

data class WaitingLobbyUiState(
    val matchId: String = "",
    val roomCode: String = "",
    val difficulty: Difficulty = Difficulty.NORMAL,
    val seed: Long = 0L,
    val gameType: String = "sudoku",
    val isWaiting: Boolean = true,
    val error: String? = null
)

data class JoinRoomUiState(
    val roomCode: String = "",
    val isSearching: Boolean = false,
    val foundMatch: Match? = null,
    val isJoining: Boolean = false,
    val error: String? = null
)

sealed class LobbyEvent {
    data class RoomCreated(val matchId: String, val roomCode: String) : LobbyEvent()
    data class MatchStarted(val matchId: String, val seed: Long, val difficulty: String, val gameType: String) : LobbyEvent()
    data class JoinSuccess(val matchId: String, val seed: Long, val difficulty: String, val gameType: String) : LobbyEvent()
    data class Error(val message: String) : LobbyEvent()
}

private val SAFE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

@HiltViewModel
class LobbyViewModel @Inject constructor(
    private val matchRepository: MatchRepository
) : ViewModel() {

    private val _createState = MutableStateFlow(CreateRoomUiState())
    val createState: StateFlow<CreateRoomUiState> = _createState.asStateFlow()

    private val _waitingState = MutableStateFlow(WaitingLobbyUiState())
    val waitingState: StateFlow<WaitingLobbyUiState> = _waitingState.asStateFlow()

    private val _joinState = MutableStateFlow(JoinRoomUiState())
    val joinState: StateFlow<JoinRoomUiState> = _joinState.asStateFlow()

    private val _events = Channel<LobbyEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            matchRepository.ensureAuthenticated()
        }
    }

    fun setDifficulty(difficulty: Difficulty) {
        _createState.update { it.copy(difficulty = difficulty) }
    }

    fun toggleMistakeLimit(enabled: Boolean) {
        _createState.update { it.copy(mistakeLimitEnabled = enabled) }
    }

    fun createRoom(gameType: String) {
        viewModelScope.launch {
            _createState.update { it.copy(isCreating = true, error = null) }
            try {
                // Ensure auth is complete before creating the room
                matchRepository.ensureAuthenticated()

                val difficulty = _createState.value.difficulty
                val seed = Random.nextLong()
                val roomCode = generateRoomCode()

                val match = matchRepository.createRoom(
                    difficulty, 
                    seed, 
                    roomCode, 
                    if (_createState.value.mistakeLimitEnabled) "$gameType|mistakes" else gameType
                )

                _waitingState.value = WaitingLobbyUiState(
                    matchId = match.id,
                    roomCode = match.roomCode,
                    difficulty = difficulty,
                    seed = seed,
                    gameType = match.gameType
                )

                _createState.update { it.copy(isCreating = false) }
                _events.send(LobbyEvent.RoomCreated(match.id, match.roomCode))
            } catch (e: Exception) {
                _createState.update { it.copy(isCreating = false) }
                _events.send(LobbyEvent.Error(e.toUserFriendlyMessage()))
            }
        }
    }

    fun startWaitingForOpponent(matchId: String, roomCode: String) {
        viewModelScope.launch {
            // 1. Start polling DB concurrently so channel join doesn't block it
            launch {
                matchRepository.observeMatch(matchId).collect { match ->
                    _waitingState.update { 
                        it.copy(
                            matchId = match.id,
                            roomCode = match.roomCode,
                            difficulty = match.difficulty,
                            seed = match.seed,
                            gameType = match.gameType
                        ) 
                    }
                    if (match.status == com.zincstate.playmatics.domain.model.MatchStatus.IN_PROGRESS && _waitingState.value.isWaiting) {
                        startGame(match.id, match.seed, match.difficulty.name, match.gameType)
                    }
                }
            }

            try {
                // 2. Join the realtime channel
                matchRepository.joinMatchChannel(matchId)

                // 3. Listen for presence as an immediate fallback (combine to avoid race condition)
                launch {
                    combine(
                        matchRepository.observeOpponentPresence().onStart { emit(false) },
                        _waitingState
                    ) { isPresent, state ->
                        if (isPresent && state.isWaiting && state.matchId.isNotEmpty()) {
                            startGame(state.matchId, state.seed, state.difficulty.name, state.gameType)
                        }
                    }.collect()
                }
            } catch (e: Exception) {
                // If channel fails, DB polling continues as fallback
                android.util.Log.e("LobbyViewModel", "Error joining channel", e)
            }
        }
    }

    private suspend fun startGame(matchId: String, seed: Long, difficulty: String, gameType: String) {
        _waitingState.update { it.copy(isWaiting = false) }
        _events.send(LobbyEvent.MatchStarted(matchId, seed, difficulty, gameType))
    }

    fun cancelRoom() {
        viewModelScope.launch {
            try {
                val matchId = _waitingState.value.matchId
                if (matchId.isNotEmpty()) {
                    matchRepository.leaveMatchChannel()
                    matchRepository.deleteRoom(matchId)
                }
            } catch (_: Exception) { /* best effort */ }
        }
    }

    // ---- Join flow ----

    fun updateRoomCode(code: String) {
        val filtered = code.uppercase().filter { it in SAFE_ALPHABET }.take(6)
        _joinState.update { it.copy(roomCode = filtered, error = null, foundMatch = null) }
    }

    fun searchRoom() {
        viewModelScope.launch {
            val code = _joinState.value.roomCode
            if (code.length != 6) {
                _events.send(LobbyEvent.Error("Enter a 6-character room code"))
                return@launch
            }

            _joinState.update { it.copy(isSearching = true, error = null) }
            try {
                val match = matchRepository.findRoomByCode(code)
                if (match != null) {
                    _joinState.update { it.copy(isSearching = false, foundMatch = match) }
                } else {
                    _joinState.update { it.copy(isSearching = false) }
                    _events.send(LobbyEvent.Error("Room not found or already started"))
                }
            } catch (e: Exception) {
                _joinState.update { it.copy(isSearching = false) }
                _events.send(LobbyEvent.Error(e.toUserFriendlyMessage()))
            }
        }
    }

    fun confirmJoin() {
        viewModelScope.launch {
            _joinState.update { it.copy(isJoining = true, error = null) }
            try {
                val match = matchRepository.joinRoom(_joinState.value.roomCode)
                _joinState.update { it.copy(isJoining = false) }
                _events.send(
                    LobbyEvent.JoinSuccess(
                        match.id,
                        match.seed,
                        match.difficulty.name,
                        match.gameType
                    )
                )
            } catch (e: Exception) {
                val msg = if (e.message?.contains("ROOM_UNAVAILABLE") == true) {
                    "Room not found or already started (Err: ROOM_UNAVAILABLE)."
                } else {
                    e.toUserFriendlyMessage()
                }
                _joinState.update { it.copy(isJoining = false) }
                _events.send(LobbyEvent.Error(msg))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            try { matchRepository.leaveMatchChannel() } catch (_: Exception) {}
        }
    }

    private fun generateRoomCode(): String {
        return buildString {
            repeat(6) {
                append(SAFE_ALPHABET[Random.nextInt(SAFE_ALPHABET.length)])
            }
        }
    }

    private fun Throwable.toUserFriendlyMessage(): String {
        val msg = this.message ?: ""
        return when {
            this is java.net.UnknownHostException || msg.contains("Unable to resolve host") || msg.contains("No address associated with hostname") -> 
                "Network Error (Err: DNS_RESOLUTION_FAILED): Please check your internet connection."
            this is java.net.SocketTimeoutException || msg.contains("timeout") || msg.contains("HttpRequestTimeoutException") ->
                "Network Error (Err: CONNECTION_TIMEOUT): The request timed out. Please try again."
            this is java.net.ConnectException || msg.contains("Failed to connect") ->
                "Network Error (Err: CONNECTION_REFUSED): Unable to connect to the server."
            msg.contains("duplicate key value violates unique constraint") -> 
                "Room code collision (Err: CODE_COLLISION): Please try creating again."
            else -> {
                val shortMsg = msg.take(80)
                "Error: $shortMsg"
            }
        }
    }
}
