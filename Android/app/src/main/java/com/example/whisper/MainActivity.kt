package com.example.whisper

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.whisper.MyApplication.MyApplication
import com.example.whisper.databinding.ActivityMainBinding
import com.example.whisper.fragment.ProfileFragment
import com.example.whisper.WhisperActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    @Inject
    lateinit var myApp : MyApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var navController = findNavController(R.id.fragmentContainerView)
        var bottomnav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomnav.setupWithNavController(navController)
        myApp.userId = myApp.loginUserId
        binding.whisperBtn.setOnClickListener {
            val intent = Intent(this, WhisperActivity::class.java)
            startActivity(intent)
        }
    }
}