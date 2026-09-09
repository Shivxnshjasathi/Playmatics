package com.zincstate.playmatics.domain.engine.variants

import com.zincstate.playmatics.domain.engine.Difficulty
import com.zincstate.playmatics.domain.engine.GameType
import com.zincstate.playmatics.domain.engine.PuzzleEngine
import com.zincstate.playmatics.domain.engine.SudokuPuzzle
import com.zincstate.playmatics.domain.engine.VariantMetadata
import kotlin.random.Random

object WordSearchEngine : PuzzleEngine {
    override val gameType = GameType.WORD_SEARCH

    private val WORD_BANK = listOf(
        "KOTLIN", "ANDROID", "COMPOSE", "PUZZLE", "GAME", "LOGIC", "MOBILE",
        "STUDIO", "DEVICE", "SCREEN", "BUTTON", "WIDGET", "LAYOUT", "CODE",
        "BUILD", "GRADLE", "DEBUG", "RELEASE", "TEST", "ERROR", "CRASH",
        "MEMORY", "THREAD", "FUTURE", "PROMISE", "COROUTINE", "SCOPE", "STATE",
        "FLOW", "LIVEDATA", "VIEWMODEL", "ACTIVITY", "FRAGMENT", "SERVICE",
        "INTENT", "BROADCAST", "RECEIVER", "PROVIDER", "DATABASE", "ROOM",
        "SQLITE", "QUERY", "TABLE", "COLUMN", "ROW", "INDEX", "KEY", "VALUE"
    )

    override fun generate(seed: Long, difficulty: Difficulty): SudokuPuzzle {
        val random = Random(seed)
        
        val (size, wordCount) = when (difficulty) {
            Difficulty.EASY -> 8 to 4
            Difficulty.NORMAL -> 10 to 6
            Difficulty.HARD -> 12 to 8
            Difficulty.EXPERT -> 14 to 10
        }
        
        val board = Array(size) { IntArray(size) { 0 } }
        val solution = Array(size) { IntArray(size) { 0 } }
        
        // Target words to hide
        val words = WORD_BANK.shuffled(random).take(wordCount)
        val wordPositions = mutableMapOf<String, Pair<Pair<Int, Int>, Pair<Int, Int>>>()
        
        // Hide words
        for (word in words) {
            var placed = false
            var attempts = 0
            while (!placed && attempts < 100) {
                attempts++
                val row = random.nextInt(size)
                val col = random.nextInt(size)
                val dr = random.nextInt(3) - 1 // -1, 0, 1
                val dc = random.nextInt(3) - 1
                if (dr == 0 && dc == 0) continue
                
                var canPlace = true
                for (i in word.indices) {
                    val r = row + i * dr
                    val c = col + i * dc
                    if (r !in 0 until size || c !in 0 until size) {
                        canPlace = false
                        break
                    }
                    if (board[r][c] != 0 && board[r][c] != word[i].code - 'A'.code) {
                        canPlace = false
                        break
                    }
                }
                
                if (canPlace) {
                    for (i in word.indices) {
                        val r = row + i * dr
                        val c = col + i * dc
                        board[r][c] = word[i].code - 'A'.code
                    }
                    wordPositions[word] = Pair(Pair(row, col), Pair(row + (word.length - 1) * dr, col + (word.length - 1) * dc))
                    placed = true
                }
            }
        }
        
        // Fill remaining with random letters
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (board[r][c] == 0) {
                    board[r][c] = random.nextInt(26)
                }
                // solution is just 0 since the board is static
            }
        }
        
        return SudokuPuzzle(
            seed = seed,
            difficulty = difficulty,
            givenCells = board, // The letters are given
            solution = solution,
            gameType = gameType,
            variantMetadata = VariantMetadata.WordSearchData(words, wordPositions)
        )
    }

    override fun isValidPlacement(
        board: Array<IntArray>,
        row: Int,
        col: Int,
        value: Int,
        metadata: VariantMetadata?
    ): Boolean = true // WordSearch doesn't have standard cell placement

    override fun conflictingCells(
        board: Array<IntArray>,
        row: Int,
        col: Int,
        value: Int,
        metadata: VariantMetadata?
    ): Set<Pair<Int, Int>> = emptySet()

    override fun countCorrectCells(board: Array<IntArray>, solution: Array<IntArray>): Int {
        // Words found count handled dynamically on the client for this engine
        return 0 
    }
}
