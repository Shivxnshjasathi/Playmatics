package com.zincstate.playmatics.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.zincstate.playmatics.presentation.home.HomeScreen
import com.zincstate.playmatics.presentation.multiplayer.lobby.CreateRoomScreen
import com.zincstate.playmatics.presentation.multiplayer.lobby.JoinRoomScreen
import com.zincstate.playmatics.presentation.multiplayer.lobby.WaitingLobbyScreen
import com.zincstate.playmatics.presentation.multiplayer.match.MultiplayerMatchScreen
import com.zincstate.playmatics.presentation.settings.SettingsScreen
import com.zincstate.playmatics.presentation.singleplayer.SinglePlayerScreen
import com.zincstate.playmatics.presentation.stats.StatsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    connectivityObserver: com.zincstate.playmatics.data.remote.ConnectivityObserver? = null,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Home,
        modifier = modifier
    ) {
        composable<Home> {
            HomeScreen(
                onPlaySingle = { seed, difficulty ->
                    navController.navigate(SinglePlayer(seed, difficulty.name))
                },
                onCreateRoom = {
                    navController.navigate(CreateRoom)
                },
                onJoinRoom = {
                    navController.navigate(JoinRoom)
                },
                onStats = {
                    navController.navigate(Stats)
                },
                onSettings = {
                    navController.navigate(Settings)
                },
                connectivityObserver = connectivityObserver
            )
        }

        composable<SinglePlayer> { backStackEntry ->
            val route = backStackEntry.toRoute<SinglePlayer>()
            SinglePlayerScreen(
                seed = route.seed,
                difficulty = route.difficulty,
                onBack = { navController.popBackStack() }
            )
        }

        composable<CreateRoom> {
            CreateRoomScreen(
                onRoomCreated = { matchId, roomCode ->
                    navController.navigate(WaitingLobby(matchId, roomCode)) {
                        popUpTo<CreateRoom> { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable<JoinRoom> {
            JoinRoomScreen(
                onMatchJoined = { matchId, seed, difficulty ->
                    navController.navigate(MultiplayerMatch(matchId, seed, difficulty)) {
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
                onMatchStarted = { matchId, seed, difficulty ->
                    navController.navigate(MultiplayerMatch(matchId, seed, difficulty)) {
                        popUpTo<Home>()
                    }
                },
                onCancel = { navController.popBackStack(Home, false) }
            )
        }

        composable<MultiplayerMatch> { backStackEntry ->
            val route = backStackEntry.toRoute<MultiplayerMatch>()
            MultiplayerMatchScreen(
                matchId = route.matchId,
                seed = route.seed,
                difficulty = route.difficulty,
                onHome = { navController.popBackStack(Home, false) }
            )
        }

        composable<Stats> {
            StatsScreen(onBack = { navController.popBackStack() })
        }

        composable<Settings> {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
