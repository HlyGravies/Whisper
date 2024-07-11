package com.example.whisper

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Toast
import com.example.whisper.MyApplication.MyApplication
import com.example.whisper.databinding.ActivityLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    @Inject
    lateinit var myApp: MyApplication
    @Inject
    lateinit var client: OkHttpClient
    @Inject
    lateinit var mediaType: MediaType
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        var isPassVisible = false

        // When PasswordOV is clicked
        binding.passwordobservationView.setOnClickListener {
            if (isPassVisible) {
                binding.passwordEdit.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.passwordobservationView.setImageResource(R.drawable.baseline_visibility_off_24)
            } else {
                binding.passwordEdit.transformationMethod = HideReturnsTransformationMethod.getInstance()
                binding.passwordobservationView.setImageResource(R.drawable.baseline_visibility_24)
            }
            isPassVisible = !isPassVisible
            binding.passwordEdit.setSelection(binding.passwordEdit.text.length)
        }

        binding.loginButton.setOnClickListener {
            val userIdText = binding.userIdEdit.text.toString()
            val passwordText = binding.passwordEdit.text.toString()
            if (userIdText.isNotEmpty() && passwordText.isNotEmpty()) {
                login(userIdText, passwordText, editor)
            } else {
                Toast.makeText(this, "Please enter both user ID and password", Toast.LENGTH_LONG).show()
            }
        }

        if (myApp.loginUserId == "lo") {
            Log.d("logout", "true")
            editor.clear()
            editor.apply()
        }

        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            myApp.loginUserId = sharedPref.getString("userId", "").toString()
            Log.d("islogin", "${myApp.loginUserId}")
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
        }

        binding.createButton.setOnClickListener {
            val intent = Intent(this@LoginActivity, CreateUserActivity::class.java)
            startActivity(intent)
        }
    }
    private fun login(userId: String, password: String, editor: SharedPreferences.Editor) {
        val requestBody = JSONObject().apply {
            put("userId", userId)
            put("password", password)
        }.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url(myApp.apiUrl + "loginAuth.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "Network Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string()
                try {
                    val jsonResponse = JSONObject(json)
                    if (jsonResponse.getString("result") == "success") {
                        myApp.loginUserId = userId
                        if (binding.rememberBox.isChecked) {
                            editor.putString("userId", userId)
                            editor.putBoolean("isLoggedIn", true)
                            editor.apply()
                        }
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, jsonResponse.getString("errorDetails"), Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "Error parsing the response", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }
}
