package com.example.applicationlock.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Central preferences helper for pins, locked apps, and attempt tracking.
 * Full feature set preserved.
 */
class Prefs(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_lock_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_APP_PIN = "app_pin"
        private const val KEY_LOCK_PIN = "lock_pin"
        private const val KEY_LOCKED_APPS = "locked_apps"
        private const val KEY_ATTEMPTS_PREFIX = "attempts_"   // attempts_<scope>
        private const val KEY_GATED_PREFIX = "gated_"         // gated_<scope> (true when blocked)
        private const val DEFAULT_ATTEMPTS = 3
    }

    private val packageName: String = context.packageName

    // App PIN (ApplicationLock itself)
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
    fun getLockedApps(): MutableSet<String> {
        // return a mutable set copy to avoid SharedPreferences returning internal set
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

    // Attempt limiter API (per-scope)
    fun getAttemptsLeft(scope: String): Int =
        prefs.getInt(KEY_ATTEMPTS_PREFIX + scope, DEFAULT_ATTEMPTS)

    fun decrementAttempts(scope: String) {
        val cur = getAttemptsLeft(scope)
        val next = (cur - 1).coerceAtLeast(0)
        prefs.edit().putInt(KEY_ATTEMPTS_PREFIX + scope, next).apply()
        if (next <= 0) {
            prefs.edit().putBoolean(KEY_GATED_PREFIX + scope, true).apply()
        }
    }

    fun resetAttempts(scope: String) {
        prefs.edit()
            .putInt(KEY_ATTEMPTS_PREFIX + scope, DEFAULT_ATTEMPTS)
            .putBoolean(KEY_GATED_PREFIX + scope, false)
            .apply()
    }

    fun isGateLocked(scope: String): Boolean = prefs.getBoolean(KEY_GATED_PREFIX + scope, false)

    // Reset attempts for a set of scopes (locked apps + app itself)
    fun resetAllAttemptsForScopes(scopes: Set<String>) {
        val editor = prefs.edit()
        for (s in scopes) {
            editor.putInt(KEY_ATTEMPTS_PREFIX + s, DEFAULT_ATTEMPTS)
            editor.putBoolean(KEY_GATED_PREFIX + s, false)
        }
        // ensure app itself is reset too
        editor.putInt(KEY_ATTEMPTS_PREFIX + packageName, DEFAULT_ATTEMPTS)
        editor.apply()
    }

    // Convenience: reset attempts for current locked apps + app
    fun resetAllAttempts() {
        val locked = getLockedApps()
        resetAllAttemptsForScopes(locked + packageName)
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
