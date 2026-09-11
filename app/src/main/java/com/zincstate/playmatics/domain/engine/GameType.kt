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
    WORDOKU(
        displayName = "Wordoku",
        description = "Sudoku but with 9 letters instead of numbers.",
        key = "wordoku"
    ),
    KENKEN(
        displayName = "KenKen",
        description = "Fill the grid so each row and column has unique digits, satisfying the math clues.",
        key = "kenken"
    ),
    CROSSWORD(
        displayName = "Crossword",
        description = "Fill the grid with words that match the given clues.",
        key = "crossword"
    ),
    WORD_SEARCH(
        displayName = "Word Search",
        description = "Find the hidden words in the matrix of letters.",
        key = "word_search"
    );

    companion object {
        /** Look up by Supabase key, falling back to [SUDOKU]. */
        fun fromKey(key: String): GameType {
            val baseKey = key.substringBefore("|")
            return entries.firstOrNull { it.key == baseKey } ?: SUDOKU
        }
    }
}
