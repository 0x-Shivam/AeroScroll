package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReelDao {
    @Query("SELECT * FROM daily_scrolls ORDER BY date DESC")
    fun getAllDailyScrolls(): Flow<List<DailyScroll>>

    @Query("SELECT * FROM daily_scrolls WHERE date = :date LIMIT 1")
    suspend fun getDailyScrollByDate(date: String): DailyScroll?

    @Query("SELECT * FROM daily_scrolls WHERE date = :date LIMIT 1")
    fun getDailyScrollByDateFlow(date: String): Flow<DailyScroll?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDailyScroll(scroll: DailyScroll)

    @Query("SELECT * FROM friends ORDER BY isMe DESC, count DESC")
    fun getAllFriends(): Flow<List<Friend>>

    @Query("SELECT * FROM friends WHERE isMe = 1 LIMIT 1")
    fun getMyProfileFlow(): Flow<Friend?>

    @Query("SELECT * FROM friends WHERE isMe = 1 LIMIT 1")
    suspend fun getMyProfileDirect(): Friend?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateFriend(friend: Friend)

    @Query("DELETE FROM friends WHERE id = :friendId")
    suspend fun deleteFriend(friendId: String)

    @Query("UPDATE friends SET count = 0")
    suspend fun resetAllFriendCounts()

    @Query("DELETE FROM daily_scrolls WHERE date < :date")
    suspend fun deleteOldScrolls(date: String)
}
