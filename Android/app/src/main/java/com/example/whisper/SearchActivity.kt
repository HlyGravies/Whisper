package com.example.whisper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whisper.MyApplication.overMenu

import com.example.whisper.adapter.GoodListAdapter
import com.example.whisper.adapter.UserListAdapter
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException



class SearchActivity : AppCompatActivity() {
    data class UserRowData(
        val userImage: String,
        val userName: String,
        val followCnt: Int,
        val followerCnt: Int
    )


    private lateinit var searchEdit: EditText
    private lateinit var searchButton: Button
    private lateinit var searchRecycle: RecyclerView
    private lateinit var radioGroup: RadioGroup
    private lateinit var userRadio: RadioButton
    private lateinit var whisperRadio: RadioButton

    private lateinit var overMenu: overMenu
    private lateinit var listGood: MutableList<GoodListAdapter.GoodRowData>
    private lateinit var listUser: MutableList<UserRowData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        overMenu = overMenu(this)

        searchEdit = findViewById(R.id.searchEdit)
        searchButton = findViewById(R.id.searchButton)
        searchRecycle = findViewById(R.id.searchRecycle)
        radioGroup = findViewById(R.id.radioGroup)
        userRadio = findViewById(R.id.userRadio)
        whisperRadio = findViewById(R.id.whisperRadio)

        listGood = mutableListOf()
        listUser = mutableListOf()

        searchButton.setOnClickListener {
            val query = searchEdit.text.toString()
            if (query.isEmpty()) {
                Toast.makeText(this, "Please enter search text.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            fetchSearchResults(query)
        }
    }
    private fun fetchSearchResults(query: String) {
        // ここにAPIリクエストの実装を記述します。
        val client = OkHttpClient()
        val mediaType : MediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = "{\"keyword\":\"$query\"}".toRequestBody(mediaType)
        val request = Request.Builder()
            .url("http://10.200.2.137/sample.php")  // Change to your API URL
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            // リクエストが失敗した場合の処理を実装
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "リクエストが失敗しました: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            // リクエストが成功した場合の処理を実装
            override fun onResponse(call: Call, response: Response) {
                response.use { res ->
                    if (!res.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(applicationContext, "Server error: ${res.code}", Toast.LENGTH_LONG).show()
                        }
                        return
                    }
                    val body = res.body?.string()
                    val json = JSONObject(body ?: "")
                    if (json.has("error")) {
                        runOnUiThread {
                            Toast.makeText(applicationContext, json.getString("error"), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        updateUI(json)
                    }
                }

            }
        })
    }
    private fun updateUI(json: JSONObject) {
        val data = json.getJSONArray("data")
        listUser.clear()
        listGood.clear()

        for (i in 0 until data.length()) {
            val item = data.getJSONObject(i)
            if (item.getString("type") == "user") {
                listUser.add(UserRowData(item.getString("userImage"), item.getString("userName"), item.getInt("followCnt"), item.getInt("followerCnt")))
            } else if (item.getString("type") == "whisper") {
                listGood.add(
                    GoodListAdapter.GoodRowData(
                        item.getString("userId"),
                        item.getString("userImage"),
                        item.getString("userName"),
                        item.getString("whisperText"),
                        item.getInt("goodCnt")
                    )
                )
            }
        }

        runOnUiThread {
            searchRecycle.layoutManager = LinearLayoutManager(this)
            if (userRadio.isChecked) {
                searchRecycle.adapter = UserListAdapter(listUser)
            } else if (whisperRadio.isChecked) {
                searchRecycle.adapter = GoodListAdapter(listGood,this)
            }
        }
    }



    // メニューを生成する際、overMenuオブジェクトを通じて処理を行う
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return overMenu.onCreateOptionsMenu(menu)
    }

    // メニューアイテムが選択された際の処理をoverMenuオブジェクトに委譲する
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return overMenu.onOptionsItemSelected(item)
    }


}