package com.example.data

import kotlinx.coroutines.flow.Flow

class ReelRepository(private val reelDao: ReelDao) {
    val allDailyScrolls: Flow<List<DailyScroll>> = reelDao.getAllDailyScrolls()
    val allFriends: Flow<List<Friend>> = reelDao.getAllFriends()
    val myProfile: Flow<Friend?> = reelDao.getMyProfileFlow()

    suspend fun getDailyScroll(date: String): DailyScroll? {
        return reelDao.getDailyScrollByDate(date)
    }

    fun getDailyScrollFlow(date: String): Flow<DailyScroll?> {
        return reelDao.getDailyScrollByDateFlow(date)
    }

    suspend fun saveDailyScroll(scroll: DailyScroll) {
        reelDao.insertOrUpdateDailyScroll(scroll)
        // Also update my stats in friends table to keep them in sync
        val currentMe = reelDao.getMyProfileDirect()
        if (currentMe != null) {
            reelDao.insertOrUpdateFriend(currentMe.copy(count = scroll.count))
        } else {
            reelDao.insertOrUpdateFriend(
                Friend(
                    id = "me",
                    name = "You",
                    count = scroll.count,
                    status = "Catching up...",
                    isMe = true
                )
            )
        }
    }

    suspend fun getMyProfileDirect(): Friend? {
        return reelDao.getMyProfileDirect()
    }

    suspend fun saveFriend(friend: Friend) {
        reelDao.insertOrUpdateFriend(friend)
    }

    suspend fun deleteFriend(friendId: String) {
        reelDao.deleteFriend(friendId)
    }
}
