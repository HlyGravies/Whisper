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
import com.example.whisper.MyApplication.overMenu
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
    private lateinit var overMenu: overMenu
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_whisper)

        wisperEdit = findViewById(R.id.wisperEdit)
        wisperButton = findViewById(R.id.wisperButton)
        cancelButton = findViewById(R.id.cancelButton)
        myApp = application as MyApplication
        overMenu = overMenu(this)

        wisperButton.setOnClickListener {
            if (wisperEdit.text.isBlank()) {
                Toast.makeText(this, "Input field cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            try {
                val client = OkHttpClient()
                val mediaType: MediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = JSONObject().apply {
                    put("userId", myApp.loginUserId)
                    put("content", wisperEdit.text.toString())
                }.toString().toRequestBody(mediaType)
                val request = Request.Builder()
                    .url(myApp.apiUrl+ "whisperAdd.php")
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
                        Log.d(TAG, "aaa: $responseBody")
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
                            intent.putExtra("content", wisperEdit.text.toString())
                            startActivity(intent)
                            finish()


                        } catch (e: Exception) {
                            Toast.makeText(this@WhisperActivity, "${e.message}", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }

                })
            } catch (e: Exception) {
                Log.d(TAG, "APItest: ${e.message}")
            }



        }

        // 1-3. Create click event listener for cancelButton
        cancelButton.setOnClickListener {
            val intent = Intent(this@WhisperActivity, WhisperActivity::class.java)
            startActivity(intent)
        }
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