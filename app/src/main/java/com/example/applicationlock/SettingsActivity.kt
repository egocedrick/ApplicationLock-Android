package com.example.applicationlock

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.applicationlock.data.LockedAppsRepo
import com.example.applicationlock.data.PinStore
import com.example.applicationlock.service.AppLockService

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
            }
        }

        btnRemove.setOnClickListener {
            val p = editPkg.text.toString().trim()
            if (p.isNotEmpty()) {
                repo.remove(p)
                txtStatus.text = "Unlocked: $p"
            }
        }

        btnUsage.setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        btnOverlay.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivity(intent)
            }
        }

        btnStart.setOnClickListener {
            repo.resetAllAttempts()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(Intent(this, AppLockService::class.java))
            } else {
                startService(Intent(this, AppLockService::class.java))
            }
            txtStatus.text = "Protection started"
        }

        btnStop.setOnClickListener {
            stopService(Intent(this, AppLockService::class.java))
            repo.resetAllAttempts()
            txtStatus.text = "Protection stopped"
        }
    }
}
