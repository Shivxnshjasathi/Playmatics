package com.zincstate.playmatics.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Abc
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zincstate.playmatics.data.remote.ConnectivityObserver
import com.zincstate.playmatics.domain.engine.Difficulty
import com.zincstate.playmatics.domain.engine.GameType
import com.zincstate.playmatics.presentation.components.PlaymaticsLogo
import com.zincstate.playmatics.ui.theme.LogoGreen
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
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val difficultyOptions = listOf(Difficulty.EASY, Difficulty.NORMAL, Difficulty.HARD)
    var activeGame by rememberSaveable(stateSaver = androidx.compose.runtime.saveable.Saver(
        save = { it.key },
        restore = { GameType.fromKey(it) }
    )) { mutableStateOf(GameType.SUDOKU) }
    val allGames = GameType.entries

    val primaryGradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp),
                drawerShape = RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, top = 48.dp, bottom = 24.dp)
                ) {
                    PlaymaticsLogo()
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your Puzzle Playground",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        letterSpacing = 0.5.sp
                    )
                }
                
                HorizontalDivider(
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                NavigationDrawerItem(
                    label = { Text("Stats", fontWeight = FontWeight.Medium) },
                    icon = { Icon(Icons.Filled.Leaderboard, contentDescription = null) },
                    selected = false,
                    onClick = { 
                        scope.launch { drawerState.close() }
                        onStats() 
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Settings", fontWeight = FontWeight.Medium) },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    selected = false,
                    onClick = { 
                        scope.launch { drawerState.close() }
                        onSettings(activeGame.key) 
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("About", fontWeight = FontWeight.Medium) },
                    icon = { Icon(Icons.Filled.Info, contentDescription = null) },
                    selected = false,
                    onClick = { 
                        scope.launch { drawerState.close() }
                        onAbout() 
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        PlaymaticsLogo()
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Menu,
                                        contentDescription = "Menu",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    },
                    actions = {},
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // ── GAMES section ──────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "GAMES",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    "View All",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Game category chips in a horizontal scroll row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                allGames.forEach { game ->
                    GameCategoryChip(
                        game = game,
                        isSelected = activeGame == game,
                        primaryGradient = primaryGradient,
                        onClick = { activeGame = game }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Play Offline card ──────────────────────────────────
            PlayOfflineCard(
                game = activeGame,
                primaryGradient = primaryGradient,
                onClick = { showDifficultyDialog = true },
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── MULTIPLAYER section ────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "MULTIPLAYER",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    "Real-time duels",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Create Room card
            MultiplayerCard(
                tag = "1V1",
                title = "Create\nRoom",
                subtitle = "Host a private match for a friend or rival",
                icon = Icons.Filled.PersonAdd,
                bottomInfo = "5 Min Blitz / Untimed",
                actionText = "Setup Lobby \u2192",
                enabled = isOnline,
                onClick = { onCreateRoom(activeGame.key) },
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Join Room card
            MultiplayerCard(
                tag = "JOIN",
                title = "Join\nRoom",
                subtitle = "Enter a room code to play with a friend",
                icon = Icons.Filled.PersonOutline,
                bottomInfo = "Any Mode / Speed",
                actionText = "Enter Code \u2192",
                enabled = isOnline,
                onClick = onJoinRoom,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            if (!isOnline) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Go online to play multiplayer",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
    }


    // ── Difficulty picker dialog ───────────────────────────────────────
    if (showDifficultyDialog) {
        val sheetState = androidx.compose.material3.rememberModalBottomSheetState()
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { showDifficultyDialog = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .padding(bottom = 24.dp)
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        // Game name badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                activeGame.displayName.uppercase(),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            "Select Difficulty",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(40.dp)
                    ) {
                        IconButton(onClick = { showDifficultyDialog = false }) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Difficulty cards
                DifficultyOptionCard(
                    label = "Easy",
                    description = "Relaxed pace, more hints",
                    accentColor = MaterialTheme.colorScheme.primary,
                    icon = Icons.Filled.SentimentSatisfied,
                    onClick = {
                        showDifficultyDialog = false
                        onPlaySingle(Random.nextLong(), Difficulty.EASY, activeGame.key)
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                DifficultyOptionCard(
                    label = "Normal",
                    description = "Balanced challenge",
                    accentColor = Color(0xFFF59E0B),
                    icon = Icons.Filled.Speed,
                    onClick = {
                        showDifficultyDialog = false
                        onPlaySingle(Random.nextLong(), Difficulty.NORMAL, activeGame.key)
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                DifficultyOptionCard(
                    label = "Hard",
                    description = "For puzzle masters",
                    accentColor = Color(0xFFEF4444),
                    icon = Icons.Filled.LocalFireDepartment,
                    onClick = {
                        showDifficultyDialog = false
                        onPlaySingle(Random.nextLong(), Difficulty.HARD, activeGame.key)
                    }
                )
            }
        }
    }
}

// ── Game Category Chip ─────────────────────────────────────────────────
@Composable
private fun GameCategoryChip(
    game: GameType,
    isSelected: Boolean,
    primaryGradient: Brush,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .width(64.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    brush = if (isSelected) primaryGradient
                    else Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant)),
                    shape = RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            val icon: ImageVector = when (game) {
                GameType.SUDOKU -> Icons.Filled.GridOn
                GameType.WORDOKU -> Icons.Filled.Star
                GameType.KENKEN -> Icons.Filled.Functions
                GameType.CROSSWORD -> Icons.Filled.Abc
                GameType.WORD_SEARCH -> Icons.Filled.Search
            }
            Icon(
                icon,
                contentDescription = game.displayName,
                tint = if (isSelected) Color.White
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.6f),
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            game.displayName,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.8f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 11.sp
        )
    }
}

// ── Play Offline Card ──────────────────────────────────────────────────
@Composable
private fun PlayOfflineCard(
    game: GameType,
    primaryGradient: Brush,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(primaryGradient)
        ) {


            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ) {
                    Text(
                        game.displayName.uppercase(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Play\nOffline",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 36.sp,
                    letterSpacing = (-0.5).sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    game.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(MaterialTheme.colorScheme.secondary, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Casual • Medium • Hard",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Multiplayer Card ───────────────────────────────────────────────────
@Composable
private fun MultiplayerCard(
    tag: String,
    title: String,
    subtitle: String,
    icon: ImageVector,
    bottomInfo: String,
    actionText: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentAlpha = if (enabled) 1f else 0.4f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.3f))
    ) {
        Box(
            modifier = Modifier.padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Tag badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            tag,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.background.copy(alpha=0.5f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 32.sp,
                    letterSpacing = (-0.5).sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha * 0.8f),
                    letterSpacing = 0.5.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            bottomInfo,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
                        )
                    }
                    
                    Text(
                        actionText,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = contentAlpha),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ── Difficulty Option Card ──────────────────────────────────────────────
@Composable
private fun DifficultyOptionCard(
    label: String,
    description: String,
    accentColor: Color,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Accent icon box
            Surface(
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.15f),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = label,
                        tint = accentColor,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // Label + description
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            // Arrow
            Surface(
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.1f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = "Play $label",
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
