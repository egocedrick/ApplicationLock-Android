package com.example.applicationlock.security

import android.content.Context
import com.example.applicationlock.data.Prefs

/**
 * Small adapter for attempt logic (wraps Prefs methods).
 * COMMENTED OUT: Entire class disabled to remove attempt limiter logic.
 */
/*
class AttemptLimiter(context: Context, private val scope: String) {
    private val prefs = Prefs(context)

    fun registerFailure() {
        // COMMENTED OUT: attempt tracking disabled
        prefs.decrementAttempts(scope)
    }

    fun reset() {
        // COMMENTED OUT: reset logic disabled
        prefs.resetAttempts(scope)
    }

    fun isLockedOut(): Boolean =
        // COMMENTED OUT: lockout check disabled
        prefs.isGateLocked(scope)

    fun remainingAttempts(): Int =
        // COMMENTED OUT: attempt count disabled
        prefs.getAttemptsLeft(scope)

    /**
     * Returns remaining lockout time in milliseconds, or 0 if not locked.
     */
    fun remainingLockMillis(): Long =
        // COMMENTED OUT: lockout timer disabled
        prefs.getBlockRemainingMillis(scope)

    /**
     * Convenience: returns remaining lockout time in minutes.
     */
    fun remainingLockMinutes(): Long =
        (remainingLockMillis() / 60000L).coerceAtLeast(0L)
}
*/