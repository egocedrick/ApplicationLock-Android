package com.example.applicationlock

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
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
import com.example.applicationlock.service.AppLockService

class SettingsActivity : AppCompatActivity() {

    private lateinit var repo: LockedAppsRepo
    private lateinit var pinStore: PinStore

    @SuppressLint("MissingInflatedId")
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
        val btnEnableAdmin = findViewById<Button>(R.id.btn_enable_admin)
        val btnDisableAdmin = findViewById<Button>(R.id.btn_disable_admin)
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

        btnEnableAdmin.setOnClickListener {
            val compName = ComponentName(this, MyDeviceAdminReceiver::class.java)
            val dpm = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
            if (dpm.isAdminActive(compName)) {
                Toast.makeText(this, "Device Admin already enabled", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
                intent.putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Enable Device Admin to prevent unauthorized uninstall."
                )
                startActivity(intent)
            }
        }

        btnDisableAdmin?.setOnClickListener {
            val compName = ComponentName(this, MyDeviceAdminReceiver::class.java)
            val dpm = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
            if (dpm.isAdminActive(compName)) {
                dpm.removeActiveAdmin(compName)
                Toast.makeText(this, "Device Admin disabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Device Admin not active", Toast.LENGTH_SHORT).show()
            }
        }

        btnStart.setOnClickListener {

            val svc = Intent(this, AppLockService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(svc)
            } else {
                startService(svc)
            }
            getSharedPreferences("app_lock_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("protection_enabled", true)
                .apply()
            txtStatus.text = "Protection started"
        }

        btnStop.setOnClickListener {
            stopService(Intent(this, AppLockService::class.java))

            getSharedPreferences("app_lock_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("protection_enabled", false)
                .apply()
            txtStatus.text = "Protection stopped"
        }
    }
}