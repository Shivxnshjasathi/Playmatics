package com.zincstate.playmatics.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
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
    val difficultyOptions = listOf(Difficulty.EASY, Difficulty.NORMAL, Difficulty.HARD)
    var activeGame by rememberSaveable(stateSaver = androidx.compose.runtime.saveable.Saver(
        save = { it.key },
        restore = { GameType.fromKey(it) }
    )) { mutableStateOf(GameType.SUDOKU) }
    val allGames = GameType.entries

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    PlaymaticsLogo()
                },
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
            Text(
                "Games",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Game category chips in a horizontal scroll row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allGames.forEach { game ->
                    GameCategoryChip(
                        game = game,
                        isSelected = activeGame == game,
                        onClick = { activeGame = game }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Play Offline card ──────────────────────────────────
            GameModeCard(
                tag = activeGame.displayName,
                tagColor = LogoGreen,
                tagTextColor = Color.Black,
                title = "Play\nOffline",
                subtitle = activeGame.description,
                arrowColor = LogoGreen,
                enabled = true,
                onClick = { showDifficultyDialog = true },
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── MULTIPLAYER section ────────────────────────────────
            Text(
                "Multiplayer",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Create Room card
            GameModeCard(
                tag = "1v1",
                tagColor = MaterialTheme.colorScheme.primary,
                tagTextColor = MaterialTheme.colorScheme.onPrimary,
                title = "Create\nRoom",
                subtitle = "Host a match for a friend",
                arrowColor = MaterialTheme.colorScheme.primary,
                enabled = isOnline,
                onClick = { onCreateRoom(activeGame.key) },
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Join Room card
            GameModeCard(
                tag = "Join",
                tagColor = MaterialTheme.colorScheme.primary,
                tagTextColor = MaterialTheme.colorScheme.onPrimary,
                title = "Join\nRoom",
                subtitle = "Enter a room code to play",
                arrowColor = MaterialTheme.colorScheme.primary,
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            activeGame.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = LogoGreen,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Select Difficulty",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { showDifficultyDialog = false }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Difficulty cards
                DifficultyOptionCard(
                    label = "Easy",
                    description = "Relaxed pace, more hints",
                    accentColor = LogoGreen,
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
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) LogoGreen.copy(alpha = 0.12f)
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .width(60.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    brush = if (isSelected) Brush.linearGradient(listOf(LogoGreen, LogoGreen.copy(alpha=0.6f)))
                            else Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant)),
                    shape = RoundedCornerShape(16.dp)
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
                tint = if (isSelected) Color.Black
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            game.displayName,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) LogoGreen
            else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 9.sp
        )
    }
}

// ── Game Mode Card ─────────────────────────────────────────────────────
@Composable
private fun GameModeCard(
    tag: String,
    tagColor: Color,
    tagTextColor: Color,
    title: String,
    subtitle: String,
    arrowColor: Color,
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .padding(24.dp)
        ) {
            Column {
            // Tag badge
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = tagColor.copy(alpha = if (enabled) 1f else 0.4f)
            ) {
                Text(
                    tag,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = tagTextColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 34.sp,
                        letterSpacing = (-0.5).sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = contentAlpha * 0.8f
                        ),
                        letterSpacing = 0.5.sp
                    )
                }

                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = "Go",
                    tint = arrowColor.copy(alpha = if (enabled) 1f else 0.3f),
                    modifier = Modifier.size(32.dp)
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
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Accent icon box
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = accentColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Label + description
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Arrow
            Icon(
                Icons.Filled.PlayArrow,
                contentDescription = "Play $label",
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
