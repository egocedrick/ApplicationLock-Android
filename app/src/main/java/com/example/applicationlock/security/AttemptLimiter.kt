package com.example.applicationlock.security

import android.content.Context
import com.example.applicationlock.data.Prefs

/**
 * Small adapter for attempt logic (wraps Prefs methods).
 */
class AttemptLimiter(context: Context, private val scope: String) {
    private val prefs = Prefs(context)

    fun registerFailure() { prefs.decrementAttempts(scope) }
    fun reset() { prefs.resetAttempts(scope) }
    fun isLockedOut(): Boolean = prefs.isGateLocked(scope)
    fun remainingAttempts(): Int = prefs.getAttemptsLeft(scope)
}