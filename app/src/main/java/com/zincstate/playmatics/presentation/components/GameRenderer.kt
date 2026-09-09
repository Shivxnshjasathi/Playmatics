package com.zincstate.playmatics.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditOff
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zincstate.playmatics.domain.engine.GameType
import com.zincstate.playmatics.domain.engine.VariantMetadata
import com.zincstate.playmatics.domain.model.CellState

@Composable
fun GameRenderer(
    gameType: GameType,
    board: Array<IntArray>,
    givenCells: Array<IntArray> = Array(board.size) { IntArray(board[0].size) { 0 } },
    cellStates: Array<Array<CellState>>,
    selectedCell: Pair<Int, Int>?,
    conflictCells: Set<Pair<Int, Int>>,
    pencilNotes: Map<Int, Set<Int>>,
    isNotesMode: Boolean,
    variantMetadata: VariantMetadata?,
    highlightMistakes: Boolean,
    hapticsEnabled: Boolean,
    onCellClick: (Int, Int) -> Unit,
    onDigit: (Int) -> Unit,
    onErase: () -> Unit,
    onToggleNotes: () -> Unit,
    onAction: (String) -> Unit = {},
    digitCounts: Map<Int, Int> = emptyMap(),
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        when (gameType) {
            GameType.KENKEN -> {
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
            }
            GameType.CROSSWORD -> {
                CrosswordBoard(
                    board = board,
                    givenCells = givenCells,
                    selectedCell = selectedCell,
                    variantMetadata = variantMetadata,
                    onCellClick = onCellClick,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                CrosswordClues(
                    variantMetadata = variantMetadata,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LetterPad(
                    onLetter = { char -> onDigit(char.code - 'A'.code + 1) },
                    onErase = onErase
                )
            }
            GameType.WORD_SEARCH -> {
                WordSearchBoard(
                    board = givenCells, // We use givenCells as the board layout in this engine
                    variantMetadata = variantMetadata,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            GameType.WORDOKU -> {
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
                
                // Actions (Erase, Notes)
                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    androidx.compose.material3.FilledTonalIconButton(
                        onClick = onErase,
                        modifier = Modifier.size(56.dp),
                        shape = androidx.compose.foundation.shape.CircleShape
                    ) {
                        androidx.compose.material3.Icon(
                            androidx.compose.material.icons.Icons.AutoMirrored.Outlined.Backspace,
                            contentDescription = "Erase",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(32.dp))

                    if (isNotesMode) {
                        androidx.compose.material3.FilledIconButton(
                            onClick = onToggleNotes,
                            modifier = Modifier.size(56.dp),
                            shape = androidx.compose.foundation.shape.CircleShape
                        ) {
                            androidx.compose.material3.Icon(
                                androidx.compose.material.icons.Icons.Outlined.Edit,
                                contentDescription = "Notes",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    } else {
                        androidx.compose.material3.FilledTonalIconButton(
                            onClick = onToggleNotes,
                            modifier = Modifier.size(56.dp),
                            shape = androidx.compose.foundation.shape.CircleShape
                        ) {
                            androidx.compose.material3.Icon(
                                androidx.compose.material.icons.Icons.Outlined.EditOff,
                                contentDescription = "Notes",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LetterPad(
                    onLetter = { char -> onDigit(char.code - 'A'.code + 1) },
                    onErase = onErase
                )
            }
            GameType.SUDOKU -> {
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
            }
        }
    }
}
