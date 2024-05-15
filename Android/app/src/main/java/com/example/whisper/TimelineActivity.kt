package com.example.whisper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.example.whisper.MyApplication.overMenu

class TimelineActivity : AppCompatActivity() {
    private lateinit var overMenu: overMenu
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timeline)

        overMenu = overMenu(this)
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Gọi onCreateOptionsMenu từ overMenu
        return overMenu.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Gọi onOptionsItemSelected từ overMenu
        return overMenu.onOptionsItemSelected(item)
    }
}