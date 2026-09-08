package com.zincstate.playmatics.presentation.singleplayer

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
import com.zincstate.playmatics.presentation.components.NumberPad
import com.zincstate.playmatics.presentation.components.ResultModal
import com.zincstate.playmatics.presentation.components.MatchResult
import com.zincstate.playmatics.presentation.components.SudokuBoard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SinglePlayerScreen(
    seed: Long,
    difficulty: String,
    gameType: String,
    onBack: () -> Unit,
    onSettings: () -> Unit,
    viewModel: SinglePlayerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val highlightMistakes by viewModel.highlightMistakes.collectAsState()
    val hapticsEnabled by viewModel.hapticsEnabled.collectAsState()

    LaunchedEffect(seed, difficulty) {
        viewModel.initGame(seed, difficulty, gameType)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "playmatics.",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = com.zincstate.playmatics.ui.theme.LogoGreen,
                        letterSpacing = (-0.5).sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        difficulty,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Timer,
                            contentDescription = "Timer",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 4.dp).size(18.dp)
                        )
                        Text(
                            formatTime(state.elapsedSeconds),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Text(
                        "${state.correctCount}/81",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                SudokuBoard(
                    board = state.board,
                    cellStates = state.cellStates,
                    selectedCell = state.selectedCell,
                    conflictCells = state.conflictCells,
                    pencilNotes = state.pencilNotes,
                    highlightMistakes = highlightMistakes,
                    variantMetadata = state.variantMetadata,
                    onCellClick = viewModel::selectCell,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                NumberPad(
                    digitCounts = remember(state.board) { viewModel.getDigitCounts() },
                    isNotesMode = state.isNotesMode,
                    hapticsEnabled = hapticsEnabled,
                    onDigit = viewModel::enterDigit,
                    onErase = viewModel::eraseCell,
                    onToggleNotes = viewModel::toggleNotesMode
                )
            }
        }

        // Completion dialog
        ResultModal(
            visible = state.isCompleted,
            result = MatchResult.WIN,
            yourTime = formatTime(state.elapsedSeconds),
            onHome = onBack
        )
    }
}

fun formatTime(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}
