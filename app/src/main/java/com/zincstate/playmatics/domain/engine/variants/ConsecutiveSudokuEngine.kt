package com.zincstate.playmatics.domain.engine.variants

import com.zincstate.playmatics.domain.engine.*
import kotlin.math.abs
import kotlin.random.Random

/**
 * Consecutive Sudoku engine.
 *
 * Standard Sudoku rules **plus** markers between adjacent cells:
 * - If a marker is present, the two values must differ by exactly 1.
 * - If no marker is present, the two values must NOT differ by exactly 1.
 *
 * Markers are derived from the generated solution, so the puzzle is
 * always consistent and deterministic.
 */
object ConsecutiveSudokuEngine : PuzzleEngine {

    override val gameType: GameType = GameType.CONSECUTIVE_SUDOKU

    private const val SIZE = 9

    override fun generate(seed: Long, difficulty: Difficulty): SudokuPuzzle {
        // Generate a standard Sudoku first
        val basePuzzle = SudokuEngine.generate(seed, difficulty)
        val sol = basePuzzle.solution

        // Derive consecutive markers from the solution
        val hMarkers = Array(SIZE) { r ->
            BooleanArray(SIZE - 1) { c ->
                abs(sol[r][c] - sol[r][c + 1]) == 1
            }
        }
        val vMarkers = Array(SIZE - 1) { r ->
            BooleanArray(SIZE) { c ->
                abs(sol[r][c] - sol[r + 1][c]) == 1
            }
        }

        return SudokuPuzzle(
            seed = seed,
            difficulty = difficulty,
            givenCells = basePuzzle.givenCells,
            solution = sol,
            gameType = GameType.CONSECUTIVE_SUDOKU,
            variantMetadata = VariantMetadata.ConsecutiveMarkers(hMarkers, vMarkers)
        )
    }

    override fun isValidPlacement(
        board: Array<IntArray>, row: Int, col: Int, value: Int, metadata: VariantMetadata?
    ): Boolean {
        if (value == 0) return true
        if (!SudokuEngine.isValidPlacement(board, row, col, value)) return false
        val markers = metadata as? VariantMetadata.ConsecutiveMarkers ?: return true
        return checkConsecutiveConstraints(board, row, col, value, markers)
    }

    override fun conflictingCells(
        board: Array<IntArray>, row: Int, col: Int, value: Int, metadata: VariantMetadata?
    ): Set<Pair<Int, Int>> {
        if (value == 0) return emptySet()
        val conflicts = SudokuEngine.conflictingCells(board, row, col, value).toMutableSet()
        val markers = metadata as? VariantMetadata.ConsecutiveMarkers ?: return conflicts

        // Check horizontal neighbors
        if (col < SIZE - 1 && board[row][col + 1] != 0) {
            val diff = abs(value - board[row][col + 1])
            val shouldBeConsecutive = markers.horizontalMarkers[row][col]
            if ((shouldBeConsecutive && diff != 1) || (!shouldBeConsecutive && diff == 1)) {
                conflicts += row to (col + 1)
            }
        }
        if (col > 0 && board[row][col - 1] != 0) {
            val diff = abs(value - board[row][col - 1])
            val shouldBeConsecutive = markers.horizontalMarkers[row][col - 1]
            if ((shouldBeConsecutive && diff != 1) || (!shouldBeConsecutive && diff == 1)) {
                conflicts += row to (col - 1)
            }
        }
        // Check vertical neighbors
        if (row < SIZE - 1 && board[row + 1][col] != 0) {
            val diff = abs(value - board[row + 1][col])
            val shouldBeConsecutive = markers.verticalMarkers[row][col]
            if ((shouldBeConsecutive && diff != 1) || (!shouldBeConsecutive && diff == 1)) {
                conflicts += (row + 1) to col
            }
        }
        if (row > 0 && board[row - 1][col] != 0) {
            val diff = abs(value - board[row - 1][col])
            val shouldBeConsecutive = markers.verticalMarkers[row - 1][col]
            if ((shouldBeConsecutive && diff != 1) || (!shouldBeConsecutive && diff == 1)) {
                conflicts += (row - 1) to col
            }
        }
        return conflicts
    }

    override fun countCorrectCells(board: Array<IntArray>, solution: Array<IntArray>): Int =
        SudokuEngine.countCorrectCells(board, solution)

    // ── Helpers ──────────────────────────────────────────────────────

    private fun checkConsecutiveConstraints(
        board: Array<IntArray>, row: Int, col: Int, value: Int,
        markers: VariantMetadata.ConsecutiveMarkers
    ): Boolean {
        // Right neighbor
        if (col < SIZE - 1 && board[row][col + 1] != 0) {
            val diff = abs(value - board[row][col + 1])
            val shouldBe = markers.horizontalMarkers[row][col]
            if (shouldBe && diff != 1) return false
            if (!shouldBe && diff == 1) return false
        }
        // Left neighbor
        if (col > 0 && board[row][col - 1] != 0) {
            val diff = abs(value - board[row][col - 1])
            val shouldBe = markers.horizontalMarkers[row][col - 1]
            if (shouldBe && diff != 1) return false
            if (!shouldBe && diff == 1) return false
        }
        // Bottom neighbor
        if (row < SIZE - 1 && board[row + 1][col] != 0) {
            val diff = abs(value - board[row + 1][col])
            val shouldBe = markers.verticalMarkers[row][col]
            if (shouldBe && diff != 1) return false
            if (!shouldBe && diff == 1) return false
        }
        // Top neighbor
        if (row > 0 && board[row - 1][col] != 0) {
            val diff = abs(value - board[row - 1][col])
            val shouldBe = markers.verticalMarkers[row - 1][col]
            if (shouldBe && diff != 1) return false
            if (!shouldBe && diff == 1) return false
        }
        return true
    }
}
