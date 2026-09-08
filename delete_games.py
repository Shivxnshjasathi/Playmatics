import re

with open("app/src/main/java/com/zincstate/playmatics/domain/engine/GameType.kt", "r") as f:
    content = f.read()

# We need to remove the enum values WINDOKU, ODD_EVEN_SUDOKU, WORDOKU, SAMURAI_SUDOKU, KENKEN
# Since they are formatted with indentation and line breaks, let's just replace the block.
# I'll just write a script to keep SUDOKU, MAZE, QUEENS, SCRABBLE, DRAWING, CROSSWORD.

new_content = """package com.zincstate.playmatics.domain.engine

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
    MAZE(
        displayName = "Maze",
        description = "Find the path from start to finish through the labyrinth.",
        key = "maze"
    ),
    QUEENS(
        displayName = "Queens",
        description = "Place one Queen in each colored region, row, and column. Queens cannot touch each other, not even diagonally.",
        key = "queens"
    ),
    SCRABBLE(
        displayName = "Scrabble",
        description = "Form words on a 15x15 board to score points based on letter values and premium squares.",
        key = "scrabble"
    ),
    DRAWING(
        displayName = "Drawing",
        description = "Draw the target word while the other player guesses it.",
        key = "drawing"
    ),
    CROSSWORD(
        displayName = "Crossword",
        description = "Fill the grid with words that match the given clues.",
        key = "crossword"
    );

    companion object {
        /** Look up by Supabase key, falling back to [SUDOKU]. */
        fun fromKey(key: String): GameType =
            entries.firstOrNull { it.key == key } ?: SUDOKU
    }
}
"""

with open("app/src/main/java/com/zincstate/playmatics/domain/engine/GameType.kt", "w") as f:
    f.write(new_content)

# Fix EngineFactory.kt
with open("app/src/main/java/com/zincstate/playmatics/domain/engine/EngineFactory.kt", "r") as f:
    content = f.read()

new_engine_content = """package com.zincstate.playmatics.domain.engine

import com.zincstate.playmatics.domain.engine.variants.DrawingEngine
import com.zincstate.playmatics.domain.engine.variants.MazeEngine
import com.zincstate.playmatics.domain.engine.variants.QueensEngine
import com.zincstate.playmatics.domain.engine.variants.ScrabbleEngine
import com.zincstate.playmatics.domain.engine.variants.StandardSudokuEngine
import com.zincstate.playmatics.domain.engine.variants.CrosswordEngine

object EngineFactory {
    fun getEngine(gameType: GameType): PuzzleEngine {
        return when (gameType) {
            GameType.SUDOKU -> StandardSudokuEngine
            GameType.MAZE -> MazeEngine
            GameType.QUEENS -> QueensEngine
            GameType.SCRABBLE -> ScrabbleEngine
            GameType.DRAWING -> DrawingEngine
            GameType.CROSSWORD -> CrosswordEngine
            else -> StandardSudokuEngine
        }
    }
}
"""
with open("app/src/main/java/com/zincstate/playmatics/domain/engine/EngineFactory.kt", "w") as f:
    f.write(new_engine_content)
