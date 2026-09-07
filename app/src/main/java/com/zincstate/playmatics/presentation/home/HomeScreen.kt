package com.zincstate.playmatics.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zincstate.playmatics.data.remote.ConnectivityObserver
import com.zincstate.playmatics.domain.engine.Difficulty
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onPlaySingle: (seed: Long, difficulty: Difficulty) -> Unit,
    onCreateRoom: () -> Unit,
    onJoinRoom: () -> Unit,
    onStats: () -> Unit,
    onSettings: () -> Unit,
    connectivityObserver: ConnectivityObserver? = null
) {
    val isOnline = connectivityObserver?.isOnline?.collectAsState()?.value ?: false
    var selectedDifficulty by remember { mutableStateOf(Difficulty.NORMAL) }
    val difficultyOptions = listOf(Difficulty.EASY, Difficulty.NORMAL, Difficulty.HARD)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { /* TODO Menu */ }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {}, // Replaced with floating nav bar inside Box
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo
                Text(
                    text = "playmatics.",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = com.zincstate.playmatics.ui.theme.LogoGreen, // Always green as requested
                    letterSpacing = (-1.5).sp,
                    modifier = Modifier.padding(bottom = 64.dp)
                )

            // Difficulty Selector
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                difficultyOptions.forEachIndexed { index, diff ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = difficultyOptions.size),
                        onClick = { selectedDifficulty = diff },
                        selected = diff == selectedDifficulty,
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            activeContentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(diff.name.lowercase().replaceFirstChar { it.uppercase() })
                    }
                }
            }

            // Play Offline Button (like "New Game")
            Button(
                onClick = { onPlaySingle(Random.nextLong(), selectedDifficulty) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text("Play Offline", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Create Room Button (like "How to Play ?")
            OutlinedButton(
                onClick = onCreateRoom,
                enabled = isOnline,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = if (isOnline) "Create Room" else "Create Room (Offline)", 
                    fontSize = 18.sp, 
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Join Room Button
            OutlinedButton(
                onClick = onJoinRoom,
                enabled = isOnline,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = if (isOnline) "Join Room" else "Join Room (Offline)", 
                    fontSize = 18.sp, 
                    fontWeight = FontWeight.SemiBold
                )
            }
            }
            
            // Custom Floating Nav Bar
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(32.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {}) {
                    Icon(
                        Icons.Filled.Home, 
                        contentDescription = "Home", 
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onStats) {
                    Icon(
                        Icons.Filled.Leaderboard, 
                        contentDescription = "Stats", 
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
