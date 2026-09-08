with open("app/src/main/java/com/zincstate/playmatics/data/repository/MatchRepositoryImpl.kt", "r") as f:
    content = f.read()

new_methods = """
    override suspend fun sendDrawStroke(stroke: List<Float>) {
        val userId = supabaseManager.currentUserId ?: return
        supabaseManager.sendDrawStroke(userId, stroke)
    }

    override suspend fun sendGuess(guess: String) {
        val userId = supabaseManager.currentUserId ?: return
        supabaseManager.sendGuess(userId, guess)
    }

    override suspend fun sendClearBoard() {
        val userId = supabaseManager.currentUserId ?: return
        supabaseManager.sendClearBoard(userId)
    }

    override fun observeDrawStrokes(): Flow<List<Float>> {
        val userId = supabaseManager.currentUserId
        return supabaseManager.observeDrawStrokes()?.mapNotNull {
            if (it.playerId != userId) it.stroke else null
        } ?: kotlinx.coroutines.flow.emptyFlow()
    }

    override fun observeGuesses(): Flow<String> {
        val userId = supabaseManager.currentUserId
        return supabaseManager.observeGuesses()?.mapNotNull {
            if (it.playerId != userId) it.guess else null
        } ?: kotlinx.coroutines.flow.emptyFlow()
    }

    override fun observeClearBoard(): Flow<Unit> {
        val userId = supabaseManager.currentUserId
        return supabaseManager.observeClearBoard()?.mapNotNull {
            if (it.playerId != userId) Unit else null
        } ?: kotlinx.coroutines.flow.emptyFlow()
    }
"""

if "override suspend fun sendDrawStroke" not in content:
    content = content.replace("override fun observeConnectivity()", new_methods + "\n    override fun observeConnectivity()")

with open("app/src/main/java/com/zincstate/playmatics/data/repository/MatchRepositoryImpl.kt", "w") as f:
    f.write(content)
