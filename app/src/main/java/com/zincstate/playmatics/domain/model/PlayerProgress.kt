package com.zincstate.playmatics.domain.model

/**
 * A player's progress in a multiplayer match.
 * [solvedCount] = number of correctly filled cells out of 81.
 */
data class PlayerProgress(
    val playerId: String,
    val solvedCount: Int
)
