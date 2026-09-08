import re

with open("app/src/main/java/com/zincstate/playmatics/presentation/home/HomeScreen.kt", "r") as f:
    content = f.read()

# Replace allGames list
old_games = "GameType.SUDOKU, GameType.WORDOKU, GameType.MAZE, GameType.SCRABBLE, GameType.CROSSWORD, GameType.WORD_SEARCH"
new_games = "GameType.SUDOKU, GameType.WORDOKU, GameType.KENKEN, GameType.CROSSWORD, GameType.WORD_SEARCH"
content = content.replace(old_games, new_games)

# Replace gameIcon logic
old_icon_logic = """                                val gameIcon = when (game) {
                                    GameType.MAZE -> Icons.Filled.Timeline
                                    GameType.WORDOKU -> Icons.Filled.Star
                                    GameType.SCRABBLE -> Icons.Filled.FontDownload
                                    else -> Icons.Filled.GridOn
                                }"""
new_icon_logic = """                                val gameIcon = when (game) {
                                    GameType.KENKEN -> Icons.Filled.Functions
                                    GameType.WORDOKU -> Icons.Filled.Star
                                    else -> Icons.Filled.GridOn
                                }"""
content = content.replace(old_icon_logic, new_icon_logic)

with open("app/src/main/java/com/zincstate/playmatics/presentation/home/HomeScreen.kt", "w") as f:
    f.write(content)
