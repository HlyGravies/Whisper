package com.example.whisper

import android.annotation.SuppressLint
import com.example.whisper.Polling.NotificationService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.whisper.MyApplication.MyApplication
import com.example.whisper.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var notificationReceiver: BroadcastReceiver
    @Inject
    lateinit var myApp : MyApplication
    @Inject
    lateinit var client: okhttp3.OkHttpClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var navController = findNavController(R.id.fragmentContainerView)
        var bottomnav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomnav.setupWithNavController(navController)
        myApp.userId = myApp.loginUserId

        startService(Intent(this, NotificationService::class.java))
        notificationReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val notificationCount = intent.getIntExtra("notificationCount", 0)
                    Log.d("Maincheck", "onResponse: $notificationCount")
                updateNotificationBadge(notificationCount)
            }
        }
        registerReceiver(notificationReceiver, IntentFilter("com.example.whisper.NOTIFICATION"))
        binding.whisperBtn.setOnClickListener {
            val intent = Intent(this, WhisperActivity::class.java)
            startActivity(intent)
        }
        checkForNotificationsOnStartup()

    }
    private fun checkForNotificationsOnStartup() {
        val request = Request.Builder()
            .url("${myApp.apiUrl}/check_notifications.php?userId=${myApp.loginUserId}")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()

                if (responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    val notificationCount = jsonResponse.getInt("notificationCount")
                    runOnUiThread {
                        updateNotificationBadge(notificationCount)
                    }
                }
            }
        })
    }
    fun updateNotificationBadge(count: Int) {
        if (count > 0) {
            // Show badge with count
            val badge = binding.bottomNavigationView.getOrCreateBadge(R.id.notificationFragment)
            badge.number = count
            badge.isVisible = true
        } else {
            // Hide badge
            binding.bottomNavigationView.removeBadge(R.id.notificationFragment)
        }
    }
}