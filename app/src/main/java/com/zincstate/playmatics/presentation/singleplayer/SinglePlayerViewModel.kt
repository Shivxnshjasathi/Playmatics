package com.zincstate.playmatics.presentation.singleplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zincstate.playmatics.domain.engine.Difficulty
import com.zincstate.playmatics.domain.engine.EngineFactory
import com.zincstate.playmatics.domain.engine.GameType
import com.zincstate.playmatics.domain.engine.SudokuEngine
import com.zincstate.playmatics.domain.model.CellState
import com.zincstate.playmatics.domain.model.GameState
import com.zincstate.playmatics.domain.repository.PuzzleRepository
import com.zincstate.playmatics.domain.repository.SavedPuzzle
import com.zincstate.playmatics.domain.repository.SettingsRepository
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

@HiltViewModel
class SinglePlayerViewModel @Inject constructor(
    private val puzzleRepository: PuzzleRepository,
    settingsRepository: SettingsRepository,
    private val audioPlayer: com.zincstate.playmatics.presentation.audio.AudioPlayer
) : ViewModel() {

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    val highlightMistakes = settingsRepository.observeHighlightMistakes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val hapticsEnabled = settingsRepository.observeHapticsEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private var timerJob: Job? = null
    private var saveJob: Job? = null
    private var currentEngine = EngineFactory.getEngine(GameType.SUDOKU)

    fun initGame(seed: Long, difficultyName: String, gameTypeName: String = "sudoku") {
        val difficulty = try { Difficulty.valueOf(difficultyName) } catch (_: Exception) { Difficulty.NORMAL }
        val gameType = GameType.fromKey(gameTypeName)
        currentEngine = EngineFactory.getEngine(gameType)

        viewModelScope.launch {
            // Generate puzzle on background thread
            val puzzle = withContext(Dispatchers.Default) {
                currentEngine.generate(seed, difficulty)
            }

            val board = puzzle.givenCells.map { it.copyOf() }.toTypedArray()
            val cellStates = computeCellStates(board, puzzle.givenCells, puzzle.solution)
            val correctCount = currentEngine.countCorrectCells(board, puzzle.solution)

            _state.value = GameState(
                seed = seed,
                difficulty = difficulty,
                gameType = gameType,
                board = board,
                givenCells = puzzle.givenCells,
                solution = puzzle.solution,
                cellStates = cellStates,
                correctCount = correctCount,
                isLoading = false,
                variantMetadata = puzzle.variantMetadata
            )

            startTimer()
        }
    }

    fun restoreGame(savedPuzzle: SavedPuzzle) {
        viewModelScope.launch {
            val difficulty = savedPuzzle.difficulty
            val puzzle = withContext(Dispatchers.Default) {
                currentEngine.generate(savedPuzzle.seed, difficulty)
            }

            val board = boardFromString(savedPuzzle.currentBoardSnapshot)
            val cellStates = computeCellStates(board, puzzle.givenCells, puzzle.solution)
            val correctCount = currentEngine.countCorrectCells(board, puzzle.solution)

            _state.value = GameState(
                seed = savedPuzzle.seed,
                difficulty = difficulty,
                board = board,
                givenCells = puzzle.givenCells,
                solution = puzzle.solution,
                pencilNotes = savedPuzzle.pencilNotes,
                elapsedSeconds = savedPuzzle.elapsedSeconds,
                cellStates = cellStates,
                correctCount = correctCount,
                isLoading = false,
                savedPuzzleId = savedPuzzle.id,
                variantMetadata = puzzle.variantMetadata
            )

            startTimer()
        }
    }

    fun selectCell(row: Int, col: Int) {
        _state.update { it.copy(selectedCell = row to col, conflictCells = emptySet()) }
    }

    fun enterDigit(digit: Int) {
        val current = _state.value
        val (row, col) = current.selectedCell ?: return
        if (current.cellStates[row][col] == CellState.GIVEN) return
        if (current.isCompleted) return

        if (current.isNotesMode) {
            // Toggle pencil note
            val key = row * current.board.size + col
            val existingNotes = current.pencilNotes[key] ?: emptySet()
            val newNotes = if (digit in existingNotes) existingNotes - digit else existingNotes + digit
            val updatedPencilNotes = current.pencilNotes.toMutableMap()
            if (newNotes.isEmpty()) updatedPencilNotes.remove(key)
            else updatedPencilNotes[key] = newNotes

            _state.update {
                it.copy(
                    pencilNotes = updatedPencilNotes,
                    board = it.board.also { board -> board[row][col] = 0 }
                )
            }
        } else {
            // Place digit
            val newBoard = current.board.map { it.copyOf() }.toTypedArray()
            newBoard[row][col] = digit

            // Clear pencil note for this cell
            val updatedNotes = current.pencilNotes.toMutableMap()
            updatedNotes.remove(row * current.board.size + col)

            val conflicts = currentEngine.conflictingCells(
                newBoard, row, col, digit, current.variantMetadata
            )
            val cellStates = computeCellStates(newBoard, current.givenCells, current.solution)
            val correctCount = currentEngine.countCorrectCells(newBoard, current.solution)
            val totalCells = current.solution.sumOf { r -> r.count { it != 0 } }
            val isCompleted = correctCount == totalCells

            _state.update {
                it.copy(
                    board = newBoard,
                    cellStates = cellStates,
                    conflictCells = conflicts,
                    correctCount = correctCount,
                    isCompleted = isCompleted,
                    pencilNotes = updatedNotes
                )
            }

            if (isCompleted) {
                onPuzzleCompleted()
                audioPlayer.playWin()
            } else if (conflicts.isNotEmpty()) {
                audioPlayer.playError()
            } else {
                audioPlayer.playClick()
            }
        }

        scheduleSave()
    }

    fun eraseCell() {
        val current = _state.value
        val (row, col) = current.selectedCell ?: return
        if (current.cellStates[row][col] == CellState.GIVEN) return
        if (current.isCompleted) return

        val newBoard = current.board.map { it.copyOf() }.toTypedArray()
        newBoard[row][col] = 0

        // Also clear pencil notes
        val updatedNotes = current.pencilNotes.toMutableMap()
        updatedNotes.remove(row * current.board.size + col)

        val cellStates = computeCellStates(newBoard, current.givenCells, current.solution)
        val correctCount = currentEngine.countCorrectCells(newBoard, current.solution)

        _state.update {
            it.copy(
                board = newBoard,
                cellStates = cellStates,
                conflictCells = emptySet(),
                correctCount = correctCount,
                pencilNotes = updatedNotes
            )
        }

        audioPlayer.playClick()
        scheduleSave()
    }

    fun toggleNotesMode() {
        _state.update { it.copy(isNotesMode = !it.isNotesMode) }
    }

    /** Count how many times each digit 1-9 appears on the board. */
    fun getDigitCounts(): Map<Int, Int> {
        val board = _state.value.board
        val counts = mutableMapOf<Int, Int>()
        for (row in board) {
            for (v in row) {
                if (v != 0) counts[v] = (counts[v] ?: 0) + 1
            }
        }
        return counts
    }

    // ------------------------------------------------------------------ //
    //  Timer                                                              //
    // ------------------------------------------------------------------ //

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (!_state.value.isCompleted) {
                    _state.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
                }
            }
        }
    }

    // ------------------------------------------------------------------ //
    //  Persistence                                                        //
    // ------------------------------------------------------------------ //

    private fun scheduleSave() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(500) // debounce
            val s = _state.value
            val puzzleId = puzzleRepository.savePuzzle(
                SavedPuzzle(
                    id = s.savedPuzzleId ?: 0L,
                    seed = s.seed,
                    difficulty = s.difficulty,
                    givenCellsSnapshot = boardToString(s.givenCells),
                    currentBoardSnapshot = boardToString(s.board),
                    pencilNotes = s.pencilNotes,
                    elapsedSeconds = s.elapsedSeconds,
                    isCompleted = s.isCompleted
                )
            )
            if (s.savedPuzzleId == null) {
                _state.update { it.copy(savedPuzzleId = puzzleId) }
            }
        }
    }

    private fun onPuzzleCompleted() {
        timerJob?.cancel()
        val s = _state.value
        viewModelScope.launch {
            puzzleRepository.recordSinglePlayerCompletion(
                s.difficulty,
                s.elapsedSeconds * 1000
            )
        }
        scheduleSave()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        saveJob?.cancel()
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

    companion object {
        fun boardToString(board: Array<IntArray>): String {
            return board.joinToString("") { row ->
                row.joinToString("") { it.toString() }
            }
        }

        fun boardFromString(s: String): Array<IntArray> {
            val size = kotlin.math.sqrt(s.length.toDouble()).toInt()
            require(size * size == s.length) { "Board string length must be a perfect square, got ${s.length}" }
            return Array(size) { r ->
                IntArray(size) { c ->
                    s[r * size + c].digitToInt()
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