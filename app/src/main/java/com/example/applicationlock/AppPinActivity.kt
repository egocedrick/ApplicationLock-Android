package com.example.applicationlock

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.applicationlock.data.PinStore
import com.example.applicationlock.viewmodel.AppPinViewModel


class AppPinActivity : AppCompatActivity() {
    private lateinit var appPinViewModel: AppPinViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_pin)

        val edit = findViewById<EditText>(R.id.edit_pin)
        val btn = findViewById<Button>(R.id.btn_confirm_pin)
        val pinStore = PinStore(this)
        val setFor = intent.getStringExtra(Constants.EXTRA_SET_FOR) ?: "app"

        appPinViewModel = AppPinViewModel(pinStore)

        appPinViewModel.pinSetupStatus.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            if (message == "PIN saved") {
                startActivity(Intent(this, SettingsActivity::class.java))
                finish()
            }
        }

        btn.setOnClickListener {
            val pin = edit.text.toString().trim()
            appPinViewModel.savePin(pin, setFor)
        }
    }
}