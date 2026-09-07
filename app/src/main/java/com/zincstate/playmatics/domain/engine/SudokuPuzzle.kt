package com.zincstate.playmatics.domain.engine

/**
 * Represents a generated Sudoku puzzle.
 *
 * @param seed       the seed used to generate this puzzle (deterministic)
 * @param difficulty the difficulty level
 * @param givenCells 9×9 grid: non-zero values are pre-filled (given) clues, 0 = empty
 * @param solution   9×9 grid: the unique complete solution
 */
data class SudokuPuzzle(
    val seed: Long,
    val difficulty: Difficulty,
    val givenCells: Array<IntArray>,
    val solution: Array<IntArray>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SudokuPuzzle) return false
        return seed == other.seed &&
                difficulty == other.difficulty &&
                givenCells.contentDeepEquals(other.givenCells) &&
                solution.contentDeepEquals(other.solution)
    }

    override fun hashCode(): Int {
        var result = seed.hashCode()
        result = 31 * result + difficulty.hashCode()
        result = 31 * result + givenCells.contentDeepHashCode()
        result = 31 * result + solution.contentDeepHashCode()
        return result
    }
}
