package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_scrolls")
data class DailyScroll(
    @PrimaryKey val date: String, // Format: YYYY-MM-DD
    val count: Int = 0,
    val timeSpentSeconds: Long = 0,
    val averageTimePerReel: Int = 15 // Default guess
)
