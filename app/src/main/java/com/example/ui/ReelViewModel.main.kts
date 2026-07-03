package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.DailyScroll
import com.example.data.Friend
import com.example.data.ReelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ReelViewModel(private val repository: ReelRepository) : ViewModel() {

    private val _currentDate = MutableStateFlow(getTodayDateString())
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    // Active Tab State (0 = Home, 1 = Stats, 2 = Friends, 3 = Settings)
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Current daily scroll state
    private val _todayScroll = MutableStateFlow<DailyScroll?>(null)
    val todayScroll: StateFlow<DailyScroll?> = _todayScroll.asStateFlow()

    // Security states
    private val _isPasscodeEnabled = MutableStateFlow(false)
    val isPasscodeEnabled: StateFlow<Boolean> = _isPasscodeEnabled.asStateFlow()

    private val _savedPasscode = MutableStateFlow("")
    val savedPasscode: StateFlow<String> = _savedPasscode.asStateFlow()

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    fun initSecurity(context: Context) {
        val prefs = context.getSharedPreferences("aeroscroll_prefs", Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean("passcode_enabled", false)
        val code = prefs.getString("passcode_value", "") ?: ""
        _isPasscodeEnabled.value = enabled
        _savedPasscode.value = code
        // If passcode is active, lock the app immediately on startup
        if (enabled && code.isNotEmpty()) {
            _isLocked.value = true
        }
    }

    fun setPasscode(context: Context, enabled: Boolean, code: String) {
        val prefs = context.getSharedPreferences("aeroscroll_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("passcode_enabled", enabled)
            putString("passcode_value", code)
            apply()
        }
        _isPasscodeEnabled.value = enabled
        _savedPasscode.value = code
        _isLocked.value = false
    }

    fun unlock(code: String): Boolean {
        return if (code == _savedPasscode.value) {
            _isLocked.value = false
            true
        } else {
            false
        }
    }

    fun lock() {
        if (_isPasscodeEnabled.value && _savedPasscode.value.isNotEmpty()) {
            _isLocked.value = true
        }
    }



    // All friends leaderboard
    val leaderboard: StateFlow<List<Friend>> = repository.allFriends
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Collect today's scroll record dynamically from the database
        viewModelScope.launch {
            val dateStr = getTodayDateString()
            repository.getDailyScrollFlow(dateStr).collect { updatedRecord ->
                if (updatedRecord != null) {
                    _todayScroll.value = updatedRecord
                }
            }
        }

        viewModelScope.launch {
            // Load or initialize today's scroll record
            val dateStr = getTodayDateString()
            var record = repository.getDailyScroll(dateStr)
            if (record == null) {
                record = DailyScroll(date = dateStr, count = 0)
                repository.updateDailyScroll(record)
            }
            _todayScroll.value = record

            // Ensure we have a "Me" profile in the Friends list
            val myProfile = repository.getMyProfileDirect()
            if (myProfile == null) {
                repository.saveFriend(
                    Friend(
                        id = "me",
                        name = "You",
                        count = record.count,
                        status = "Catching up...",
                        isMe = true
                    )
                )
            } else {
                // Sync current count
                if (myProfile.count != record.count) {
                    repository.saveFriend(myProfile.copy(count = record.count))
                }
            }

            // Populate mock friends (Alex Simmons & Maya Kapoor) if no friends exist
            val allFriendsList = repository.allFriends.first()
            val hasOtherFriends = allFriendsList.any { !it.isMe }
            if (!hasOtherFriends) {
                repository.saveFriend(
                    Friend(
                        id = "friend_alex",
                        name = "Alex Simmons",
                        count = 248,
                        status = "Master Scroller",
                        isMe = false
                    )
                )
                repository.saveFriend(
                    Friend(
                        id = "friend_maya",
                        name = "Maya Kapoor",
                        count = 121,
                        status = "Quiet observer",
                        isMe = false
                    )
                )
            }
        }
    }

    fun selectTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun updateScrollCount(delta: Int) {
        viewModelScope.launch {
            val current = _todayScroll.value ?: DailyScroll(date = getTodayDateString(), count = 0)
            val newCount = (current.count + delta).coerceAtLeast(0)

            // Adjust estimated time (e.g. average of 18 seconds per reel)
            val newTimeSpent = newCount * current.averageTimePerReel.toLong()

            val updated = current.copy(
                count = newCount,
                timeSpentSeconds = newTimeSpent
            )
            repository.updateDailyScroll(updated)
            _todayScroll.value = updated

            // Update status based on count
            val myProfile = repository.getMyProfileDirect() ?: Friend(id = "me", name = "You", count = newCount, status = "Catching up...", isMe = true)
            val newStatus = when {
                newCount > 200 -> "Master Scroller 🔥"
                newCount > 100 -> "Active Competitor ⚡"
                newCount > 50 -> "Decent Scroller 👍"
                else -> "Catching up..."
            }
            repository.saveFriend(myProfile.copy(count = newCount, status = newStatus))
        }
    }


    fun addManualFriend(name: String, initialCount: Int) {
        viewModelScope.launch {
            val newFriend = Friend(
                id = "manual_${UUID.randomUUID()}",
                name = name,
                count = initialCount,
                status = "Challenger ⚔️",
                isMe = false
            )
            repository.saveFriend(newFriend)
        }
    }

    fun removeFriend(id: String) {
        viewModelScope.launch {
            repository.deleteFriend(id)
        }
    }

    // Parse and handle share links
    // Format: aeroscroll://join/squad?name=Name&count=142&status=Master
    fun importFriendFromLink(url: String): Boolean {
        return try {
            if (!url.startsWith("aeroscroll://") && !url.contains("join/squad")) return false
            val queryStr = url.substringAfter("?")
            val params = queryStr.split("&").associate {
                val parts = it.split("=")
                parts[0] to java.net.URLDecoder.decode(parts[1], "UTF-8")
            }
            val name = params["name"] ?: return false
            val count = params["count"]?.toIntOrNull() ?: 0
            val status = params["status"] ?: "Squad Mate 👥"
            val id = params["id"] ?: "friend_${UUID.randomUUID()}"

            viewModelScope.launch {
                repository.saveFriend(
                    Friend(
                        id = id,
                        name = name,
                        count = count,
                        status = status,
                        isMe = false,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


