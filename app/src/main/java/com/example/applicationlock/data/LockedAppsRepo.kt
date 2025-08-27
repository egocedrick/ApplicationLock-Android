package com.example.applicationlock.data

import android.content.Context
import androidx.core.content.edit

class LockedAppsRepo(context: Context) {
    private val prefs = context.getSharedPreferences("locked_apps", Context.MODE_PRIVATE)

    fun getLocked(): Set<String> = prefs.getStringSet("packages", emptySet()) ?: emptySet()

    fun add(pkg: String) {
        val s = getLocked().toMutableSet()
        s.add(pkg)
        prefs.edit { putStringSet("packages", s) }
    }

    fun remove(pkg: String) {
        val s = getLocked().toMutableSet()
        s.remove(pkg)
        prefs.edit { putStringSet("packages", s) }
    }

    fun isLocked(pkg: String?) = pkg != null && getLocked().contains(pkg)
}
