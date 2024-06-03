package com.example.whisper

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whisper.MyApplication.MyApplication
import com.example.whisper.MyApplication.overMenu
import com.example.whisper.adapter.UserListAdapter
import com.example.whisper.model.User
import com.example.whisper.model.Whisper
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class SearchActivity : AppCompatActivity() {
    private val myApp: MyApplication by lazy { application as MyApplication }
    private lateinit var overMenu: overMenu

    private lateinit var searchEdit: EditText
    private lateinit var searchButton: Button
    private lateinit var searchRecycle: RecyclerView
    private lateinit var radioGroup: RadioGroup
    private lateinit var userRadio: RadioButton
    private lateinit var whisperRadio: RadioButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        overMenu = overMenu(this)

        // 1-1. Khai báo các biến cho các đối tượng trong giao diện
        searchEdit = findViewById(R.id.searchEdit)
        searchButton = findViewById(R.id.searchButton)
        searchRecycle = findViewById(R.id.searchRecycle)
        radioGroup = findViewById(R.id.radioGroup)
        userRadio = findViewById(R.id.userRadio)
        whisperRadio = findViewById(R.id.whisperRadio)

        // 1-2. Tạo sự kiện click cho searchButton
        searchButton.setOnClickListener {
            // 1-2-1. Kiểm tra nếu input trống, hiển thị lỗi và dừng xử lý
            val query = searchEdit.text.toString()
            if (query.isEmpty()) {
                Toast.makeText(this, "Please enter search text.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 1-2-2. Gửi yêu cầu API để lấy kết quả tìm kiếm
            val section = if (userRadio.isChecked) "1" else "2"
            fetchSearchResults(section, query)
        }
    }

    private fun fetchSearchResults(section: String, query: String) {
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = JSONObject().apply {
            put("section", section)
            put("string", query)
        }.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("${myApp.apiUrl}search.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    // 1-2-4-1. Hiển thị lỗi khi yêu cầu thất bại
                    Toast.makeText(applicationContext, "Request failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("SearchActivity", "onResponse: $responseBody")
                try {
                    val jsonResponse = JSONObject(responseBody)
                    if (jsonResponse.has("error")) {
                        runOnUiThread {
                            Toast.makeText(applicationContext, jsonResponse.getString("error"), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        // 1-2-3. Xử lý khi nhận được phản hồi thành công
                        updateUI(jsonResponse, section)
                    }
                } catch (e: JSONException) {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Error parsing the response", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun updateUI(json: JSONObject, section: String) {
//        val data = json.getJSONArray("data")
        val listUser = mutableListOf<User>()
        val listWhisper = mutableListOf<Whisper>()

        //val user = json.getJSONArray("userList")


        runOnUiThread {
            searchRecycle.layoutManager = LinearLayoutManager(this)
            if (section == "1") {
                val user = json.getJSONObject("userList")
                listUser.add(
                    User(
                        userId = user.getString("userId"),
                        userName = user.getString("userName"),
                        whisperCount = user.getInt("whisperCount"),
                        followCount = user.getInt("followCount"),
                        followerCount = user.getInt("followerCount")
                    )
                )
                searchRecycle.layoutManager = LinearLayoutManager(this@SearchActivity)
                val adapter = UserListAdapter( listUser)
                searchRecycle.adapter = adapter
                adapter.notifyDataSetChanged()
            }
        }
    }

    // 2. Tạo menu khi onCreateOptionsMenu được gọi
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return overMenu.onCreateOptionsMenu(menu)
    }

    // 3. Xử lý sự kiện chọn item trong menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return overMenu.onOptionsItemSelected(item)
    }
}

