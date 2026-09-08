package com.zincstate.playmatics.domain.engine.variants

import com.zincstate.playmatics.domain.engine.Difficulty
import com.zincstate.playmatics.domain.engine.VariantMetadata
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KenKenEngineTest {

    @Test
    fun `generate produces valid latin square solution`() {
        val puzzle = KenKenEngine.generate(42L, Difficulty.NORMAL)
        val solution = puzzle.solution

        assertEquals(6, solution.size)

        // Check each row has 1-6 exactly once
        for (row in 0 until 6) {
            val digits = solution[row].toSet()
            assertEquals("Row $row does not have exactly 6 unique digits: ${solution[row].toList()}", 6, digits.size)
            assertTrue("Row $row has invalid digit", digits.all { it in 1..6 })
        }

        // Check each col has 1-6 exactly once
        for (col in 0 until 6) {
            val digits = (0 until 6).map { r -> solution[r][col] }.toSet()
            assertEquals("Col $col does not have exactly 6 unique digits", 6, digits.size)
            assertTrue("Col $col has invalid digit", digits.all { it in 1..6 })
        }
    }

    @Test
    fun `generate produces valid cages covering entire board`() {
        val puzzle = KenKenEngine.generate(123L, Difficulty.NORMAL)
        val cages = (puzzle.variantMetadata as VariantMetadata.KenKenCages).cages

        // Every cell must be in exactly one cage
        val cellCount = cages.sumOf { it.cells.size }
        assertEquals("Cages must cover all 36 cells", 36, cellCount)

        val uniqueCells = cages.flatMap { it.cells }.toSet()
        assertEquals("Cages must cover exactly 36 unique cells", 36, uniqueCells.size)
    }

    @Test
    fun `isValidPlacement validates only row and column`() {
        val board = Array(6) { IntArray(6) }
        board[0][0] = 5

        // Same row -> invalid
        assertFalse(KenKenEngine.isValidPlacement(board, 0, 1, 5))
        // Same col -> invalid
        assertFalse(KenKenEngine.isValidPlacement(board, 1, 0, 5))
        // Valid
        assertTrue(KenKenEngine.isValidPlacement(board, 1, 1, 5))
    }
}
