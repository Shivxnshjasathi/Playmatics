package com.zincstate.playmatics.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO matching the Supabase `matches` table columns.
 */
@Serializable
data class MatchDto(
    val id: String = "",
    @SerialName("room_code") val roomCode: String = "",
    val seed: Long = 0L,
    val difficulty: String = "",
    @SerialName("game_type") val gameType: String = "sudoku",
    val status: String = "waiting",
    @SerialName("host_id") val hostId: String = "",
    @SerialName("guest_id") val guestId: String? = null,
    @SerialName("winner_id") val winnerId: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("completed_at") val completedAt: String? = null
)
