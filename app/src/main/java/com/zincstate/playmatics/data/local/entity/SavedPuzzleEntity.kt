package com.zincstate.playmatics.data.local.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "saved_puzzles")
data class SavedPuzzleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val seed: Long,
    val difficulty: String,             // "EASY" / "NORMAL" / "HARD"
    val givenCellsSnapshot: String,     // 81-char digit string
    val currentBoardSnapshot: String,   // 81-char digit string
    val pencilNotes: String,            // JSON Map<Int, Set<Int>>
    val elapsedSeconds: Long,
    val isCompleted: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
