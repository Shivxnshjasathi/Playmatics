package com.zincstate.playmatics.domain.model

import com.zincstate.playmatics.domain.engine.Difficulty

/**
 * Complete UI state for a single-player Sudoku game.
 */
data class GameState(
    val seed: Long = 0L,
    val difficulty: Difficulty = Difficulty.NORMAL,
    /** Current board: 0 = empty, 1-9 = digit. */
    val board: Array<IntArray> = Array(9) { IntArray(9) },
    /** The original given cells (immutable clues). */
    val givenCells: Array<IntArray> = Array(9) { IntArray(9) },
    /** The unique solution. */
    val solution: Array<IntArray> = Array(9) { IntArray(9) },
    /** Currently selected cell, null if none. */
    val selectedCell: Pair<Int, Int>? = null,
    /** Pencil (candidate) notes per cell index (row*9+col) → set of digits. */
    val pencilNotes: Map<Int, Set<Int>> = emptyMap(),
    /** Whether notes mode is active. */
    val isNotesMode: Boolean = false,
    /** Elapsed time in seconds. */
    val elapsedSeconds: Long = 0L,
    /** Whether the puzzle is fully and correctly solved. */
    val isCompleted: Boolean = false,
    /** Number of correctly filled cells (out of 81). */
    val correctCount: Int = 0,
    /** Cells currently in conflict (highlighted as errors). */
    val conflictCells: Set<Pair<Int, Int>> = emptySet(),
    /** Cell states for rendering. */
    val cellStates: Array<Array<CellState>> = Array(9) { Array(9) { CellState.EMPTY } },
    /** Whether the puzzle is still being generated. */
    val isLoading: Boolean = true,
    /** Saved puzzle ID in Room (null for new games). */
    val savedPuzzleId: Long? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GameState) return false
        return seed == other.seed &&
                difficulty == other.difficulty &&
                board.contentDeepEquals(other.board) &&
                givenCells.contentDeepEquals(other.givenCells) &&
                solution.contentDeepEquals(other.solution) &&
                selectedCell == other.selectedCell &&
                pencilNotes == other.pencilNotes &&
                isNotesMode == other.isNotesMode &&
                elapsedSeconds == other.elapsedSeconds &&
                isCompleted == other.isCompleted &&
                correctCount == other.correctCount &&
                conflictCells == other.conflictCells &&
                cellStates.contentDeepEquals(other.cellStates) &&
                isLoading == other.isLoading &&
                savedPuzzleId == other.savedPuzzleId
    }

    override fun hashCode(): Int {
        var result = seed.hashCode()
        result = 31 * result + difficulty.hashCode()
        result = 31 * result + board.contentDeepHashCode()
        result = 31 * result + selectedCell.hashCode()
        result = 31 * result + isCompleted.hashCode()
        result = 31 * result + correctCount
        return result
    }
}
