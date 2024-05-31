package com.example.whisper

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whisper.MyApplication.MyApplication
import com.example.whisper.MyApplication.overMenu
import com.example.whisper.adapter.FollowListAdapter
import com.example.whisper.adapter.UserListAdapter
import com.example.whisper.model.User
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class FollowListActivity : AppCompatActivity() {
    private val myApp: MyApplication by lazy { application as MyApplication }
    private lateinit var overMenu: overMenu
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow_list)

        overMenu = overMenu(this)

        userId = intent.getStringExtra("userId")

        val isFollow = intent.getBooleanExtra("isFollow", true)
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
                                    followerCount = user.getInt("followerCount")
                                )
                            )
                        }
                        runOnUiThread {
                            val followRecycle = findViewById<RecyclerView>(R.id.followRecycler)
                            followRecycle.layoutManager = LinearLayoutManager(this@FollowListActivity)
                            val adapter = FollowListAdapter(list)
                            followRecycle.adapter = adapter
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return overMenu.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return overMenu.onOptionsItemSelected(item)
    }
}