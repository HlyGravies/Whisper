package com.example.whisper

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.whisper.MyApplication.MyApplication
import com.example.whisper.databinding.ActivityCreateUserBinding
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject
@AndroidEntryPoint
class CreateUserActivity : AppCompatActivity() {
    @Inject
    lateinit var myApp: MyApplication

    @Inject
    lateinit var client: OkHttpClient

    @Inject
    lateinit var mediaType: MediaType
    private lateinit var binding: ActivityCreateUserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var isPassVisible = false

        // When passwordOV1 is clicked
        binding.passwordView1.setOnClickListener {
            if (isPassVisible) {
                binding.passwordEdit.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.passwordView1.setImageResource(R.drawable.baseline_visibility_off_24)
            } else {
                binding.passwordEdit.transformationMethod = HideReturnsTransformationMethod.getInstance()
                binding.passwordView1.setImageResource(R.drawable.baseline_visibility_24)
            }
            isPassVisible = !isPassVisible
            binding.passwordEdit.setSelection(binding.passwordEdit.text.length)
        }

        // When passwordOV2 is clicked
        binding.rePasswordObservationView.setOnClickListener {
            if (isPassVisible) {
                binding.rePasswordEdit.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.rePasswordObservationView.setImageResource(R.drawable.baseline_visibility_off_24)
            } else {
                binding.rePasswordEdit.transformationMethod = HideReturnsTransformationMethod.getInstance()
                binding.rePasswordObservationView.setImageResource(R.drawable.baseline_visibility_24)
            }
            isPassVisible = !isPassVisible
            binding.rePasswordEdit.setSelection(binding.rePasswordEdit.text.length)
        }

        binding.createButton.setOnClickListener {
            val userName = binding.userNameEdit.text.toString()
            val userId = binding.userIdEdit.text.toString()
            val password = binding.passwordEdit.text.toString()
            val rePassword = binding.rePasswordEdit.text.toString()

            // 1-2-1. When input fields are empty
            if (userName.isEmpty() || userId.isEmpty() || password.isEmpty() || rePassword.isEmpty()) {
                Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 1-2-2. When password and re-entered password do not match
            if (password != rePassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Call the API to create a user
            createUser(userName, userId, password)
        }

        // Set click listener for cancelButton
        binding.cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun createUser(userName: String, userId: String, password: String) {
        val requestBody = JSONObject().apply {
            put("userName", userName)
            put("userId", userId)
            put("password", password)
        }.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("${myApp.apiUrl}userAdd.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Network Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let {
                    // Log server response
                    Log.d("Server response", it)

                    try {
                        val jsonResponse = JSONObject(it)
                        if (jsonResponse.has("error")) {
                            runOnUiThread {
                                Toast.makeText(applicationContext, jsonResponse.getString("error"), Toast.LENGTH_LONG).show()
                            }
                        } else {
                            runOnUiThread {
                                val userId = jsonResponse.getJSONObject("userData").getString("userId")
                                myApp.loginUserId = userId
                                val intent = Intent(this@CreateUserActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(applicationContext, "Error parsing the response", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        })
    }
}
