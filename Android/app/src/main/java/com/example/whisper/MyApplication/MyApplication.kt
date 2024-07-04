package com.example.whisper.MyApplication

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {
    // Declare global variables loginUserId and apiUrl
    var loginUserId: String = ""
    var apiUrl: String = "https://click.ecc.ac.jp/ecc/whisper24_a/API/"
    //var apiUrl: String = "http://10.200.5.9/W/Whisper/API/"
    //
    var iconPath: String = ""
    var userId = ""

}
