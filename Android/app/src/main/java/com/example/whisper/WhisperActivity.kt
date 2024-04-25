package com.example.whisper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_whisper)

        val wisperEdit: EditText = findViewById(R.id.wisperEdit)
        val wisperButton: Button = findViewById(R.id.wisperButton)
        val cancelButton: Button = findViewById(R.id.cancelButton)
        val myApp = application as MyApplication
        val apiUrl = myApp.getApiUrl()
        val loginUserId = myApp.getLoginUserId()

        wisperButton.setOnClickListener {
            if (wisperEdit.text.isBlank()) {
                Toast.makeText(this, "Input field cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val client = OkHttpClient()
            val mediaType : MediaType = "application/json; charset=utf-8".toMediaType()
            val whisperText = wisperEdit.text.toString() // just sapmle
            val requestBody = JSONObject().apply {
                put("whisperText", whisperText)
            }.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url(apiUrl)
                .post(requestBody)
                .build()
            client.newCall(request!!).enqueue(object : Callback{
                override fun onFailure(call: Call, e: IOException) {
                    this@WhisperActivity.runOnUiThread {
                        Toast.makeText(this@WhisperActivity, "Input field cannot be empty${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    try {
                        val jsonResponse = JSONObject(responseBody)

                        if (jsonResponse.has("error")) {
                            val errorMessage = jsonResponse.getString("error")
                            this@WhisperActivity.runOnUiThread {
                                Toast.makeText(this@WhisperActivity, errorMessage, Toast.LENGTH_SHORT).show()
                            }
                            return
                        }
                        val intent = Intent(this@WhisperActivity, UserInfoActivity::class.java)
                        intent.putExtra("userId", loginUserId)


                    }catch (e : Exception){
                        Toast.makeText(this@WhisperActivity, "${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

            })

        }

        // 1-3. Create click event listener for cancelButton
        cancelButton.setOnClickListener {
        }
    }


}