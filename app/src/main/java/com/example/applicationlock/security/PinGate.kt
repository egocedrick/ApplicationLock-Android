package com.example.applicationlock.security

/**
 * Session-level unlock tracker. Kept for API compatibility but NOT used to persist across app closings.
 */
object PinGate {
    private val unlocked = mutableSetOf<String>()
    fun unlockForSession(pkg: String) { unlocked.add(pkg) }
    fun isAppUnlocked(pkg: String) = unlocked.contains(pkg)
    fun clear(pkg: String) { unlocked.remove(pkg) }
    fun clearAll() = unlocked.clear()
}
