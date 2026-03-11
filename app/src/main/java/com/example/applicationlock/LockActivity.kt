package com.example.applicationlock

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.applicationlock.data.PinStore
import com.example.applicationlock.viewmodel.LockViewModel

class LockActivity : AppCompatActivity() {
    private lateinit var pinInput: EditText
    private lateinit var submit: Button
    private lateinit var status: TextView
    private lateinit var lockViewModel: LockViewModel
    private var targetPkg: String = ""
    private var entryPoint: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock)

        pinInput = findViewById(R.id.pin_input)
        submit = findViewById(R.id.submit_button)
        status = findViewById(R.id.status_text)

        val pinStore = PinStore(this)
        lockViewModel = LockViewModel(pinStore)

        targetPkg = intent.getStringExtra(Constants.EXTRA_TARGET_PKG) ?: packageName
        entryPoint = intent.getStringExtra(Constants.EXTRA_ENTRY_POINT)

        status.text = if (targetPkg == packageName) {
            getString(R.string.enter_app_pin)
        } else {
            getString(R.string.enter_lock_pin)
        }

        lockViewModel.pinStatus.observe(this) { ok ->
            if (!ok) {
                status.text = getString(R.string.wrong_pin)
                Toast.makeText(this, getString(R.string.wrong_pin), Toast.LENGTH_SHORT).show()
            }
        }

        lockViewModel.unlockAction.observe(this) { action ->
            when (action) {
                "settings" -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    finish()
                }
                "unlocked" -> {
                    finish()
                }
            }
        }

        submit.setOnClickListener {
            val entered = pinInput.text.toString()
            lockViewModel.validatePin(entered, targetPkg, packageName, entryPoint)
        }
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("GestureBackNavigation")
    override fun onBackPressed() {
        super.onBackPressed()
    }
}