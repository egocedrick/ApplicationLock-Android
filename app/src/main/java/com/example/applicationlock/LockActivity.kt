package com.example.applicationlock

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.applicationlock.data.PinStore
import com.example.applicationlock.data.Prefs
import com.example.applicationlock.security.AttemptLimiter
import com.example.applicationlock.security.PinGate

class LockActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TARGET_PKG = "target_pkg"
    }

    private lateinit var pinStore: PinStore
    private lateinit var prefs: Prefs
    private lateinit var limiter: AttemptLimiter
    private lateinit var targetPkg: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1) Make sure the layout name matches this file (res/layout/activity_lock.xml).
        setContentView(R.layout.activity_lock)

        // 2) Init helpers (these classes must exist in your project under the packages shown)
        pinStore = PinStore(this)           // verifies appPin & lockPin (hashed or plain per your implementation)
        prefs = Prefs(this)                 // for list of locked apps (if needed)
        targetPkg = intent.getStringExtra(EXTRA_TARGET_PKG) ?: packageName
        limiter = AttemptLimiter(this, scope = targetPkg, maxAttempts = 5)

        // 3) Bind views (IDs must match those in activity_lock.xml)
        val txtApp = findViewById<TextView>(R.id.txtApp)
        val editPin = findViewById<EditText>(R.id.editPin)
        val btnUnlock = findViewById<Button>(R.id.btnUnlock)

        // 4) Show friendly label
        txtApp.text = if (targetPkg == packageName) "App Locker" else "Unlock: $targetPkg"

        // 5) Unlock flow
        btnUnlock.setOnClickListener {
            if (limiter.isLockedOut()) {
                Toast.makeText(this, "Too many failed attempts. PIN entry disabled.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val entered = editPin.text.toString()

            // verify using PinStore API (app PIN vs lock PIN)
            val success = if (targetPkg == packageName) {
                pinStore.verifyAppPin(entered)
            } else {
                pinStore.verifyLockPin(entered)
            }

            if (success) {
                limiter.reset()
                if (targetPkg == packageName) {
                    // app itself -> go to Settings or main area
                    startActivity(Intent(this, SettingsActivity::class.java))
                    finish()
                } else {
                    // third-party app: unlock for this session and finish to reveal app
                    PinGate.unlockForSession(targetPkg)
                    finish()
                }
            } else {
                limiter.registerFailure()
                val rem = limiter.remainingAttempts()
                if (rem > 0) {
                    Toast.makeText(this, "Wrong PIN ($rem tries left)", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Too many failed attempts. PIN entry disabled.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    @SuppressLint("GestureBackNavigation")
    override fun onBackPressed() {
        super.onBackPressed()
        // prevent bypass — do nothing
    }
}
