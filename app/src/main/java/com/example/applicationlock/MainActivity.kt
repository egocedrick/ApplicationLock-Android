package com.example.applicationlock

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.applicationlock.data.PinStore

/**
 * Entry point: if app PIN not set -> open AppPinActivity (setup).
 * Otherwise always redirect to LockActivity (so app asks PIN every launch).
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pinStore = PinStore(this)
        if (!pinStore.isAppPinSet()) {
            startActivity(Intent(this, AppPinActivity::class.java))
        } else {
            val i = Intent(this, LockActivity::class.java)
            i.putExtra(LockActivity.EXTRA_TARGET_PKG, packageName) // protect self
            startActivity(i)
        }
        finish()
    }
}
