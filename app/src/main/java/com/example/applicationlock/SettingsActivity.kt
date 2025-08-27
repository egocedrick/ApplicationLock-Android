package com.example.applicationlock

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.applicationlock.data.Prefs
import com.example.applicationlock.service.WatchdogService

/**
 * Simple settings UI — allows:
 * - Set App PIN (launch AppPinActivity with SET_FOR="app")
 * - Set Lock PIN (launch AppPinActivity with SET_FOR="lock")
 * - Add package to locked list
 * - Open usage access & overlay settings
 * - Start/Stop watchdog
 *
 * This is the place the user goes to change PINs.
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefs = Prefs(this)

        val editPin = findViewById<EditText>(R.id.editPin)
        val btnSavePin = findViewById<Button>(R.id.btnSavePin)
        val editPackage = findViewById<EditText>(R.id.editPackage)
        val btnAddPackage = findViewById<Button>(R.id.btnAddPackage)
        val btnUsage = findViewById<Button>(R.id.btnUsage)
        val btnOverlay = findViewById<Button>(R.id.btnOverlay)
        val btnStart = findViewById<Button>(R.id.btnStart)
        val btnStop = findViewById<Button>(R.id.btnStop)
        val txtStatus = findViewById<TextView>(R.id.txtStatus)

        btnSavePin.setOnClickListener {
            // Choose which PIN to set: show a small chooser? For simplicity start AppPinActivity with extra.
            // Use editPin value presence to decide: if editPin is non-empty we treat it as "lockPin"
            // but better: always open AppPinActivity and user chooses via dialog — keep simple:
            startActivity(Intent(this, AppPinActivity::class.java).apply {
                putExtra(AppPinActivity.EXTRA_SET_FOR, "app")
            })
            txtStatus.text = "Open Set App PIN screen"
        }

        // Button to set lockPin
        val btnSetLockPin = findViewById<Button>(R.id.btnSetLockPin)
        btnSetLockPin.setOnClickListener {
            startActivity(Intent(this, AppPinActivity::class.java).apply {
                putExtra(AppPinActivity.EXTRA_SET_FOR, "lock")
            })
            txtStatus.text = "Open Set Lock PIN screen"
        }

        btnAddPackage.setOnClickListener {
            val pkg = editPackage.text.toString()
            if (pkg.isNotEmpty()) {
                prefs.addLockedApp(pkg)
                txtStatus.text = "Locked: $pkg"
            } else {
                txtStatus.text = "Enter a package name"
            }
        }

        btnUsage.setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        btnOverlay.setOnClickListener {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        }

        btnStart.setOnClickListener {
            startService(Intent(this, WatchdogService::class.java))
            txtStatus.text = "Protection started"
        }

        btnStop.setOnClickListener {
            stopService(Intent(this, WatchdogService::class.java))
            txtStatus.text = "Protection stopped"
        }
    }
}
