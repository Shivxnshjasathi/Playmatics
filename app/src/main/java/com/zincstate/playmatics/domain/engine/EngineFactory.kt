package com.zincstate.playmatics.domain.engine

import com.zincstate.playmatics.domain.engine.variants.*

/**
 * Factory that maps a [GameType] to its corresponding [PuzzleEngine].
 */
object EngineFactory {

    fun getEngine(gameType: GameType): PuzzleEngine = when (gameType) {
        GameType.SUDOKU -> SudokuEngine
        GameType.KILLER_SUDOKU -> KillerSudokuEngine
        GameType.DIAGONAL_SUDOKU -> DiagonalSudokuEngine
        GameType.JIGSAW_SUDOKU -> JigsawSudokuEngine
        GameType.WINDOKU -> WindokuEngine
        GameType.CONSECUTIVE_SUDOKU -> ConsecutiveSudokuEngine
        GameType.ODD_EVEN_SUDOKU -> OddEvenSudokuEngine
        GameType.WORDOKU -> WordokuEngine
        GameType.SAMURAI_SUDOKU -> SamuraiSudokuEngine
    }
}
