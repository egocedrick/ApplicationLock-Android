package com.example.applicationlock

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.applicationlock.data.LockedAppsRepo
import com.example.applicationlock.data.PinStore
import com.example.applicationlock.security.AttemptLimiter
import com.example.applicationlock.security.PinGate

class LockActivity : Activity() {

    private lateinit var pinInput: EditText
    private lateinit var submit: Button
    private lateinit var status: TextView
    private lateinit var pinStore: PinStore
    private lateinit var repo: LockedAppsRepo
    private lateinit var limiter: AttemptLimiter
    private var targetPkg: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock)

        pinInput = findViewById(R.id.pin_input)
        submit = findViewById(R.id.submit_button)
        status = findViewById(R.id.status_text)

        pinStore = PinStore(this)
        repo = LockedAppsRepo(this)

        targetPkg = intent.getStringExtra(Constants.EXTRA_TARGET_PKG) ?: packageName
        limiter = AttemptLimiter(this, targetPkg)

        // If gate locked for scope -> block
        if (limiter.isLockedOut()) {
            status.text = getString(R.string.locked_too_many)
            submit.isEnabled = false
            return
        }

        // Show correct prompt text
        status.text = if (targetPkg == packageName) getString(R.string.enter_app_pin) else getString(R.string.enter_lock_pin)

        submit.setOnClickListener {
            val entered = pinInput.text.toString()
            val ok = if (targetPkg == packageName) {
                pinStore.verifyAppPin(entered)
            } else {
                pinStore.verifyLockPin(entered)
            }

            if (ok) {
                limiter.reset()
                if (targetPkg == packageName) {
                    // unlocked app itself -> open settings
                    startActivity(Intent(this, SettingsActivity::class.java))
                } else {
                    // unlock third-party app for session so reopening while in-app won't re-prompt
                    PinGate.unlockForSession(targetPkg)
                }
                finish()
            } else {
                limiter.registerFailure()
                val rem = limiter.remainingAttempts()
                if (limiter.isLockedOut()) {
                    status.text = getString(R.string.locked_too_many)
                    submit.isEnabled = false
                    Toast.makeText(this, getString(R.string.locked_too_many), Toast.LENGTH_LONG).show()
                } else {
                    status.text = getString(R.string.wrong_pin) + " (" + rem + ")"
                    Toast.makeText(this, getString(R.string.wrong_pin), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("GestureBackNavigation")
    override fun onBackPressed() {
        // prevent bypass
    }
}
