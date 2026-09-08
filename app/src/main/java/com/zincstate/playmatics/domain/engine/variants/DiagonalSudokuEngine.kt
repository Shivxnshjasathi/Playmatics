package com.zincstate.playmatics.domain.engine.variants

import com.zincstate.playmatics.domain.engine.*
import kotlin.random.Random

/**
 * Diagonal (X) Sudoku engine.
 *
 * Same rules as classic Sudoku **plus** both main diagonals
 * (top-left → bottom-right and top-right → bottom-left) must
 * each contain all digits 1–9 with no repeats.
 */
object DiagonalSudokuEngine : PuzzleEngine {

    override val gameType: GameType = GameType.DIAGONAL_SUDOKU

    private const val SIZE = 9
    private const val BOX = 3

    override fun generate(seed: Long, difficulty: Difficulty): SudokuPuzzle {
        val rng = Random(seed)
        val solution = Array(SIZE) { IntArray(SIZE) }
        fillBoard(solution, rng)

        val puzzle = solution.map { it.copyOf() }.toTypedArray()
        removeClues(puzzle, difficulty.cellsToRemove, rng)

        return SudokuPuzzle(
            seed = seed,
            difficulty = difficulty,
            givenCells = puzzle,
            solution = solution,
            gameType = GameType.DIAGONAL_SUDOKU,
            variantMetadata = VariantMetadata.DiagonalMarker
        )
    }

    override fun isValidPlacement(
        board: Array<IntArray>, row: Int, col: Int, value: Int, metadata: VariantMetadata?
    ): Boolean {
        if (value == 0) return true
        // Standard checks
        if (!SudokuEngine.isValidPlacement(board, row, col, value)) return false
        // Main diagonal (\)
        if (row == col) {
            for (i in 0 until SIZE) {
                if (i != row && board[i][i] == value) return false
            }
        }
        // Anti-diagonal (/)
        if (row + col == SIZE - 1) {
            for (i in 0 until SIZE) {
                if (i != row && board[i][SIZE - 1 - i] == value) return false
            }
        }
        return true
    }

    override fun conflictingCells(
        board: Array<IntArray>, row: Int, col: Int, value: Int, metadata: VariantMetadata?
    ): Set<Pair<Int, Int>> {
        if (value == 0) return emptySet()
        val conflicts = SudokuEngine.conflictingCells(board, row, col, value).toMutableSet()
        // Main diagonal
        if (row == col) {
            for (i in 0 until SIZE) {
                if (i != row && board[i][i] == value) conflicts += i to i
            }
        }
        // Anti-diagonal
        if (row + col == SIZE - 1) {
            for (i in 0 until SIZE) {
                if (i != row && board[i][SIZE - 1 - i] == value) conflicts += i to (SIZE - 1 - i)
            }
        }
        return conflicts
    }

    override fun countCorrectCells(board: Array<IntArray>, solution: Array<IntArray>): Int =
        SudokuEngine.countCorrectCells(board, solution)

    // ── Generation helpers ────────────────────────────────────────────

    private fun fillBoard(board: Array<IntArray>, rng: Random): Boolean {
        val empty = findEmpty(board) ?: return true
        val (r, c) = empty
        for (num in (1..SIZE).shuffled(rng)) {
            if (isValidPlacement(board, r, c, num)) {
                board[r][c] = num
                if (fillBoard(board, rng)) return true
                board[r][c] = 0
            }
        }
        return false
    }

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
            if (hasUniqueSolution(board)) removed++ else board[r][c] = backup
        }
    }

    private fun hasUniqueSolution(board: Array<IntArray>): Boolean {
        val copy = board.map { it.copyOf() }.toTypedArray()
        return countSolutions(copy, 2) == 1
    }

    private fun countSolutions(board: Array<IntArray>, limit: Int): Int {
        val empty = findEmpty(board) ?: return 1
        val (r, c) = empty
        var count = 0
        for (num in 1..SIZE) {
            if (isValidPlacement(board, r, c, num)) {
                board[r][c] = num
                count += countSolutions(board, limit - count)
                board[r][c] = 0
                if (count >= limit) return count
            }
        }
        return count
    }

    private fun findEmpty(board: Array<IntArray>): Pair<Int, Int>? {
        for (r in 0 until SIZE) for (c in 0 until SIZE) if (board[r][c] == 0) return r to c
        return null
    }
}
