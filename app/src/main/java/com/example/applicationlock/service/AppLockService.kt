package com.example.applicationlock.service

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.applicationlock.Constants
import com.example.applicationlock.LockActivity
import com.example.applicationlock.data.LockedAppsRepo
import com.example.applicationlock.security.PinGate

class AppLockService : Service() {

    private lateinit var handler: Handler
    private lateinit var repo: LockedAppsRepo
    private var prevForeground: String? = null

    // short debounce map to avoid double-launch back-to-back events
    private val lastPromptTime = mutableMapOf<String, Long>()
    private val DEBOUNCE_MS = 800L

    companion object {
        private const val TAG = "AppLockService"
        private const val CHANNEL_ID = "applock_channel_final"
        private const val NOTIF_ID = 1001
        private const val POLL_INTERVAL_MS = 900L
    }

    override fun onCreate() {
        super.onCreate()
        repo = LockedAppsRepo(this)

        val thread = HandlerThread("AppLockServiceThread")
        thread.start()
        handler = Handler(thread.looper)

        createChannelIfNeeded()
        startForegroundNotification()
        handler.post(checkTask)

        Log.d(TAG, "onCreate: AppLockService started")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundNotification()
        Log.d(TAG, "onStartCommand: START_STICKY")
        return START_STICKY
    }

    private val checkTask = object : Runnable {
        override fun run() {
            try {
                val pkg = getForegroundPackage()

                // If previously unlocked app (session) and user left it, clear the session unlock
                if (prevForeground != null && prevForeground != pkg) {
                    // clear unlocked session for the previous foreground app
                    try {
                        PinGate.clear(prevForeground!!)
                        Log.d(TAG, "Cleared session unlock for $prevForeground")
                    } catch (_: Throwable) {}
                }

                prevForeground = pkg

                if (pkg != null && pkg != packageName && repo.isLocked(pkg)) {
                    // If the app is already unlocked for this session, don't relaunch LockActivity.
                    if (PinGate.isAppUnlocked(pkg)) {
                        Log.d(TAG, "Package $pkg is unlocked for session — skipping lock")
                    } else {
                        val now = System.currentTimeMillis()
                        val last = lastPromptTime[pkg] ?: 0L
                        if (now - last > DEBOUNCE_MS) {
                            lastPromptTime[pkg] = now
                            val i = Intent(this@AppLockService, LockActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                .putExtra(Constants.EXTRA_TARGET_PKG, pkg)
                            startActivity(i)
                            Log.d(TAG, "LockActivity launched for $pkg")
                        }
                    }
                }
            } catch (t: Throwable) {
                Log.w(TAG, "poll error", t)
            } finally {
                try { handler.postDelayed(this, POLL_INTERVAL_MS) } catch (_: Throwable) {}
            }
        }
    }

    private fun getForegroundPackage(): String? {
        try {
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
        } catch (t: Throwable) {
            Log.w(TAG, "UsageStats query failed — have you granted Usage Access?", t)
            return null
        }
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            val ch = android.app.NotificationChannel(CHANNEL_ID, "App Lock Service", NotificationManager.IMPORTANCE_LOW)
            nm?.createNotificationChannel(ch)
        }
    }

    @SuppressLint("ForegroundServiceType")
    private fun startForegroundNotification() {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("App Locker running")
            .setContentText("Monitoring locked apps")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        val notif = builder.build()
        startForeground(NOTIF_ID, notif)
    }

    private fun pendingIntentFlag(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        else
            PendingIntent.FLAG_UPDATE_CURRENT
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        try {
            val restartIntent = Intent(applicationContext, AppLockService::class.java)
            restartIntent.setPackage(packageName)
            val pending = PendingIntent.getService(this, 1, restartIntent, pendingIntentFlag())
            val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val triggerAt = System.currentTimeMillis() + 500L
            am.set(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            Log.d(TAG, "onTaskRemoved: scheduled restart")
        } catch (t: Throwable) {
            Log.w(TAG, "onTaskRemoved failed", t)
        }
    }

    override fun onDestroy() {
        try { handler.removeCallbacksAndMessages(null) } catch (_: Throwable) {}
        Log.d(TAG, "AppLockService destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
