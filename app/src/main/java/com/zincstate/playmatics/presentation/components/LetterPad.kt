package com.zincstate.playmatics.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LetterPad(
    onLetter: (Char) -> Unit,
    onErase: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = listOf(
        listOf('Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P'),
        listOf('A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L'),
        listOf('Z', 'X', 'C', 'V', 'B', 'N', 'M')
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (row in rows) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (char in row) {
                    Box(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .height(48.dp)
                            .widthIn(min = 32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onLetter(char) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char.toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Add backspace to the last row
                if (row.contains('Z')) {
                    Box(
                        modifier = Modifier
                            .weight(1.5f, fill = false)
                            .height(48.dp)
                            .widthIn(min = 48.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .clickable { onErase() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Erase",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}
