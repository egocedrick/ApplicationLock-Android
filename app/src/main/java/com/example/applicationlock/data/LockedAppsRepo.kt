package com.example.applicationlock.data

import android.content.Context

/**
 * Thin wrapper around Prefs for the locked-apps domain.
 * NOTE: Attempt reset calls commented out (unlimited retries).
 */
class LockedAppsRepo(context: Context) {
    private val prefs = Prefs(context)

    fun add(pkg: String) = prefs.addLockedApp(pkg)

    fun remove(pkg: String) {
        prefs.removeLockedApp(pkg)
        // COMMENTED OUT: clear attempts disabled (unlimited retries)
        // prefs.resetAttempts(pkg)
    }

    fun getLocked(): Set<String> = prefs.getLockedApps()
    fun isLocked(pkg: String?): Boolean = pkg != null && prefs.isAppLocked(pkg)

    // COMMENTED OUT: bulk reset disabled (unlimited retries)
    // fun resetAllAttempts() = prefs.resetAllAttempts()
}