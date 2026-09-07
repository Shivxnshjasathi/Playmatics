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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

data class CreateRoomUiState(
    val difficulty: Difficulty = Difficulty.NORMAL,
    val isCreating: Boolean = false,
    val error: String? = null
)

data class WaitingLobbyUiState(
    val matchId: String = "",
    val roomCode: String = "",
    val difficulty: Difficulty = Difficulty.NORMAL,
    val seed: Long = 0L,
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
    data class MatchStarted(val matchId: String, val seed: Long, val difficulty: String) : LobbyEvent()
    data class JoinSuccess(val matchId: String, val seed: Long, val difficulty: String) : LobbyEvent()
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

    private val _events = MutableSharedFlow<LobbyEvent>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            matchRepository.ensureAuthenticated()
        }
    }

    fun setDifficulty(difficulty: Difficulty) {
        _createState.update { it.copy(difficulty = difficulty) }
    }

    fun createRoom() {
        viewModelScope.launch {
            _createState.update { it.copy(isCreating = true, error = null) }
            try {
                val difficulty = _createState.value.difficulty
                val seed = Random.nextLong()
                val roomCode = generateRoomCode()

                val match = matchRepository.createRoom(difficulty, seed, roomCode)

                _waitingState.value = WaitingLobbyUiState(
                    matchId = match.id,
                    roomCode = match.roomCode,
                    difficulty = difficulty,
                    seed = seed
                )

                _createState.update { it.copy(isCreating = false) }
                _events.emit(LobbyEvent.RoomCreated(match.id, match.roomCode))
            } catch (e: Exception) {
                _createState.update { it.copy(isCreating = false, error = e.message) }
            }
        }
    }

    fun startWaitingForOpponent(matchId: String, roomCode: String) {
        viewModelScope.launch {
            try {
                matchRepository.joinMatchChannel(matchId)

                // Poll for match status change (guest joined)
                matchRepository.observeMatch(matchId).collect { match ->
                    if (match.status == MatchStatus.IN_PROGRESS) {
                        _waitingState.update { it.copy(isWaiting = false) }
                        _events.emit(
                            LobbyEvent.MatchStarted(
                                match.id,
                                match.seed,
                                match.difficulty.name
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _waitingState.update { it.copy(error = e.message) }
            }
        }
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
                _joinState.update { it.copy(error = "Enter a 6-character room code") }
                return@launch
            }

            _joinState.update { it.copy(isSearching = true, error = null) }
            try {
                val match = matchRepository.findRoomByCode(code)
                if (match != null) {
                    _joinState.update { it.copy(isSearching = false, foundMatch = match) }
                } else {
                    _joinState.update { it.copy(isSearching = false, error = "Room not found or already started") }
                }
            } catch (e: Exception) {
                _joinState.update { it.copy(isSearching = false, error = e.message) }
            }
        }
    }

    fun confirmJoin() {
        viewModelScope.launch {
            _joinState.update { it.copy(isJoining = true, error = null) }
            try {
                val match = matchRepository.joinRoom(_joinState.value.roomCode)
                _joinState.update { it.copy(isJoining = false) }
                _events.emit(
                    LobbyEvent.JoinSuccess(
                        match.id,
                        match.seed,
                        match.difficulty.name
                    )
                )
            } catch (e: Exception) {
                val msg = if (e.message?.contains("ROOM_UNAVAILABLE") == true)
                    "Room not found or already started"
                else e.message ?: "Failed to join"
                _joinState.update { it.copy(isJoining = false, error = msg) }
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
}
