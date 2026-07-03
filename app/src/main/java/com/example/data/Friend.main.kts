package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friends")
data class Friend(
    @PrimaryKey val id: String, // user-custom ID or share code
    val name: String,
    val count: Int,
    val status: String,
    val isMe: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)


