package com.example.whisper

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.whisper.Interface.OnDataRefreshNeededListener
import com.example.whisper.MyApplication.MyApplication
import com.example.whisper.databinding.ActivityUserInfoBinding
import com.example.whisper.model.Whisper
import com.google.android.material.tabs.TabLayout
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class UserInfoActivity : AppCompatActivity() , OnDataRefreshNeededListener {

    private lateinit var binding: ActivityUserInfoBinding
    private val myApp: MyApplication by lazy { application as MyApplication }
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getStringExtra("userId")
        binding.followButton.visibility = if (userId == myApp.loginUserId) View.GONE else View.VISIBLE

        getUserInfoApiCall()
        getFollowInfoApiCall()
        getUserWhispersApiCall()

        binding.userRecycle.layoutManager = LinearLayoutManager(this)
        binding.userRecycle.adapter = WhisperListAdapter(this@UserInfoActivity, mutableListOf(), myApp.loginUserId,this)

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Whisper"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Good Info"))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                (binding.userRecycle.adapter as WhisperListAdapter).clearData()
                if (tab.position == 0) {
                    binding.errorText.text = "" // Clear the error text
                    getUserWhispersApiCall()
                } else if (tab.position == 1) {
                    binding.errorText.text = "" // Clear the error text
                    getUserGoodWhispersApiCall()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        binding.followText.setOnClickListener {
            val intent = Intent(this, FollowListActivity::class.java)
            intent.putExtra("userId", userId)
            intent.putExtra("isFollow", true)
            startActivity(intent)
        }

        binding.followerText.setOnClickListener {
            val intent = Intent(this, FollowListActivity::class.java)
            intent.putExtra("userId", userId)
            intent.putExtra("isFollow", false)
            startActivity(intent)
        }

        binding.followButton.setOnClickListener {
            val followFlg = binding.followButton.text != "Following"
            followManageApiCall(userId!!, followFlg)
        }
        binding.backBtn.setOnClickListener {
            finish()
        }
    }
    override fun onDataRefreshNeeded() {
        // Implementation of onDataRefreshNeeded
        // For example, you might want to refresh the user info
        getUserInfoApiCall()
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
                    if (jsonResponse.getJSONObject("data").isNull("allLikedWhisperList")) {
                        runOnUiThread {
                            binding.errorText.text = "”いいね！”した囁やきがありません！"
                        }
                    } else {
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
                                    whisper.getInt("goodCount"),
                                    whisper.getBoolean("goodFlg"),
                                    whisper.getString("iconPath"),
                                    whisper.getInt("commentCount")
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
                                binding.userRecycle.layoutManager = LinearLayoutManager(this@UserInfoActivity)
                                val adapter = WhisperListAdapter(this@UserInfoActivity, list, myApp.loginUserId,this@UserInfoActivity)
                                binding.userRecycle.adapter = adapter
                                adapter.notifyDataSetChanged()
                            }
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
                        if (jsonResponse.has("userData") && !jsonResponse.isNull("userData")) {
                            val userData = jsonResponse.getJSONObject("userData")
                            runOnUiThread {
                                binding.userNameText.text = userData.getString("userName")
                                val profile = userData.optString("profile", "")
                                binding.profileText.text = if (profile == "null") "" else profile
                                val iconPath = userData.getString("iconPath")
                                if (iconPath.isNotEmpty()) {
                                    myApp.iconPath = myApp.apiUrl + iconPath
                                    Glide.with(this@UserInfoActivity)
                                        .load(myApp.iconPath)
                                        .diskCacheStrategy(DiskCacheStrategy.NONE) // Disable cache
                                        .skipMemoryCache(true) // Skip memory cache
                                        .placeholder(R.drawable.loading)
                                        .error(R.drawable.avatar)
                                        .into(binding.userImage)
                                }
                            }
                        } else {
                            // Handle the case where userData is not present or null
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
                            binding.followButton.text = if (isFollowing) "Following" else "Follow"
                            binding.followCntText.text = followList.length().toString()
                            binding.followerCntText.text = followerList.length().toString()
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
                Log.d("API Response", responseBody ?: "No response body")

                try {
                    val jsonResponse = JSONObject(responseBody)
                    if (jsonResponse.getJSONObject("data").isNull("whisperList")) {
                        runOnUiThread {
                            binding.errorText.text = "まだ囁やきがありません！"
                        }
                    } else {
                        val list = mutableListOf<Whisper>()
                        val whispers = jsonResponse.getJSONObject("data").getJSONArray("whisperList")
                        for (i in 0 until whispers.length()) {
                            val whisper = whispers.getJSONObject(i)
                            list.add(
                                Whisper(
                                    whisper.getInt("whisperNo"),
                                    whisper.getString("userId"),
                                    whisper.getString("userName"),
                                    whisper.getString("postDate"),
                                    whisper.getString("content"),
                                    whisper.getInt("goodCount"),
                                    whisper.getBoolean("goodFlg"),
                                    whisper.getString("iconPath"),
                                    whisper.getInt("commentCount")
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
                                binding.userRecycle.layoutManager = LinearLayoutManager(this@UserInfoActivity)
                                val adapter = WhisperListAdapter(this@UserInfoActivity, list, myApp.loginUserId,this@UserInfoActivity)
                                binding.userRecycle.adapter = adapter
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
                } catch (e: JSONException) {
                    runOnUiThread {
                        Toast.makeText(myApp, "Error parsing the response", Toast.LENGTH_LONG).show()
                        Log.e("JSON Parsing Error", "Error parsing the response", e)
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
                            binding.followButton.text = if (followFlg) "Following" else "Follow"
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
}
