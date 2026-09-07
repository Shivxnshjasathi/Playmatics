package com.zincstate.playmatics.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BackSpace
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditOff
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Number pad with digits 1-9, Erase, and Notes toggle.
 * Digits already placed 9 times are greyed out.
 */
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
        // Digits 1-9 in rows of 5 and 4
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (digit in 1..5) {
                val count = digitCounts[digit] ?: 0
                val disabled = count >= 9

                DigitButton(
                    digit = digit,
                    disabled = disabled,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onDigit(digit)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (digit in 6..9) {
                val count = digitCounts[digit] ?: 0
                val disabled = count >= 9

                DigitButton(
                    digit = digit,
                    disabled = disabled,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onDigit(digit)
                    }
                )
            }
            // Empty weight to balance
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action buttons row
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Erase button
            FilledTonalButton(
                onClick = {
                    if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onErase()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .semantics { contentDescription = "Erase" },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.BackSpace,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Erase", style = MaterialTheme.typography.labelLarge)
            }

            // Notes toggle
            val notesColor by animateColorAsState(
                targetValue = if (isNotesMode)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                animationSpec = tween(200),
                label = "notesColor"
            )

            FilledTonalButton(
                onClick = {
                    if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggleNotes()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .semantics { contentDescription = if (isNotesMode) "Notes mode on" else "Notes mode off" },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = notesColor
                )
            ) {
                Icon(
                    imageVector = if (isNotesMode) Icons.Outlined.Edit else Icons.Outlined.EditOff,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Notes", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun DigitButton(
    digit: Int,
    disabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = !disabled,
        modifier = modifier
            .height(52.dp)
            .semantics { contentDescription = "Digit $digit" },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
    ) {
        Text(
            text = digit.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
