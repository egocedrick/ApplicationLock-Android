package com.example.applicationlock

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.applicationlock.data.LockedAppsRepo
import com.example.applicationlock.data.PinStore
import com.example.applicationlock.viewmodel.SettingsViewModel

class SettingsActivity : AppCompatActivity() {
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var txtStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val repo = LockedAppsRepo(this)
        val pinStore = PinStore(this)
        settingsViewModel = SettingsViewModel(this, repo, pinStore)

        txtStatus = findViewById(R.id.txt_status)

        settingsViewModel.status.observe(this) { message ->
            txtStatus.text = message
        }

        val btnSetAppPin = findViewById<Button>(R.id.btn_set_app_pin)
        val btnSetLockPin = findViewById<Button>(R.id.btn_set_lock_pin)
        val editPkg = findViewById<EditText>(R.id.edit_package)
        val btnAdd = findViewById<Button>(R.id.btn_add_pkg)
        val btnRemove = findViewById<Button>(R.id.btn_remove_pkg)
        val btnUsage = findViewById<Button>(R.id.btn_usage_perm)
        val btnOverlay = findViewById<Button>(R.id.btn_overlay_perm)
        val btnEnableAdmin = findViewById<Button>(R.id.btn_enable_admin)
        val btnDisableAdmin = findViewById<Button>(R.id.btn_disable_admin)
        val btnStart = findViewById<Button>(R.id.btn_start)
        val btnStop = findViewById<Button>(R.id.btn_stop)

        btnSetAppPin.setOnClickListener { settingsViewModel.setAppPin() }
        btnSetLockPin.setOnClickListener { settingsViewModel.setLockPin() }
        btnAdd.setOnClickListener {
            val p = editPkg.text.toString().trim()
            if (p.isNotEmpty()) settingsViewModel.addPackage(p)
        }
        btnRemove.setOnClickListener {
            val p = editPkg.text.toString().trim()
            if (p.isNotEmpty()) settingsViewModel.removePackage(p)
        }
        btnUsage.setOnClickListener { settingsViewModel.openUsagePermission() }
        btnOverlay.setOnClickListener { settingsViewModel.openOverlayPermission() }
        btnEnableAdmin.setOnClickListener { settingsViewModel.enableAdmin() }
        btnDisableAdmin.setOnClickListener { settingsViewModel.disableAdmin() }
        btnStart.setOnClickListener { settingsViewModel.startProtection() }
        btnStop.setOnClickListener { settingsViewModel.stopProtection() }
    }
}