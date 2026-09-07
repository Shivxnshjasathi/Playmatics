package com.zincstate.playmatics.domain.engine

import kotlin.random.Random

/**
 * Pure-Kotlin Sudoku engine: deterministic puzzle generation, validation,
 * conflict detection, and uniqueness checking.
 *
 * Every public function is free of Android dependencies and safe to call
 * from any thread (no I/O, no shared mutable state).
 */
object SudokuEngine {

    private const val SIZE = 9
    private const val BOX = 3

    // ------------------------------------------------------------------ //
    //  Public API                                                         //
    // ------------------------------------------------------------------ //

    /**
     * Generate a puzzle deterministically from [seed] + [difficulty].
     * The same inputs **always** produce the same board on any device.
     *
     * The generator:
     * 1. Fills a complete valid grid using backtracking with shuffled candidates.
     * 2. Removes [Difficulty.cellsToRemove] cells while preserving uniqueness.
     */
    fun generate(seed: Long, difficulty: Difficulty): SudokuPuzzle {
        val rng = Random(seed)
        val solution = Array(SIZE) { IntArray(SIZE) }
        fillBoard(solution, rng)

        val puzzle = solution.map { it.copyOf() }.toTypedArray()
        removeClues(puzzle, difficulty.cellsToRemove, rng)

        return SudokuPuzzle(
            seed = seed,
            difficulty = difficulty,
            givenCells = puzzle,
            solution = solution
        )
    }

    /**
     * Returns `true` if placing [value] at ([row], [col]) doesn't violate
     * any Sudoku constraint (row, column, 3×3 box) on the current [board].
     * Empty cells (0) in peers are ignored.
     */
    fun isValidPlacement(board: Array<IntArray>, row: Int, col: Int, value: Int): Boolean {
        if (value == 0) return true
        // Row check
        for (c in 0 until SIZE) {
            if (c != col && board[row][c] == value) return false
        }
        // Column check
        for (r in 0 until SIZE) {
            if (r != row && board[r][col] == value) return false
        }
        // Box check
        val boxRow = (row / BOX) * BOX
        val boxCol = (col / BOX) * BOX
        for (r in boxRow until boxRow + BOX) {
            for (c in boxCol until boxCol + BOX) {
                if (r != row || c != col) {
                    if (board[r][c] == value) return false
                }
            }
        }
        return true
    }

    /**
     * Returns the set of cell coordinates that conflict with placing
     * [value] at ([row], [col]) on the current [board].
     */
    fun conflictingCells(
        board: Array<IntArray>,
        row: Int,
        col: Int,
        value: Int
    ): Set<Pair<Int, Int>> {
        if (value == 0) return emptySet()
        val conflicts = mutableSetOf<Pair<Int, Int>>()

        // Row
        for (c in 0 until SIZE) {
            if (c != col && board[row][c] == value) conflicts += row to c
        }
        // Column
        for (r in 0 until SIZE) {
            if (r != row && board[r][col] == value) conflicts += r to col
        }
        // Box
        val boxRow = (row / BOX) * BOX
        val boxCol = (col / BOX) * BOX
        for (r in boxRow until boxRow + BOX) {
            for (c in boxCol until boxCol + BOX) {
                if ((r != row || c != col) && board[r][c] == value) {
                    conflicts += r to c
                }
            }
        }
        return conflicts
    }

    /**
     * Counts cells in [board] that are filled AND match the [solution].
     * This is the canonical progress metric: only correct cells count.
     */
    fun countCorrectCells(board: Array<IntArray>, solution: Array<IntArray>): Int {
        var count = 0
        for (r in 0 until SIZE) {
            for (c in 0 until SIZE) {
                if (board[r][c] != 0 && board[r][c] == solution[r][c]) count++
            }
        }
        return count
    }

    /**
     * Returns `true` if [board] (with 0s as blanks) has exactly one solution.
     * Internally uses a counting solver that bails out as soon as it finds
     * a second solution — O(1) for most valid puzzles, bounded worst-case.
     */
    fun hasUniqueSolution(board: Array<IntArray>): Boolean {
        val copy = board.map { it.copyOf() }.toTypedArray()
        return countSolutions(copy, limit = 2) == 1
    }

    // ------------------------------------------------------------------ //
    //  Board generation (private)                                         //
    // ------------------------------------------------------------------ //

    /**
     * Fills an empty 9×9 [board] completely using randomized backtracking.
     * Returns true on success (always succeeds for a blank board).
     */
    private fun fillBoard(board: Array<IntArray>, rng: Random): Boolean {
        val emptyCell = findEmpty(board) ?: return true // all filled
        val (row, col) = emptyCell
        val candidates = (1..SIZE).shuffled(rng)
        for (num in candidates) {
            if (isValidPlacement(board, row, col, num)) {
                board[row][col] = num
                if (fillBoard(board, rng)) return true
                board[row][col] = 0
            }
        }
        return false
    }

    /**
     * Removes exactly [count] clues from [board] while guaranteeing the
     * remaining puzzle has a unique solution.
     *
     * Strategy: shuffle all 81 positions, try removing each one; if removal
     * breaks uniqueness, put it back and move on.
     */
    private fun removeClues(board: Array<IntArray>, count: Int, rng: Random) {
        val positions = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until SIZE) for (c in 0 until SIZE) positions += r to c
        positions.shuffle(rng)

        var removed = 0
        for ((r, c) in positions) {
            if (removed >= count) break
            val backup = board[r][c]
            if (backup == 0) continue
            board[r][c] = 0
            if (hasUniqueSolution(board)) {
                removed++
            } else {
                board[r][c] = backup // put it back, uniqueness violated
            }
        }
    }

    // ------------------------------------------------------------------ //
    //  Solver (private)                                                   //
    // ------------------------------------------------------------------ //

    /**
     * Counts solutions of [board] up to [limit]. Stops early once [limit]
     * solutions have been found. Mutates [board] but restores it.
     */
    private fun countSolutions(board: Array<IntArray>, limit: Int): Int {
        val emptyCell = findEmpty(board) ?: return 1 // board is complete
        val (row, col) = emptyCell
        var count = 0
        for (num in 1..SIZE) {
            if (isValidPlacement(board, row, col, num)) {
                board[row][col] = num
                count += countSolutions(board, limit - count)
                board[row][col] = 0
                if (count >= limit) return count
            }
        }
        return count
    }

    // ------------------------------------------------------------------ //
    //  Utility                                                            //
    // ------------------------------------------------------------------ //

    /** Returns the first empty cell (row, col) scanning left-to-right, top-to-bottom. */
    private fun findEmpty(board: Array<IntArray>): Pair<Int, Int>? {
        for (r in 0 until SIZE) {
            for (c in 0 until SIZE) {
                if (board[r][c] == 0) return r to c
            }
        }
        return null
    }
}
