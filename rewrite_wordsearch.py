import re

content = """package com.zincstate.playmatics.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zincstate.playmatics.domain.engine.VariantMetadata
import kotlin.math.abs

@Composable
fun WordSearchBoard(
    board: Array<IntArray>,
    variantMetadata: VariantMetadata?,
    modifier: Modifier = Modifier
) {
    val meta = variantMetadata as? VariantMetadata.WordSearchData
    val rows = board.size
    val cols = board.firstOrNull()?.size ?: 0

    val foundWords = remember { mutableStateListOf<String>() }
    val foundLines = remember { mutableStateListOf<Pair<Pair<Int, Int>, Pair<Int, Int>>>() }
    
    var dragStart by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var dragEnd by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var boxSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(if (cols > 0) cols.toFloat() / rows.toFloat() else 1f)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            if (rows > 0 && cols > 0 && boxSize.width > 0) {
                                val cellW = boxSize.width / cols
                                val cellH = boxSize.height / rows
                                val c = (offset.x / cellW).toInt().coerceIn(0, cols - 1)
                                val r = (offset.y / cellH).toInt().coerceIn(0, rows - 1)
                                dragStart = r to c
                                dragEnd = r to c
                            }
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val offset = change.position
                            if (rows > 0 && cols > 0 && boxSize.width > 0) {
                                val cellW = boxSize.width / cols
                                val cellH = boxSize.height / rows
                                val c = (offset.x / cellW).toInt().coerceIn(0, cols - 1)
                                val r = (offset.y / cellH).toInt().coerceIn(0, rows - 1)
                                
                                // Restrict dragEnd to be on a straight line (horizontal, vertical, diagonal)
                                val start = dragStart
                                if (start != null) {
                                    val dr = abs(r - start.first)
                                    val dc = abs(c - start.second)
                                    if (dr == 0 || dc == 0 || dr == dc) {
                                        dragEnd = r to c
                                    }
                                }
                            }
                        },
                        onDragEnd = {
                            val start = dragStart
                            val end = dragEnd
                            if (start != null && end != null && meta != null) {
                                // Extract the word string from the board
                                val dr = (end.first - start.first).sign()
                                val dc = (end.second - start.second).sign()
                                val length = maxOf(abs(end.first - start.first), abs(end.second - start.second)) + 1
                                
                                val selectedWord = buildString {
                                    for (i in 0 until length) {
                                        val r = start.first + i * dr
                                        val c = start.second + i * dc
                                        if (r in 0 until rows && c in 0 until cols) {
                                            append((board[r][c] + 'A'.code).toChar())
                                        }
                                    }
                                }
                                
                                val reversedWord = selectedWord.reversed()
                                
                                if (meta.words.contains(selectedWord) && !foundWords.contains(selectedWord)) {
                                    foundWords.add(selectedWord)
                                    foundLines.add(start to end)
                                } else if (meta.words.contains(reversedWord) && !foundWords.contains(reversedWord)) {
                                    foundWords.add(reversedWord)
                                    foundLines.add(start to end)
                                }
                            }
                            dragStart = null
                            dragEnd = null
                        },
                        onDragCancel = {
                            dragStart = null
                            dragEnd = null
                        }
                    )
                }
        ) {
            // Draw grid of letters
            Column(modifier = Modifier.fillMaxSize()) {
                for (r in board.indices) {
                    Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        for (c in board[r].indices) {
                            val value = board[r][c]
                            val char = (value + 'A'.code).toChar()

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .border(0.5.dp, Color.LightGray)
                                    .background(Color.White)
                            ) {
                                Text(
                                    text = char.toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color.Black,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }
            
            // Draw lines on top
            Canvas(modifier = Modifier.fillMaxSize()) {
                boxSize = size
                if (rows == 0 || cols == 0) return@Canvas
                
                val cellW = size.width / cols
                val cellH = size.height / rows
                
                val strokeW = minOf(cellW, cellH) * 0.6f
                val highlightColor = Color(0x664CAF50) // Semi-transparent Green
                val activeColor = Color(0x66FF9800) // Semi-transparent Orange
                
                // Draw found lines
                foundLines.forEach { (start, end) ->
                    val startX = start.second * cellW + cellW / 2
                    val startY = start.first * cellH + cellH / 2
                    val endX = end.second * cellW + cellW / 2
                    val endY = end.first * cellH + cellH / 2
                    
                    drawLine(
                        color = highlightColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                }
                
                // Draw active drag line
                val start = dragStart
                val end = dragEnd
                if (start != null && end != null) {
                    val startX = start.second * cellW + cellW / 2
                    val startY = start.first * cellH + cellH / 2
                    val endX = end.second * cellW + cellW / 2
                    val endY = end.first * cellH + cellH / 2
                    
                    drawLine(
                        color = activeColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Target Words
        if (meta != null) {
            Text("Find these words:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            val chunkedWords = meta.words.chunked(3)
            Column(modifier = Modifier.fillMaxWidth()) {
                chunkedWords.forEach { rowWords ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), 
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        rowWords.forEach { word ->
                            val isFound = foundWords.contains(word)
                            Text(
                                text = word,
                                fontSize = 16.sp,
                                fontWeight = if (isFound) FontWeight.Normal else FontWeight.Bold,
                                color = if (isFound) Color.Gray else Color.Unspecified,
                                textDecoration = if (isFound) TextDecoration.LineThrough else TextDecoration.None
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun Int.sign(): Int = if (this > 0) 1 else if (this < 0) -1 else 0
"""

with open("app/src/main/java/com/zincstate/playmatics/presentation/components/WordSearchBoard.kt", "w") as f:
    f.write(content)
