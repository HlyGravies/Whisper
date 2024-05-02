package com.example.whisper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import okhttp3.Call
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.lang.Exception

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val UserId =findViewById<EditText>(R.id.userIdEdit)
        val Password = findViewById<EditText>(R.id.passwordEdit)
        val LoginButton = findViewById<Button>(R.id.loginButton)
        val createUser = findViewById<Button>(R.id.createButton)


        LoginButton.setOnClickListener{
            // HTTP接続用インスタンス生成
            val client = OkHttpClient()
            // JSON形式でパラメータを送るようデータ形式を設定
            val mediaType : MediaType = "application/json; charset=utf-8".toMediaType()
            // Bodyのデータ(APIに渡したいパラメータを設定)
            val requestBody = "{\"id\":\"${UserId.text}\"&\"password\":\"${Password.text}\"}"
            // Requestを作成(先ほど設定したデータ形式とパラメータ情報をもとにリクエストデータを作成)
            val request = Request.Builder().url("http://10.0.2.2/SampleProject/sample.php").post(requestBody.toRequestBody(mediaType)).build()

            // リクエスト送信（非同期処理）

            client.newCall(request!!).enqueue(object : Callback {
                // リクエストが失敗した場合の処理を実装
                override fun onFailure(call: Call, e: IOException) {
                    // runOnUiThreadメソッドを使うことでUIを操作することができる。(postメソッドでも可)
                    this@LoginActivity.runOnUiThread {
                        //ここにToastでエラーを表示する
                        Toast.makeText(this@LoginActivity,"${e.message}",Toast.LENGTH_LONG).show()
                    }
                }
                // リクエストが成功した場合の処理を実装
                override fun onResponse(call: Call, response: Response) {
                    try{
                        val insert = Intent(this@LoginActivity,MainActivity::class.java)
                        startActivity(insert)
                    }catch (e :Exception){
                        Toast.makeText(applicationContext,"${e.message}",Toast.LENGTH_LONG).show()
                    }
                }
            })
        }
        createUser.setOnClickListener {
            val insert = Intent(this@LoginActivity,MainActivity::class.java)
            startActivity(insert)
        }
    }
}

