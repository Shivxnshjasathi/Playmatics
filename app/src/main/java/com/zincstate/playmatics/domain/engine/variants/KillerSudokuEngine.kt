package com.zincstate.playmatics.domain.engine.variants

import com.zincstate.playmatics.domain.engine.*
import kotlin.random.Random

/**
 * Killer Sudoku engine.
 *
 * Standard Sudoku rules **plus** groups of cells ("cages") that must
 * sum to a given total with no repeated digits within a cage.
 *
 * Cages are generated deterministically from the seed after a valid
 * solution is built, then clues are removed while preserving uniqueness.
 */
object KillerSudokuEngine : PuzzleEngine {

    override val gameType: GameType = GameType.KILLER_SUDOKU

    private const val SIZE = 9
    private const val BOX = 3

    override fun generate(seed: Long, difficulty: Difficulty): SudokuPuzzle {
        val rng = Random(seed)
        val solution = Array(SIZE) { IntArray(SIZE) }
        SudokuEngine.run {
            // Use internal fill via the public generate, then take the solution
        }
        // Generate a standard solution
        val basePuzzle = SudokuEngine.generate(seed, difficulty)
        val sol = basePuzzle.solution

        // Generate cages from the solution
        val cages = generateCages(sol, rng)

        // For Killer Sudoku, we remove more clues since cages provide extra info
        val puzzle = sol.map { it.copyOf() }.toTypedArray()
        val extraRemoval = minOf(difficulty.cellsToRemove + 10, 64)
        removeClues(puzzle, extraRemoval, cages, rng)

        return SudokuPuzzle(
            seed = seed,
            difficulty = difficulty,
            givenCells = puzzle,
            solution = sol,
            gameType = GameType.KILLER_SUDOKU,
            variantMetadata = VariantMetadata.KillerCages(cages)
        )
    }

    override fun isValidPlacement(
        board: Array<IntArray>, row: Int, col: Int, value: Int, metadata: VariantMetadata?
    ): Boolean {
        if (value == 0) return true
        if (!SudokuEngine.isValidPlacement(board, row, col, value)) return false
        val cages = (metadata as? VariantMetadata.KillerCages)?.cages ?: return true
        // Find cage containing this cell
        val cage = cages.firstOrNull { (row to col) in it.cells } ?: return true
        // No repeats in cage
        for ((r, c) in cage.cells) {
            if ((r != row || c != col) && board[r][c] == value) return false
        }
        // If cage is fully filled, check sum
        val filledValues = cage.cells.map { (r, c) ->
            if (r == row && c == col) value else board[r][c]
        }
        if (filledValues.all { it != 0 }) {
            if (filledValues.sum() != cage.targetSum) return false
        } else {
            // Partial sum must not exceed target
            if (filledValues.filter { it != 0 }.sum() >= cage.targetSum) return false
        }
        return true
    }

    override fun conflictingCells(
        board: Array<IntArray>, row: Int, col: Int, value: Int, metadata: VariantMetadata?
    ): Set<Pair<Int, Int>> {
        if (value == 0) return emptySet()
        val conflicts = SudokuEngine.conflictingCells(board, row, col, value).toMutableSet()
        val cages = (metadata as? VariantMetadata.KillerCages)?.cages ?: return conflicts
        val cage = cages.firstOrNull { (row to col) in it.cells } ?: return conflicts
        // Duplicate in cage
        for ((r, c) in cage.cells) {
            if ((r != row || c != col) && board[r][c] == value) conflicts += r to c
        }
        return conflicts
    }

    override fun countCorrectCells(board: Array<IntArray>, solution: Array<IntArray>): Int =
        SudokuEngine.countCorrectCells(board, solution)

    // ── Cage generation ──────────────────────────────────────────────

    /**
     * Generates non-overlapping cages covering all 81 cells.
     * Each cage is 2–5 contiguous cells. Target sum = sum of solution digits.
     */
    private fun generateCages(
        solution: Array<IntArray>, rng: Random
    ): List<VariantMetadata.CageDef> {
        val used = Array(SIZE) { BooleanArray(SIZE) }
        val cages = mutableListOf<VariantMetadata.CageDef>()
        val allCells = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until SIZE) for (c in 0 until SIZE) allCells += r to c
        allCells.shuffle(rng)

        for ((startR, startC) in allCells) {
            if (used[startR][startC]) continue

            // Grow cage from this cell
            val cageCells = mutableListOf(startR to startC)
            used[startR][startC] = true
            val maxSize = rng.nextInt(2, 6) // 2-5 cells

            while (cageCells.size < maxSize) {
                // Find unused neighbors of any cage cell
                val neighbors = mutableListOf<Pair<Int, Int>>()
                for ((r, c) in cageCells) {
                    val dirs = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
                    for ((dr, dc) in dirs) {
                        val nr = r + dr
                        val nc = c + dc
                        if (nr in 0 until SIZE && nc in 0 until SIZE && !used[nr][nc]) {
                            neighbors += nr to nc
                        }
                    }
                }
                if (neighbors.isEmpty()) break
                val next = neighbors[rng.nextInt(neighbors.size)]
                cageCells += next
                used[next.first][next.second] = true
            }

            val targetSum = cageCells.sumOf { (r, c) -> solution[r][c] }
            cages += VariantMetadata.CageDef(cageCells, targetSum)
        }

        return cages
    }

    private fun removeClues(
        board: Array<IntArray>, count: Int,
        cages: List<VariantMetadata.CageDef>, rng: Random
    ) {
        val positions = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until SIZE) for (c in 0 until SIZE) positions += r to c
        positions.shuffle(rng)
        var removed = 0
        for ((r, c) in positions) {
            if (removed >= count) break
            val backup = board[r][c]
            if (backup == 0) continue
            board[r][c] = 0
            if (SudokuEngine.hasUniqueSolution(board)) removed++ else board[r][c] = backup
        }
    }
}
