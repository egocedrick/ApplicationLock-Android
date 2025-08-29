package com.example.applicationlock.data

import android.content.Context

class LockedAppsRepo(context: Context) {
    private val prefs = Prefs(context)

    fun add(pkg: String) = prefs.addLockedApp(pkg)
    fun remove(pkg: String) = prefs.removeLockedApp(pkg)
    fun getLocked(): Set<String> = prefs.getLockedApps()
    fun isLocked(pkg: String?): Boolean = pkg != null && prefs.isAppLocked(pkg)
}
