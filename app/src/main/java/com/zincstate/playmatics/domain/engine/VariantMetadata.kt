package com.zincstate.playmatics.domain.engine

/**
 * Variant-specific metadata attached to a puzzle.
 *
 * Each variant that needs extra visual or constraint data beyond the
 * standard 9×9 grid carries it in a subclass here.
 */
sealed class VariantMetadata {

    // ── Killer Sudoku ────────────────────────────────────────────────
    /** A cage: a group of cells with a target sum and no repeated digits. */
    data class CageDef(
        val cells: List<Pair<Int, Int>>,
        val targetSum: Int
    )

    data class KillerCages(val cages: List<CageDef>) : VariantMetadata()

    // ── KenKen ───────────────────────────────────────────────────────
    /** A cage: a group of cells with a target value and an operator (+, -, *, /) or "" for single cells. */
    data class KenKenCageDef(
        val cells: List<Pair<Int, Int>>,
        val target: Int,
        val operator: String
    )
    
    data class KenKenCages(val cages: List<KenKenCageDef>) : VariantMetadata()

    // ── Futoshiki ────────────────────────────────────────────────────
    /** 
     * Inequalities between cells. 
     * horizontal[r][c] is relation between (r,c) and (r,c+1): 1 means (r,c) > (r,c+1), -1 means <, 0 means none.
     * vertical[r][c] is relation between (r,c) and (r+1,c): 1 means (r,c) > (r+1,c), -1 means <, 0 means none.
     */
    data class FutoshikiInequalities(
        val horizontal: Array<IntArray>,
        val vertical: Array<IntArray>
    ) : VariantMetadata()

    // ── Skyscrapers ──────────────────────────────────────────────────
    /** Clues on the perimeter of the board indicating how many "skyscrapers" can be seen from that angle. */
    data class SkyscraperClues(
        val top: IntArray,
        val bottom: IntArray,
        val left: IntArray,
        val right: IntArray
    ) : VariantMetadata()

    // ── Jigsaw Sudoku ────────────────────────────────────────────────
    /** regionMap[r][c] = region ID (0–8) for that cell. */
    data class JigsawRegions(val regionMap: Array<IntArray>) : VariantMetadata() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is JigsawRegions) return false
            return regionMap.contentDeepEquals(other.regionMap)
        }
        override fun hashCode(): Int = regionMap.contentDeepHashCode()
    }

    // ── Consecutive Sudoku ───────────────────────────────────────────
    /**
     * Markers between horizontally or vertically adjacent cells.
     * [horizontalMarkers]\[r][c] = true ⇒ cells (r,c) and (r,c+1) are consecutive.
     * [verticalMarkers]\[r][c]   = true ⇒ cells (r,c) and (r+1,c) are consecutive.
     */
    data class ConsecutiveMarkers(
        val horizontalMarkers: Array<BooleanArray>,   // 9×8
        val verticalMarkers: Array<BooleanArray>      // 8×9
    ) : VariantMetadata() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ConsecutiveMarkers) return false
            return horizontalMarkers.contentDeepEquals(other.horizontalMarkers) &&
                    verticalMarkers.contentDeepEquals(other.verticalMarkers)
        }
        override fun hashCode(): Int =
            horizontalMarkers.contentDeepHashCode() * 31 + verticalMarkers.contentDeepHashCode()
    }

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

    // ── Diagonal (X) Sudoku ──────────────────────────────────────────
    /** Marker object — no extra data needed; diagonals are implied. */
    data object DiagonalMarker : VariantMetadata()

    // ── Samurai Sudoku ───────────────────────────────────────────────
    /**
     * Five overlapping puzzles on a 21×21 master grid.
     * [activeMask]\[r][c] = true ⇒ the cell is part of a puzzle (not the masked gap).
     * [gridIndex]\[r][c] = which sub-grid (0–4) the cell belongs to; -1 if masked.
     */
    data class SamuraiLayout(
        val activeMask: Array<BooleanArray>,     // 21×21
        val gridIndex: Array<IntArray>            // 21×21
    ) : VariantMetadata() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is SamuraiLayout) return false
            return activeMask.contentDeepEquals(other.activeMask) &&
                    gridIndex.contentDeepEquals(other.gridIndex)
        }
        override fun hashCode(): Int =
            activeMask.contentDeepHashCode() * 31 + gridIndex.contentDeepHashCode()
    }
}
