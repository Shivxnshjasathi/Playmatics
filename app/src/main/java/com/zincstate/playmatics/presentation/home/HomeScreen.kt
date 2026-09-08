package com.zincstate.playmatics.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Abc
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zincstate.playmatics.data.remote.ConnectivityObserver
import com.zincstate.playmatics.domain.engine.Difficulty
import com.zincstate.playmatics.domain.engine.GameType
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onPlaySingle: (seed: Long, difficulty: Difficulty, gameType: String) -> Unit,
    onCreateRoom: (gameType: String) -> Unit,
    onJoinRoom: () -> Unit,
    onStats: () -> Unit,
    onSettings: (String) -> Unit,
    onAbout: () -> Unit,
    onComingSoonClick: (String) -> Unit,
    connectivityObserver: ConnectivityObserver? = null
) {
    val isOnline = connectivityObserver?.isOnline?.collectAsState()?.value ?: false
    var showDifficultyDialog by remember { mutableStateOf(false) }
    val difficultyOptions = listOf(Difficulty.EASY, Difficulty.NORMAL, Difficulty.HARD)
    
    var activeGame by remember { mutableStateOf(GameType.SUDOKU) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.background,
                drawerContentColor = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "playmatics.",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = com.zincstate.playmatics.ui.theme.LogoGreen,
                            letterSpacing = (-1.0).sp,
                            modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp)
                        )
                        
                        // SUDOKU FAMILY
                        Text("SUDOKU FAMILY", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 28.dp, bottom = 8.dp))
                        val sudokuGames = listOf(
                            GameType.SUDOKU, GameType.KILLER_SUDOKU, GameType.DIAGONAL_SUDOKU,
                            GameType.JIGSAW_SUDOKU, GameType.WINDOKU, GameType.CONSECUTIVE_SUDOKU,
                            GameType.ODD_EVEN_SUDOKU, GameType.WORDOKU, GameType.SAMURAI_SUDOKU
                        )
                        sudokuGames.forEach { game ->
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Filled.GridOn, contentDescription = game.displayName) },
                                label = { 
                                    Text(
                                        text = if (activeGame == game) "${game.displayName} (Active)" else game.displayName, 
                                        fontWeight = if (activeGame == game) FontWeight.Bold else FontWeight.Normal,
                                        style = MaterialTheme.typography.bodyMedium
                                    ) 
                                },
                                selected = activeGame == game,
                                onClick = { 
                                    activeGame = game
                                    scope.launch { drawerState.close() }
                                },
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp))
                        
                        // NUMBER GRIDS
                        Text("NUMBER GRIDS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 28.dp, bottom = 8.dp))
                        val numberGames = listOf(
                            GameType.KAKURO, GameType.KENKEN, GameType.FUTOSHIKI,
                            GameType.SKYSCRAPERS, GameType.STR8TS, GameType.NUMBRIX_HIDATO
                        )
                        numberGames.forEach { game ->
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Filled.Numbers, contentDescription = game.displayName) },
                                label = { 
                                    Text(
                                        text = if (activeGame == game) "${game.displayName} (Active)" else game.displayName, 
                                        fontWeight = if (activeGame == game) FontWeight.Bold else FontWeight.Normal,
                                        style = MaterialTheme.typography.bodyMedium
                                    ) 
                                },
                                selected = activeGame == game,
                                onClick = { 
                                    activeGame = game
                                    scope.launch { drawerState.close() }
                                },
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp))

                        // DRAWING PUZZLES
                        Text("DRAWING PUZZLES", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 28.dp, bottom = 8.dp))
                        val drawingGames = listOf("Nonograms (Picross)", "Slitherlink", "Nurikabe", "Hashiwokakero", "Masyu", "Light Up (Akari)", "Tents and Trees", "Shikaku", "Fillomino")
                        drawingGames.forEach { game ->
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Filled.Create, contentDescription = game) },
                                label = { Text(game, style = MaterialTheme.typography.bodyMedium) },
                                badge = { Text("Soon", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                selected = false,
                                onClick = { 
                                    scope.launch { drawerState.close() }
                                    onComingSoonClick(game)
                                },
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp))

                        // WORD GAMES
                        Text("WORD GAMES", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 28.dp, bottom = 8.dp))
                        val wordGames = listOf("Word Search", "Mini Crossword", "Boggle", "Anagram Scramble", "Wordle Daily")
                        wordGames.forEach { game ->
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Filled.Abc, contentDescription = game) },
                                label = { Text(game, style = MaterialTheme.typography.bodyMedium) },
                                badge = { Text("Soon", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                selected = false,
                                onClick = { 
                                    scope.launch { drawerState.close() }
                                    onComingSoonClick(game)
                                },
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp))

                        // CLASSIC MINIGAMES
                        Text("CLASSIC MINIGAMES", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 28.dp, bottom = 8.dp))
                        val minigames = listOf("Minesweeper", "2048", "Lights Out", "Memory Match", "Peg Solitaire")
                        minigames.forEach { game ->
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Filled.Gamepad, contentDescription = game) },
                                label = { Text(game, style = MaterialTheme.typography.bodyMedium) },
                                badge = { Text("Soon", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                selected = false,
                                onClick = { 
                                    scope.launch { drawerState.close() }
                                    onComingSoonClick(game)
                                },
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(modifier = Modifier.height(16.dp))

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.Info, contentDescription = "About Us") },
                        label = { Text("About Us", fontWeight = FontWeight.SemiBold) },
                        selected = false,
                        onClick = { 
                            scope.launch { drawerState.close() }
                            onAbout()
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "playmatics.",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = com.zincstate.playmatics.ui.theme.LogoGreen,
                            letterSpacing = (-1.0).sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onBackground)
                        }
                    },
                    actions = {
                        IconButton(onClick = { onSettings(activeGame.key) }) {
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
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.weight(1f))

                    // Selected Game Title
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = activeGame.displayName,
                            fontSize = if (activeGame.displayName.length > 14) 28.sp else 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            letterSpacing = (-1.0).sp,
                            maxLines = 1,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Select a mode to begin",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 48.dp)
                        )
                    }

                    // Play Offline Button
                    Button(
                        onClick = { showDifficultyDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 8.dp,
                            pressedElevation = 2.dp
                        )
                    ) {
                        Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Play Offline", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Multiplayer Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Create Room Button
                        OutlinedButton(
                            onClick = { onCreateRoom(activeGame.key) },
                            enabled = isOnline,
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary,
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.AddCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Create",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Join Room Button
                        OutlinedButton(
                            onClick = onJoinRoom,
                            enabled = isOnline,
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary,
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.Group, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Join",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1.5f))
                }

                // Custom Floating Nav Bar
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(32.dp)
                        )
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(32.dp))
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

    if (showDifficultyDialog) {
        AlertDialog(
            onDismissRequest = { showDifficultyDialog = false },
            title = { 
                Text("Select Difficulty", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) 
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    difficultyOptions.forEach { diff ->
                        Button(
                            onClick = { 
                                showDifficultyDialog = false
                                onPlaySingle(Random.nextLong(), diff, activeGame.key) 
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(diff.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            confirmButton = {},
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}
