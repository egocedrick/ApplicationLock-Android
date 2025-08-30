package com.example.applicationlock.data

/**
 * Thin wrapper around Prefs for the locked-apps domain.
 */
class LockedAppsRepo(context: android.content.Context) {
    private val prefs = Prefs(context)

    fun add(pkg: String) = prefs.addLockedApp(pkg)
    fun remove(pkg: String) = prefs.removeLockedApp(pkg)
    fun getLocked(): Set<String> = prefs.getLockedApps()
    fun isLocked(pkg: String?): Boolean = pkg != null && prefs.isAppLocked(pkg)
    fun resetAllAttempts() = prefs.resetAllAttempts()
}
