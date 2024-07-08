package com.example.whisper

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.whisper.MyApplication.MyApplication
import com.example.whisper.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var myApp: MyApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sử dụng View Binding để setContentView
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = resources.getColor(R.color.blue)
        window.navigationBarColor = resources.getColor(R.color.blue)


        myApp = application as MyApplication

        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)

        // Sử dụng Handler để trì hoãn 2 giây
        Handler(Looper.getMainLooper()).postDelayed({
            if (isLoggedIn) {
                myApp.loginUserId = sharedPref.getString("userId", "").toString()
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 1000) // 2000 milliseconds = 2 seconds
    }
}
