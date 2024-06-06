package com.example.whisper.MyApplication

import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.whisper.LoginActivity
import com.example.whisper.R
import com.example.whisper.SearchActivity
import com.example.whisper.TimelineActivity
import com.example.whisper.UserEditActivity
import com.example.whisper.UserInfoActivity
import com.example.whisper.WhisperActivity
import android.content.Context
class overMenu(private val activity: AppCompatActivity){
    val myApp = activity.application as MyApplication
    fun onCreateOptionsMenu(menu: Menu): Boolean {
        activity.menuInflater.inflate(R.menu.overflowmenu, menu)
        return true
    }

    fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.timeline -> {
                val intent = Intent(activity, TimelineActivity::class.java)
                activity.startActivity(intent)
            }

            R.id.search -> {
                val intent = Intent(activity, SearchActivity::class.java)
                activity.startActivity(intent)
            }

            R.id.whisper -> {
                val intent = Intent(activity, WhisperActivity::class.java)
                activity.startActivity(intent)
            }

            R.id.myprofile -> {
                // Navigate to My Profile screen
                val intent = Intent(activity, UserInfoActivity::class.java)
                intent.putExtra("userId", myApp.loginUserId)
                activity.startActivity(intent)
            }

            R.id.profileedit -> {
                val intent = Intent(activity, UserEditActivity::class.java)
                activity.startActivity(intent)
            }

            R.id.logout -> {
                // Clear loginUserId global variable
                myApp.loginUserId = "lo"
                // Navigate to Login screen and clear previous screen info
                val intent = Intent(activity, LoginActivity::class.java)
//                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                activity.startActivity(intent)
            }
        }
        return true
    }
}