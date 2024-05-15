package com.example.whisper

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.whisper.MyApplication.MyApplication
import com.example.whisper.MyApplication.overMenu
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
    private lateinit var myApp : MyApplication
    private lateinit var overMenu: overMenu

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
        overMenu = overMenu(this)
        Log.d(TAG, "check: ${myApp.loginUserId}")

        // Request user information
        val client = OkHttpClient()
        val mediaType : MediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = "{\"userId\":\"${myApp.loginUserId}\"}"
        val request = Request.Builder()
            .url(myApp.apiUrl+"userInfo.php").post(requestBody.toRequestBody(mediaType))
            .build()

        client.newCall(request!!).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@UserEditActivity, e.message, Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseData = response.body?.string()
                    val json = JSONObject(responseData)

                    if (json.has("error")) {
                        runOnUiThread {
                            Toast.makeText(this@UserEditActivity, json.getString("error"), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        runOnUiThread {
                            val userData = json.getJSONObject("userData")
                            val userName = userData.getString("userName")
                            val profile = userData.getString("profile")
                            val iconPath = userData.getString("iconPath")

                            userIdText.text = myApp.loginUserId
                            userNameEdit.text = Editable.Factory.getInstance().newEditable(userName)
                            profileEdit.text = Editable.Factory.getInstance().newEditable(profile)


//                            if (iconPath.isNotEmpty()) {
//                                // Load hình ảnh từ iconPath và hiển thị lên ImageView
//                                Picasso.get().load(iconPath).into(iconImageView)
//                            }
                        }
                    }
                }catch (e: Exception){
                    Toast.makeText(this@UserEditActivity, e.message, Toast.LENGTH_LONG).show()
                }
            }
        })


            changeButton.setOnClickListener {
                val client = OkHttpClient()
                val mediaType: MediaType = "application/json; charset=utf-8".toMediaType()
                val userName = userNameEdit.text.toString()
                val profile = profileEdit.text.toString()
                val requestBody = JSONObject().apply {
                    put("userId", myApp.loginUserId)
                    put("userName", userName)
                    put("profile", profile)
                }.toString().toRequestBody(mediaType)
                val request = Request.Builder()
                    .url(myApp.apiUrl+"userUpdate.php")
                    .post(requestBody) // Use put() for a PUT request, post() for a POST request
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
        // Gọi onCreateOptionsMenu từ overMenu
        return overMenu.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Gọi onOptionsItemSelected từ overMenu
        return overMenu.onOptionsItemSelected(item)
    }
}