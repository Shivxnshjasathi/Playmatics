package com.zincstate.playmatics.presentation.navigation

import kotlinx.serialization.Serializable

/** Type-safe navigation routes. */
@Serializable
object Home

@Serializable
data class SinglePlayer(val seed: Long, val difficulty: String, val gameType: String)

@Serializable
data class CreateRoom(val gameType: String)

@Serializable
object JoinRoom

@Serializable
data class WaitingLobby(val matchId: String, val roomCode: String, val gameType: String)

@Serializable
data class MultiplayerMatch(
    val matchId: String,
    val seed: Long,
    val difficulty: String,
    val gameType: String
)

@Serializable
object Stats

@Serializable
data class Settings(val gameType: String? = null)

@Serializable
object About

@Serializable
data class ComingSoon(val gameName: String)
