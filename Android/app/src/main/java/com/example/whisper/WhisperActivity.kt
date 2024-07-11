package com.example.whisper

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.whisper.MyApplication.MyApplication
import com.example.whisper.databinding.ActivityWhisperBinding
import com.example.whisper.fragment.TimelineFragment
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject
@AndroidEntryPoint
class WhisperActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWhisperBinding
    @Inject
    lateinit var myApp: MyApplication

    @Inject
    lateinit var client: OkHttpClient

    @Inject
    lateinit var mediaType: MediaType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWhisperBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.wisperButton.setOnClickListener {
            val content = binding.wisperEdit.text.toString()
            if (content.isBlank()) {
                Toast.makeText(this, "Input field cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            addWhisper(myApp.loginUserId, content)
        }

        binding.cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun addWhisper(userId: String, content: String) {
        try {
            val requestBody = JSONObject().apply {
                put("userId", userId)
                put("content", content.replace("\n", "\\n"))
            }.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url("${myApp.apiUrl}whisperAdd.php")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(
                            this@WhisperActivity,
                            "Error: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    Log.d(TAG, "Response: $responseBody")
                    try {
                        val jsonResponse = JSONObject(responseBody)

                        if (jsonResponse.has("error")) {
                            val errorMessage = jsonResponse.getString("error")
                            runOnUiThread {
                                Toast.makeText(
                                    this@WhisperActivity,
                                    errorMessage,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            return
                        }

                        runOnUiThread {
                            val intent = Intent(this@WhisperActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(this@WhisperActivity, e.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        } catch (e: Exception) {
            Log.d(TAG, "API error: ${e.message}")
        }
    }
}
