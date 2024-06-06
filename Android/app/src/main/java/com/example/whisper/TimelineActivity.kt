package com.example.whisper

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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

class TimelineActivity : AppCompatActivity() {
    private lateinit var overMenu: overMenu
    private lateinit var timelineRecycle: RecyclerView
    private val myApp: MyApplication by lazy { application as MyApplication }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timeline)

        overMenu = overMenu(this)
        timelineRecycle = findViewById(R.id.timelineRecycle)

        // Gọi API lấy thông tin timeline
        getTimelineInfoApiCall()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Gọi onCreateOptionsMenu từ overMenu
        return overMenu.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Gọi onOptionsItemSelected từ overMenu
        return overMenu.onOptionsItemSelected(item)
    }

    private fun getTimelineInfoApiCall() {
        val client = OkHttpClient()
        val mediaType: MediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = JSONObject().apply {
            put("userId", myApp.loginUserId)
        }.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("${myApp.apiUrl}timelineInfo.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@TimelineActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("TimelineApi", "onResponse: $responseBody")
                try {
                    val jsonResponse = JSONObject(responseBody)
                    if (jsonResponse.has("whisperList")) {
                        val timelineList = jsonResponse.getJSONArray("whisperList")
                        val whispers = mutableListOf<Whisper>()

                        for (i in 0 until timelineList.length()) {
                            val item = timelineList.getJSONObject(i)
                            val whisper = Whisper(
                                item.getInt("whisperNo"),
                                item.getString("userId"),
                                item.getString("userName"),
                                item.getString("postDate"),
                                item.getString("content"),
                                item.getBoolean("goodFlg")
                            )
                            whispers.add(whisper)
                        }

                        runOnUiThread {
                            timelineRecycle.layoutManager = LinearLayoutManager(this@TimelineActivity)
                            val adapter = WhisperListAdapter(this@TimelineActivity, whispers, myApp.loginUserId)
                            timelineRecycle.adapter = adapter
                            adapter.notifyDataSetChanged()
                        }
                    }
                } catch (e: JSONException) {
                    runOnUiThread {
                        Toast.makeText(this@TimelineActivity, "Error parsing the response", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }
}
