package com.zincstate.playmatics.domain.engine.variants

import com.zincstate.playmatics.domain.engine.*
import kotlin.random.Random

/**
 * Odd/Even Sudoku engine.
 *
 * Standard Sudoku rules **plus** each cell is pre-marked as requiring
 * an odd digit (1, 3, 5, 7, 9) or an even digit (2, 4, 6, 8).
 * The parity map is derived from the generated solution.
 */
object OddEvenSudokuEngine : PuzzleEngine {

    override val gameType: GameType = GameType.ODD_EVEN_SUDOKU

    private const val SIZE = 9

    override fun generate(seed: Long, difficulty: Difficulty): SudokuPuzzle {
        // Generate a standard Sudoku first
        val basePuzzle = SudokuEngine.generate(seed, difficulty)

        // Derive parity map from the solution
        val parityMap = Array(SIZE) { r ->
            BooleanArray(SIZE) { c ->
                basePuzzle.solution[r][c] % 2 != 0  // true = odd
            }
        }

        return SudokuPuzzle(
            seed = seed,
            difficulty = difficulty,
            givenCells = basePuzzle.givenCells,
            solution = basePuzzle.solution,
            gameType = GameType.ODD_EVEN_SUDOKU,
            variantMetadata = VariantMetadata.OddEvenMap(parityMap)
        )
    }

    override fun isValidPlacement(
        board: Array<IntArray>, row: Int, col: Int, value: Int, metadata: VariantMetadata?
    ): Boolean {
        if (value == 0) return true
        if (!SudokuEngine.isValidPlacement(board, row, col, value)) return false
        // Parity check
        val oeMap = metadata as? VariantMetadata.OddEvenMap ?: return true
        val shouldBeOdd = oeMap.parityMap[row][col]
        val isOdd = value % 2 != 0
        return shouldBeOdd == isOdd
    }

    override fun conflictingCells(
        board: Array<IntArray>, row: Int, col: Int, value: Int, metadata: VariantMetadata?
    ): Set<Pair<Int, Int>> {
        if (value == 0) return emptySet()
        val conflicts = SudokuEngine.conflictingCells(board, row, col, value).toMutableSet()
        // If parity mismatch, conflict with self
        val oeMap = metadata as? VariantMetadata.OddEvenMap
        if (oeMap != null) {
            val shouldBeOdd = oeMap.parityMap[row][col]
            val isOdd = value % 2 != 0
            if (shouldBeOdd != isOdd) {
                conflicts += row to col
            }
        }
        return conflicts
    }

    override fun countCorrectCells(board: Array<IntArray>, solution: Array<IntArray>): Int =
        SudokuEngine.countCorrectCells(board, solution)
}
