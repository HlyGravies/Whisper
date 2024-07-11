package com.example.whisper.Polling

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.example.whisper.MyApplication.MyApplication
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class NotificationService : Service() {
    private val client = OkHttpClient()
    private lateinit var myApp: MyApplication
    private val handler = Handler()
    private val interval: Long = 1000 // 1 second
    override fun onCreate() {
        super.onCreate()
        myApp = application as MyApplication
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handler.post(pollingRunnable)
        return START_STICKY
    }

    private val pollingRunnable = object : Runnable {
        override fun run() {
            checkForNotifications()
            handler.postDelayed(this, interval)
        }
    }

    private fun checkForNotifications() {
        val request = Request.Builder()
            .url("${myApp.apiUrl}/check_notifications.php?userId=${myApp.loginUserId}")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("poll", "onResponse: $e")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("poll", "onResponse: $responseBody")
                if (responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    val notificationCount = jsonResponse.getInt("notificationCount")

                    val intent = Intent("com.example.whisper.NOTIFICATION")
                    intent.putExtra("notificationCount", notificationCount)
                    sendBroadcast(intent)
                }
            }
        })
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
