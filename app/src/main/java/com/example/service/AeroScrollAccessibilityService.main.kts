package com.example.service


import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.data.AppDatabase
import com.example.data.DailyScroll
import com.example.data.Friend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.CoroutineContext

class AeroScrollAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var lastScrollTime = 0L
    private var lastNodeHash = 0

    companion object {
        var isServiceRunning = false
            private set

        fun getServiceIntent(context: Context): Intent {
            return Intent(context, AeroScrollAccessibilityService::class.java)
        }

        fun isServiceEnabled(context: Context): Boolean {
            val expectedId = "${context.packageName}/${AeroScrollAccessibilityService::class.java.name}"
            val enabledServicesSetting = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServicesSetting)
            while (colonSplitter.hasNext()) {
                val componentNameString = colonSplitter.next()
                if (componentNameString.equals(expectedId, ignoreCase = true)) {
                    return true
                }
            }
            return false
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceRunning = true

        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_VIEW_SCROLLED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            packageNames = arrayOf("com.instagram.android")
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 100
        }
        this.serviceInfo = info
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        if (packageName != "com.instagram.android") return

        val eventType = event.eventType

        // Debounced check to avoid double-counting scrolls during rapid gesture segments
        if (eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            val now = System.currentTimeMillis()
            if (now - lastScrollTime > 1500) { // Typically people spend at least 1.5 seconds per Reel
                lastScrollTime = now
                incrementInstagramScrollCount()
            }
        } else if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            val source = event.source ?: return
            val hash = source.hashCode()
            if (hash != lastNodeHash) {
                lastNodeHash = hash
                val now = System.currentTimeMillis()
                if (now - lastScrollTime > 1800) {
                    if (source.isScrollable) {
                        lastScrollTime = now
                        incrementInstagramScrollCount()
                    }
                }
            }
        }
    }

    private fun incrementInstagramScrollCount() {
        val db = AppDatabase.getDatabase(applicationContext)
        val dao = db.reelDao()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        serviceScope.launch {
            val scrollRecord = dao.getDailyScrollByDate(today) ?: DailyScroll(date = today, count = 0)
            val newCount = scrollRecord.count + 1
            val newTimeSpent = newCount * scrollRecord.averageTimePerReel.toLong()
            dao.insertOrUpdateDailyScroll(scrollRecord.copy(count = newCount, timeSpentSeconds = newTimeSpent))

            // Sync with local Profile
            val myProfile = dao.getMyProfileDirect()
            if (myProfile != null) {
                dao.insertOrUpdateFriend(myProfile.copy(count = newCount))
            } else {
                dao.insertOrUpdateFriend(Friend(
                    id = "me",
                    name = "You",
                    count = newCount,
                    status = "Scroller",
                    isMe = true
                ))
            }
        }
    }

    override fun onInterrupt() {
        // Required method override
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
    }
}
