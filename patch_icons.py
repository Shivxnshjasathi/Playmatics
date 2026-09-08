import re

with open("app/src/main/java/com/zincstate/playmatics/presentation/home/HomeScreen.kt", "r") as f:
    content = f.read()

# Replace the hardcoded icon with a conditional one
new_icon_logic = """
                                val gameIcon = when (game) {
                                    GameType.MAZE -> Icons.Filled.Timeline
                                    GameType.QUEENS -> Icons.Filled.Star
                                    GameType.SCRABBLE -> Icons.Filled.FontDownload
                                    else -> Icons.Filled.GridOn
                                }
                                Icon(gameIcon, contentDescription = game.displayName)
"""

# Find the exact lines
target = "icon = { Icon(Icons.Filled.GridOn, contentDescription = game.displayName) },"
replacement = "icon = {\n" + new_icon_logic + "\n                                },"

content = content.replace(target, replacement)

# Add imports if missing
if "import androidx.compose.material.icons.filled.Timeline" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.GridOn", "import androidx.compose.material.icons.filled.GridOn\nimport androidx.compose.material.icons.filled.Star\nimport androidx.compose.material.icons.filled.FontDownload\nimport androidx.compose.material.icons.filled.Timeline")

with open("app/src/main/java/com/zincstate/playmatics/presentation/home/HomeScreen.kt", "w") as f:
    f.write(content)
