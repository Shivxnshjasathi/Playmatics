import re

with open("app/src/main/java/com/zincstate/playmatics/presentation/multiplayer/match/MultiplayerMatchViewModel.kt", "r") as f:
    content = f.read()

# Remove DrawingData imports, PathStroke imports
content = re.sub(r'import com\.zincstate\.playmatics\.presentation\.components\.PathStroke\n', '', content)
content = re.sub(r'import com\.zincstate\.playmatics\.domain\.engine\.VariantMetadata\.DrawingData\n', '', content)

# Remove strokes, guesses, targetWord from MultiplayerUiState
content = re.sub(r'val strokes: List<PathStroke> = emptyList\(\),\n\s*val guesses: List<String> = emptyList\(\),\n', '', content)

# Remove the drawing specific block in match loading
drawing_init = """
            var targetWord = ""
            if (gameType == GameType.DRAWING) {
                targetWord = (variant as? VariantMetadata.DrawingData)?.targetWord ?: ""
                listenToDrawingEvents()
            }
"""
content = content.replace(drawing_init, "")

# Remove listenToDrawingEvents completely
drawing_listener = r'    private fun listenToDrawingEvents\(\) \{[\s\S]*?\}\n\n'
content = re.sub(drawing_listener, '', content)

# Fix the syntax error around listenToPresence
# It currently has:
#    private fun listenToPresence()
#            listenToDrawingEvents() {
#        viewModelScope.launch {
#
content = content.replace("    private fun listenToPresence()\n            listenToDrawingEvents() {\n        viewModelScope.launch {", "    private fun listenToPresence() {\n        viewModelScope.launch {")
content = content.replace("    private fun listenToPresence()\n            listenToDrawingEvents() {", "    private fun listenToPresence() {")

# Remove Drawing related methods like sendStroke, sendGuess, clearBoard
stroke_guess = r'    fun sendStroke[\s\S]*?    \}\n\n'
content = re.sub(stroke_guess, '', content)
content = re.sub(r'    fun sendGuess[\s\S]*?    \}\n\n', '', content)
content = re.sub(r'    fun clearBoard[\s\S]*?    \}\n\n', '', content)


with open("app/src/main/java/com/zincstate/playmatics/presentation/multiplayer/match/MultiplayerMatchViewModel.kt", "w") as f:
    f.write(content)

