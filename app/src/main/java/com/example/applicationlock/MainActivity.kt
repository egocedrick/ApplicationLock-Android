package com.example.applicationlock

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.applicationlock.data.PinStore
import com.example.applicationlock.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pinStore = PinStore(this)
        mainViewModel = MainViewModel(pinStore)

        if (!mainViewModel.isAppPinSet()) {
            startActivity(Intent(this, AppPinActivity::class.java).putExtra(Constants.EXTRA_SET_FOR, "app"))
        } else {
            startActivity(Intent(this, LockActivity::class.java).putExtra(Constants.EXTRA_TARGET_PKG, packageName))
        }
        finish()
    }
}