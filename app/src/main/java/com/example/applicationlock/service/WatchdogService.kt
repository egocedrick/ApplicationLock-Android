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
import com.example.applicationlock.security.PinGate

class WatchdogService : Service() {

    private lateinit var handler: Handler
    private lateinit var repo: LockedAppsRepo
    private var lastPkgLaunched: String? = null
    private var lastLaunchAt: Long = 0L
    private val LAUNCH_THROTTLE_MS = 900L

    override fun onCreate() {
        super.onCreate()
        repo = LockedAppsRepo(this)
        handler = Handler(mainLooper)
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
        }
        val n = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
                .setContentTitle("AppLocker running")
                .setContentText("Monitoring locked apps")
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("AppLocker running")
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
                Log.d("WatchdogService", "foreground=$pkg lastLaunched=$lastPkgLaunched lockVisible=${LockState.lockActivityVisible}")

                if (pkg == null) {
                    // nothing
                } else if (pkg == packageName) {
                    if (lastPkgLaunched != null && repo.isLocked(lastPkgLaunched!!)) {
                        PinGate.clear(lastPkgLaunched!!)
                        lastPkgLaunched = null
                    }
                } else {
                    if (repo.isLocked(pkg)) {
                        if (lastPkgLaunched != null && lastPkgLaunched != pkg) {
                            PinGate.clear(lastPkgLaunched!!)
                        }
                        val now = System.currentTimeMillis()
                        if (!PinGate.isAppUnlocked(pkg) && !LockState.lockActivityVisible && now - lastLaunchAt > LAUNCH_THROTTLE_MS) {
                            try {
                                val i = Intent(this@WatchdogService, LockActivity::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    .putExtra(Constants.EXTRA_TARGET_PKG, pkg)
                                startActivity(i)
                                lastLaunchAt = now
                                lastPkgLaunched = pkg
                            } catch (t: Throwable) {
                                Log.w("WatchdogService", "failed to launch LockActivity", t)
                            }
                        }
                    } else {
                        if (lastPkgLaunched != null && repo.isLocked(lastPkgLaunched!!)) {
                            PinGate.clear(lastPkgLaunched!!)
                        }
                        lastPkgLaunched = null
                    }
                }
            } catch (t: Throwable) { Log.w("WatchdogService", "checkTask error", t) }
            finally { handler.postDelayed(this, 1000) }
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
                if (ev.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) last = ev.packageName
            }
            last
        } catch (t: Throwable) {
            Log.w("WatchdogService", "getForegroundPackage failed", t)
            null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(checkTask)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
