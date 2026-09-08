import re

with open("app/src/main/java/com/zincstate/playmatics/presentation/multiplayer/match/MultiplayerMatchViewModel.kt", "r") as f:
    content = f.read()

# 1. Update State
new_state_fields = """    val gameType: GameType = GameType.SUDOKU,
    val variantMetadata: VariantMetadata? = null,
    val isDrawer: Boolean = false,
    val strokes: List<com.zincstate.playmatics.presentation.components.PathStroke> = emptyList(),
    val guesses: List<String> = emptyList(),
    val targetWord: String = ""
"""
if "val isDrawer: Boolean" not in content:
    content = content.replace("    val gameType: GameType = GameType.SUDOKU,\n    val variantMetadata: VariantMetadata? = null\n", new_state_fields)

# 2. Add properties to ViewModel
if "private var isHost = false" not in content:
    content = content.replace("private var currentEngine = EngineFactory.getEngine(GameType.SUDOKU)", "private var currentEngine = EngineFactory.getEngine(GameType.SUDOKU)\n    private var isHost = false\n    private var targetWord = \"\"")

# 3. Update initMatch to resolve isHost and targetWord
if "isHost = userId == match.hostId" not in content:
    init_match_target = "            // Join the realtime channel"
    init_match_replace = """            
            // Determine host and target word
            val match = matchRepository.findRoomByCode(matchId) // Actually matchId is passed, not roomCode. Wait, observeMatch gives Match.
            val matchStream = matchRepository.observeMatch(matchId).kotlinx.coroutines.flow.firstOrNull()
            val userId = matchRepository.ensureAuthenticated()
            isHost = matchStream?.hostId == userId
            
            val word = if (puzzle.variantMetadata is VariantMetadata.DrawingData) puzzle.variantMetadata.targetWord else ""
            targetWord = word

            _state.update {
                it.copy(
                    isDrawer = isHost,
                    targetWord = word
                )
            }
            
            // Join the realtime channel"""
    # Just a small fix, we need to import kotlinx.coroutines.flow.firstOrNull
    if "kotlinx.coroutines.flow.firstOrNull" not in content:
        content = content.replace("import kotlinx.coroutines.flow.update", "import kotlinx.coroutines.flow.update\nimport kotlinx.coroutines.flow.firstOrNull")
    content = content.replace(init_match_target, init_match_replace)

# 4. Add listenToDrawingEvents
if "private fun listenToDrawingEvents()" not in content:
    listen_events = """
    private fun listenToDrawingEvents() {
        if (_state.value.gameType != GameType.DRAWING) return

        viewModelScope.launch {
            matchRepository.observeDrawStrokes().collect { points ->
                val offsets = points.chunked(2).map { androidx.compose.ui.geometry.Offset(it[0], it[1]) }
                val stroke = com.zincstate.playmatics.presentation.components.PathStroke(offsets)
                _state.update { it.copy(strokes = it.strokes + stroke) }
            }
        }

        viewModelScope.launch {
            matchRepository.observeGuesses().collect { guess ->
                _state.update { it.copy(guesses = it.guesses + guess) }
                if (guess.equals(targetWord, ignoreCase = true)) {
                    // Game won! The guesser guessed correctly.
                    onMatchCompleted()
                    audioPlayer.playWin()
                } else {
                    audioPlayer.playError()
                }
            }
        }

        viewModelScope.launch {
            matchRepository.observeClearBoard().collect {
                _state.update { it.copy(strokes = emptyList()) }
            }
        }
    }
"""
    content = content.replace("    private fun listenToOpponentProgress()", listen_events + "\n    private fun listenToOpponentProgress()")
    content = content.replace("listenToPresence()", "listenToPresence()\n            listenToDrawingEvents()")

# 5. Add drawing actions
if "fun addDrawStroke" not in content:
    drawing_actions = """
    fun addDrawStroke(stroke: com.zincstate.playmatics.presentation.components.PathStroke) {
        _state.update { it.copy(strokes = it.strokes + stroke) }
        viewModelScope.launch {
            val flatPoints = stroke.points.flatMap { listOf(it.x, it.y) }
            matchRepository.sendDrawStroke(flatPoints)
        }
    }

    fun submitGuess(guess: String) {
        _state.update { it.copy(guesses = it.guesses + guess) }
        viewModelScope.launch {
            matchRepository.sendGuess(guess)
        }
        if (guess.equals(targetWord, ignoreCase = true)) {
            onMatchCompleted()
            audioPlayer.playWin()
        }
    }

    fun clearCanvas() {
        _state.update { it.copy(strokes = emptyList()) }
        viewModelScope.launch {
            matchRepository.sendClearBoard()
        }
    }
"""
    content = content.replace("    fun selectCell", drawing_actions + "\n    fun selectCell")

with open("app/src/main/java/com/zincstate/playmatics/presentation/multiplayer/match/MultiplayerMatchViewModel.kt", "w") as f:
    f.write(content)
