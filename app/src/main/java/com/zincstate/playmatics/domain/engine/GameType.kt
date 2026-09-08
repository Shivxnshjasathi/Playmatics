package com.zincstate.playmatics.domain.engine

/**
 * All supported puzzle game types.
 *
 * Each value maps 1-to-1 with a [PuzzleEngine] implementation and
 * a serialised key stored in the Supabase `matches.game_type` column.
 */
enum class GameType(
    val displayName: String,
    val description: String,
    /** Supabase / serialisation key (lowercase, underscored). */
    val key: String
) {
    SUDOKU(
        displayName = "Sudoku",
        description = "Fill the 9×9 grid so every row, column, and 3×3 box contains 1–9.",
        key = "sudoku"
    ),
    KILLER_SUDOKU(
        displayName = "Killer Sudoku",
        description = "Standard Sudoku rules plus dotted cages whose cells must sum to the given total with no repeats.",
        key = "killer_sudoku"
    ),
    DIAGONAL_SUDOKU(
        displayName = "Diagonal (X) Sudoku",
        description = "Standard Sudoku rules plus both main diagonals must also contain 1–9.",
        key = "diagonal_sudoku"
    ),
    JIGSAW_SUDOKU(
        displayName = "Jigsaw Sudoku",
        description = "Rows and columns as usual, but the nine regions are irregular shapes instead of 3×3 boxes.",
        key = "jigsaw_sudoku"
    ),
    WINDOKU(
        displayName = "Windoku",
        description = "Standard Sudoku rules plus four shaded 3×3 'windows' that must also contain 1–9.",
        key = "windoku"
    ),
    CONSECUTIVE_SUDOKU(
        displayName = "Consecutive Sudoku",
        description = "Standard Sudoku rules plus markers between adjacent cells indicate their values differ by exactly 1.",
        key = "consecutive_sudoku"
    ),
    ODD_EVEN_SUDOKU(
        displayName = "Odd/Even Sudoku",
        description = "Standard Sudoku rules plus each cell is marked as odd or even — your digit must match.",
        key = "odd_even_sudoku"
    ),
    WORDOKU(
        displayName = "Wordoku",
        description = "Same logic as Sudoku but uses 9 unique letters instead of digits 1–9.",
        key = "wordoku"
    ),
    SAMURAI_SUDOKU(
        displayName = "Samurai Sudoku",
        description = "Five overlapping 9×9 grids that share corner boxes — solve them all at once.",
        key = "samurai_sudoku"
    );

    companion object {
        /** Look up by Supabase key, falling back to [SUDOKU]. */
        fun fromKey(key: String): GameType =
            entries.firstOrNull { it.key == key } ?: SUDOKU
    }
}
