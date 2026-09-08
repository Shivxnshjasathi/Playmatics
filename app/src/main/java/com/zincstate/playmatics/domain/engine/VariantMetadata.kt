package com.zincstate.playmatics.domain.engine

/**
 * Variant-specific metadata attached to a puzzle.
 *
 * Each variant that needs extra visual or constraint data beyond the
 * standard 9×9 grid carries it in a subclass here.
 */
sealed class VariantMetadata {

    // ── KenKen ───────────────────────────────────────────────────────
    /** A cage: a group of cells with a target value and an operator (+, -, *, /) or "" for single cells. */
    data class KenKenCageDef(
        val cells: List<Pair<Int, Int>>,
        val target: Int,
        val operator: String
    )
    
    data class KenKenCages(val cages: List<KenKenCageDef>) : VariantMetadata()

    // ── Odd/Even Sudoku ──────────────────────────────────────────────
    /**
     * [parityMap]\[r][c] = true ⇒ the cell at (r,c) must contain an odd digit.
     * false ⇒ the cell must contain an even digit.
     */
    data class OddEvenMap(val parityMap: Array<BooleanArray>) : VariantMetadata() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is OddEvenMap) return false
            return parityMap.contentDeepEquals(other.parityMap)
        }
        override fun hashCode(): Int = parityMap.contentDeepHashCode()
    }

    // ── Wordoku ──────────────────────────────────────────────────────
    /**
     * Maps digits 1–9 to unique letters.
     * The board is still stored as ints; the UI renders them as [letterMap] chars.
     */
    data class WordokuMapping(val letterMap: Map<Int, Char>) : VariantMetadata()

    // ── Windoku ──────────────────────────────────────────────────────
    /** Carries the four window region cell lists (for drawing overlays). */
    data class WindokuWindows(val windows: List<List<Pair<Int, Int>>>) : VariantMetadata()

    // ── Samurai Sudoku ───────────────────────────────────────────────
    /**
     * Five overlapping puzzles on a 21×21 master grid.
     * [activeMask]\[r][c] = true ⇒ the cell is part of a puzzle (not the masked gap).
     */
    data class SamuraiLayout(
        val activeMask: Array<BooleanArray>
    ) : VariantMetadata() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as SamuraiLayout
            return activeMask.contentDeepEquals(other.activeMask)
        }
        override fun hashCode(): Int {
            return activeMask.contentDeepHashCode()
        }
    }

    // ── Queens ───────────────────────────────────────────────────────
    /**
     * Maps each cell to a region ID (0 to N-1).
     */
    data class QueensRegions(val regionMap: Array<IntArray>) : VariantMetadata() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is QueensRegions) return false
            return regionMap.contentDeepEquals(other.regionMap)
        }
        override fun hashCode(): Int = regionMap.contentDeepHashCode()
    }

    // ── Crossword ────────────────────────────────────────────────────
    /**
     * Stores the clues for the crossword puzzle.
     */
    data class CrosswordData(
        val acrossClues: Map<Int, String>,
        val downClues: Map<Int, String>,
        val cluePositions: Map<Int, Pair<Int, Int>>
    ) : VariantMetadata()
    
    // ── Word Search ──────────────────────────────────────────────────
    /**
     * Stores the hidden words and their start/end coordinates.
     */
    data class WordSearchData(
        val words: List<String>,
        val wordPositions: Map<String, Pair<Pair<Int, Int>, Pair<Int, Int>>>
    ) : VariantMetadata()
}
