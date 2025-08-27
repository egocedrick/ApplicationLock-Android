package com.example.applicationlock.security

import android.content.Context

/**
 * Permanent lock after [maxAttempts] failures for a scope (e.g. "app" or package name).
 */
class AttemptLimiter(
    context: Context,
    private val scope: String,
    private val maxAttempts: Int = 5
) {
    private val prefs = context.getSharedPreferences("attempts_$scope", Context.MODE_PRIVATE)

    fun registerFailure() {
        val count = prefs.getInt(KEY_COUNT, 0) + 1
        prefs.edit().putInt(KEY_COUNT, count).apply()
    }

    fun reset() {
        prefs.edit().putInt(KEY_COUNT, 0).apply()
    }

    fun isLockedOut(): Boolean {
        val count = prefs.getInt(KEY_COUNT, 0)
        return count >= maxAttempts
    }

    fun remainingAttempts(): Int {
        val count = prefs.getInt(KEY_COUNT, 0)
        return (maxAttempts - count).coerceAtLeast(0)
    }

    companion object {
        private const val KEY_COUNT = "count"
    }
}
