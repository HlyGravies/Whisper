package com.example.whisper

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import okhttp3.Call
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.example.whisper.MyApplication.MyApplication
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception

class LoginActivity : AppCompatActivity() {
    lateinit var myApp : MyApplication
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val UserId = findViewById<EditText>(R.id.userIdEdit)
        val Password = findViewById<EditText>(R.id.passwordEdit)
        val LoginButton = findViewById<Button>(R.id.loginButton)
        val createUser = findViewById<Button>(R.id.createButton)
        val RememberBox = findViewById<CheckBox>(R.id.rememberBox)
        val PasswordOV = findViewById<ImageView>(R.id.passwordobservationView)
        myApp = application as MyApplication

        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        var isPassVisible = false;

        //passwordOVを押したとき
        PasswordOV.setOnClickListener{
            Log.d("パスワード表示ボタン","押せてる");
            if(isPassVisible){
                Password.transformationMethod = PasswordTransformationMethod.getInstance();
                PasswordOV.setImageResource(R.drawable.baseline_visibility_off_24)
            }else{
                Password.transformationMethod = HideReturnsTransformationMethod.getInstance()
                PasswordOV.setImageResource(R.drawable.baseline_visibility_24)
            }
            isPassVisible = !isPassVisible
            Password.setSelection(Password.text.length)
        }

        LoginButton.setOnClickListener {
            //1-2-1
            val userIdText = UserId.text.toString()
            val passwordText = Password.text.toString()
            Log.d("UIText","ID;$userIdText")
            Log.d("PWText","pass;$passwordText")
            if (userIdText.isNotEmpty() && passwordText.isNotEmpty()) {
                // HTTP接続用インスタンス生成
                val client = OkHttpClient()
                // JSON形式でパラメータを送るようデータ形式を設定
                val mediaType: MediaType = "application/json; charset=utf-8".toMediaType()
                // Bodyのデータ(APIに渡したいパラメータを設定)
                val requestBody = JSONObject().apply {
                    put("userId", userIdText)
                    put("password", passwordText)
                }.toString().toRequestBody(mediaType)
                // Requestを作成(先ほど設定したデータ形式とパラメータ情報をもとにリクエストデータを作成)
                val request = Request.Builder()
                        .url(myApp.apiUrl+"loginAuth.php")
                        .post(requestBody)
                        .build()
                Log.d("request","$requestBody/$request")

                // リクエスト送信（非同期処理）1-2-2
                client.newCall(request).enqueue(object : Callback {
                    // リクエストが失敗した場合の処理を実装
                    override fun onFailure(call: Call, e: IOException) {
                        // runOnUiThreadメソッドを使うことでUIを操作することができる。(postメソッドでも可)
                        runOnUiThread {
                            //ここにToastでエラーを表示する
                            Toast.makeText(this@LoginActivity, "${e.message}", Toast.LENGTH_LONG).show()

                        }
                    }

                    // リクエストが成功した場合の処理を実装 1-2-3
                    override fun onResponse(call: Call, response: Response) {
                        val json = response.body?.string()
                        Log.d("json","内容;$json")
                        try{
                            val jsonResponse = JSONObject(json)
                            if (jsonResponse.has("result") ) {
                                val result = jsonResponse.getString("result")
                                if(result == "success"){
                                    myApp.loginUserId = userIdText
                                    //チェック項目(remember me)が押されているか
                                    if(RememberBox.isChecked){
                                        // ログイン情報を保存
                                        editor.putString("userId", userIdText)
                                        editor.putBoolean("isLoggedIn", true)
                                        editor.apply()
                                    }else{

                                    }

                                    val insert = Intent(this@LoginActivity, TimelineActivity::class.java)
                                    startActivity(insert)

                                }else {
                                    val errorNo = jsonResponse.getJSONObject("errorDetails")
                                    val errortxt = errorNo.getString("003");
                                    runOnUiThread {
                                        Toast.makeText(
                                            this@LoginActivity,
                                            "$errortxt",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        }catch (e: Exception){
                            runOnUiThread {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "$e",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                })
            }else {
                Toast.makeText(this@LoginActivity, "Please enter both user ID and password", Toast.LENGTH_LONG).show()
            }

        }


        if(myApp.loginUserId=="lo"){
            Log.d("logout","ture")
            editor.clear()
            editor.apply()
        }
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)

        //ログインしっぱなしか確認するところ
        if (isLoggedIn){
            myApp.loginUserId = sharedPref.getString("userId","").toString()
            Log.d("islogin","${myApp.loginUserId}")
            val insert = Intent(this@LoginActivity, TimelineActivity::class.java)
            startActivity(insert)
        }




        createUser.setOnClickListener {
            //1-3-1
            val insert = Intent(this@LoginActivity, CreateUserActivity::class.java)
            startActivity(insert)
        }
    }

}