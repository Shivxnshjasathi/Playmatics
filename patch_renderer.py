import re

with open("app/src/main/java/com/zincstate/playmatics/presentation/components/GameRenderer.kt", "r") as f:
    content = f.read()

# Replace MAZE and SCRABBLE branches with KENKEN
old_block = r"""            GameType\.MAZE -> \{
                MazeBoard\(
                    board = board,
                    playerPos = selectedCell,
                    onPlayerMove = \{ r, c -> 
                        onCellClick\(r, c\)
                        onDigit\(4\)
                    \},
                    modifier = Modifier\.fillMaxWidth\(\)
                \)
            \}
            GameType\.SCRABBLE -> \{
                ScrabbleBoard\(
                    board = board,
                    selectedCell = selectedCell,
                    variantMetadata = variantMetadata,
                    onCellClick = onCellClick,
                    modifier = Modifier\.fillMaxWidth\(\)
                \)
                
                Spacer\(modifier = Modifier\.height\(20\.dp\)\)
                
                LetterPad\(
                    onLetter = \{ char -> onDigit\(char\.code\) \},
                    onErase = onErase
                \)
            \}"""

new_block = """            GameType.KENKEN -> {
                SudokuBoard(
                    board = board,
                    cellStates = cellStates,
                    selectedCell = selectedCell,
                    conflictCells = conflictCells,
                    pencilNotes = pencilNotes,
                    highlightMistakes = highlightMistakes,
                    variantMetadata = variantMetadata,
                    onCellClick = onCellClick,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                NumberPad(
                    digitCounts = digitCounts,
                    isNotesMode = isNotesMode,
                    hapticsEnabled = hapticsEnabled,
                    onDigit = onDigit,
                    onErase = onErase,
                    onToggleNotes = onToggleNotes,
                    variantMetadata = variantMetadata
                )
            }"""

content = re.sub(old_block, new_block, content)

with open("app/src/main/java/com/zincstate/playmatics/presentation/components/GameRenderer.kt", "w") as f:
    f.write(content)
