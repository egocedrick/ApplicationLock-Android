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
import android.os.Looper
import com.example.applicationlock.Constants
import com.example.applicationlock.LockActivity
import com.example.applicationlock.data.LockedAppsRepo
import com.example.applicationlock.security.PinGate

/**
 * Foreground service that monitors foreground app and launches LockActivity for locked apps.
 * Requires Usage Access permission.
 */
class WatchdogService : Service() {

    private lateinit var handler: Handler
    private lateinit var repo: LockedAppsRepo
    private var lastPkgLaunched: String? = null
    private var prevForeground: String? = null

    override fun onCreate() {
        super.onCreate()
        repo = LockedAppsRepo(this)
        handler = Handler(Looper.getMainLooper())
        startForegroundIfNeeded()
        handler.post(checkTask)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    @SuppressLint("ForegroundServiceType")
    private fun startForegroundIfNeeded() {
        val channelId = "applocker_channel"
        val nm = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(channelId, "AppLocker", NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(ch)
            val n = Notification.Builder(this, channelId)
                .setContentTitle("AppLocker running")
                .setContentText("Monitoring locked apps")
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .build()
            startForeground(1, n)
        } else {
            val n = Notification.Builder(this)
                .setContentTitle("AppLocker running")
                .setContentText("Monitoring locked apps")
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .build()
            startForeground(1, n)
        }
    }

    private val checkTask = object : Runnable {
        override fun run() {
            try {
                val pkg = getForegroundPackage()

                // If previously prompted for an app and user moved away from it,
                // clear lastPkgLaunched so reopening that app triggers the lock again.
                if (prevForeground != null && prevForeground != pkg && prevForeground == lastPkgLaunched) {
                    PinGate.clear(prevForeground!!)
                    lastPkgLaunched = null
                }

                prevForeground = pkg

                if (pkg != null &&
                    pkg != packageName &&
                    repo.isLocked(pkg) &&
                    pkg != lastPkgLaunched
                ) {
                    lastPkgLaunched = pkg
                    val i = Intent(this@WatchdogService, LockActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra(Constants.EXTRA_TARGET_PKG, pkg)
                    startActivity(i)
                }
            } catch (t: Throwable) {
                // ignore
            } finally {
                handler.postDelayed(this, 900L)
            }
        }
    }

    private fun getForegroundPackage(): String? {
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
        return last
    }

    override fun onDestroy() {
        super.onDestroy()
        try { handler.removeCallbacksAndMessages(null) } catch (_: Throwable) {}
        PinGate.clearAll()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        try { PinGate.clearAll() } catch (_: Throwable) {}
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
