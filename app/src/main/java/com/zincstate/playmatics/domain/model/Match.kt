package com.zincstate.playmatics.domain.model

import com.zincstate.playmatics.domain.engine.Difficulty

/**
 * Domain model for a multiplayer match (mirrors the Supabase `matches` row).
 */
data class Match(
    val id: String,
    val roomCode: String,
    val seed: Long,
    val difficulty: Difficulty,
    val status: MatchStatus,
    val hostId: String,
    val guestId: String? = null,
    val winnerId: String? = null,
    val createdAt: Long = 0L,
    val startedAt: Long? = null,
    val completedAt: Long? = null
)
