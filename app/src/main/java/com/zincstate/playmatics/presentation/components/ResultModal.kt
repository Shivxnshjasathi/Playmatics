package com.zincstate.playmatics.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.zincstate.playmatics.ui.theme.LossRed
import com.zincstate.playmatics.ui.theme.WinGreen

enum class MatchResult { WIN, LOSS, FORFEIT }

@Composable
fun ResultModal(
    visible: Boolean,
    result: MatchResult,
    yourTime: String,
    opponentTime: String? = null,
    onRematch: (() -> Unit)? = null,
    onNextLevel: (() -> Unit)? = null,
    onHome: () -> Unit
) {
    if (!visible) return

    Dialog(onDismissRequest = { /* non-dismissable */ }) {
        AnimatedVisibility(
            visible = true,
            enter = scaleIn(
                initialScale = 0.8f,
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(0.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icon
                    val (icon, tint, title) = when (result) {
                        MatchResult.WIN -> Triple(Icons.Filled.EmojiEvents, WinGreen, "You Won!")
                        MatchResult.LOSS -> Triple(Icons.Filled.SentimentDissatisfied, LossRed, "You Lost")
                        MatchResult.FORFEIT -> Triple(Icons.AutoMirrored.Filled.ExitToApp, MaterialTheme.colorScheme.onSurfaceVariant, "Opponent Left")
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = tint,
                        modifier = Modifier.size(56.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = tint
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Times
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Your Time", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(yourTime, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                        if (opponentTime != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Opponent", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(opponentTime, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Buttons
                    if (onNextLevel != null) {
                        Button(
                            onClick = onNextLevel,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Next Level")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (onRematch != null) {
                        Button(
                            onClick = onRematch,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Rematch")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    OutlinedButton(
                        onClick = onHome,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Home")
                    }
                }
            }
        }
    }
}
