package com.zincstate.playmatics.domain.engine.variants

import com.zincstate.playmatics.domain.engine.Difficulty
import com.zincstate.playmatics.domain.engine.GameType
import com.zincstate.playmatics.domain.engine.PuzzleEngine
import com.zincstate.playmatics.domain.engine.SudokuEngine
import com.zincstate.playmatics.domain.engine.SudokuPuzzle
import com.zincstate.playmatics.domain.engine.VariantMetadata

object WordokuEngine : PuzzleEngine {
    override val gameType = GameType.WORDOKU

    // Generates a standard Sudoku puzzle but uses 1-9 to represent letters A-I
    override fun generate(seed: Long, difficulty: Difficulty): SudokuPuzzle {
        val sudokuPuzzle = SudokuEngine.generate(seed, difficulty)
        
        // We do NOT subtract 1, because 0 MUST mean empty cell in the generic UI.
        // We will just provide a WordokuMapping so the UI knows to render 1-9 as A-I.
        val letterMap = mapOf(
            1 to 'A', 2 to 'B', 3 to 'C',
            4 to 'D', 5 to 'E', 6 to 'F',
            7 to 'G', 8 to 'H', 9 to 'I'
        )
        
        return sudokuPuzzle.copy(
            gameType = GameType.WORDOKU,
            variantMetadata = VariantMetadata.WordokuMapping(letterMap)
        )
    }

    override fun isValidPlacement(
        board: Array<IntArray>,
        row: Int,
        col: Int,
        value: Int,
        metadata: VariantMetadata?
    ): Boolean {
        // Value is 1..9, same as Sudoku
        return SudokuEngine.isValidPlacement(board, row, col, value, metadata)
    }

    override fun conflictingCells(
        board: Array<IntArray>,
        row: Int,
        col: Int,
        value: Int,
        metadata: VariantMetadata?
    ): Set<Pair<Int, Int>> {
        return SudokuEngine.conflictingCells(board, row, col, value, metadata)
    }

    override fun countCorrectCells(board: Array<IntArray>, solution: Array<IntArray>): Int {
        return SudokuEngine.countCorrectCells(board, solution)
    }
}
