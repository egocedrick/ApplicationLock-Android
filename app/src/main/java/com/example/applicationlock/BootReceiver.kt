package com.example.applicationlock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.applicationlock.service.AppLockService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val i = Intent(context, AppLockService::class.java)
            context.startForegroundService(i)
        }
    }
}
