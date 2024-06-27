package com.example.whisper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import okhttp3.Call
import okhttp3.Callback
import okhttp3.*
import java.io.IOException
import okhttp3.OkHttpClient
import org.json.JSONObject
import android.content.Intent
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.ImageView
import com.example.whisper.MyApplication.MyApplication
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class CreateUserActivity : AppCompatActivity() {
    lateinit var myApp: MyApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)

        val userNameEdit = findViewById<EditText>(R.id.userNameEdit)
        val userIdEdit = findViewById<EditText>(R.id.userIdEdit)
        val passwordEdit = findViewById<EditText>(R.id.passwordEdit)
        val rePasswordEdit = findViewById<EditText>(R.id.rePasswordEdit)
        val createButton = findViewById<Button>(R.id.createButton)
        val cancelButton = findViewById<Button>(R.id.cancelButton)
        val PasswordOV1 = findViewById<ImageView>(R.id.passwordView1)
        val PasswordOV2 = findViewById<ImageView>(R.id.rePasswordObservationView)
        myApp = application as MyApplication

        var isPassVisible = false;

        //passwordOV1を押したとき
        PasswordOV1.setOnClickListener{
            Log.d("パスワード表示ボタン","押せてる");
            if(isPassVisible){
                passwordEdit.transformationMethod = PasswordTransformationMethod.getInstance();
                PasswordOV1.setImageResource(R.drawable.baseline_visibility_off_24)
            }else{
                passwordEdit.transformationMethod = HideReturnsTransformationMethod.getInstance()
                PasswordOV1.setImageResource(R.drawable.baseline_visibility_24)
            }
            isPassVisible = !isPassVisible
            passwordEdit.setSelection(passwordEdit.text.length)
        }//passwordOV2を押したとき
        PasswordOV2.setOnClickListener{
            Log.d("パスワード表示ボタン","押せてる");
            if(isPassVisible){
                rePasswordEdit.transformationMethod = PasswordTransformationMethod.getInstance();
                PasswordOV2.setImageResource(R.drawable.baseline_visibility_off_24)
            }else{
                rePasswordEdit.transformationMethod = HideReturnsTransformationMethod.getInstance()
                PasswordOV2.setImageResource(R.drawable.baseline_visibility_24)
            }
            isPassVisible = !isPassVisible
            rePasswordEdit.setSelection(rePasswordEdit.text.length)
        }
        createButton.setOnClickListener {
            val userName = userNameEdit.text.toString()
            val userId = userIdEdit.text.toString()
            val password = passwordEdit.text.toString()
            val rePassword = rePasswordEdit.text.toString()

            // 1-2-1. 入力項目が空白の時
            if (userName.isEmpty() || userId.isEmpty() || password.isEmpty() || rePassword.isEmpty()) {
                Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 1-2-2. パスワードと確認パスワードの内容が違う時
            if (password != rePassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            // ユーザー作成APIを呼び出し
            createUser(userName, userId, password)
        }

        // cancelButtonのクリックリスナーを設定
        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun createUser(userName: String, userId: String, password: String) {
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = JSONObject().apply {
            put("userName", userName)
            put("userId", userId)
            put("password", password)
        }.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(myApp.apiUrl+"userAdd.php")
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
                    // サーバーレスポンスのログ出力
                    println("Server response: $it")

                    try {
                        val jsonResponse = JSONObject(it)
                        if (jsonResponse.has("error")) {
                            runOnUiThread {
                                Toast.makeText(applicationContext, jsonResponse.getString("error"), Toast.LENGTH_LONG).show()
                            }
                        } else {
                            runOnUiThread {
                                val intent = Intent(this@CreateUserActivity, TimelineActivity::class.java)
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
