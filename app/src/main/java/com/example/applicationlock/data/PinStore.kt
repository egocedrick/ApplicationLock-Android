package com.example.applicationlock.data

import android.content.Context

class PinStore(context: Context) {
    private val prefs = Prefs(context)

    fun saveAppPin(pin: String) = prefs.saveAppPin(pin)
    fun saveLockPin(pin: String) = prefs.saveLockPin(pin)

    fun isAppPinSet(): Boolean = prefs.isAppPinSet()
    fun isLockPinSet(): Boolean = prefs.isLockPinSet()

    fun verifyAppPin(pin: String): Boolean = prefs.getAppPin() == pin
    fun verifyLockPin(pin: String): Boolean = prefs.getLockPin() == pin
}
