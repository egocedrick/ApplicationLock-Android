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
// import com.example.applicationlock.data.Prefs
// import com.example.applicationlock.security.AttemptLimiter
import com.example.applicationlock.security.PinGate

class LockActivity : Activity() {

    private lateinit var pinInput: EditText
    private lateinit var submit: Button
    private lateinit var status: TextView
    private lateinit var pinStore: PinStore
    private lateinit var repo: LockedAppsRepo
    private var targetPkg: String = ""
    private var entryPoint: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock)

        pinInput = findViewById(R.id.pin_input)
        submit = findViewById(R.id.submit_button)
        status = findViewById(R.id.status_text)

        pinStore = PinStore(this)
        repo = LockedAppsRepo(this)

        targetPkg = intent.getStringExtra(Constants.EXTRA_TARGET_PKG) ?: packageName
        entryPoint = intent.getStringExtra(Constants.EXTRA_ENTRY_POINT)

        status.text = if (targetPkg == packageName) {
            getString(R.string.enter_app_pin)
        } else {
            getString(R.string.enter_lock_pin)
        }

        submit.setOnClickListener {
            val entered = pinInput.text.toString()
            val ok = if (targetPkg == packageName) {
                pinStore.verifyAppPin(entered)
            } else {
                pinStore.verifyLockPin(entered)
            }

            if (ok) {

                if (targetPkg == packageName) {
                    if (entryPoint == "notification") {
                        startActivity(Intent(this, SettingsActivity::class.java))
                    } else {
                        startActivity(Intent(this, SettingsActivity::class.java))
                    }
                } else {
                    PinGate.unlockForSession(targetPkg)
                }
                finish()
            } else {

                status.text = getString(R.string.wrong_pin)
                Toast.makeText(this, getString(R.string.wrong_pin), Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("GestureBackNavigation")
    override fun onBackPressed() {
    }
}