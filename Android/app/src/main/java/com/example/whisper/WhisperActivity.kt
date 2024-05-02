package com.example.whisper

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.whisper.MyApplication.MyApplication
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject

class WhisperActivity : AppCompatActivity() {
    lateinit var wisperEdit: EditText
    lateinit var wisperButton: Button
    lateinit var cancelButton: Button
    lateinit var myApp: MyApplication
    lateinit var toolbar: Toolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_whisper)

        wisperEdit = findViewById(R.id.wisperEdit)
        wisperButton = findViewById(R.id.wisperButton)
        cancelButton = findViewById(R.id.cancelButton)
        myApp = application as MyApplication
        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)

        wisperButton.setOnClickListener {
            if (wisperEdit.text.isBlank()) {
                Toast.makeText(this, "Input field cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val client = OkHttpClient()
            val mediaType: MediaType = "application/json; charset=utf-8".toMediaType()
            val whisperText = wisperEdit.text.toString() // just sapmle
            val requestBody = JSONObject().apply {
                put("whisperText", whisperText)
            }.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url(myApp.apiUrl)
                .post(requestBody)
                .build()
            client.newCall(request!!).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    this@WhisperActivity.runOnUiThread {
                        Toast.makeText(
                            this@WhisperActivity,
                            "Error :${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    try {
                        val jsonResponse = JSONObject(responseBody)

                        if (jsonResponse.has("error")) {
                            val errorMessage = jsonResponse.getString("error")
                            this@WhisperActivity.runOnUiThread {
                                Toast.makeText(
                                    this@WhisperActivity,
                                    errorMessage,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            return
                        }
                        val intent = Intent(this@WhisperActivity, UserInfoActivity::class.java)
                        intent.putExtra("userId", myApp.loginUserId)
                        startActivity(intent)
                        finish()


                    } catch (e: Exception) {
                        Toast.makeText(this@WhisperActivity, "${e.message}", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

            })

        }

        // 1-3. Create click event listener for cancelButton
        cancelButton.setOnClickListener {
            val intent = Intent(this@WhisperActivity, WhisperActivity::class.java)
            startActivity(intent)
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