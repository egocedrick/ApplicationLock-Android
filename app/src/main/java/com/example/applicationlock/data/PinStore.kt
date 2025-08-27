package com.example.applicationlock.data

import android.content.Context
import android.util.Base64
import java.security.MessageDigest

/**
 * Single store for two hashed PINs (appPin and lockPin).
 * Persisted in SharedPreferences (survives reboot).
 */
class PinStore(context: Context) {
    private val prefs = context.getSharedPreferences("pin_store", Context.MODE_PRIVATE)

    private fun hash(pin: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(pin.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun saveAppPin(pin: String) {
        prefs.edit().putString(KEY_APP_PIN, hash(pin)).apply()
    }

    fun verifyAppPin(pin: String): Boolean {
        val stored = prefs.getString(KEY_APP_PIN, null) ?: return false
        return stored == hash(pin)
    }

    fun isAppPinSet(): Boolean = prefs.contains(KEY_APP_PIN)

    fun saveLockPin(pin: String) {
        prefs.edit().putString(KEY_LOCK_PIN, hash(pin)).apply()
    }

    fun verifyLockPin(pin: String): Boolean {
        val stored = prefs.getString(KEY_LOCK_PIN, null) ?: return false
        return stored == hash(pin)
    }

    fun isLockPinSet(): Boolean = prefs.contains(KEY_LOCK_PIN)

    companion object {
        private const val KEY_APP_PIN = "app_pin_hash"
        private const val KEY_LOCK_PIN = "lock_pin_hash"
    }
}
