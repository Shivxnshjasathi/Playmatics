package com.zincstate.playmatics.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [SudokuEngine].
 * - Determinism: same seed → identical board
 * - Uniqueness: generated puzzles have exactly one solution
 * - Validator: correctly flags known conflicts
 * - Progress: countCorrectCells returns expected values
 */
class SudokuEngineTest {

    // ------------------------------------------------------------------ //
    //  Determinism                                                        //
    // ------------------------------------------------------------------ //

    @Test
    fun `same seed and difficulty produce identical puzzles`() {
        val seed = 42L

        val puzzle1 = SudokuEngine.generate(seed, Difficulty.NORMAL)
        val puzzle2 = SudokuEngine.generate(seed, Difficulty.NORMAL)

        assertTrue(
            "givenCells should be identical for same seed",
            puzzle1.givenCells.contentDeepEquals(puzzle2.givenCells)
        )
        assertTrue(
            "solution should be identical for same seed",
            puzzle1.solution.contentDeepEquals(puzzle2.solution)
        )
    }

    @Test
    fun `different seeds produce different puzzles`() {
        val puzzle1 = SudokuEngine.generate(1L, Difficulty.NORMAL)
        val puzzle2 = SudokuEngine.generate(2L, Difficulty.NORMAL)

        assertFalse(
            "different seeds should produce different solutions",
            puzzle1.solution.contentDeepEquals(puzzle2.solution)
        )
    }

    @Test
    fun `same seed different difficulty produce different given cells`() {
        val seed = 123L
        val easy = SudokuEngine.generate(seed, Difficulty.EASY)
        val hard = SudokuEngine.generate(seed, Difficulty.HARD)

        // Same solution (same seed generates the same full grid)
        assertTrue(easy.solution.contentDeepEquals(hard.solution))

        // Different number of given cells
        val easyGivens = easy.givenCells.sumOf { row -> row.count { it != 0 } }
        val hardGivens = hard.givenCells.sumOf { row -> row.count { it != 0 } }

        assertEquals(81 - Difficulty.EASY.cellsToRemove, easyGivens)
        assertTrue("Hard should have fewer givens than Easy", hardGivens < easyGivens)
    }

    // ------------------------------------------------------------------ //
    //  Uniqueness                                                         //
    // ------------------------------------------------------------------ //

    @Test
    fun `easy puzzle has unique solution`() {
        val puzzle = SudokuEngine.generate(100L, Difficulty.EASY)
        assertTrue(
            "EASY puzzle must have a unique solution",
            SudokuEngine.hasUniqueSolution(puzzle.givenCells)
        )
    }

    @Test
    fun `normal puzzle has unique solution`() {
        val puzzle = SudokuEngine.generate(200L, Difficulty.NORMAL)
        assertTrue(
            "NORMAL puzzle must have a unique solution",
            SudokuEngine.hasUniqueSolution(puzzle.givenCells)
        )
    }

    @Test
    fun `hard puzzle has unique solution`() {
        val puzzle = SudokuEngine.generate(300L, Difficulty.HARD)
        assertTrue(
            "HARD puzzle must have a unique solution",
            SudokuEngine.hasUniqueSolution(puzzle.givenCells)
        )
    }

    @Test
    fun `multiple seeds all produce unique solutions`() {
        for (seed in listOf(0L, 1L, 999L, Long.MAX_VALUE, -42L)) {
            for (difficulty in Difficulty.entries) {
                val puzzle = SudokuEngine.generate(seed, difficulty)
                assertTrue(
                    "seed=$seed, difficulty=$difficulty must have unique solution",
                    SudokuEngine.hasUniqueSolution(puzzle.givenCells)
                )
            }
        }
    }

    // ------------------------------------------------------------------ //
    //  Validator                                                          //
    // ------------------------------------------------------------------ //

    @Test
    fun `valid placement returns true`() {
        val board = Array(9) { IntArray(9) }
        board[0][0] = 1
        assertTrue(SudokuEngine.isValidPlacement(board, 0, 1, 2))
    }

    @Test
    fun `row conflict detected`() {
        val board = Array(9) { IntArray(9) }
        board[0][0] = 5
        assertFalse(
            "placing 5 in same row should be invalid",
            SudokuEngine.isValidPlacement(board, 0, 4, 5)
        )
    }

    @Test
    fun `column conflict detected`() {
        val board = Array(9) { IntArray(9) }
        board[3][2] = 7
        assertFalse(
            "placing 7 in same column should be invalid",
            SudokuEngine.isValidPlacement(board, 6, 2, 7)
        )
    }

    @Test
    fun `box conflict detected`() {
        val board = Array(9) { IntArray(9) }
        board[1][1] = 3
        assertFalse(
            "placing 3 in same 3x3 box should be invalid",
            SudokuEngine.isValidPlacement(board, 2, 2, 3)
        )
    }

    @Test
    fun `conflictingCells returns correct cells`() {
        val board = Array(9) { IntArray(9) }
        board[0][0] = 5
        board[4][4] = 5

        val conflicts = SudokuEngine.conflictingCells(board, 0, 4, 5)

        assertTrue("row conflict at (0,0)", (0 to 0) in conflicts)
        assertFalse("(4,4) is not in same row/col/box", (4 to 4) in conflicts)
    }

    @Test
    fun `conflictingCells returns empty for no conflicts`() {
        val board = Array(9) { IntArray(9) }
        board[0][0] = 1

        val conflicts = SudokuEngine.conflictingCells(board, 5, 5, 2)
        assertTrue("no conflicts expected", conflicts.isEmpty())
    }

    // ------------------------------------------------------------------ //
    //  Progress                                                           //
    // ------------------------------------------------------------------ //

    @Test
    fun `countCorrectCells on solved board returns 81`() {
        val puzzle = SudokuEngine.generate(42L, Difficulty.EASY)
        val correctCount = SudokuEngine.countCorrectCells(puzzle.solution, puzzle.solution)
        assertEquals(81, correctCount)
    }

    @Test
    fun `countCorrectCells on empty board returns 0`() {
        val puzzle = SudokuEngine.generate(42L, Difficulty.EASY)
        val empty = Array(9) { IntArray(9) }
        val correctCount = SudokuEngine.countCorrectCells(empty, puzzle.solution)
        assertEquals(0, correctCount)
    }

    @Test
    fun `countCorrectCells counts only matching cells`() {
        val puzzle = SudokuEngine.generate(42L, Difficulty.EASY)
        val board = puzzle.givenCells.map { it.copyOf() }.toTypedArray()

        // Given cells should already be correct
        val givenCount = board.sumOf { row -> row.count { it != 0 } }
        val correctCount = SudokuEngine.countCorrectCells(board, puzzle.solution)
        assertEquals(givenCount, correctCount)
    }

    @Test
    fun `wrong digit is not counted as correct`() {
        val puzzle = SudokuEngine.generate(42L, Difficulty.EASY)
        val board = puzzle.givenCells.map { it.copyOf() }.toTypedArray()

        // Find an empty cell and fill it with a wrong value
        outer@ for (r in 0 until 9) {
            for (c in 0 until 9) {
                if (board[r][c] == 0) {
                    val correct = puzzle.solution[r][c]
                    val wrong = if (correct == 9) 1 else correct + 1
                    board[r][c] = wrong
                    break@outer
                }
            }
        }

        val givenCount = puzzle.givenCells.sumOf { row -> row.count { it != 0 } }
        val correctCount = SudokuEngine.countCorrectCells(board, puzzle.solution)
        assertEquals(
            "wrong digit should not increase correct count",
            givenCount,
            correctCount
        )
    }

    // ------------------------------------------------------------------ //
    //  Cell removal count                                                 //
    // ------------------------------------------------------------------ //

    @Test
    fun `correct number of cells removed per difficulty`() {
        for (difficulty in Difficulty.entries) {
            val puzzle = SudokuEngine.generate(42L, difficulty)
            val emptyCells = puzzle.givenCells.sumOf { row -> row.count { it == 0 } }
            assertEquals(
                "difficulty=$difficulty should remove ${difficulty.cellsToRemove} cells",
                difficulty.cellsToRemove,
                emptyCells
            )
        }
    }
}
