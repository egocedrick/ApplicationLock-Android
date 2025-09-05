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

        private const val KEY_GATE_TIMESTAMP_PREFIX = "gate_time_"
        private const val ONE_HOUR_MS = 60 * 60 * 1000L
    }

    private val packageName: String = context.packageName

    fun isAppPinSet(): Boolean = prefs.contains(KEY_APP_PIN)
    fun saveAppPin(pin: String) = prefs.edit().putString(KEY_APP_PIN, pin).apply()
    fun getAppPin(): String? = prefs.getString(KEY_APP_PIN, null)

    fun isLockPinSet(): Boolean = prefs.contains(KEY_LOCK_PIN)
    fun saveLockPin(pin: String) = prefs.edit().putString(KEY_LOCK_PIN, pin).apply()
    fun getLockPin(): String? = prefs.getString(KEY_LOCK_PIN, null)

    fun getLockedApps(): MutableSet<String> {
        val set = prefs.getStringSet(KEY_LOCKED_APPS, null)
        return (set?.toMutableSet() ?: mutableSetOf())
    }

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

    fun getAttemptsLeft(scope: String): Int {
        // Auto-reset for app itself after 1 hour if locked
        if (scope == packageName && isGateLocked(scope)) {
            val lockedAt = prefs.getLong(KEY_GATE_TIMESTAMP_PREFIX + scope, 0L)
            if (lockedAt > 0 && System.currentTimeMillis() - lockedAt >= ONE_HOUR_MS) {
                resetAttempts(scope)
            }
        }
        return prefs.getInt(KEY_ATTEMPTS_PREFIX + scope, DEFAULT_ATTEMPTS)
    }

    fun decrementAttempts(scope: String) {
        val cur = getAttemptsLeft(scope)
        val next = (cur - 1).coerceAtLeast(0)
        prefs.edit().putInt(KEY_ATTEMPTS_PREFIX + scope, next).apply()
        if (next <= 0) {
            prefs.edit()
                .putBoolean(KEY_GATED_PREFIX + scope, true)
                .putLong(KEY_GATE_TIMESTAMP_PREFIX + scope, System.currentTimeMillis())
                .apply()
        }
    }

    fun resetAttempts(scope: String) {
        prefs.edit()
            .putInt(KEY_ATTEMPTS_PREFIX + scope, DEFAULT_ATTEMPTS)
            .putBoolean(KEY_GATED_PREFIX + scope, false)
            .remove(KEY_GATE_TIMESTAMP_PREFIX + scope)
            .apply()
    }

    fun isGateLocked(scope: String): Boolean =
        prefs.getBoolean(KEY_GATED_PREFIX + scope, false)

    fun resetAllAttemptsForScopes(scopes: Set<String>) {
        val editor = prefs.edit()
        for (s in scopes) {
            editor.putInt(KEY_ATTEMPTS_PREFIX + s, DEFAULT_ATTEMPTS)
            editor.putBoolean(KEY_GATED_PREFIX + s, false)
            editor.remove(KEY_GATE_TIMESTAMP_PREFIX + s)
        }
        editor.putInt(KEY_ATTEMPTS_PREFIX + packageName, DEFAULT_ATTEMPTS)
        editor.putBoolean(KEY_GATED_PREFIX + packageName, false)
        editor.remove(KEY_GATE_TIMESTAMP_PREFIX + packageName)
        editor.apply()
    }

    fun resetAllAttempts() {
        val locked = getLockedApps()
        resetAllAttemptsForScopes(locked + packageName)
    }

    // NEW: kunin ang timestamp kung kailan na-lock ang scope
    fun getGateLockedAt(scope: String): Long =
        prefs.getLong(KEY_GATE_TIMESTAMP_PREFIX + scope, 0L)
}