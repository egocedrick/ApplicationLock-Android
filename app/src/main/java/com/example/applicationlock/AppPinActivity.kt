package com.example.applicationlock

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.applicationlock.data.PinStore

/**
 * Set PIN screen. Use extra Constants.EXTRA_SET_FOR: "app" or "lock".
 * NOTE: This activity only sets/saves PIN.
 * No attempt limiter logic here, so unlimited retries unaffected.
 */
class AppPinActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_pin)

        val edit = findViewById<EditText>(R.id.edit_pin)
        val btn = findViewById<Button>(R.id.btn_confirm_pin)
        val pinStore = PinStore(this)

        val setFor = intent.getStringExtra(Constants.EXTRA_SET_FOR) ?: "app"

        btn.setOnClickListener {
            val pin = edit.text.toString().trim()
            if (pin.length < 4 || pin.length > 8) {
                Toast.makeText(this, "PIN must be 4–8 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (setFor == "app") pinStore.saveAppPin(pin) else pinStore.saveLockPin(pin)
            Toast.makeText(this, "PIN saved", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
        }
    }
}