package com.example.whisper.MyApplication

import android.app.Application

class MyApplication : Application() {
    // Declare global variables loginUserId and apiUrl
    var loginUserId: String = ""
    var apiUrl: String = "http://click.ecc.ac.jp/ecc/whisperSystem/"

    // Getter method for loginUserId
    fun getLoginUserId(): String {
        return loginUserId
    }

    // Setter method for loginUserId
    fun setLoginUserId(loginUserId: String) {
        this.loginUserId = loginUserId
    }

    // Getter method for apiUrl
    fun getApiUrl(): String {
        return apiUrl
    }

    // Setter method for apiUrl
    fun setApiUrl(apiUrl: String) {
        this.apiUrl = apiUrl
    }

    override fun onCreate() {
        super.onCreate()
        // Perform initialization tasks for the application here (if needed)
    }
}
