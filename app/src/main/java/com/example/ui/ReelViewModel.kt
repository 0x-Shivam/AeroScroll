package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DailyScroll
import com.example.data.Friend
import com.example.data.ReelRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ReelViewModel(private val repository: ReelRepository) : ViewModel() {

    val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private val _timeUntilReset = MutableStateFlow("")
    val timeUntilReset: StateFlow<String> = _timeUntilReset

    val todayScroll: StateFlow<DailyScroll?> = repository.getDailyScrollFlow(todayDate)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allFriends: StateFlow<List<Friend>> = repository.allFriends
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            // If we don't have a record for today yet, it's a new day
            // Reset friend counts for the new daily competition
            val scroll = repository.getDailyScroll(todayDate)
            if (scroll == null) {
                repository.resetDailyData(todayDate)
            }
        }
        startResetTimer()
    }

    private fun startResetTimer() {
        viewModelScope.launch {
            while (true) {
                val now = Calendar.getInstance()
                val midnight = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val diff = midnight.timeInMillis - now.timeInMillis
                val hours = diff / (1000 * 60 * 60)
                val minutes = (diff / (1000 * 60)) % 60
                val seconds = (diff / 1000) % 60

                _timeUntilReset.value = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                
                // If it's exactly midnight, we could trigger a refresh
                if (hours == 0L && minutes == 0L && seconds == 0L) {
                    // Refresh logic if needed
                }

                delay(1000)
            }
        }
    }

    fun incrementReelCount() {
        viewModelScope.launch {
            val current = todayScroll.value ?: DailyScroll(date = todayDate)
            repository.saveDailyScroll(current.copy(count = current.count + 1))
        }
    }

    fun addFriend(name: String, id: String) {
        viewModelScope.launch {
            repository.saveFriend(Friend(id = id, name = name, count = 0, status = "New friend"))
        }
    }

    fun removeFriend(id: String) {
        viewModelScope.launch {
            repository.deleteFriend(id)
        }
    }
}
