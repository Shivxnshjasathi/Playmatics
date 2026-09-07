package com.zincstate.playmatics.presentation.multiplayer.match

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zincstate.playmatics.presentation.components.DualProgressBar
import com.zincstate.playmatics.presentation.components.MatchResult
import com.zincstate.playmatics.presentation.components.NumberPad
import com.zincstate.playmatics.presentation.components.ResultModal
import com.zincstate.playmatics.presentation.components.SudokuBoard
import com.zincstate.playmatics.presentation.singleplayer.formatTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiplayerMatchScreen(
    matchId: String,
    seed: Long,
    difficulty: String,
    onHome: () -> Unit,
    viewModel: MultiplayerMatchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(matchId) {
        viewModel.initMatch(matchId, seed, difficulty)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "1v1",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            Icons.Filled.Timer,
                            contentDescription = "Timer",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            formatTime(state.elapsedSeconds),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (!state.isOpponentOnline && state.opponentDisconnectSeconds > 0) {
                            Icon(
                                Icons.Filled.WifiOff,
                                contentDescription = "Opponent disconnected",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "${state.opponentDisconnectSeconds}s",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Generating puzzle…", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress bars
                DualProgressBar(
                    yourProgress = state.yourProgress,
                    opponentProgress = state.opponentProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                SudokuBoard(
                    board = state.board,
                    cellStates = state.cellStates,
                    selectedCell = state.selectedCell,
                    conflictCells = state.conflictCells,
                    pencilNotes = state.pencilNotes,
                    highlightMistakes = true,
                    onCellClick = viewModel::selectCell,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                NumberPad(
                    digitCounts = remember(state.board) { viewModel.getDigitCounts() },
                    isNotesMode = state.isNotesMode,
                    hapticsEnabled = true,
                    onDigit = viewModel::enterDigit,
                    onErase = viewModel::eraseCell,
                    onToggleNotes = viewModel::toggleNotesMode
                )
            }
        }

        // Result modal
        ResultModal(
            visible = state.matchResult != null,
            result = state.matchResult ?: MatchResult.LOSS,
            yourTime = formatTime(state.elapsedSeconds),
            opponentTime = null,
            onRematch = null,
            onHome = onHome
        )
    }
}
