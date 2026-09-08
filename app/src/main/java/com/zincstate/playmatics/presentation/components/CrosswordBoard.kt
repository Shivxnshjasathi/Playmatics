package com.zincstate.playmatics.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zincstate.playmatics.domain.engine.VariantMetadata
import com.zincstate.playmatics.domain.engine.VariantMetadata.CrosswordData

@Composable
fun CrosswordBoard(
    board: Array<IntArray>,
    givenCells: Array<IntArray>,
    selectedCell: Pair<Int, Int>?,
    variantMetadata: VariantMetadata?,
    onCellClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val meta = variantMetadata as? CrosswordData
    val size = board.size

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(2.dp, MaterialTheme.colorScheme.onSurface)
    ) {
        for (r in 0 until size) {
            Row(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
                for (c in 0 until size) {
                    val isBlack = givenCells[r][c] == -1
                    val value = board[r][c]
                    val isSelected = selectedCell == Pair(r, c)
                    
                    // Find clue number if any
                    val clueNum = meta?.cluePositions?.entries?.find { it.value == Pair(r, c) }?.key
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(
                                when {
                                    isBlack -> Color.Black
                                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                                    else -> MaterialTheme.colorScheme.surface
                                }
                            )
                            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                            .clickable(enabled = !isBlack) { onCellClick(r, c) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!isBlack) {
                            if (clueNum != null) {
                                Text(
                                    text = clueNum.toString(),
                                    fontSize = 10.sp,
                                    modifier = Modifier.align(Alignment.TopStart).padding(start = 2.dp, top = 2.dp),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (value in 1..26) {
                                Text(
                                    text = ('A' + value - 1).toString(),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CrosswordClues(
    variantMetadata: VariantMetadata?,
    modifier: Modifier = Modifier
) {
    val meta = variantMetadata as? CrosswordData ?: return

    Row(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text("Across", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            LazyColumn {
                items(meta.acrossClues.entries.toList().sortedBy { it.key }) { entry ->
                    val num = entry.key
                    val clue = entry.value
                    Text("$num. $clue", fontSize = 12.sp, lineHeight = 14.sp)
                }
            }
        }
        Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
            Text("Down", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            LazyColumn {
                items(meta.downClues.entries.toList().sortedBy { it.key }) { entry ->
                    val num = entry.key
                    val clue = entry.value
                    Text("$num. $clue", fontSize = 12.sp, lineHeight = 14.sp)
                }
            }
        }
    }
}
