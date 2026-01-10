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

        private const val KEY_ATTEMPTS_PREFIX = "attempts_"        // attempts_<scope>
        private const val KEY_GATED_PREFIX = "gated_"              // gated_<scope>
        private const val KEY_BLOCK_UNTIL_PREFIX = "block_until_"  // block_until_<scope>

        private const val DEFAULT_ATTEMPTS = 3
        private const val ONE_HOUR_MS = 60 * 60 * 1000L
    }

    private val packageName: String = context.packageName

    // ----------------- PIN MANAGEMENT -----------------

    fun isAppPinSet(): Boolean = prefs.contains(KEY_APP_PIN)
    fun saveAppPin(pin: String) = prefs.edit().putString(KEY_APP_PIN, pin).apply()
    fun getAppPin(): String? = prefs.getString(KEY_APP_PIN, null)
    fun clearAppPin() = prefs.edit().remove(KEY_APP_PIN).apply()

    fun isLockPinSet(): Boolean = prefs.contains(KEY_LOCK_PIN)
    fun saveLockPin(pin: String) = prefs.edit().putString(KEY_LOCK_PIN, pin).apply()
    fun getLockPin(): String? = prefs.getString(KEY_LOCK_PIN, null)
    fun clearLockPin() = prefs.edit().remove(KEY_LOCK_PIN).apply()

    // ----------------- LOCKED APPS -----------------

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

    fun isAppLocked(pkg: String?): Boolean =
        pkg != null && getLockedApps().contains(pkg)

    // ----------------- ATTEMPT LIMITER -----------------
    // COMMENTED OUT: All attempt limiter logic disabled for unlimited retries

    /*
    fun getAttemptsLeft(scope: String): Int {
        val attempts = prefs.getInt(KEY_ATTEMPTS_PREFIX + scope, DEFAULT_ATTEMPTS)

        // Auto-refresh if block expired
        if (scope == packageName && !isGateLocked(scope) && attempts <= 0) {
            resetAttempts(scope)
            return DEFAULT_ATTEMPTS
        }

        return attempts
    }

    fun decrementAttempts(scope: String) {
        val cur = getAttemptsLeft(scope)
        val next = (cur - 1).coerceAtLeast(0)
        val editor = prefs.edit().putInt(KEY_ATTEMPTS_PREFIX + scope, next)

        if (next <= 0) {
            if (scope == packageName) {
                // Lock the app itself for 1 hour
                val until = System.currentTimeMillis() + ONE_HOUR_MS
                editor.putLong(KEY_BLOCK_UNTIL_PREFIX + scope, until)
            }
            editor.putBoolean(KEY_GATED_PREFIX + scope, true)
        }

        editor.apply()
    }

    fun resetAttempts(scope: String) {
        prefs.edit()
            .putInt(KEY_ATTEMPTS_PREFIX + scope, DEFAULT_ATTEMPTS)
            .putBoolean(KEY_GATED_PREFIX + scope, false)
            .putLong(KEY_BLOCK_UNTIL_PREFIX + scope, 0L)
            .apply()
    }

    fun isGateLocked(scope: String): Boolean {
        if (scope == packageName) {
            val now = System.currentTimeMillis()
            val until = prefs.getLong(KEY_BLOCK_UNTIL_PREFIX + scope, 0L)

            return if (until > now) {
                true // still locked
            } else {
                // expired → refresh attempts automatically
                if (prefs.getBoolean(KEY_GATED_PREFIX + scope, false)) {
                    resetAttempts(scope)
                }
                false
            }
        }
        return prefs.getBoolean(KEY_GATED_PREFIX + scope, false)
    }

    fun getBlockRemainingMillis(scope: String): Long {
        val until = prefs.getLong(KEY_BLOCK_UNTIL_PREFIX + scope, 0L)
        val now = System.currentTimeMillis()
        return (until - now).coerceAtLeast(0L)
    }

    fun resetAllAttemptsForScopes(scopes: Set<String>) {
        val editor = prefs.edit()
        for (s in scopes) {
            editor.putInt(KEY_ATTEMPTS_PREFIX + s, DEFAULT_ATTEMPTS)
            editor.putBoolean(KEY_GATED_PREFIX + s, false)
            editor.putLong(KEY_BLOCK_UNTIL_PREFIX + s, 0L)
        }
        editor.apply()
    }

    fun resetAllAttempts() {
        val locked = getLockedApps()
        resetAllAttemptsForScopes(locked + packageName)
    }
    */

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}