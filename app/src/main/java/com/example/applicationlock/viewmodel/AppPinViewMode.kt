package com.example.applicationlock.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.applicationlock.data.PinStore

// ViewModel
class AppPinViewModel(private val pinStore: PinStore) : ViewModel() {
    private val _pinSetupStatus = MutableLiveData<String>()
    val pinSetupStatus: LiveData<String> get() = _pinSetupStatus

    fun savePin(pin: String, setFor: String) {
        if (pin.length < 4 || pin.length > 8) {
            _pinSetupStatus.value = "PIN must be 4–8 digits"
            return
        }
        if (setFor == "app") pinStore.saveAppPin(pin) else pinStore.saveLockPin(pin)
        _pinSetupStatus.value = "PIN saved"
    }
}