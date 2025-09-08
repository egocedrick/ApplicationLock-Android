package com.example.applicationlock.security

import android.content.Context
import com.example.applicationlock.data.Prefs

/**
 * Small adapter for attempt logic (wraps Prefs methods).
 */
class AttemptLimiter(context: Context, private val scope: String) {
    private val prefs = Prefs(context)

    fun registerFailure() {
        prefs.decrementAttempts(scope)
    }

    fun reset() {
        prefs.resetAttempts(scope)
    }

    fun isLockedOut(): Boolean =
        prefs.isGateLocked(scope)

    fun remainingAttempts(): Int =
        prefs.getAttemptsLeft(scope)

    /**
     * Returns remaining lockout time in milliseconds, or 0 if not locked.
     */
    fun remainingLockMillis(): Long =
        prefs.getBlockRemainingMillis(scope)

    /**
     * Convenience: returns remaining lockout time in minutes.
     */
    fun remainingLockMinutes(): Long =
        (remainingLockMillis() / 60000L).coerceAtLeast(0L)
}
