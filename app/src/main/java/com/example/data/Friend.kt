package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friends")
data class Friend(
    @PrimaryKey val id: String,
    val name: String,
    val count: Int,
    val status: String,
    val isMe: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)
