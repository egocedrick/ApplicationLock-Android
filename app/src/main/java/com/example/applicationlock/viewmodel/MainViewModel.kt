package com.example.applicationlock.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.applicationlock.data.PinStore

// ViewModel
class MainViewModel(private val pinStore: PinStore) : ViewModel() {
    fun isAppPinSet(): Boolean = pinStore.isAppPinSet()
}