package com.ustenko.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class Match(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val player1Name: String,
    val player2Name: String,
    val score1: Int,
    val score2: Int,
    val targetScore: Int,
    val avgReaction1: Long,
    val avgReaction2: Long,
    val bestReaction: Long,
    val falseStarts: String,
    val playedAt: Long
)
