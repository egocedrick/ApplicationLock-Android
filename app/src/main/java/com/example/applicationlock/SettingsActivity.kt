package com.example.applicationlock

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.applicationlock.data.LockedAppsRepo
import com.example.applicationlock.data.PinStore
import com.example.applicationlock.service.WatchdogService

class SettingsActivity : AppCompatActivity() {

    private lateinit var repo: LockedAppsRepo
    private lateinit var pinStore: PinStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        repo = LockedAppsRepo(this)
        pinStore = PinStore(this)

        val btnSetAppPin = findViewById<Button>(R.id.btn_set_app_pin)
        val btnSetLockPin = findViewById<Button>(R.id.btn_set_lock_pin)
        val editPkg = findViewById<EditText>(R.id.edit_package)
        val btnAdd = findViewById<Button>(R.id.btn_add_pkg)
        val btnRemove = findViewById<Button>(R.id.btn_remove_pkg)
        val txtStatus = findViewById<TextView>(R.id.txt_status)
        val btnUsage = findViewById<Button>(R.id.btn_usage_perm)
        val btnOverlay = findViewById<Button>(R.id.btn_overlay_perm)
        val btnStart = findViewById<Button>(R.id.btn_start)
        val btnStop = findViewById<Button>(R.id.btn_stop)

        btnSetAppPin.setOnClickListener {
            startActivity(Intent(this, AppPinActivity::class.java).putExtra(Constants.EXTRA_SET_FOR, "app"))
        }

        btnSetLockPin.setOnClickListener {
            startActivity(Intent(this, AppPinActivity::class.java).putExtra(Constants.EXTRA_SET_FOR, "lock"))
        }

        btnAdd.setOnClickListener {
            val p = editPkg.text.toString().trim()
            if (p.isNotEmpty()) {
                repo.add(p)
                txtStatus.text = "Locked: $p"
            } else {
                Toast.makeText(this, "Enter package", Toast.LENGTH_SHORT).show()
            }
        }

        btnRemove.setOnClickListener {
            val p = editPkg.text.toString().trim()
            if (p.isNotEmpty()) {
                repo.remove(p)
                txtStatus.text = "Unlocked: $p"
            } else {
                Toast.makeText(this, "Enter package", Toast.LENGTH_SHORT).show()
            }
        }

        btnUsage.setOnClickListener {
            try {
                val i = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(i)
            } catch (e: Exception) {
                Toast.makeText(this, "Unable to open usage settings", Toast.LENGTH_SHORT).show()
            }
        }

        btnOverlay.setOnClickListener {
            try {
                // Primary: open overlay management page for this package
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } catch (e: Exception) {
                // Fallback: open general overlay settings
                try {
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                } catch (ex: Exception) {
                    Toast.makeText(this, "Unable to open overlay settings", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnStart.setOnClickListener {
            if (!hasUsageStatsPermission()) {
                Toast.makeText(this, "Grant Usage Access first", Toast.LENGTH_SHORT).show()
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                return@setOnClickListener
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(Intent(this, WatchdogService::class.java))
            else startService(Intent(this, WatchdogService::class.java))
            txtStatus.text = "Protection started"
        }

        btnStop.setOnClickListener {
            stopService(Intent(this, WatchdogService::class.java))
            txtStatus.text = "Protection stopped"
        }

        // initial status
        txtStatus.text = if (isServiceRunning()) "Service: running" else "Service: stopped"
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun isServiceRunning(): Boolean {
        // lightweight check: if watchdog started by user, we rely on txtStatus updates
        // you could implement a static flag in the service if you want a precise check
        return false
    }
}
