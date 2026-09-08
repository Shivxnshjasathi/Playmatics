package com.zincstate.playmatics.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditOff
import androidx.compose.material3.Icon
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
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
    modifier: Modifier = Modifier,
    variantMetadata: com.zincstate.playmatics.domain.engine.VariantMetadata? = null
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
            FilledTonalIconButton(
                onClick = {
                    if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onErase()
                },
                modifier = Modifier.size(56.dp),
                shape = CircleShape
            ) {
                Icon(Icons.AutoMirrored.Outlined.Backspace, contentDescription = "Erase", modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.width(32.dp))

            // Notes
            if (isNotesMode) {
                FilledIconButton(
                    onClick = {
                        if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onToggleNotes()
                    },
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Notes", modifier = Modifier.size(28.dp))
                }
            } else {
                FilledTonalIconButton(
                    onClick = {
                        if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onToggleNotes()
                    },
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Outlined.EditOff, contentDescription = "Notes", modifier = Modifier.size(28.dp))
                }
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
                        .padding(horizontal = 2.dp)
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (disabled) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(enabled = !disabled) {
                            if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onDigit(digit)
                        }
                ) {
                    val displayStr = if (variantMetadata is com.zincstate.playmatics.domain.engine.VariantMetadata.WordokuMapping) {
                        variantMetadata.letterMap[digit]?.toString() ?: digit.toString()
                    } else {
                        digit.toString()
                    }
                    Text(
                        text = displayStr,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (disabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
