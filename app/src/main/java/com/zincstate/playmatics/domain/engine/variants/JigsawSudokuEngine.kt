package com.zincstate.playmatics.domain.engine.variants

import com.zincstate.playmatics.domain.engine.*
import kotlin.random.Random

/**
 * Jigsaw Sudoku engine.
 *
 * Same row/column rules as standard Sudoku, but the nine 3×3 boxes are
 * replaced by nine **irregular** regions of 9 cells each.
 * Each region must still contain all digits 1–9.
 *
 * Regions are generated deterministically using a flood-fill approach
 * seeded from the puzzle seed.
 */
object JigsawSudokuEngine : PuzzleEngine {

    override val gameType: GameType = GameType.JIGSAW_SUDOKU

    private const val SIZE = 9

    override fun generate(seed: Long, difficulty: Difficulty): SudokuPuzzle {
        val rng = Random(seed)

        // Generate irregular regions
        val regionMap = generateRegionMap(rng)

        // Build solution respecting jigsaw regions
        val solution = Array(SIZE) { IntArray(SIZE) }
        fillBoard(solution, regionMap, rng)

        val puzzle = solution.map { it.copyOf() }.toTypedArray()
        removeClues(puzzle, difficulty.cellsToRemove, regionMap, rng)

        return SudokuPuzzle(
            seed = seed,
            difficulty = difficulty,
            givenCells = puzzle,
            solution = solution,
            gameType = GameType.JIGSAW_SUDOKU,
            variantMetadata = VariantMetadata.JigsawRegions(regionMap)
        )
    }

    override fun isValidPlacement(
        board: Array<IntArray>, row: Int, col: Int, value: Int, metadata: VariantMetadata?
    ): Boolean {
        if (value == 0) return true
        val regionMap = (metadata as? VariantMetadata.JigsawRegions)?.regionMap
            ?: return SudokuEngine.isValidPlacement(board, row, col, value)

        // Row check
        for (c in 0 until SIZE) {
            if (c != col && board[row][c] == value) return false
        }
        // Column check
        for (r in 0 until SIZE) {
            if (r != row && board[r][col] == value) return false
        }
        // Region check (instead of box)
        val region = regionMap[row][col]
        for (r in 0 until SIZE) {
            for (c in 0 until SIZE) {
                if ((r != row || c != col) && regionMap[r][c] == region && board[r][c] == value) {
                    return false
                }
            }
        }
        return true
    }

    override fun conflictingCells(
        board: Array<IntArray>, row: Int, col: Int, value: Int, metadata: VariantMetadata?
    ): Set<Pair<Int, Int>> {
        if (value == 0) return emptySet()
        val regionMap = (metadata as? VariantMetadata.JigsawRegions)?.regionMap
            ?: return SudokuEngine.conflictingCells(board, row, col, value)

        val conflicts = mutableSetOf<Pair<Int, Int>>()
        // Row
        for (c in 0 until SIZE) {
            if (c != col && board[row][c] == value) conflicts += row to c
        }
        // Column
        for (r in 0 until SIZE) {
            if (r != row && board[r][col] == value) conflicts += r to col
        }
        // Region
        val region = regionMap[row][col]
        for (r in 0 until SIZE) {
            for (c in 0 until SIZE) {
                if ((r != row || c != col) && regionMap[r][c] == region && board[r][c] == value) {
                    conflicts += r to c
                }
            }
        }
        return conflicts
    }

    override fun countCorrectCells(board: Array<IntArray>, solution: Array<IntArray>): Int =
        SudokuEngine.countCorrectCells(board, solution)

    // ── Region generation ────────────────────────────────────────────

    /**
     * Generates a valid region map where each region has exactly 9 cells,
     * all regions are contiguous, and every cell belongs to exactly one region.
     */
    private fun generateRegionMap(rng: Random): Array<IntArray> {
        // Start with standard 3×3 boxes and perturb them
        val regionMap = Array(SIZE) { r -> IntArray(SIZE) { c -> (r / 3) * 3 + c / 3 } }

        // Perform random swaps between adjacent cells of different regions
        // to create irregular shapes while maintaining contiguity
        val swapAttempts = 200
        repeat(swapAttempts) {
            val r1 = rng.nextInt(SIZE)
            val c1 = rng.nextInt(SIZE)
            val dir = rng.nextInt(4)
            val (dr, dc) = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)[dir]
            val r2 = r1 + dr
            val c2 = c1 + dc

            if (r2 in 0 until SIZE && c2 in 0 until SIZE &&
                regionMap[r1][c1] != regionMap[r2][c2]
            ) {
                val region1 = regionMap[r1][c1]
                val region2 = regionMap[r2][c2]

                // Temporarily swap
                regionMap[r1][c1] = region2
                regionMap[r2][c2] = region1

                // Verify both regions remain contiguous
                if (isRegionContiguous(regionMap, region1) &&
                    isRegionContiguous(regionMap, region2)
                ) {
                    // Keep the swap
                } else {
                    // Revert
                    regionMap[r1][c1] = region1
                    regionMap[r2][c2] = region2
                }
            }
        }

        return regionMap
    }

    private fun isRegionContiguous(regionMap: Array<IntArray>, region: Int): Boolean {
        val cells = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until SIZE) for (c in 0 until SIZE) {
            if (regionMap[r][c] == region) cells += r to c
        }
        if (cells.isEmpty()) return true

        val visited = mutableSetOf(cells[0])
        val queue = ArrayDeque<Pair<Int, Int>>()
        queue += cells[0]
        while (queue.isNotEmpty()) {
            val (r, c) = queue.removeFirst()
            for ((dr, dc) in listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)) {
                val nr = r + dr
                val nc = c + dc
                val neighbor = nr to nc
                if (neighbor in cells && neighbor !in visited) {
                    visited += neighbor
                    queue += neighbor
                }
            }
        }
        return visited.size == cells.size
    }

    // ── Generation ───────────────────────────────────────────────────

    private fun fillBoard(board: Array<IntArray>, regionMap: Array<IntArray>, rng: Random): Boolean {
        val empty = findEmpty(board) ?: return true
        val (r, c) = empty
        for (num in (1..SIZE).shuffled(rng)) {
            if (isValidPlacement(board, r, c, num, VariantMetadata.JigsawRegions(regionMap))) {
                board[r][c] = num
                if (fillBoard(board, regionMap, rng)) return true
                board[r][c] = 0
            }
        }
        return false
    }

    private fun removeClues(
        board: Array<IntArray>, count: Int,
        regionMap: Array<IntArray>, rng: Random
    ) {
        val meta = VariantMetadata.JigsawRegions(regionMap)
        val positions = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until SIZE) for (c in 0 until SIZE) positions += r to c
        positions.shuffle(rng)
        var removed = 0
        for ((r, c) in positions) {
            if (removed >= count) break
            val backup = board[r][c]
            if (backup == 0) continue
            board[r][c] = 0
            if (hasUniqueSolution(board, meta)) removed++ else board[r][c] = backup
        }
    }

    private fun hasUniqueSolution(board: Array<IntArray>, meta: VariantMetadata.JigsawRegions): Boolean {
        val copy = board.map { it.copyOf() }.toTypedArray()
        return countSolutions(copy, 2, meta) == 1
    }

    private fun countSolutions(board: Array<IntArray>, limit: Int, meta: VariantMetadata.JigsawRegions): Int {
        val empty = findEmpty(board) ?: return 1
        val (r, c) = empty
        var count = 0
        for (num in 1..SIZE) {
            if (isValidPlacement(board, r, c, num, meta)) {
                board[r][c] = num
                count += countSolutions(board, limit - count, meta)
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
