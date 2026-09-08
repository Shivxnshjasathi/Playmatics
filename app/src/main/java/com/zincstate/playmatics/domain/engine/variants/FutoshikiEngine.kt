package com.zincstate.playmatics.domain.engine.variants

import com.zincstate.playmatics.domain.engine.Difficulty
import com.zincstate.playmatics.domain.engine.GameType
import com.zincstate.playmatics.domain.engine.PuzzleEngine
import com.zincstate.playmatics.domain.engine.SudokuPuzzle
import com.zincstate.playmatics.domain.engine.VariantMetadata

import com.zincstate.playmatics.domain.engine.SudokuEngine

/**
 * Placeholder engine for Futoshiki.
 */
object FutoshikiEngine : PuzzleEngine {
    override val gameType: GameType = GameType.FUTOSHIKI

    override fun generate(seed: Long, difficulty: Difficulty): SudokuPuzzle {
        val puzzle = SudokuEngine.generate(seed, difficulty)
        return puzzle.copy(gameType = gameType)
    }

    override fun isValidPlacement(
        board: Array<IntArray>, row: Int, col: Int, value: Int, metadata: VariantMetadata?
    ): Boolean = SudokuEngine.isValidPlacement(board, row, col, value)

    override fun conflictingCells(
        board: Array<IntArray>, row: Int, col: Int, value: Int, metadata: VariantMetadata?
    ): Set<Pair<Int, Int>> = SudokuEngine.conflictingCells(board, row, col, value)

    override fun countCorrectCells(board: Array<IntArray>, solution: Array<IntArray>): Int =
        SudokuEngine.countCorrectCells(board, solution)
}
