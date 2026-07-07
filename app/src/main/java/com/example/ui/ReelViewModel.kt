package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DailyScroll
import com.example.data.Friend
import com.example.data.ReelRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReelViewModel(private val repository: ReelRepository) : ViewModel() {

    val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val todayScroll: StateFlow<DailyScroll?> = repository.getDailyScrollFlow(todayDate)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allFriends: StateFlow<List<Friend>> = repository.allFriends
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
