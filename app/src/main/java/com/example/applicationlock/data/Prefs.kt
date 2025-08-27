package com.example.applicationlock.data

import android.content.Context

/**
 * Stores locked apps list and other lightweight prefs.
 */
class Prefs(context: Context) {
    private val prefs = context.getSharedPreferences("applocker_prefs", Context.MODE_PRIVATE)

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

    companion object {
        private const val KEY_LOCKED_APPS = "locked_apps"
    }
}
