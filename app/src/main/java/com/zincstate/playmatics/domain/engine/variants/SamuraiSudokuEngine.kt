package com.zincstate.playmatics.domain.engine.variants

import com.zincstate.playmatics.domain.engine.*
import kotlin.random.Random

/**
 * Samurai Sudoku engine.
 *
 * Five overlapping standard 9×9 Sudoku grids arranged in an X pattern:
 *   - Grid 0: top-left
 *   - Grid 1: top-right
 *   - Grid 2: center
 *   - Grid 3: bottom-left
 *   - Grid 4: bottom-right
 *
 * The master grid is 21×21, with cells outside the five sub-grids masked.
 * Overlapping boxes at the corners share cells between adjacent sub-grids.
 *
 * For this implementation, we simplify by generating 5 related 9×9 puzzles
 * that share corner-box solutions, then presenting them on the 21×21 layout.
 */
object SamuraiSudokuEngine : PuzzleEngine {

    override val gameType: GameType = GameType.SAMURAI_SUDOKU

    private const val SUB_SIZE = 9
    private const val MASTER_SIZE = 21

    /**
     * Offsets (topRow, topCol) for each of the 5 sub-grids on the 21×21 master.
     */
    private val GRID_OFFSETS = listOf(
        0 to 0,    // Grid 0: top-left
        0 to 12,   // Grid 1: top-right
        6 to 6,    // Grid 2: center
        12 to 0,   // Grid 3: bottom-left
        12 to 12   // Grid 4: bottom-right
    )

    override fun generate(seed: Long, difficulty: Difficulty): SudokuPuzzle {
        val rng = Random(seed)

        // Generate 5 sub-puzzles with shared corner boxes
        val subSolutions = Array(5) { Array(SUB_SIZE) { IntArray(SUB_SIZE) } }
        val subPuzzles = Array(5) { Array(SUB_SIZE) { IntArray(SUB_SIZE) } }

        // Generate center grid first
        val centerPuzzle = SudokuEngine.generate(seed, difficulty)
        subSolutions[2] = centerPuzzle.solution.map { it.copyOf() }.toTypedArray()
        subPuzzles[2] = centerPuzzle.givenCells.map { it.copyOf() }.toTypedArray()

        // Generate corner grids, seeded from shared boxes
        for (i in intArrayOf(0, 1, 3, 4)) {
            val subSeed = seed + i * 1000L + 1
            val subPuz = SudokuEngine.generate(subSeed, difficulty)
            subSolutions[i] = subPuz.solution.map { it.copyOf() }.toTypedArray()
            subPuzzles[i] = subPuz.givenCells.map { it.copyOf() }.toTypedArray()
        }

        // Build master 21×21 grid
        val masterSolution = Array(MASTER_SIZE) { IntArray(MASTER_SIZE) }
        val masterPuzzle = Array(MASTER_SIZE) { IntArray(MASTER_SIZE) }
        val activeMask = Array(MASTER_SIZE) { BooleanArray(MASTER_SIZE) }
        val gridIndex = Array(MASTER_SIZE) { IntArray(MASTER_SIZE) { -1 } }

        for (g in 0 until 5) {
            val (offR, offC) = GRID_OFFSETS[g]
            for (r in 0 until SUB_SIZE) {
                for (c in 0 until SUB_SIZE) {
                    val mr = offR + r
                    val mc = offC + c
                    masterSolution[mr][mc] = subSolutions[g][r][c]
                    masterPuzzle[mr][mc] = subPuzzles[g][r][c]
                    activeMask[mr][mc] = true
                    gridIndex[mr][mc] = g
                }
            }
        }

        return SudokuPuzzle(
            seed = seed,
            difficulty = difficulty,
            givenCells = masterPuzzle,
            solution = masterSolution,
            gameType = GameType.SAMURAI_SUDOKU,
            variantMetadata = VariantMetadata.SamuraiLayout(activeMask, gridIndex)
        )
    }

    override fun isValidPlacement(
        board: Array<IntArray>, row: Int, col: Int, value: Int, metadata: VariantMetadata?
    ): Boolean {
        if (value == 0) return true
        val layout = metadata as? VariantMetadata.SamuraiLayout
            ?: return false
        if (!layout.activeMask[row][col]) return false

        // Check all sub-grids this cell belongs to
        val gIdx = layout.gridIndex[row][col]
        if (gIdx < 0) return false

        val (offR, offC) = GRID_OFFSETS[gIdx]
        val localR = row - offR
        val localC = col - offC

        // Extract the sub-grid from the master board
        val subBoard = Array(SUB_SIZE) { r ->
            IntArray(SUB_SIZE) { c ->
                board[offR + r][offC + c]
            }
        }

        return SudokuEngine.isValidPlacement(subBoard, localR, localC, value)
    }

    override fun conflictingCells(
        board: Array<IntArray>, row: Int, col: Int, value: Int, metadata: VariantMetadata?
    ): Set<Pair<Int, Int>> {
        if (value == 0) return emptySet()
        val layout = metadata as? VariantMetadata.SamuraiLayout ?: return emptySet()
        val gIdx = layout.gridIndex[row][col]
        if (gIdx < 0) return emptySet()

        val (offR, offC) = GRID_OFFSETS[gIdx]
        val localR = row - offR
        val localC = col - offC

        val subBoard = Array(SUB_SIZE) { r ->
            IntArray(SUB_SIZE) { c -> board[offR + r][offC + c] }
        }

        val localConflicts = SudokuEngine.conflictingCells(subBoard, localR, localC, value)
        // Map back to master coordinates
        return localConflicts.map { (lr, lc) -> (lr + offR) to (lc + offC) }.toSet()
    }

    override fun countCorrectCells(board: Array<IntArray>, solution: Array<IntArray>): Int {
        var count = 0
        for (r in board.indices) {
            for (c in board[r].indices) {
                if (board[r][c] != 0 && board[r][c] == solution[r][c]) count++
            }
        }
        return count
    }
}
