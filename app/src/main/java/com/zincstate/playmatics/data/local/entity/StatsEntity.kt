package com.zincstate.playmatics.data.local.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "stats")
data class StatsEntity(
    @PrimaryKey
    val id: Int = 1,
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val bestTimeEasyMs: Long = 0L,
    val bestTimeNormalMs: Long = 0L,
    val bestTimeHardMs: Long = 0L,
    val currentWinStreak: Int = 0,
    val longestWinStreak: Int = 0,
    val multiplayerMatchesPlayed: Int = 0,
    val multiplayerWins: Int = 0
)
