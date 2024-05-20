package com.example.whisper

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.whisper.MyApplication.MyApplication
import com.example.whisper.adapter.GoodListAdapter
//import com.example.whisper.adapter.WhisperListAdapter
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class UserInfoActivity : AppCompatActivity() {

    private lateinit var userNameTx: TextView
    private lateinit var userProfileTx: TextView
    private lateinit var followCountTx: TextView
    private lateinit var followerCountTx: TextView
    private lateinit var followBtn: Button
    private lateinit var userRecycle: RecyclerView
    private lateinit var radioGroup: RadioGroup
    private var userId: String? = null
    private val myApp: MyApplication by lazy { application as MyApplication }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)

        // 1-1. Khai báo các biến
        userNameTx = findViewById(R.id.userNameText)
        userProfileTx = findViewById(R.id.profileText)
        followCountTx = findViewById(R.id.followCntText)
        followerCountTx = findViewById(R.id.followerCntText)
        followBtn = findViewById(R.id.followButton)
        userRecycle = findViewById(R.id.userRecycle)
        radioGroup = findViewById(R.id.radioGroup)

        // 1-2. Lấy UserId từ Intent
        userId = intent.getStringExtra("userId")

        // 1-3. Gọi API để lấy thông tin người dùng
        getUserInfoApiCall()

        // 1-4. Tạo sự kiện lắng nghe cho radioGroup
        radioGroup.setOnCheckedChangeListener { _, _ ->
            // 1-4-1. Gọi lại API để lấy thông tin người dùng và cập nhật trạng thái
            getUserInfoApiCall()
        }

        // 1-5. Tạo sự kiện lắng nghe cho followCountText
        followCountTx.setOnClickListener {
            val intent = Intent(this, FollowListActivity::class.java)
            intent.putExtra("userId", userId)
            intent.putExtra("type", "follow")
            startActivity(intent)
        }

        // 1-6. Tạo sự kiện lắng nghe cho followerCountText
        followerCountTx.setOnClickListener {
            val intent = Intent(this, FollowListActivity::class.java)
            intent.putExtra("userId", userId)
            intent.putExtra("type", "follower")
            startActivity(intent)
        }

        // 1-7. Tạo sự kiện lắng nghe cho followButton
        followBtn.setOnClickListener {
            followManageApiCall()
        }
    }

    private fun getUserInfoApiCall() {
        val client = OkHttpClient()
        val mediaType: MediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = JSONObject().apply {
            put("userId", userId)
            put("loginUserId", myApp.loginUserId)
        }.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("${myApp.apiUrl}userInfo.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(myApp, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                try {
                    val jsonResponse = JSONObject(responseBody)
                    if (jsonResponse.has("error")) {
                        runOnUiThread {
                            Toast.makeText(myApp, jsonResponse.getString("error"), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        // 2-2. Xử lý dữ liệu trả về
                        runOnUiThread {
                            // 2-2-2. Đặt dữ liệu vào các TextView
                            userNameTx.text = jsonResponse.getString("userName")
                            userProfileTx.text = jsonResponse.getString("userProfile")
                            followCountTx.text = jsonResponse.getString("followCount")
                            followerCountTx.text = jsonResponse.getString("followerCount")

                            // 2-2-3. Xử lý nút follow
                            followBtn.text = if (jsonResponse.getBoolean("isFollowed")) "フォロー中" else "フォローする"
                            followBtn.visibility = if (userId == myApp.loginUserId) View.GONE else View.VISIBLE

                            // 2-2-4. Xử lý thông tin danh sách
                            val whispers = jsonResponse.getJSONArray("whisper")
                            val goodInfos = jsonResponse.getJSONArray("goodInfo")
                            val adapter = if (radioGroup.checkedRadioButtonId == R.id.whisperRadio) {
//                                WhisperListAdapter(whispers)
                            } else {
//                                GoodListAdapter(goodInfos)
                            }
//                            userRecycle.adapter = adapter
                        }
                    }
                } catch (e: JSONException) {
                    runOnUiThread {
                        Toast.makeText(myApp, "Error parsing the response", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun followManageApiCall() {
        val client = OkHttpClient()
        val mediaType: MediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = JSONObject().apply {
            put("userId", userId)
            put("loginUserId", myApp.loginUserId)
        }.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("${myApp.apiUrl}followManage.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                try {
                    val jsonResponse = JSONObject(responseBody)
                    if (jsonResponse.has("error")) {
                        runOnUiThread {
                            Toast.makeText(applicationContext, jsonResponse.getString("error"), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(applicationContext, "User created successfully", Toast.LENGTH_LONG).show()
                            val intent = Intent(this@UserInfoActivity, UserInfoActivity::class.java)
                            intent.putExtra("userId", userId)
                            startActivity(intent)
                            finish()
                        }
                    }
                } catch (e: JSONException) {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Error parsing the response", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }
}
