package com.zincstate.playmatics.presentation.multiplayer.match

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zincstate.playmatics.domain.engine.Difficulty
import com.zincstate.playmatics.domain.engine.EngineFactory
import com.zincstate.playmatics.domain.engine.GameType
import com.zincstate.playmatics.domain.engine.VariantMetadata
import com.zincstate.playmatics.domain.model.CellState
import com.zincstate.playmatics.domain.repository.MatchRepository
import com.zincstate.playmatics.domain.repository.PuzzleRepository
import com.zincstate.playmatics.presentation.components.MatchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class MultiplayerUiState(
    val board: Array<IntArray> = Array(9) { IntArray(9) },
    val givenCells: Array<IntArray> = Array(9) { IntArray(9) },
    val solution: Array<IntArray> = Array(9) { IntArray(9) },
    val cellStates: Array<Array<CellState>> = Array(9) { Array(9) { CellState.EMPTY } },
    val selectedCell: Pair<Int, Int>? = null,
    val pencilNotes: Map<Int, Set<Int>> = emptyMap(),
    val isNotesMode: Boolean = false,
    val conflictCells: Set<Pair<Int, Int>> = emptySet(),
    val correctCount: Int = 0,
    val opponentCorrectCount: Int = 0,
    val elapsedSeconds: Long = 0L,
    val isLoading: Boolean = true,
    val matchResult: MatchResult? = null,
    val isOpponentOnline: Boolean = true,
    val opponentDisconnectSeconds: Int = 0,
    val gameType: GameType = GameType.SUDOKU,
    val variantMetadata: VariantMetadata? = null,
    val mistakesMade: Int = 0
) {
    val yourProgress: Float get() = correctCount / 81f
    val opponentProgress: Float get() = opponentCorrectCount / 81f

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MultiplayerUiState) return false
        return board.contentDeepEquals(other.board) &&
                selectedCell == other.selectedCell &&
                correctCount == other.correctCount &&
                opponentCorrectCount == other.opponentCorrectCount &&
                elapsedSeconds == other.elapsedSeconds &&
                isLoading == other.isLoading &&
                matchResult == other.matchResult &&
                isNotesMode == other.isNotesMode &&
                conflictCells == other.conflictCells &&
                isOpponentOnline == other.isOpponentOnline &&
                gameType == other.gameType
    }

    override fun hashCode(): Int = correctCount * 31 + opponentCorrectCount
}

@HiltViewModel
class MultiplayerMatchViewModel @Inject constructor(
    private val matchRepository: MatchRepository,
    private val puzzleRepository: PuzzleRepository,
    private val audioPlayer: com.zincstate.playmatics.presentation.audio.AudioPlayer,
    settingsRepository: com.zincstate.playmatics.domain.repository.SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MultiplayerUiState())
    val state: StateFlow<MultiplayerUiState> = _state.asStateFlow()

    val mistakeLimitEnabled = settingsRepository.observeMistakeLimitEnabled()
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), false)

    private var timerJob: Job? = null
    private var disconnectTimerJob: Job? = null
    private var matchId: String = ""
    private var lastBroadcastedCount = 0
    private var currentEngine = EngineFactory.getEngine(GameType.SUDOKU)
    private var isInitialized = false

    fun initMatch(matchId: String, seed: Long, difficultyName: String, gameTypeName: String = "sudoku") {
        if (isInitialized) return
        isInitialized = true
        this.matchId = matchId
        val difficulty = try { Difficulty.valueOf(difficultyName) } catch (_: Exception) { Difficulty.NORMAL }
        val gameType = GameType.fromKey(gameTypeName)
        currentEngine = EngineFactory.getEngine(gameType)

        viewModelScope.launch {
            val puzzle = withContext(Dispatchers.Default) {
                currentEngine.generate(seed, difficulty)
            }

            val board = puzzle.givenCells.map { it.copyOf() }.toTypedArray()
            val cellStates = computeCellStates(board, puzzle.givenCells, puzzle.solution)
            val correctCount = currentEngine.countCorrectCells(board, puzzle.solution)

            _state.value = MultiplayerUiState(
                board = board,
                givenCells = puzzle.givenCells,
                solution = puzzle.solution,
                cellStates = cellStates,
                correctCount = correctCount,
                isLoading = false,
                gameType = gameType,
                variantMetadata = puzzle.variantMetadata
            )

            // Join the realtime channel
            matchRepository.joinMatchChannel(matchId)

            // Start listening for opponent progress
            listenToOpponentProgress()

            // Start listening for opponent forfeit
            listenToOpponentForfeit()

            // Start presence monitoring
            listenToPresence()

            // Start timer
            startTimer()

            // Broadcast initial progress
            matchRepository.sendProgress(correctCount)
        }
    }

    fun selectCell(row: Int, col: Int) {
        _state.update { it.copy(selectedCell = row to col, conflictCells = emptySet()) }
    }

    fun enterDigit(digit: Int) {
        val current = _state.value
        val (row, col) = current.selectedCell ?: return
        if (current.cellStates[row][col] == CellState.GIVEN) return
        if (current.matchResult != null) return

        if (current.isNotesMode) {
            val key = row * current.board.size + col
            val existing = current.pencilNotes[key] ?: emptySet()
            val updated = if (digit in existing) existing - digit else existing + digit
            val newNotes = current.pencilNotes.toMutableMap()
            if (updated.isEmpty()) newNotes.remove(key) else newNotes[key] = updated

            _state.update {
                it.copy(
                    pencilNotes = newNotes,
                    board = it.board.also { b -> b[row][col] = 0 }
                )
            }
        } else {
            val newBoard = current.board.map { it.copyOf() }.toTypedArray()
            newBoard[row][col] = digit

            val newNotes = current.pencilNotes.toMutableMap()
            newNotes.remove(row * current.board.size + col)

            val conflicts = currentEngine.conflictingCells(
                newBoard, row, col, digit, current.variantMetadata
            )
            val cellStates = computeCellStates(newBoard, current.givenCells, current.solution)
            val correctCount = currentEngine.countCorrectCells(newBoard, current.solution)
            val isMistake = digit != current.solution[row][col]
            val newMistakesMade = if (isMistake) current.mistakesMade + 1 else current.mistakesMade
            val hasLost = mistakeLimitEnabled.value && newMistakesMade >= 3

            _state.update {
                it.copy(
                    board = newBoard,
                    cellStates = cellStates,
                    conflictCells = conflicts,
                    correctCount = correctCount,
                    pencilNotes = newNotes,
                    mistakesMade = newMistakesMade
                )
            }

            // Broadcast progress if changed
            if (correctCount != lastBroadcastedCount) {
                lastBroadcastedCount = correctCount
                viewModelScope.launch {
                    matchRepository.sendProgress(correctCount)
                }
            }

            // Check for completion
            val totalCells = current.solution.sumOf { r -> r.count { it != 0 } }
            if (hasLost) {
                onMatchCompleted(lostByMistake = true)
                audioPlayer.playError()
            } else if (correctCount == totalCells) {
                onMatchCompleted()
                audioPlayer.playWin()
            } else if (conflicts.isNotEmpty() || isMistake) {
                audioPlayer.playError()
            } else {
                audioPlayer.playClick()
            }
        }
    }

    fun eraseCell() {
        val current = _state.value
        val (row, col) = current.selectedCell ?: return
        if (current.cellStates[row][col] == CellState.GIVEN) return
        if (current.matchResult != null) return

        val newBoard = current.board.map { it.copyOf() }.toTypedArray()
        newBoard[row][col] = 0

        val newNotes = current.pencilNotes.toMutableMap()
        newNotes.remove(row * current.board.size + col)

        val cellStates = computeCellStates(newBoard, current.givenCells, current.solution)
        val correctCount = currentEngine.countCorrectCells(newBoard, current.solution)

        _state.update {
            it.copy(
                board = newBoard,
                cellStates = cellStates,
                conflictCells = emptySet(),
                correctCount = correctCount,
                pencilNotes = newNotes
            )
        }

        if (correctCount != lastBroadcastedCount) {
            lastBroadcastedCount = correctCount
            viewModelScope.launch { matchRepository.sendProgress(correctCount) }
        }
        
        audioPlayer.playClick()
    }

    fun toggleNotesMode() {
        _state.update { it.copy(isNotesMode = !it.isNotesMode) }
    }

    fun getDigitCounts(): Map<Int, Int> {
        val board = _state.value.board
        val counts = mutableMapOf<Int, Int>()
        for (row in board) for (v in row) if (v != 0) counts[v] = (counts[v] ?: 0) + 1
        return counts
    }

    // ------------------------------------------------------------------ //
    //  Match completion                                                    //
    // ------------------------------------------------------------------ //

    private fun onMatchCompleted(lostByMistake: Boolean = false) {
        timerJob?.cancel()
        viewModelScope.launch {
            if (lostByMistake) {
                val userId = matchRepository.ensureAuthenticated()
                matchRepository.sendForfeitBroadcast()
                matchRepository.forfeitMatch(matchId, userId)
                _state.update { it.copy(matchResult = MatchResult.LOSS) }
                puzzleRepository.recordMultiplayerResult(false)
            } else {
                val result = matchRepository.completeMatch(matchId)
                if (result != null && result.winnerId != null) {
                    val userId = matchRepository.ensureAuthenticated()
                    val won = result.winnerId == userId
                    _state.update {
                        it.copy(matchResult = if (won) MatchResult.WIN else MatchResult.LOSS)
                    }
                    puzzleRepository.recordMultiplayerResult(won)
                } else {
                    // Someone else already won
                    _state.update { it.copy(matchResult = MatchResult.LOSS) }
                    puzzleRepository.recordMultiplayerResult(false)
                }
            }
        }
    }

    // ------------------------------------------------------------------ //
    //  Realtime listeners                                                  //
    // ------------------------------------------------------------------ //

    private fun listenToOpponentProgress() {
        viewModelScope.launch {
            matchRepository.observeOpponentProgress().collect { progress ->
                if (progress.playerId.isNotEmpty()) {
                    _state.update { it.copy(opponentCorrectCount = progress.solvedCount) }

                    // Check if opponent completed (they'll call complete_match, but update UI)
                    val totalCells = _state.value.solution.sumOf { r -> r.count { it != 0 } }
                    if (progress.solvedCount == totalCells && _state.value.matchResult == null) {
                        // Wait briefly for the RPC result
                        delay(500)
                        if (_state.value.matchResult == null) {
                            _state.update { it.copy(matchResult = MatchResult.LOSS) }
                            puzzleRepository.recordMultiplayerResult(false)
                        }
                    }
                }
            }
        }
    }

    private fun listenToOpponentForfeit() {
        viewModelScope.launch {
            matchRepository.observeForfeit().collect { _ ->
                if (_state.value.matchResult == null) {
                    _state.update { it.copy(matchResult = MatchResult.WIN) }
                    puzzleRepository.recordMultiplayerResult(true)
                }
            }
        }
    }

    private fun listenToPresence() {
        viewModelScope.launch {
            matchRepository.observeOpponentPresence().collect { isPresent ->
                _state.update { it.copy(isOpponentOnline = isPresent) }
            }
        }
    }

    // ------------------------------------------------------------------ //
    //  Timer                                                              //
    // ------------------------------------------------------------------ //

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_state.value.matchResult == null) {
                    _state.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        disconnectTimerJob?.cancel()
        viewModelScope.launch {
            try { matchRepository.leaveMatchChannel() } catch (_: Exception) {}
        }
    }

    // ------------------------------------------------------------------ //
    //  Helpers                                                            //
    // ------------------------------------------------------------------ //

    private fun computeCellStates(
        board: Array<IntArray>,
        given: Array<IntArray>,
        solution: Array<IntArray>
    ): Array<Array<CellState>> {
        return Array(board.size) { r ->
            Array(board[r].size) { c ->
                when {
                    given[r][c] != 0 -> CellState.GIVEN
                    board[r][c] == 0 -> CellState.EMPTY
                    board[r][c] == solution[r][c] -> CellState.USER_CORRECT
                    else -> CellState.USER_INCORRECT
                }
            }
        }
    }

    fun onAction(action: String) {
        when (action) {
            "PLAY_WIN" -> audioPlayer.playWin()
            "PLAY_ERROR" -> audioPlayer.playError()
            "PLAY_CLICK" -> audioPlayer.playClick()
        }
    }
}