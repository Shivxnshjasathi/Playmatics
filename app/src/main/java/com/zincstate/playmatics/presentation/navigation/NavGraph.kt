package com.zincstate.playmatics.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.zincstate.playmatics.presentation.home.HomeScreen
import com.zincstate.playmatics.presentation.multiplayer.lobby.CreateRoomScreen
import com.zincstate.playmatics.presentation.multiplayer.lobby.JoinRoomScreen
import com.zincstate.playmatics.presentation.multiplayer.lobby.WaitingLobbyScreen
import com.zincstate.playmatics.presentation.multiplayer.match.MultiplayerMatchScreen
import com.zincstate.playmatics.presentation.settings.SettingsScreen
import com.zincstate.playmatics.presentation.singleplayer.SinglePlayerScreen
import com.zincstate.playmatics.presentation.stats.StatsScreen
import com.zincstate.playmatics.presentation.about.AboutScreen
import com.zincstate.playmatics.presentation.comingsoon.ComingSoonScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    connectivityObserver: com.zincstate.playmatics.data.remote.ConnectivityObserver? = null,
    modifier: Modifier = Modifier
) {
    // Track current route to show/hide the nav bar
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute by remember(navBackStackEntry) {
        derivedStateOf {
            navBackStackEntry?.destination?.route ?: ""
        }
    }

    // Show nav bar only on these main screens
    val showNavBar by remember(currentRoute) {
        derivedStateOf {
            currentRoute.contains("Home") ||
            currentRoute.contains("Stats") ||
            currentRoute.contains("Settings")
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Home
        ) {
            composable<Home> {
                HomeScreen(
                    onPlaySingle = { seed, difficulty, gameType ->
                        navController.navigate(SinglePlayer(seed, difficulty.name, gameType))
                    },
                    onCreateRoom = { gameType ->
                        navController.navigate(CreateRoom(gameType))
                    },
                    onJoinRoom = {
                        navController.navigate(JoinRoom)
                    },
                    onStats = {
                        navController.navigate(Stats)
                    },
                    onSettings = { gameType ->
                        navController.navigate(Settings(gameType))
                    },
                    onAbout = {
                        navController.navigate(About)
                    },
                    onComingSoonClick = { gameName ->
                        navController.navigate(ComingSoon(gameName))
                    },
                    connectivityObserver = connectivityObserver
                )
            }

            composable<SinglePlayer> { backStackEntry ->
                val route = backStackEntry.toRoute<SinglePlayer>()
                SinglePlayerScreen(
                    seed = route.seed,
                    difficulty = route.difficulty,
                    gameType = route.gameType,
                    onBack = { navController.popBackStack() },
                    onSettings = { navController.navigate(Settings(route.gameType)) }
                )
            }

            composable<CreateRoom> { backStackEntry ->
                val route = backStackEntry.toRoute<CreateRoom>()
                CreateRoomScreen(
                    gameType = route.gameType,
                    onRoomCreated = { matchId, roomCode ->
                        navController.navigate(WaitingLobby(matchId, roomCode, route.gameType)) {
                            popUpTo<CreateRoom> { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<JoinRoom> {
                JoinRoomScreen(
                    onMatchJoined = { matchId, seed, difficulty, gameType ->
                        navController.navigate(MultiplayerMatch(matchId, seed, difficulty, gameType)) {
                            popUpTo<Home>()
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<WaitingLobby> { backStackEntry ->
                val route = backStackEntry.toRoute<WaitingLobby>()
                WaitingLobbyScreen(
                    matchId = route.matchId,
                    roomCode = route.roomCode,
                    onMatchStarted = { matchId, seed, difficulty, gameType ->
                        navController.navigate(MultiplayerMatch(matchId, seed, difficulty, gameType)) {
                            popUpTo<Home>()
                        }
                    },
                    onCancel = { navController.popBackStack<Home>(inclusive = false) }
                )
            }

            composable<MultiplayerMatch> { backStackEntry ->
                val route = backStackEntry.toRoute<MultiplayerMatch>()
                MultiplayerMatchScreen(
                    matchId = route.matchId,
                    seed = route.seed,
                    difficulty = route.difficulty,
                    gameType = route.gameType,
                    onHome = { navController.popBackStack<Home>(inclusive = false) },
                    onSettings = { navController.navigate(Settings(route.gameType)) }
                )
            }

            composable<Stats> {
                StatsScreen(onBack = { navController.popBackStack() })
            }

            composable<Settings> { backStackEntry ->
                val route = backStackEntry.toRoute<Settings>()
                SettingsScreen(
                    gameType = route.gameType,
                    onBack = { navController.popBackStack() },
                    onAbout = { navController.navigate(About) }
                )
            }

            composable<About> {
                AboutScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable<ComingSoon> { backStackEntry ->
                val route = backStackEntry.toRoute<ComingSoon>()
                ComingSoonScreen(
                    gameName = route.gameName,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        // ── Floating bottom nav bar (visible on Home, Stats, About) ────
        AnimatedVisibility(
            visible = showNavBar,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        RoundedCornerShape(32.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isHome = currentRoute.contains("Home")
                val isStats = currentRoute.contains("Stats")
                val isSettings = currentRoute.contains("Settings")
                val isAbout = currentRoute.contains("About")

                IconButton(onClick = {
                    if (!isHome) {
                        navController.navigate(Home) {
                            popUpTo<Home> { inclusive = true }
                        }
                    }
                }) {
                    Icon(
                        Icons.Filled.Home,
                        contentDescription = "Home",
                        tint = if (isHome) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = {
                    if (!isStats) {
                        navController.navigate(Stats) {
                            popUpTo<Home>()
                        }
                    }
                }) {
                    Icon(
                        Icons.Filled.Leaderboard,
                        contentDescription = "Stats",
                        tint = if (isStats) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = {
                    if (!isSettings) {
                        navController.navigate(com.zincstate.playmatics.presentation.navigation.Settings()) {
                            popUpTo<Home>()
                        }
                    }
                }) {
                    Icon(
                        Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = if (isSettings) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
