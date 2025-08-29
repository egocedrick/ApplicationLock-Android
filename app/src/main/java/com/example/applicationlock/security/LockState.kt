package com.example.applicationlock.security

object LockState {
    @Volatile
    var lockActivityVisible: Boolean = false
}
