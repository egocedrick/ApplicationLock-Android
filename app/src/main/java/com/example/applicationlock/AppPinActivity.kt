package com.example.applicationlock

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.applicationlock.data.PinStore

/**
 * Used to set/change either the appPin or the lockPin.
 * Launch with extra "SET_FOR" = "app" or "lock".
 * When save pressed, only saves and returns to Settings (does NOT immediately lock or validate).
 */
class AppPinActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SET_FOR = "SET_FOR" // "app" or "lock"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_pin)

        val editPin = findViewById<EditText>(R.id.editAppPin)
        val btnConfirm = findViewById<Button>(R.id.btnConfirmAppPin)
        val pinStore = PinStore(this)

        val setFor = intent.getStringExtra(EXTRA_SET_FOR) ?: "app"

        btnConfirm.setOnClickListener {
            val pin = editPin.text.toString()
            if (pin.length in 4..8) {
                if (setFor == "app") {
                    pinStore.saveAppPin(pin)
                } else {
                    pinStore.saveLockPin(pin)
                }
                Toast.makeText(this, "PIN saved", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, SettingsActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "PIN must be 4–8 digits", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
