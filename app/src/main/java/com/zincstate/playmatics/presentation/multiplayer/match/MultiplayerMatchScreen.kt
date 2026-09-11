package com.zincstate.playmatics.presentation.multiplayer.match

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

import com.zincstate.playmatics.presentation.components.MatchResult
import com.zincstate.playmatics.presentation.components.NumberPad
import com.zincstate.playmatics.presentation.components.ResultModal
import com.zincstate.playmatics.presentation.components.SudokuBoard
import com.zincstate.playmatics.presentation.components.GameRenderer
import com.zincstate.playmatics.presentation.singleplayer.formatTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiplayerMatchScreen(
    matchId: String,
    seed: Long,
    difficulty: String,
    gameType: String,
    onHome: () -> Unit,
    onSettings: () -> Unit,
    viewModel: MultiplayerMatchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(matchId) {
        viewModel.initMatch(matchId, seed, difficulty, gameType)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    com.zincstate.playmatics.presentation.components.PlaymaticsLogo()
                },
                navigationIcon = {
                    IconButton(onClick = onHome) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "1v1 MATCH",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    if (state.mistakeLimitEnabled) {
                        Text(
                            "Mistakes: ${state.mistakesMade}/3",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (state.mistakesMade >= 2) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!state.isOpponentOnline && state.opponentDisconnectSeconds > 0) {
                            Icon(
                                Icons.Filled.WifiOff,
                                contentDescription = "Opponent disconnected",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "${state.opponentDisconnectSeconds}s",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        
                        Icon(
                            Icons.Filled.Timer,
                            contentDescription = "Timer",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            formatTime(state.elapsedSeconds),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }


                GameRenderer(
                    gameType = state.gameType,
                    board = state.board,
                    givenCells = state.givenCells,
                    cellStates = state.cellStates,
                    selectedCell = state.selectedCell,
                    conflictCells = state.conflictCells,
                    pencilNotes = state.pencilNotes,
                    isNotesMode = state.isNotesMode,
                    variantMetadata = state.variantMetadata,
                    highlightMistakes = true,
                    hapticsEnabled = true,
                    onCellClick = viewModel::selectCell,
                    onDigit = viewModel::enterDigit,
                    onErase = viewModel::eraseCell,
                    onToggleNotes = viewModel::toggleNotesMode,
                    digitCounts = viewModel.getDigitCounts(),
                    modifier = Modifier.fillMaxWidth()
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
