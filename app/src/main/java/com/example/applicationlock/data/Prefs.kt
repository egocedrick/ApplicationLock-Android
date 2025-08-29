package com.example.applicationlock.data

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_lock_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_APP_PIN = "app_pin"
        private const val KEY_LOCK_PIN = "lock_pin"
        private const val KEY_LOCKED_APPS = "locked_apps"
        private const val KEY_ATTEMPTS_PREFIX = "attempts_"
        private const val KEY_GATED_PREFIX = "gated_"
        private const val DEFAULT_ATTEMPTS = 3
    }

    // App PIN (ApplicationLock)
    fun isAppPinSet(): Boolean = prefs.contains(KEY_APP_PIN)
    fun saveAppPin(pin: String) = prefs.edit().putString(KEY_APP_PIN, pin).apply()
    fun getAppPin(): String? = prefs.getString(KEY_APP_PIN, null)
    fun clearAppPin() = prefs.edit().remove(KEY_APP_PIN).apply()

    // Lock PIN (for other apps)
    fun isLockPinSet(): Boolean = prefs.contains(KEY_LOCK_PIN)
    fun saveLockPin(pin: String) = prefs.edit().putString(KEY_LOCK_PIN, pin).apply()
    fun getLockPin(): String? = prefs.getString(KEY_LOCK_PIN, null)
    fun clearLockPin() = prefs.edit().remove(KEY_LOCK_PIN).apply()

    // Locked apps list
    fun getLockedApps(): MutableSet<String> =
        prefs.getStringSet(KEY_LOCKED_APPS, mutableSetOf())?.toMutableSet() ?: mutableSetOf()

    fun addLockedApp(pkg: String) {
        val set = getLockedApps()
        set.add(pkg)
        prefs.edit().putStringSet(KEY_LOCKED_APPS, set).apply()
    }

    fun removeLockedApp(pkg: String) {
        val set = getLockedApps()
        set.remove(pkg)
        prefs.edit().putStringSet(KEY_LOCKED_APPS, set).apply()
    }

    fun isAppLocked(pkg: String?): Boolean = pkg != null && getLockedApps().contains(pkg)

    // Attempt limiter API (per-scope)
    fun getAttemptsLeft(scope: String): Int =
        prefs.getInt(KEY_ATTEMPTS_PREFIX + scope, DEFAULT_ATTEMPTS)

    fun decrementAttempts(scope: String) {
        val cur = getAttemptsLeft(scope)
        val next = (cur - 1).coerceAtLeast(0)
        prefs.edit().putInt(KEY_ATTEMPTS_PREFIX + scope, next).apply()
        if (next <= 0) prefs.edit().putBoolean(KEY_GATED_PREFIX + scope, true).apply()
    }

    fun resetAttempts(scope: String) {
        prefs.edit()
            .putInt(KEY_ATTEMPTS_PREFIX + scope, DEFAULT_ATTEMPTS)
            .putBoolean(KEY_GATED_PREFIX + scope, false)
            .apply()
    }

    fun isGateLocked(scope: String): Boolean = prefs.getBoolean(KEY_GATED_PREFIX + scope, false)

    fun clearAll() = prefs.edit().clear().apply()
}
