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
    val solution: Array<IntArray>,
    val gameType: GameType = GameType.SUDOKU,
    val variantMetadata: VariantMetadata? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SudokuPuzzle) return false
        return seed == other.seed &&
                difficulty == other.difficulty &&
                gameType == other.gameType &&
                givenCells.contentDeepEquals(other.givenCells) &&
                solution.contentDeepEquals(other.solution) &&
                variantMetadata == other.variantMetadata
    }

    override fun hashCode(): Int {
        var result = seed.hashCode()
        result = 31 * result + difficulty.hashCode()
        result = 31 * result + gameType.hashCode()
        result = 31 * result + givenCells.contentDeepHashCode()
        result = 31 * result + solution.contentDeepHashCode()
        return result
    }
}
