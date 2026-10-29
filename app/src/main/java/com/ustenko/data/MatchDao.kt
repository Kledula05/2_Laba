package com.ustenko.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MatchDao {

    @Insert
    suspend fun insert(match: Match): Long

    @Query("SELECT * FROM matches ORDER BY playedAt DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<Match>
}
