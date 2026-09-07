package com.zincstate.playmatics.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zincstate.playmatics.domain.model.CellState
import com.zincstate.playmatics.ui.theme.CellConflict
import com.zincstate.playmatics.ui.theme.CellHighlightSame
import com.zincstate.playmatics.ui.theme.CellSelected
import com.zincstate.playmatics.ui.theme.CellUserCorrect
import com.zincstate.playmatics.ui.theme.CellUserIncorrect

/**
 * 9×9 Sudoku board with Canvas grid lines and Composable cells.
 */
@Composable
fun SudokuBoard(
    board: Array<IntArray>,
    cellStates: Array<Array<CellState>>,
    selectedCell: Pair<Int, Int>?,
    conflictCells: Set<Pair<Int, Int>>,
    pencilNotes: Map<Int, Set<Int>>,
    highlightMistakes: Boolean,
    onCellClick: (row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surface = MaterialTheme.colorScheme.surface
    val outline = MaterialTheme.colorScheme.outline
    val primary = MaterialTheme.colorScheme.primary

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        val boardSizePx = with(LocalDensity.current) { maxWidth.toPx() }
        val cellSizePx = boardSizePx / 9f
        val cellSizeDp = maxWidth / 9f

        // Grid lines via Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val thinLine = 1.dp.toPx()
            val thickLine = 2.5.dp.toPx()

            // Cell borders (thin lines)
            for (i in 1 until 9) {
                if (i % 3 != 0) {
                    // Vertical
                    drawLine(
                        color = outline,
                        start = Offset(i * cellSizePx, 0f),
                        end = Offset(i * cellSizePx, boardSizePx),
                        strokeWidth = thinLine
                    )
                    // Horizontal
                    drawLine(
                        color = outline,
                        start = Offset(0f, i * cellSizePx),
                        end = Offset(boardSizePx, i * cellSizePx),
                        strokeWidth = thinLine
                    )
                }
            }

            // Block borders (thick lines)
            for (i in 0..9 step 3) {
                // Vertical
                drawLine(
                    color = onSurface,
                    start = Offset(i * cellSizePx, 0f),
                    end = Offset(i * cellSizePx, boardSizePx),
                    strokeWidth = thickLine
                )
                // Horizontal
                drawLine(
                    color = onSurface,
                    start = Offset(0f, i * cellSizePx),
                    end = Offset(boardSizePx, i * cellSizePx),
                    strokeWidth = thickLine
                )
            }
        }

        // Cells
        for (row in 0 until 9) {
            for (col in 0 until 9) {
                val value = board[row][col]
                val state = cellStates[row][col]
                val isSelected = selectedCell == row to col
                val isConflict = (row to col) in conflictCells
                val isSameNumber = selectedCell != null &&
                        board[selectedCell.first][selectedCell.second] != 0 &&
                        value == board[selectedCell.first][selectedCell.second] &&
                        !isSelected
                val isSameRowColBox = selectedCell != null && !isSelected && (
                        row == selectedCell.first ||
                                col == selectedCell.second ||
                                (row / 3 == selectedCell.first / 3 && col / 3 == selectedCell.second / 3)
                        )

                val bgColor by animateColorAsState(
                    targetValue = when {
                        isSelected -> CellSelected
                        isConflict && highlightMistakes -> CellConflict
                        isSameNumber -> CellHighlightSame
                        isSameRowColBox -> CellHighlightSame.copy(alpha = 0.05f)
                        else -> Color.Transparent
                    },
                    animationSpec = tween(150),
                    label = "cellBg"
                )

                val textColor by animateColorAsState(
                    targetValue = when (state) {
                        CellState.GIVEN -> onSurface
                        CellState.USER_CORRECT -> if (highlightMistakes) CellUserCorrect else primary
                        CellState.USER_INCORRECT -> if (highlightMistakes) CellUserIncorrect else primary
                        CellState.EMPTY -> onSurface
                    },
                    animationSpec = tween(150),
                    label = "cellText"
                )

                val noteIndex = row * 9 + col
                val notes = pencilNotes[noteIndex] ?: emptySet()

                Box(
                    modifier = Modifier
                        .size(cellSizeDp)
                        .offset(
                            x = cellSizeDp * col,
                            y = cellSizeDp * row
                        )
                        .background(bgColor)
                        .clickable { onCellClick(row, col) }
                        .semantics {
                            contentDescription = buildCellDescription(row, col, value, state)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (value != 0) {
                        Text(
                            text = value.toString(),
                            color = textColor,
                            fontSize = (cellSizeDp.value * 0.5f).sp,
                            fontWeight = if (state == CellState.GIVEN) FontWeight.Bold else FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    } else if (notes.isNotEmpty()) {
                        PencilNotesGrid(notes = notes, cellSize = cellSizeDp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PencilNotesGrid(notes: Set<Int>, cellSize: Dp) {
    val noteSize = cellSize / 3
    val noteFontSize = (noteSize.value * 0.55f).sp
    val noteColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(modifier = Modifier.fillMaxSize()) {
        for (n in notes) {
            val noteRow = (n - 1) / 3
            val noteCol = (n - 1) % 3
            Text(
                text = n.toString(),
                color = noteColor,
                fontSize = noteFontSize,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .size(noteSize)
                    .offset(
                        x = noteSize * noteCol,
                        y = noteSize * noteRow
                    )
            )
        }
    }
}

private fun buildCellDescription(row: Int, col: Int, value: Int, state: CellState): String {
    val pos = "Row ${row + 1}, Column ${col + 1}"
    return when {
        value == 0 -> "$pos, empty"
        state == CellState.GIVEN -> "$pos, given $value"
        state == CellState.USER_CORRECT -> "$pos, $value, correct"
        state == CellState.USER_INCORRECT -> "$pos, $value, incorrect"
        else -> "$pos, $value"
    }
}
