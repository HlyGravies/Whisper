package com.example.whisper

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.whisper.MyApplication.MyApplication
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class UserEditActivity : AppCompatActivity() {
    private lateinit var userNameEdit: EditText
    private lateinit var profileEdit: EditText
    private lateinit var changeButton: Button
    private lateinit var cancelButton: Button
    private lateinit var userImage: ImageView
    private lateinit var userIdText: TextView
    lateinit var myApp : MyApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_edit)

        userNameEdit = findViewById(R.id.userNameEdit)
        profileEdit = findViewById(R.id.profileEdit)
        changeButton = findViewById(R.id.changeButton)
        cancelButton = findViewById(R.id.cancelButton)
        userImage = findViewById(R.id.userImage)
        userIdText = findViewById(R.id.userIdText)
        myApp = application as MyApplication

        // Request user information
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(myApp.apiUrl)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@UserEditActivity, e.message, Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                val json = JSONObject(responseData)

                if (json.has("error")) {
                    runOnUiThread {
                        Toast.makeText(this@UserEditActivity, json.getString("error"), Toast.LENGTH_LONG).show()
                    }
                } else {
                    runOnUiThread {
                        // Set the retrieved data to each object
                        userNameEdit.setText(json.getString("username"))
                        profileEdit.setText(json.getString("profile"))
                        // Set user image and user id text
                    }
                }
            }
        })

        changeButton.setOnClickListener {
            val client = OkHttpClient()
            val mediaType: MediaType = "application/json; charset=utf-8".toMediaType()
            val userName = userNameEdit.text.toString()
            val profile = profileEdit.text.toString()
            val requestBody = JSONObject().apply {
                put("username", userName)
                put("profile", profile)
            }.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url(myApp.apiUrl) // Replace with your actual User Update API endpoint
                .put(requestBody) // Use put() for a PUT request, post() for a POST request
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@UserEditActivity, e.message, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseData = response.body?.string()
                    val json = JSONObject(responseData)

                    if (json.has("error")) {
                        runOnUiThread {
                            Toast.makeText(this@UserEditActivity, json.getString("error"), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        runOnUiThread {
                            // Start a new activity and finish the current one
                            val intent = Intent(this@UserEditActivity, UserInfoActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            })
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.overflowmenu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.timeline -> {
                val intent = Intent(this, TimelineActivity::class.java)
                startActivity(intent)
            }

            R.id.search -> {
                val intent = Intent(this, SearchActivity::class.java)
                startActivity(intent)
            }

            R.id.whisper -> {
                val intent = Intent(this, WhisperActivity::class.java)
                startActivity(intent)
            }

            R.id.myprofile -> {
                // Navigate to My Profile screen
                val intent = Intent(this, UserInfoActivity::class.java)
                intent.putExtra("userId", myApp.loginUserId)
                startActivity(intent)
            }

            R.id.profileedit -> {
                val intent = Intent(this, UserEditActivity::class.java)
                startActivity(intent)
            }

            R.id.logout -> {
                // Clear loginUserId global variable
                myApp.loginUserId = ""
                // Navigate to Login screen and clear previous screen info
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}