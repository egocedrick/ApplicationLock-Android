package com.example.applicationlock.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import com.example.applicationlock.LockActivity
import com.example.applicationlock.data.Prefs
import com.example.applicationlock.security.PinGate

/**
 * Foreground service that watches foreground apps and launches LockActivity for locked apps.
 * Requires USAGE_STATS permission to work (user must grant Usage Access).
 */
class WatchdogService : Service() {

    private lateinit var handler: Handler
    private lateinit var prefs: Prefs

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(this)
        handler = Handler(mainLooper)
        startForegroundIfNeeded()
        handler.post(checkTask)
    }

    @SuppressLint("ForegroundServiceType")
    private fun startForegroundIfNeeded() {
        val channelId = "applocker_channel"
        val channelName = "App Locker Protection"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            manager.createNotificationChannel(channel)
            val notification: Notification = Notification.Builder(this, channelId)
                .setContentTitle("AppLock Protection")
                .setContentText("Protection running")
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .build()
            startForeground(1, notification)
        } else {
            val notification = Notification.Builder(this)
                .setContentTitle("AppLock Protection")
                .setContentText("Protection running")
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .build()
            startForeground(1, notification)
        }
    }

    private val checkTask = object : Runnable {
        override fun run() {
            val current = getForegroundApp()
            val locked = prefs.getLockedApps()
            if (current != null && locked.contains(current)) {
                if (!PinGate.isAppUnlocked(current)) {
                    val intent = Intent(this@WatchdogService, LockActivity::class.java)
                    intent.putExtra(LockActivity.EXTRA_TARGET_PKG, current)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }
            handler.postDelayed(this, 1000)
        }
    }

    private fun getForegroundApp(): String? {
        val usm = getSystemService(USAGE_STATS_SERVICE) as android.app.usage.UsageStatsManager
        val end = System.currentTimeMillis()
        val begin = end - 1000
        val events = usm.queryEvents(begin, end)
        val ev = android.app.usage.UsageEvents.Event()
        var last: String? = null
        while (events.hasNextEvent()) {
            events.getNextEvent(ev)
            if (ev.eventType == android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND) {
                last = ev.packageName
            }
        }
        return last
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
