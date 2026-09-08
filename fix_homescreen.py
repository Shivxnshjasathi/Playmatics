with open("app/src/main/java/com/zincstate/playmatics/presentation/home/HomeScreen.kt", "r") as f:
    content = f.read()

# Replace the allGames list
content = content.replace("val allGames = listOf(\n                            GameType.SUDOKU, GameType.WINDOKU, GameType.ODD_EVEN_SUDOKU,\n                            GameType.WORDOKU, GameType.SAMURAI_SUDOKU, GameType.KENKEN,\n                            GameType.MAZE, GameType.QUEENS, GameType.SCRABBLE\n                        )", "val allGames = listOf(\n                            GameType.SUDOKU, GameType.MAZE, GameType.QUEENS, GameType.SCRABBLE, GameType.DRAWING, GameType.CROSSWORD\n                        )")

with open("app/src/main/java/com/zincstate/playmatics/presentation/home/HomeScreen.kt", "w") as f:
    f.write(content)
