import re

# 1. WordSearchBoard.kt
with open("app/src/main/java/com/zincstate/playmatics/presentation/components/WordSearchBoard.kt", "r") as f:
    content = f.read()
if "import com.zincstate.playmatics.domain.engine.VariantMetadata" in content:
    content = content.replace("import com.zincstate.playmatics.domain.engine.VariantMetadata", "import com.zincstate.playmatics.domain.engine.VariantMetadata\nimport com.zincstate.playmatics.domain.engine.VariantMetadata.WordSearchData")
with open("app/src/main/java/com/zincstate/playmatics/presentation/components/WordSearchBoard.kt", "w") as f:
    f.write(content)

# 2. HomeScreen.kt
with open("app/src/main/java/com/zincstate/playmatics/presentation/home/HomeScreen.kt", "r") as f:
    content = f.read()
content = content.replace("GameType.SUDOKU, GameType.MAZE, GameType.QUEENS, GameType.SCRABBLE, GameType.DRAWING, GameType.CROSSWORD", "GameType.SUDOKU, GameType.WORDOKU, GameType.MAZE, GameType.SCRABBLE, GameType.CROSSWORD, GameType.WORD_SEARCH")
content = content.replace("GameType.QUEENS -> Icons.Filled.Star", "GameType.WORDOKU -> Icons.Filled.Grid3x3")
content = content.replace("GameType.DRAWING -> Icons.Filled.Brush", "GameType.WORD_SEARCH -> Icons.Filled.Search")
with open("app/src/main/java/com/zincstate/playmatics/presentation/home/HomeScreen.kt", "w") as f:
    f.write(content)

# 3. MultiplayerMatchScreen.kt
with open("app/src/main/java/com/zincstate/playmatics/presentation/multiplayer/match/MultiplayerMatchScreen.kt", "r") as f:
    content = f.read()
drawing_ui = r'            GameType\.DRAWING -> \{[\s\S]*?            \}\n'
content = re.sub(drawing_ui, '', content)
content = re.sub(r'import com\.zincstate\.playmatics\.presentation\.components\.DrawingBoard\n', '', content)
content = re.sub(r'import com\.zincstate\.playmatics\.presentation\.components\.GuessPad\n', '', content)
with open("app/src/main/java/com/zincstate/playmatics/presentation/multiplayer/match/MultiplayerMatchScreen.kt", "w") as f:
    f.write(content)

# 4. MultiplayerMatchViewModel.kt
with open("app/src/main/java/com/zincstate/playmatics/presentation/multiplayer/match/MultiplayerMatchViewModel.kt", "r") as f:
    content = f.read()

# Replace MultiplayerUiState entirely
old_state = r'data class MultiplayerUiState\([\s\S]*?\) \{'
new_state = """data class MultiplayerUiState(
    val board: Array<IntArray> = Array(9) { IntArray(9) },
    val givenCells: Array<IntArray> = Array(9) { IntArray(9) },
    val solution: Array<IntArray> = Array(9) { IntArray(9) },
    val cellStates: Array<Array<CellState>> = Array(9) { Array(9) { CellState.EMPTY } },
    val selectedCell: Pair<Int, Int>? = null,
    val pencilNotes: Map<Int, Set<Int>> = emptyMap(),
    val isNotesMode: Boolean = false,
    val conflictCells: Set<Pair<Int, Int>> = emptySet(),
    val correctCount: Int = 0,
    val opponentCorrectCount: Int = 0,
    val elapsedSeconds: Long = 0L,
    val isLoading: Boolean = true,
    val matchResult: MatchResult? = null,
    val isOpponentOnline: Boolean = true,
    val opponentDisconnectSeconds: Int = 0,
    val gameType: GameType = GameType.SUDOKU,
    val variantMetadata: VariantMetadata? = null
) {"""
content = re.sub(old_state, new_state, content)

# Remove isDrawer from initMatch
content = re.sub(r'                    isDrawer = isHost,\n', '', content)
content = re.sub(r'                    targetWord = word\n', '', content)
content = re.sub(r'            val word = if \(puzzle.variantMetadata is VariantMetadata.DrawingData\) puzzle.variantMetadata.targetWord else ""\n', '', content)
content = re.sub(r'            targetWord = word\n', '', content)
content = re.sub(r'            val matchStream = matchRepository.observeMatch\(matchId\).kotlinx.coroutines.flow.firstOrNull\(\)\n', '', content)
content = re.sub(r'            val userId = matchRepository.ensureAuthenticated\(\)\n', '', content)
content = re.sub(r'            isHost = matchStream\?\.hostId == userId\n', '', content)

# Remove addDrawStroke, submitGuess, clearCanvas
content = re.sub(r'    fun addDrawStroke[\s\S]*?    \}\n\n', '', content)
content = re.sub(r'    fun submitGuess[\s\S]*?    \}\n\n', '', content)
content = re.sub(r'    fun clearCanvas[\s\S]*?    \}\n\n', '', content)

with open("app/src/main/java/com/zincstate/playmatics/presentation/multiplayer/match/MultiplayerMatchViewModel.kt", "w") as f:
    f.write(content)

