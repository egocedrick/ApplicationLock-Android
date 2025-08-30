package com.example.applicationlock

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.applicationlock.data.PinStore

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pinStore = PinStore(this)
        if (!pinStore.isAppPinSet()) {
            startActivity(Intent(this, AppPinActivity::class.java).putExtra(Constants.EXTRA_SET_FOR, "app"))
        } else {
            startActivity(Intent(this, LockActivity::class.java).putExtra(Constants.EXTRA_TARGET_PKG, packageName))
        }
        finish()
    }
}
