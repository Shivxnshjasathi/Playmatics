package com.zincstate.playmatics.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zincstate.playmatics.ui.theme.AccentBlue
import com.zincstate.playmatics.ui.theme.AccentBlueLight

@Composable
fun NumberPad(
    digitCounts: Map<Int, Int>,
    isNotesMode: Boolean,
    hapticsEnabled: Boolean,
    onDigit: (Int) -> Unit,
    onErase: () -> Unit,
    onToggleNotes: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Actions (Erase, Notes) - styled as pills/circles
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placeholder for Undo (to match spacing in design, optionally)
            
            // Erase
            IconButton(
                onClick = {
                    if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onErase()
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AccentBlueLight),
                colors = IconButtonDefaults.iconButtonColors(contentColor = AccentBlue)
            ) {
                Icon(Icons.Outlined.Backspace, contentDescription = "Erase", modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(24.dp))

            // Notes
            val notesBgColor by animateColorAsState(
                targetValue = if (isNotesMode) AccentBlue else AccentBlueLight,
                animationSpec = tween(200),
                label = "notesBgColor"
            )
            val notesIconColor by animateColorAsState(
                targetValue = if (isNotesMode) MaterialTheme.colorScheme.surface else AccentBlue,
                animationSpec = tween(200),
                label = "notesIconColor"
            )

            IconButton(
                onClick = {
                    if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggleNotes()
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(notesBgColor),
                colors = IconButtonDefaults.iconButtonColors(contentColor = notesIconColor)
            ) {
                Icon(
                    if (isNotesMode) Icons.Outlined.Edit else Icons.Outlined.EditOff, 
                    contentDescription = "Notes", 
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Digits 1-9 in a single row
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (digit in 1..9) {
                val count = digitCounts[digit] ?: 0
                val disabled = count >= 9

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clickable(enabled = !disabled) {
                            if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onDigit(digit)
                        }
                ) {
                    Text(
                        text = digit.toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (disabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f) else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
