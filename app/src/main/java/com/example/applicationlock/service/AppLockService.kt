package com.example.applicationlock.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.example.applicationlock.Constants
import com.example.applicationlock.LockActivity
import com.example.applicationlock.data.LockedAppsRepo
import com.example.applicationlock.security.LockState

class AppLockService : Service() {

    private lateinit var handler: Handler
    private lateinit var repo: LockedAppsRepo
    private var lastLaunchAt: Long = 0L
    private val LAUNCH_THROTTLE_MS = 900L

    override fun onCreate() {
        super.onCreate()
        repo = LockedAppsRepo(this)
        handler = Handler(mainLooper)
        startForegroundIfNeeded()
        handler.post(checkTask)
    }

    @SuppressLint("ForegroundServiceType")
    private fun startForegroundIfNeeded() {
        val channelId = "applocker_channel"
        val nm = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(channelId, "Application Lock", NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(ch)
        }
        val n = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
                .setContentTitle("Application Lock running")
                .setContentText("Monitoring locked apps")
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("Application Lock running")
                .setContentText("Monitoring locked apps")
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .build()
        }
        startForeground(1, n)
    }

    private val checkTask = object : Runnable {
        override fun run() {
            try {
                val pkg = getForegroundPackage()
                Log.d("AppLockService", "foreground=$pkg lockVisible=${LockState.lockActivityVisible}")

                if (pkg != null && pkg != packageName && repo.isLocked(pkg)) {
                    val now = System.currentTimeMillis()
                    if (!LockState.lockActivityVisible && now - lastLaunchAt > LAUNCH_THROTTLE_MS) {
                        try {
                            val i = Intent(this@AppLockService, LockActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                .putExtra(Constants.EXTRA_TARGET_PKG, pkg)
                            startActivity(i)
                            lastLaunchAt = now
                        } catch (t: Throwable) {
                            Log.w("AppLockService", "failed to launch LockActivity", t)
                        }
                    }
                }
            } catch (t: Throwable) {
                Log.w("AppLockService", "checkTask error", t)
            } finally {
                handler.postDelayed(this, 1000)
            }
        }
    }

    private fun getForegroundPackage(): String? {
        return try {
            val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val end = System.currentTimeMillis()
            val begin = end - 2000
            val events = usm.queryEvents(begin, end)
            val ev = UsageEvents.Event()
            var last: String? = null
            while (events.hasNextEvent()) {
                events.getNextEvent(ev)
                if (ev.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    last = ev.packageName
                }
            }
            last
        } catch (t: Throwable) {
            Log.w("AppLockService", "getForegroundPackage failed", t)
            null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(checkTask)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
