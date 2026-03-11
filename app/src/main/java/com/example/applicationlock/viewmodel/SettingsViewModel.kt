package com.example.applicationlock.viewmodel

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.applicationlock.AppPinActivity
import com.example.applicationlock.Constants
import com.example.applicationlock.MyDeviceAdminReceiver
import com.example.applicationlock.data.LockedAppsRepo
import com.example.applicationlock.data.PinStore
import com.example.applicationlock.service.AppLockService

class SettingsViewModel(
    private val context: Context,
    private val repo: LockedAppsRepo,
    private val pinStore: PinStore
) : ViewModel() {

    private val _status = MutableLiveData<String>()
    val status: LiveData<String> get() = _status

    fun setAppPin() {
        val intent = Intent(context, AppPinActivity::class.java)
            .putExtra(Constants.EXTRA_SET_FOR, "app")
        context.startActivity(intent)
    }

    fun setLockPin() {
        val intent = Intent(context, AppPinActivity::class.java)
            .putExtra(Constants.EXTRA_SET_FOR, "lock")
        context.startActivity(intent)
    }

    fun addPackage(pkg: String) {
        repo.add(pkg)
        _status.value = "Locked: $pkg"
    }

    fun removePackage(pkg: String) {
        repo.remove(pkg)
        _status.value = "Unlocked: $pkg"
    }

    fun openUsagePermission() {
        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }

    fun openOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            context.startActivity(intent)
        }
    }

    fun enableAdmin() {
        val compName = ComponentName(context, MyDeviceAdminReceiver::class.java)
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        if (dpm.isAdminActive(compName)) {
            _status.value = "Device Admin already enabled"
        } else {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
            intent.putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Enable Device Admin to prevent unauthorized uninstall."
            )
            context.startActivity(intent)
        }
    }

    fun disableAdmin() {
        val compName = ComponentName(context, MyDeviceAdminReceiver::class.java)
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        if (dpm.isAdminActive(compName)) {
            dpm.removeActiveAdmin(compName)
            _status.value = "Device Admin disabled"
        } else {
            _status.value = "Device Admin not active"
        }
    }

    fun startProtection() {
        val svc = Intent(context, AppLockService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(svc)
        } else {
            context.startService(svc)
        }
        context.getSharedPreferences("app_lock_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("protection_enabled", true)
            .apply()
        _status.value = "Protection started"
    }

    fun stopProtection() {
        context.stopService(Intent(context, AppLockService::class.java))
        context.getSharedPreferences("app_lock_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("protection_enabled", false)
            .apply()
        _status.value = "Protection stopped"
    }
}