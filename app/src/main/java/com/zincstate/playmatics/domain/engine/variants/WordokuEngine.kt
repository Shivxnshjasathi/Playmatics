package com.zincstate.playmatics.domain.engine.variants

import com.zincstate.playmatics.domain.engine.*
import kotlin.random.Random

/**
 * Wordoku engine.
 *
 * Identical logic to classic Sudoku but the board is displayed using
 * 9 unique letters instead of digits 1–9.
 * Internally the board is still stored as ints; the UI maps them
 * through [VariantMetadata.WordokuMapping].
 */
object WordokuEngine : PuzzleEngine {

    override val gameType: GameType = GameType.WORDOKU

    private const val SIZE = 9

    /** Pool of consonants+vowels that produce readable "wordoku" grids. */
    private val LETTER_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toList()

    override fun generate(seed: Long, difficulty: Difficulty): SudokuPuzzle {
        val rng = Random(seed)

        // Generate a standard Sudoku
        val basePuzzle = SudokuEngine.generate(seed, difficulty)

        // Pick 9 distinct letters deterministically
        val shuffledLetters = LETTER_POOL.shuffled(rng)
        val chosen = shuffledLetters.take(SIZE)
        val letterMap = (1..SIZE).zip(chosen).toMap()

        return SudokuPuzzle(
            seed = seed,
            difficulty = difficulty,
            givenCells = basePuzzle.givenCells,
            solution = basePuzzle.solution,
            gameType = GameType.WORDOKU,
            variantMetadata = VariantMetadata.WordokuMapping(letterMap)
        )
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
