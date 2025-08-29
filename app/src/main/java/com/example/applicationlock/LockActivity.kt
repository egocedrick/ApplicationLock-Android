package com.example.applicationlock

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.applicationlock.data.PinStore
import com.example.applicationlock.security.AttemptLimiter
import com.example.applicationlock.security.LockState
import com.example.applicationlock.security.PinGate

class LockActivity : Activity() {

    private lateinit var pinInput: EditText
    private lateinit var submit: Button
    private lateinit var status: TextView
    private lateinit var pinStore: PinStore
    private lateinit var limiter: AttemptLimiter
    private var targetPkg: String = ""

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        targetPkg = intent.getStringExtra(Constants.EXTRA_TARGET_PKG) ?: packageName
        pinStore = PinStore(this)
        limiter = AttemptLimiter(this, targetPkg)

        setContentView(R.layout.activity_lock)
        pinInput = findViewById(R.id.edit_pin_input)
        submit = findViewById(R.id.btn_unlock)
        status = findViewById(R.id.txt_lock_message)

        if (limiter.isLockedOut()) {
            status.text = "Too many wrong attempts. Locked."
            submit.isEnabled = false
            return
        }

        if (targetPkg != packageName && PinGate.isAppUnlocked(targetPkg)) {
            finish()
            return
        }

        status.text = if (targetPkg == packageName) "Enter App PIN" else "Enter Lock PIN"

        submit.setOnClickListener {
            val entered = pinInput.text.toString()
            val ok = if (targetPkg == packageName) pinStore.verifyAppPin(entered) else pinStore.verifyLockPin(entered)

            if (ok) {
                limiter.reset()
                if (targetPkg == packageName) {
                    try { startActivity(Intent(this, SettingsActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } catch (_: Exception) {}
                } else {
                    PinGate.unlockForSession(targetPkg)
                }
                finish()
            } else {
                limiter.registerFailure()
                val rem = limiter.remainingAttempts()
                if (limiter.isLockedOut()) {
                    status.text = "Too many wrong attempts. Locked."
                    submit.isEnabled = false
                    Toast.makeText(this, "Too many wrong attempts. Locked.", Toast.LENGTH_LONG).show()
                } else {
                    status.text = "Wrong PIN ($rem)"
                    Toast.makeText(this, "Wrong PIN", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        LockState.lockActivityVisible = true
    }

    override fun onStop() {
        super.onStop()
        LockState.lockActivityVisible = false
    }

    override fun onDestroy() {
        super.onDestroy()
        LockState.lockActivityVisible = false
    }

    @SuppressLint("GestureBackNavigation")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // prevent bypass
    }
}
