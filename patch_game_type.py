import re

with open("app/src/main/java/com/zincstate/playmatics/domain/engine/GameType.kt", "r") as f:
    content = f.read()

# Replace MAZE and SCRABBLE with KENKEN
old_block = """    MAZE(
        displayName = "Maze",
        description = "Find the path from start to finish through the labyrinth.",
        key = "maze"
    ),
    SCRABBLE(
        displayName = "Scrabble",
        description = "Form words on a 15x15 board to score points based on letter values and premium squares.",
        key = "scrabble"
    ),"""
    
new_block = """    KENKEN(
        displayName = "KenKen",
        description = "Fill the grid so each row and column has unique digits, satisfying the math clues.",
        key = "kenken"
    ),"""

content = content.replace(old_block, new_block)

with open("app/src/main/java/com/zincstate/playmatics/domain/engine/GameType.kt", "w") as f:
    f.write(content)

