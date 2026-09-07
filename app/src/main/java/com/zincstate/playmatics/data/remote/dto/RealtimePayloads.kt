package com.zincstate.playmatics.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Broadcast: player progress update. */
@Serializable
data class ProgressEvent(
    @SerialName("player_id") val playerId: String,
    @SerialName("solved_count") val solvedCount: Int
)

/** Broadcast: match start synchronized timestamp. */
@Serializable
data class MatchStartEvent(
    val startAtEpochMillis: Long
)

/** Broadcast: player forfeit notification. */
@Serializable
data class ForfeitEvent(
    @SerialName("player_id") val playerId: String
)

/** Presence: player info tracked in the channel. */
@Serializable
data class PlayerPresence(
    @SerialName("user_id") val userId: String,
    @SerialName("display_name") val displayName: String = "Player"
)
