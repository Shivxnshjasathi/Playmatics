import re

with open("app/src/main/java/com/zincstate/playmatics/presentation/components/GameRenderer.kt", "r") as f:
    content = f.read()

# Add onAction
old_sig = """    onCellClick: (Int, Int) -> Unit,
    onDigit: (Int) -> Unit,
    onErase: () -> Unit,
    onToggleNotes: () -> Unit"""
new_sig = """    onCellClick: (Int, Int) -> Unit,
    onDigit: (Int) -> Unit,
    onErase: () -> Unit,
    onToggleNotes: () -> Unit,
    onAction: (String) -> Unit = {}"""
content = content.replace(old_sig, new_sig)

# Pass onAction to WordSearchBoard
old_ws = """            GameType.WORD_SEARCH -> {
                WordSearchBoard(
                    board = board,
                    variantMetadata = variantMetadata,
                    modifier = Modifier.fillMaxWidth()
                )
            }"""
new_ws = """            GameType.WORD_SEARCH -> {
                WordSearchBoard(
                    board = board,
                    variantMetadata = variantMetadata,
                    onWordFound = { onAction("PLAY_WIN") },
                    modifier = Modifier.fillMaxWidth()
                )
            }"""
content = content.replace(old_ws, new_ws)

with open("app/src/main/java/com/zincstate/playmatics/presentation/components/GameRenderer.kt", "w") as f:
    f.write(content)
