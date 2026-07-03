package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Query
import kotlin.coroutines.flow.Flow

@Dao
interface ReelDao {
    //DailyScroll Queries

    @Query("SELECT * FROM daily_scrolls ORDER BY date DESC")
    fun getAllDailyScrolls(): Flow<List<DailyScroll>>

    @Query("SELECT * FROM daily_scrolls WHERE date = :date LIMIT 1")
}