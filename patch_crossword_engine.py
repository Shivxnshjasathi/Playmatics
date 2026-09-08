import re

content = """package com.zincstate.playmatics.domain.engine.variants

import com.zincstate.playmatics.domain.engine.Difficulty
import com.zincstate.playmatics.domain.engine.GameType
import com.zincstate.playmatics.domain.engine.PuzzleEngine
import com.zincstate.playmatics.domain.engine.SudokuPuzzle
import com.zincstate.playmatics.domain.engine.VariantMetadata
import kotlin.random.Random

object CrosswordEngine : PuzzleEngine {
    override val gameType = GameType.CROSSWORD

    // 3 hand-crafted 5x5 Mini Crosswords with black squares represented by '#'
    private val Puzzles = listOf(
        listOf(
            "CATS#",
            "ALOES",
            "PEACH",
            "ESSAY",
            "#STYX"
        ),
        listOf(
            "PAPER",
            "ALIVE",
            "NEVER",
            "ICED#",
            "CARS#"
        ),
        listOf(
            "SHARP",
            "POLES",
            "ALONE",
            "MEND#",
            "#RYE#"
        )
    )

    private val CluesMap = listOf(
        // Puzzle 0
        mapOf(
            "A1" to "Felines", "A5" to "Soothing plants", "A6" to "Fuzzy fruit", "A7" to "Written assignment", "A8" to "River of the underworld",
            "D1" to "Superhero garment", "D2" to "Pub servings", "D3" to "Bread variety", "D4" to "Decay", "D5" to "Reserved"
        ),
        // Puzzle 1
        mapOf(
            "A1" to "Sheet material", "A6" to "Not dead", "A7" to "Not ever", "A8" to "Chilled", "A9" to "Automobiles",
            "D1" to "Intense fear", "D2" to "___ in Wonderland", "D3" to "Dog or cat", "D4" to "Over evening", "D5" to "___ Rev"
        ),
        // Puzzle 2
        mapOf(
            "A1" to "Not dull", "A6" to "Skiing gear", "A7" to "Solo", "A8" to "Fix", "A9" to "Deli bread",
            "D1" to "Unsolicited email", "D2" to "Gap", "D3" to "Plant part", "D4" to "Tear", "D5" to "ESP, e.g."
        )
    )

    private val CluePositionsMap = listOf(
        // Puzzle 0
        mapOf(
            1 to (0 to 0), 5 to (1 to 0), 6 to (2 to 0), 7 to (3 to 0), 8 to (4 to 1)
        ),
        // Puzzle 1
        mapOf(
            1 to (0 to 0), 6 to (1 to 0), 7 to (2 to 0), 8 to (3 to 0), 9 to (4 to 0)
        ),
        // Puzzle 2
        mapOf(
            1 to (0 to 0), 6 to (1 to 0), 7 to (2 to 0), 8 to (3 to 0), 9 to (4 to 1)
        )
    )
    
    // Additional down clue mappings, since the numbering depends on grid rules
    // To make it simple, we manually define D clues per puzzle
    private val DownClueKeysMap = listOf(
        // Puzzle 0
        mapOf(1 to "D1", 2 to "D2", 3 to "D3", 4 to "D4", 5 to "D5"),
        // Puzzle 1
        mapOf(1 to "D1", 2 to "D2", 3 to "D3", 4 to "D4", 5 to "D5"),
        // Puzzle 2
        mapOf(1 to "D1", 2 to "D2", 3 to "D3", 4 to "D4", 5 to "D5")
    )

    override fun generate(seed: Long, difficulty: Difficulty): SudokuPuzzle {
        val random = Random(seed)
        val pIndex = random.nextInt(Puzzles.size)
        val grid = Puzzles[pIndex]
        val clueMap = CluesMap[pIndex]
        val posMap = CluePositionsMap[pIndex]
        val downKeys = DownClueKeysMap[pIndex]
        
        val size = 5
        val givenCells = Array(size) { IntArray(size) { 0 } }
        val solution = Array(size) { IntArray(size) { 0 } }
        
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (grid[r][c] == '#') {
                    solution[r][c] = -1
                    givenCells[r][c] = -1
                } else {
                    solution[r][c] = grid[r][c] - 'A' + 1 // A=1, Z=26
                    givenCells[r][c] = 0 // 0 means empty playable cell
                }
            }
        }
        
        val acrossClues = mutableMapOf<Int, String>()
        val downClues = mutableMapOf<Int, String>()
        val cluePositions = mutableMapOf<Int, Pair<Int, Int>>()
        
        // Populate positions
        for ((num, pos) in posMap) {
            cluePositions[num] = pos
            if (clueMap.containsKey("A$num")) {
                acrossClues[num] = clueMap["A$num"]!!
            }
        }
        
        // For down clues, they might share the same number as an across clue
        // Wait, standard numbering: 1-5 top row, etc.
        // Actually, just loop 1..5 for Down clues
        for (i in 1..5) {
            // Map down clues
            if (downKeys.containsKey(i)) {
                downClues[i] = clueMap[downKeys[i]!!]!!
                // If posMap doesn't have it (e.g. D2 is at 0,1 but no across word starts there)
                // We add it to cluePositions
                if (!cluePositions.containsKey(i)) {
                    cluePositions[i] = 0 to (i - 1)
                }
            }
        }
        
        // Give some random letters based on difficulty
        val hints = when (difficulty) {
            Difficulty.EASY -> 4
            Difficulty.NORMAL -> 2
            Difficulty.HARD -> 0
            Difficulty.EXPERT -> 0
        }
        
        // Find all playable cells
        val playable = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (solution[r][c] != -1) playable.add(r to c)
            }
        }
        
        playable.shuffle(random)
        for (i in 0 until hints.coerceAtMost(playable.size)) {
            val (r, c) = playable[i]
            givenCells[r][c] = solution[r][c]
        }
        
        return SudokuPuzzle(
            seed = seed,
            difficulty = difficulty,
            gameType = GameType.CROSSWORD,
            givenCells = givenCells,
            solution = solution,
            variantMetadata = VariantMetadata.CrosswordData(
                acrossClues = acrossClues,
                downClues = downClues,
                cluePositions = cluePositions
            )
        )
    }

    override fun isValidPlacement(
        board: Array<IntArray>,
        row: Int,
        col: Int,
        value: Int,
        metadata: VariantMetadata?
    ): Boolean = true

    override fun conflictingCells(
        board: Array<IntArray>,
        row: Int,
        col: Int,
        value: Int,
        metadata: VariantMetadata?
    ): Set<Pair<Int, Int>> = emptySet()

    override fun countCorrectCells(
        board: Array<IntArray>,
        solution: Array<IntArray>
    ): Int {
        var count = 0
        for (r in board.indices) {
            for (c in board[0].indices) {
                if (solution[r][c] != -1 && board[r][c] == solution[r][c]) {
                    count++
                }
            }
        }
        return count
    }
}
"""

with open("app/src/main/java/com/zincstate/playmatics/domain/engine/variants/CrosswordEngine.kt", "w") as f:
    f.write(content)
