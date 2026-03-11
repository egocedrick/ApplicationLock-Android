package com.example.applicationlock.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.applicationlock.data.PinStore
import com.example.applicationlock.security.PinGate

class LockViewModel(private val pinStore: PinStore) : ViewModel() {
    private val _pinStatus = MutableLiveData<Boolean>()
    val pinStatus: LiveData<Boolean> get() = _pinStatus

    private val _unlockAction = MutableLiveData<String>()
    val unlockAction: LiveData<String> get() = _unlockAction

    fun validatePin(entered: String, targetPkg: String, packageName: String, entryPoint: String?) {
        val ok = if (targetPkg == packageName) {
            pinStore.verifyAppPin(entered)
        } else {
            pinStore.verifyLockPin(entered)
        }

        if (ok) {
            if (targetPkg == packageName) {
                _unlockAction.value = "settings"
            } else {
                PinGate.unlockForSession(targetPkg)
                _unlockAction.value = "unlocked"
            }
        }
        _pinStatus.value = ok
    }
}