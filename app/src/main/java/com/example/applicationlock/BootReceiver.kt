package com.example.applicationlock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.applicationlock.service.WatchdogService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val i = Intent(context, WatchdogService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) context.startForegroundService(i) else context.startService(i)
        }
    }
}
