package com.zincstate.playmatics.domain.engine.variants

import com.zincstate.playmatics.domain.engine.*
import kotlin.random.Random

/**
 * Windoku engine.
 *
 * Standard Sudoku rules **plus** four extra 3×3 "window" regions
 * (positioned symmetrically inside the grid) that must each contain 1–9.
 *
 * Window positions (0-indexed):
 *   Window 0: rows 1-3, cols 1-3
 *   Window 1: rows 1-3, cols 5-7
 *   Window 2: rows 5-7, cols 1-3
 *   Window 3: rows 5-7, cols 5-7
 */
object WindokuEngine : PuzzleEngine {

    override val gameType: GameType = GameType.WINDOKU

    private const val SIZE = 9
    private const val BOX = 3

    /** The four window regions. */
    val WINDOWS: List<List<Pair<Int, Int>>> = listOf(
        buildWindow(1, 1),
        buildWindow(1, 5),
        buildWindow(5, 1),
        buildWindow(5, 5)
    )

    /** windowOf[r][c] = window index (0-3) or -1 if not in any window. */
    private val windowOf: Array<IntArray> = Array(SIZE) { IntArray(SIZE) { -1 } }.also { map ->
        WINDOWS.forEachIndexed { idx, cells ->
            for ((r, c) in cells) map[r][c] = idx
        }
    }

    private fun buildWindow(startRow: Int, startCol: Int): List<Pair<Int, Int>> {
        val cells = mutableListOf<Pair<Int, Int>>()
        for (r in startRow until startRow + 3) {
            for (c in startCol until startCol + 3) {
                cells += r to c
            }
        }
        return cells
    }

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
            gameType = GameType.WINDOKU,
            variantMetadata = VariantMetadata.WindokuWindows(WINDOWS)
        )
    }

    override fun isValidPlacement(
        board: Array<IntArray>, row: Int, col: Int, value: Int, metadata: VariantMetadata?
    ): Boolean {
        if (value == 0) return true
        if (!SudokuEngine.isValidPlacement(board, row, col, value)) return false
        val wIdx = windowOf[row][col]
        if (wIdx >= 0) {
            for ((r, c) in WINDOWS[wIdx]) {
                if ((r != row || c != col) && board[r][c] == value) return false
            }
        }
        return true
    }

    override fun conflictingCells(
        board: Array<IntArray>, row: Int, col: Int, value: Int, metadata: VariantMetadata?
    ): Set<Pair<Int, Int>> {
        if (value == 0) return emptySet()
        val conflicts = SudokuEngine.conflictingCells(board, row, col, value).toMutableSet()
        val wIdx = windowOf[row][col]
        if (wIdx >= 0) {
            for ((r, c) in WINDOWS[wIdx]) {
                if ((r != row || c != col) && board[r][c] == value) conflicts += r to c
            }
        }
        return conflicts
    }

    override fun countCorrectCells(board: Array<IntArray>, solution: Array<IntArray>): Int =
        SudokuEngine.countCorrectCells(board, solution)

    // ── Generation ───────────────────────────────────────────────────

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
