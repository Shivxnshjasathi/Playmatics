package com.zincstate.playmatics.domain.engine

/**
 * Sudoku difficulty levels.
 * @param cellsToRemove how many of the 81 cells are blanked out in the puzzle.
 */
enum class Difficulty(val cellsToRemove: Int) {
    EASY(32),
    NORMAL(45),
    HARD(54),
    EXPERT(60)
}
