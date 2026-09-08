package com.zincstate.playmatics.domain.engine

import com.zincstate.playmatics.domain.engine.variants.CrosswordEngine
import com.zincstate.playmatics.domain.engine.variants.KenKenEngine
import com.zincstate.playmatics.domain.engine.variants.WordSearchEngine
import com.zincstate.playmatics.domain.engine.variants.WordokuEngine

object EngineFactory {
    fun getEngine(gameType: GameType): PuzzleEngine {
        return when (gameType) {
            GameType.SUDOKU -> SudokuEngine
            GameType.WORDOKU -> WordokuEngine
            GameType.KENKEN -> KenKenEngine
            GameType.CROSSWORD -> CrosswordEngine
            GameType.WORD_SEARCH -> WordSearchEngine
        }
    }
}
