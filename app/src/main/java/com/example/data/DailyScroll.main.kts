package com.example.data


import androidx.room.Entity
import androidx.room.PrimryKey


@Entity(tableName = "daily_scrolls")
data class DailyScroll(
    @PrimaryKey val data: String, //dates
    val count: Int,
    val timeSpentSeconds: Long = 0,
    val averageTimePerReel: Float = 18f // seconds
)