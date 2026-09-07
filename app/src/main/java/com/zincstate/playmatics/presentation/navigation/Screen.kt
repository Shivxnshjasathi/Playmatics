package com.zincstate.playmatics.presentation.navigation

import kotlinx.serialization.Serializable

/** Type-safe navigation routes. */
@Serializable
object Home

@Serializable
data class SinglePlayer(val seed: Long, val difficulty: String)

@Serializable
object CreateRoom

@Serializable
object JoinRoom

@Serializable
data class WaitingLobby(val matchId: String, val roomCode: String)

@Serializable
data class MultiplayerMatch(val matchId: String, val seed: Long, val difficulty: String)

@Serializable
object Stats

@Serializable
object Settings
