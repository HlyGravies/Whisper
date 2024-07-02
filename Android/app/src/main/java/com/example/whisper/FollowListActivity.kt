package com.example.whisper

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whisper.MyApplication.MyApplication
import com.example.whisper.adapter.FollowListAdapter
import com.example.whisper.databinding.ActivityFollowListBinding
import com.example.whisper.model.User
import com.google.android.material.tabs.TabLayout
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class FollowListActivity : AppCompatActivity() {
    private val myApp: MyApplication by lazy { application as MyApplication }
    private var userId: String? = null
    private lateinit var binding: ActivityFollowListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFollowListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getStringExtra("userId")
        Log.d("testu", "onCreate: $userId")

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Follow"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Follower"))

        val isFollow = intent.getBooleanExtra("isFollow", true)
        if (isFollow) {
            binding.tabLayout.getTabAt(0)?.select()
        } else {
            binding.tabLayout.getTabAt(1)?.select()
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> getFollowInfoApiCall(true)
                    1 -> getFollowInfoApiCall(false)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> getFollowInfoApiCall(true)
                    1 -> getFollowInfoApiCall(false)
                }
            }
        })

        binding.backBtn.setOnClickListener {
            finish()
        }

        getFollowInfoApiCall(isFollow)
    }

    private fun getFollowInfoApiCall(isFollow: Boolean) {
        val client = OkHttpClient()
        val mediaType: MediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = JSONObject().apply {
            put("userId", userId)
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
                Log.d("FollowApi", "onResponse: $responseBody")
                try {
                    val jsonResponse = JSONObject(responseBody)
                    if (jsonResponse.has("error")) {
                        runOnUiThread {
                            Toast.makeText(myApp, jsonResponse.getString("error"), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        val list = mutableListOf<User>()
                        val users = if (isFollow) jsonResponse.getJSONObject("data").getJSONArray("followList") else jsonResponse.getJSONObject("data").getJSONArray("followerList")
                        for (i in 0 until users.length()) {
                            val user = users.getJSONObject(i)
                            list.add(
                                User(
                                    userId = user.getString("userId"),
                                    userName = user.getString("userName"),
                                    whisperCount = user.getInt("whisperCount"),
                                    followCount = user.getInt("followCount"),
                                    followerCount = user.getInt("followerCount"),
                                    iconPath = user.getString("iconPath")
                                )
                            )
                        }
                        runOnUiThread {
                            if (list.isEmpty()) {
                                binding.errorText.text = if (isFollow) "No followers found." else "No followings found."
                                binding.errorText.visibility = View.VISIBLE
                                binding.followRecycler.visibility = View.GONE
                            } else {
                                binding.errorText.visibility = View.GONE
                                binding.followRecycler.visibility = View.VISIBLE
                                binding.followRecycler.layoutManager = LinearLayoutManager(this@FollowListActivity)
                                val adapter = FollowListAdapter(this@FollowListActivity, list)
                                binding.followRecycler.adapter = adapter
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
                } catch (e: JSONException) {
                    runOnUiThread {
                        Toast.makeText(myApp, "FollowList error parsing the response", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }
}
