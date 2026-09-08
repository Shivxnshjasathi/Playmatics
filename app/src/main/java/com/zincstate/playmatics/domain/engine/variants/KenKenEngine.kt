package com.zincstate.playmatics.domain.engine.variants

import com.zincstate.playmatics.domain.engine.Difficulty
import com.zincstate.playmatics.domain.engine.GameType
import com.zincstate.playmatics.domain.engine.PuzzleEngine
import com.zincstate.playmatics.domain.engine.SudokuPuzzle
import com.zincstate.playmatics.domain.engine.VariantMetadata
import kotlin.random.Random

/**
 * Engine for KenKen.
 * Generates a 6x6 grid.
 */
object KenKenEngine : PuzzleEngine {
    override val gameType: GameType = GameType.KENKEN
    private const val SIZE = 6

    override fun generate(seed: Long, difficulty: Difficulty): SudokuPuzzle {
        val random = Random(seed)
        val solution = generateLatinSquare(random)
        val cages = generateCages(solution, random)
        
        return SudokuPuzzle(
            seed = seed,
            difficulty = difficulty,
            givenCells = Array(SIZE) { IntArray(SIZE) },
            solution = solution,
            gameType = gameType,
            variantMetadata = VariantMetadata.KenKenCages(cages)
        )
    }

    private fun generateLatinSquare(random: Random): Array<IntArray> {
        val board = Array(SIZE) { IntArray(SIZE) }
        solveLatinSquare(board, 0, 0, random)
        return board
    }

    private fun solveLatinSquare(board: Array<IntArray>, row: Int, col: Int, random: Random): Boolean {
        if (row == SIZE) return true
        val nextRow = if (col == SIZE - 1) row + 1 else row
        val nextCol = if (col == SIZE - 1) 0 else col + 1

        val numbers = (1..SIZE).toList().shuffled(random)
        for (num in numbers) {
            if (isValidPlacementLatin(board, row, col, num)) {
                board[row][col] = num
                if (solveLatinSquare(board, nextRow, nextCol, random)) return true
                board[row][col] = 0
            }
        }
        return false
    }

    private fun isValidPlacementLatin(board: Array<IntArray>, row: Int, col: Int, value: Int): Boolean {
        for (i in 0 until SIZE) {
            if (board[row][i] == value) return false
            if (board[i][col] == value) return false
        }
        return true
    }

    private fun generateCages(solution: Array<IntArray>, random: Random): List<VariantMetadata.KenKenCageDef> {
        val unassigned = mutableSetOf<Pair<Int, Int>>()
        for (r in 0 until SIZE) {
            for (c in 0 until SIZE) {
                unassigned.add(r to c)
            }
        }

        val cages = mutableListOf<VariantMetadata.KenKenCageDef>()

        while (unassigned.isNotEmpty()) {
            val start = unassigned.random(random)
            val cageCells = mutableListOf(start)
            unassigned.remove(start)

            // Randomly expand cage up to 4 cells
            val targetSize = random.nextInt(1, 5)
            while (cageCells.size < targetSize) {
                val neighbors = cageCells.flatMap { cell ->
                    listOf(
                        cell.first - 1 to cell.second,
                        cell.first + 1 to cell.second,
                        cell.first to cell.second - 1,
                        cell.first to cell.second + 1
                    )
                }.filter { it in unassigned }

                if (neighbors.isEmpty()) break
                val nextCell = neighbors.random(random)
                cageCells.add(nextCell)
                unassigned.remove(nextCell)
            }

            cages.add(createCageDef(cageCells, solution, random))
        }

        return cages
    }

    private fun createCageDef(cells: List<Pair<Int, Int>>, solution: Array<IntArray>, random: Random): VariantMetadata.KenKenCageDef {
        if (cells.size == 1) {
            val v = solution[cells[0].first][cells[0].second]
            return VariantMetadata.KenKenCageDef(cells, v, "")
        }

        val values = cells.map { solution[it.first][it.second] }
        val ops = mutableListOf("+", "*")
        
        // Can only do subtraction or division if size is 2
        if (cells.size == 2) {
            val v1 = values[0]
            val v2 = values[1]
            if (v1 > v2 && v1 % v2 == 0 || v2 > v1 && v2 % v1 == 0) {
                ops.add("/")
            }
            ops.add("-")
        }

        val op = ops.random(random)
        val target = when (op) {
            "+" -> values.sum()
            "*" -> values.fold(1) { acc, i -> acc * i }
            "-" -> maxOf(values[0], values[1]) - minOf(values[0], values[1])
            "/" -> maxOf(values[0], values[1]) / minOf(values[0], values[1])
            else -> values.sum()
        }

        return VariantMetadata.KenKenCageDef(cells, target, op)
    }

    override fun isValidPlacement(
        board: Array<IntArray>, row: Int, col: Int, value: Int, metadata: VariantMetadata?
    ): Boolean {
        // Just check row/col, no box checking for KenKen
        for (i in board.indices) {
            if (i != col && board[row][i] == value) return false
            if (i != row && board[i][col] == value) return false
        }
        return true
    }

    override fun conflictingCells(
        board: Array<IntArray>, row: Int, col: Int, value: Int, metadata: VariantMetadata?
    ): Set<Pair<Int, Int>> {
        val conflicts = mutableSetOf<Pair<Int, Int>>()
        for (i in board.indices) {
            if (i != col && board[row][i] == value) conflicts.add(row to i)
            if (i != row && board[i][col] == value) conflicts.add(i to col)
        }
        return conflicts
    }

    override fun countCorrectCells(board: Array<IntArray>, solution: Array<IntArray>): Int {
        var count = 0
        for (r in board.indices) {
            for (c in board[r].indices) {
                if (board[r][c] == solution[r][c]) count++
            }
        }
        return count
    }
}
