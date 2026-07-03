package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.ReelRepository
import com.example.ui.ReelTrackApp
import com.example.ui.ReelViewModel
import com.example.ui.ReelViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val database by lazy {
        AppDatabase.getDatabase(applicationContext)
    }

    private val repository by lazy {
        ReelRepository(database.reelDao())
    }

    private val viewModel: ReelViewModel by viewModels {
        ReelViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ReelTrackApp(viewModel = viewModel)
            }
        }
    }
}

