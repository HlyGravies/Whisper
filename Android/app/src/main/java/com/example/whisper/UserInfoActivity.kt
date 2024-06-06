package com.example.whisper

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whisper.MyApplication.MyApplication
import com.example.whisper.MyApplication.overMenu
import com.example.whisper.model.Whisper
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
    private lateinit var overMenu: overMenu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)

        userNameTx = findViewById(R.id.userNameText)
        userProfileTx = findViewById(R.id.profileText)
        followCountTx = findViewById(R.id.followCntText)
        followerCountTx = findViewById(R.id.followerCntText)
        followBtn = findViewById(R.id.followButton)
        userRecycle = findViewById(R.id.userRecycle)
        radioGroup = findViewById(R.id.radioGroup)
        overMenu = overMenu(this)

        userId = intent.getStringExtra("userId")
        followBtn.visibility = if (userId == myApp.loginUserId) View.GONE else View.VISIBLE


        getUserInfoApiCall()
        getFollowInfoApiCall()


        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.whisperRadio) {
                getUserWhispersApiCall()
            }else if (checkedId == R.id.goodInfoRadio) {
                getUserGoodWhispersApiCall()
            }
        }

        followCountTx.setOnClickListener {
            val intent = Intent(this, FollowListActivity::class.java)
            intent.putExtra("userId", userId)
            intent.putExtra("isFollow", true)
            startActivity(intent)
        }

        followerCountTx.setOnClickListener {
            val intent = Intent(this, FollowListActivity::class.java)
            intent.putExtra("userId", userId)
            intent.putExtra("isFollow", false)
            startActivity(intent)
        }

        followBtn.setOnClickListener {
            val followFlg = followBtn.text != "Following"
            followManageApiCall(userId!!, followFlg)
        }
    }

    private fun getUserGoodWhispersApiCall() {
        val client = OkHttpClient()
        val mediaType: MediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = JSONObject().apply {
            put("userId", userId)
            put("loginUserId", myApp.loginUserId)
        }.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("${myApp.apiUrl}userWhisperInfo.php")
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
                Log.d("API Response", responseBody ?: "No response body")

                try {
                    val jsonResponse = JSONObject(responseBody)
                    val list = mutableListOf<Whisper>()
                    val whispers = jsonResponse.getJSONObject("data").getJSONArray("allLikedWhisperList")
                    for (i in 0 until whispers.length()) {
                        val whisper = whispers.getJSONObject(i)
                        list.add(
                            Whisper(
                                whisper.getInt("whisperNo"),
                                whisper.getString("userId"),
                                whisper.getString("userName"),
                                whisper.getString("postDate"),
                                whisper.getString("content"),
                                whisper.getBoolean("goodFlg")
                            )
                        )
                    }

                    if (jsonResponse.has("error")) {
                        runOnUiThread {
                            Toast.makeText(myApp, jsonResponse.getString("error"), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        runOnUiThread {
                            Log.d("GoodWhispers", list.toString()) // Log danh sách whispers để kiểm tra
                            userRecycle.layoutManager = LinearLayoutManager(this@UserInfoActivity)
                            val adapter = WhisperListAdapter(this@UserInfoActivity, list, myApp.loginUserId)
                            userRecycle.adapter = adapter
                            adapter.notifyDataSetChanged()
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
                        runOnUiThread {
                            val userData = jsonResponse.getJSONObject("userData")
                            userNameTx.text = userData.getString("userName")
                            userProfileTx.text = userData.getString("profile")
                        }
                    }
                } catch (e: JSONException) {
                    runOnUiThread {
                        Toast.makeText(myApp, "UserInfo Error parsing the response", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }
    private fun getFollowInfoApiCall() {
        val client = OkHttpClient()
        val mediaType: MediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = JSONObject().apply {
            put("userId", myApp.loginUserId)
        }.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("${myApp.apiUrl}followerInfo.php")
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
                Log.d("Followa Response", "Response: $responseBody")
                try {
                    val jsonResponse = JSONObject(responseBody)
                    if (jsonResponse.has("error")) {
                        runOnUiThread {
                            Toast.makeText(myApp, jsonResponse.getString("error"), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        val followList = jsonResponse.getJSONObject("data").getJSONArray("followList")
                        var isFollowing = false
                        for (i in 0 until followList.length()) {
                            val follow = followList.getJSONObject(i)
                            Log.d("followaaaa", "onResponse: $follow")
                            if (follow.getString("userId") == userId) {
                                isFollowing = true
                                break
                            }
                        }
                        val followerList = jsonResponse.getJSONObject("data").getJSONArray("followerList")
                        runOnUiThread {
                            followBtn.text = if (isFollowing) "Following" else "Follow"
                            followCountTx.text = followList.length().toString()
                            followerCountTx.text = followerList.length().toString()
                        }
                    }
                } catch (e: JSONException) {
                    runOnUiThread {
                        Toast.makeText(myApp, "Follow Error parsing the response", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun getUserWhispersApiCall() {
        val client = OkHttpClient()
        val mediaType: MediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = JSONObject().apply {
            put("userId", userId)
            put("loginUserId", myApp.loginUserId)
        }.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("${myApp.apiUrl}userWhisperInfo.php")
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
                //Log.d("API Response", responseBody ?: "No response body")

                try {
                    val jsonResponse = JSONObject(responseBody)
                    val list = mutableListOf<Whisper>()
                    val whispers = jsonResponse.getJSONObject("data").getJSONArray("whisperList")
                    //Log.d("Whispers", whispers.toString())
                    for (i in 0 until whispers.length()) {
                        val whisper = whispers.getJSONObject(i)
                        list.add(
                            Whisper(
                                whisper.getInt("whisperNo"),
                                whisper.getString("userId"),
                                whisper.getString("userName"),
                                whisper.getString("postDate"),
                                whisper.getString("content"),
                                whisper.getBoolean("goodFlg")
                            )
                        )

                    }

                    if (jsonResponse.has("error")) {
                        runOnUiThread {
                            Toast.makeText(myApp, jsonResponse.getString("error"), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        runOnUiThread {
                             // Log danh sách whispers để kiểm tra
                            Log.d("Whispers", list.toString())
                            userRecycle.layoutManager = LinearLayoutManager(this@UserInfoActivity)
                            val adapter = WhisperListAdapter(this@UserInfoActivity, list,myApp.loginUserId)
                            userRecycle.adapter = adapter
                            adapter.notifyDataSetChanged()

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

    private fun followManageApiCall(followUserId: String, followFlg: Boolean) {
        val client = OkHttpClient()
        val mediaType: MediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = JSONObject().apply {
            put("userId", myApp.loginUserId)
            put("followUserId", followUserId)
            put("followFlg", followFlg)
        }.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("${myApp.apiUrl}followCtl.php")
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
                Log.d("API follow Response", responseBody ?: "No response body")
                try {
                    val jsonResponse = JSONObject(responseBody)
                    if (jsonResponse.has("error")) {
                        runOnUiThread {
                            Toast.makeText(applicationContext, jsonResponse.getString("error"), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        runOnUiThread {
                            followBtn.text = if (followFlg) "Following" else "Follow"
                            getFollowInfoApiCall() // Update follow count
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
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Gọi onCreateOptionsMenu từ overMenu
        return overMenu.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Gọi onOptionsItemSelected từ overMenu
        return overMenu.onOptionsItemSelected(item)
    }
}