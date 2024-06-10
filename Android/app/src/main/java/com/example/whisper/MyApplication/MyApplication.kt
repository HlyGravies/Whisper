package com.example.whisper.MyApplication

import android.app.Application

class MyApplication : Application() {
    // Declare global variables loginUserId and apiUrl
    var loginUserId: String = ""
    var apiUrl: String = "http://click.ecc.ac.jp/ecc/whisper24_a/API/"

}
