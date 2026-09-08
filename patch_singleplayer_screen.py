import re

with open("app/src/main/java/com/zincstate/playmatics/presentation/singleplayer/SinglePlayerScreen.kt", "r") as f:
    content = f.read()

drawing_ui = """
                if (state.gameType == GameType.DRAWING) {
                    Text(
                        text = "Draw this: (Offline Mode)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                    
                    var currentPath by remember { mutableStateOf<MutableList<androidx.compose.ui.geometry.Offset>?>(null) }
                    var strokes by remember { mutableStateOf<List<com.zincstate.playmatics.presentation.components.PathStroke>>(emptyList()) }
                    
                    com.zincstate.playmatics.presentation.components.DrawingBoard(
                        isDrawer = true,
                        strokes = strokes,
                        onStrokeDrawn = { stroke -> strokes = strokes + stroke },
                        onClear = { strokes = emptyList() },
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    )
                } else {
                    GameRenderer(
                        gameType = state.gameType,
                        board = state.board,
                        cellStates = state.cellStates,
                        selectedCell = state.selectedCell,
                        conflictCells = state.conflictCells,
                        pencilNotes = state.pencilNotes,
                        isNotesMode = state.isNotesMode,
                        variantMetadata = state.variantMetadata,
                        highlightMistakes = highlightMistakes,
                        hapticsEnabled = hapticsEnabled,
                        onCellClick = viewModel::selectCell,
                        onDigit = viewModel::enterDigit,
                        onErase = viewModel::eraseCell,
                        onToggleNotes = viewModel::toggleNotesMode,
                        digitCounts = remember(state.board) { viewModel.getDigitCounts() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
"""

if "if (state.gameType == GameType.DRAWING)" not in content:
    content = content.replace("""                GameRenderer(
                    gameType = state.gameType,
                    board = state.board,
                    cellStates = state.cellStates,
                    selectedCell = state.selectedCell,
                    conflictCells = state.conflictCells,
                    pencilNotes = state.pencilNotes,
                    isNotesMode = state.isNotesMode,
                    variantMetadata = state.variantMetadata,
                    highlightMistakes = highlightMistakes,
                    hapticsEnabled = hapticsEnabled,
                    onCellClick = viewModel::selectCell,
                    onDigit = viewModel::enterDigit,
                    onErase = viewModel::eraseCell,
                    onToggleNotes = viewModel::toggleNotesMode,
                    digitCounts = remember(state.board) { viewModel.getDigitCounts() },
                    modifier = Modifier.fillMaxWidth()
                )""", drawing_ui.strip())

with open("app/src/main/java/com/zincstate/playmatics/presentation/singleplayer/SinglePlayerScreen.kt", "w") as f:
    f.write(content)
