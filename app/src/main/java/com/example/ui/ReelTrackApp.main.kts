package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foun
import java.nio.file.WatchEvent
dation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Setting
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.DisposableEffect
import com.example.data.DailyScroll
import com.example.data.Friend
import com.example.ui.theme.SleekAccent
import com.example.ui.theme.SleekBackground
import com.example.ui.theme.SleekOnSecondaryContainer
import com.example.ui.theme.SleekOnSurfaceVariant
import com.example.ui.theme.SleekOutline
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekSecondaryContainer
import com.example.ui.theme.SleekSelectedContainer
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.SleekText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

    // Mock data list for simulated reels swiper
        data class MockReel(
    val creator: String,
    val description: String,
    val likes: String,
    val comments: String,
    val accentColor: Color
)

val mockReelsList = listOf(
    MockReel("@cat_paradise", "Me watching wholesome cat videos at 3 AM instead of sleeping 🐱💤", "1.2M", "24K", Color(0xFF6750A4)),
    MockReel("@productivity_guide", "How to double your productivity in 5 simple steps... (Step 1: Stop scrolling) 📈", "420K", "12K", Color(0xFF388E3C)),
    MockReel("@satisfying_asmr", "Super crunchy kinetic sand slicing! So satisfying! 🔪✨ #asmr #slicing", "2.5M", "45K", Color(0xFFE91E63)),
    MockReel("@gym_grind_fitness", "POV: You finally hit a new personal record! No excuses! 💪🏋️‍♂️ #gym #motivation", "850K", "18K", Color(0xFF1976D2)),
    MockReel("@chef_masters", "Creamy garlic butter steak bites in under 15 minutes! Quick dinner recipe 🥩🔥", "1.8M", "30K", Color(0xFFF57C00)),
    MockReel("@code_ninja", "Debugging a production bug on Friday afternoon. Send help! 💻💀 #programmer", "320K", "9K", Color(0xFF607D8B)),
    MockReel("@nature_escape", "Waking up to this scenic view in the Swiss Alps. Pure serenity 🏔️🌲", "3.1M", "50K", Color(0xFF009688))
)

@Composable
fun ReelTrackApp(viewModel: ReelViewModel) {
    val context = LocalContext.current
    val selectedTab by viewModel.selectedTab.collectAsState()
    val todayScroll by viewModel.todayScroll.collectAsState()
    val leaderboard by viewModel.leaderboard.collectAsState()
    val isLocked by viewModel.isLocked.collectAsState()
    val isPasscodeEnabled by viewModel.isPasscodeEnabled.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initSecurity(context)
    }

    if (isLocked) {
        LockScreen(viewModel = viewModel)
        return
    }

    var showAboutDialog by remember { mutableStateOf(false) }

    // Display formatted time estimate
    val reelsCount = todayScroll?.count ?: 0
    val secondsPerReel = todayScroll?.averageTimePerReel ?: 18f
    val totalSeconds = (reelsCount * secondsPerReel).toLong()
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val timeString = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"

// Custom user initials setup (grab from the first letters of user row)


    val myProfile = leaderboard.firstOrNull { it.isMe }
    val myName = myProfile?.name ?: "You"
    val initials = if (myName.length >= 2) {
        myName.substring(0, 2).uppercase()
    } else {
        myName.take(1).uppercase().plus("D")
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SleekBackground,
        bottomBar = {
            BottomNavBar(
                selectedTab = selectedTab,
                onTabSelected = { viewModel.selectTab(it) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // 1. Simulated Status Bar
            StatusSimulation()

            // 2. Custom App Header
            HeaderBar(
                userInitials = initials,
                onMenuClick = { showAboutDialog = true }
            )

            // About Application dialog simulation
            if (showAboutDialog) {
                AboutInfoDialog(onDismiss = { showAboutDialog = false })
            }

            // 3. Tab switching content container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = 650.dp)
                ) {
                    when (selectedTab) {
                        0 -> TrackerTab(
                            viewModel = viewModel,
                            reelsCount = reelsCount,
                            timeString = timeString,
                            avgSeconds = secondsPerReel.toInt()
                        )

                        1 -> StatsTab(
                            viewModel = viewModel,
                            leaderboard = leaderboard
                        )

                        2 -> FriendsTab(
                            viewModel = viewModel,
                            leaderboard = leaderboard
                        )

                        3 -> SettingsTab(
                            viewModel = viewModel,
                            currentName = myName,
                            currentAvgSeconds = secondsPerReel
                        )
                    }
                }
            }
        }
    }
}


   // SIMULATED STATUS BAR
   @Composable
   fun StatusSimulation() {
       Row(
           modifier = Modifier
               .fillMaxWidth()
               .padding(horizontal = 24.dp, vertical = 6.dp),
           horizontalArrangement = Arrangement.SpaceBetween,
           verticalAlignment = Alignment.CenterVertically
       ) {
           Text(
               text = "9:41",
               style = MaterialTheme.typography.labelMedium.copy(
                   fontWeight = FontWeight.Bold,
                   color = SleekText,
                   fontSize = 13.sp
               )
           )Row(
                   verticalAlignment = Alignment.CenterVertically,
           horizontalArrangement = Arrangement.spacedBy(4.dp)
           ) {
           Icon(
               imageVector = Icons.Default.Wifi,
               contentDescription = "Wifi",
               tint = SleekText,
               modifier = Modifier.size(14.dp)
           )
           Icon(
               imageVector = Icons.Default.BatteryFull,
               contentDescription = "Battery",
               tint = SleekText,
               modifier = Modifier.size(14.dp)
           )
       }
       }
   }


// CUSTOM TOP APP BAR
@Composable
fun HeaderBar(userInitials: String, onMenuClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 12.dp),


