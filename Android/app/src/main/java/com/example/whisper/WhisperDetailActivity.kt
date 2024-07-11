package com.example.whisper

import android.os.Bundle
import android.util.Log
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.whisper.MyApplication.MyApplication
import com.example.whisper.adapter.CommentsAdapter
import com.example.whisper.databinding.ActivityWhisperDetailBinding
import com.example.whisper.model.Comment
import com.example.whisper.model.Whisper
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject
@AndroidEntryPoint
class WhisperDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWhisperDetailBinding
    @Inject
    lateinit var myApp: MyApplication

    @Inject
    lateinit var client: OkHttpClient

    @Inject
    lateinit var mediaType: MediaType
    private var whisperNo: Int = 0
    private lateinit var commentsAdapter: CommentsAdapter
    private lateinit var commentList: MutableList<Comment>
    private lateinit var whisper: Whisper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWhisperDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        whisperNo = intent.getIntExtra("whisperNo", 0)

        setupCommentsRecyclerView()
        setupPostCommentButton()

        // Fetch the whisper details using the whisperNo
        fetchWhisperDetails()
        setupPopupMenu()
        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun setupPopupMenu() {
        binding.dotImage.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.dot_image_menu, popup.menu)

            if (whisper.userId == myApp.loginUserId) {
                popup.menu.findItem(R.id.delete_post).isVisible = true
                popup.menu.findItem(R.id.report_post).isVisible = false
            } else {
                popup.menu.findItem(R.id.delete_post).isVisible = false
                popup.menu.findItem(R.id.report_post).isVisible = true
            }

            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.delete_post -> {
                        deleteWhisper(whisper.whisperNo)
                        true
                    }
                    R.id.report_post -> {
                        reportWhisper(whisper.whisperNo)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    private fun fetchWhisperDetails() {
        val requestBody = JSONObject().apply {
            put("whisperNo", whisperNo)
            put("loginUserId", myApp.loginUserId)
        }.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("${myApp.apiUrl}getWhisperDetail.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@WhisperDetailActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("whisperInfo", "onResponse: $responseBody")
                try {
                    val jsonResponse = JSONObject(responseBody)
                    if (jsonResponse.has("error")) {
                        runOnUiThread {
                            Toast.makeText(this@WhisperDetailActivity, jsonResponse.getString("error"), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        val whisperData = jsonResponse.getJSONObject("data")
                        Log.d("whisperInfo", "onResponse: $whisperData")
                        whisper = Whisper(
                            whisperData.getInt("whisperNo"),
                            whisperData.getString("userId"),
                            whisperData.getString("userName"),
                            whisperData.getString("postDate"),
                            whisperData.getString("content"),
                            whisperData.getInt("goodCount"),
                            whisperData.getBoolean("goodFlg"),
                            whisperData.getString("iconPath"),
                            whisperData.getInt("commentCount")
                        )
                        runOnUiThread {
                            setupWhisperDetails()
                        }
                    }
                } catch (e: JSONException) {
                    runOnUiThread {
                        Toast.makeText(this@WhisperDetailActivity, "Whisper error parsing the response", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun setupWhisperDetails() {
        binding.userNameText.text = whisper.userName
        binding.whisperText.text = whisper.content
        binding.goodCountText.text = whisper.goodCount.toString()
        binding.cmtCountText.text = whisper.commentCount.toString()
        binding.goodImage.setImageResource(if (whisper.goodFlg) R.drawable.ic_star_filled else R.drawable.ic_star_placeholder)
        Glide.with(this).load(myApp.apiUrl + whisper.iconPath).into(binding.userImage)

        binding.goodImage.setOnClickListener {
            toggleLike()
        }
    }

    private fun setupCommentsRecyclerView() {
        commentList = mutableListOf()
        commentsAdapter = CommentsAdapter(null,this, commentList,client,myApp)
        binding.commentsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.commentsRecyclerView.adapter = commentsAdapter
        fetchComments()
    }

    private fun setupPostCommentButton() {
        binding.postCommentButton.setOnClickListener {
            val commentText = binding.commentEditText.text.toString().trim()
            if (commentText.isNotEmpty()) {
                postComment(commentText)
            } else {
                Toast.makeText(this, "Please write a comment", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchComments() {
        val requestBody = JSONObject().apply {
            put("whisperNo", whisperNo)
        }.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("${myApp.apiUrl}commentInfo.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@WhisperDetailActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("comm", "loadComments: $responseBody")
                try {
                    val jsonResponse = JSONObject(responseBody)
                    if (jsonResponse.has("error")) {
                        runOnUiThread {
                            Toast.makeText(this@WhisperDetailActivity, jsonResponse.getString("error"), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        val comments = jsonResponse.getJSONArray("comments")
                        for (i in 0 until comments.length()) {
                            val commentData = comments.getJSONObject(i)
                            val comment = Comment(
                                commentData.getLong("commentId"),
                                commentData.getLong("whisperNo"),
                                commentData.getString("userId"),
                                commentData.getString("userName"),
                                commentData.getString("iconPath"),
                                commentData.getString("content"),
                                commentData.getString("commentDate"),
                                commentData.getInt("likeCount")
                            )
                            commentList.add(comment)
                        }
                        runOnUiThread {
                            commentsAdapter.notifyDataSetChanged()
                        }
                    }
                } catch (e: JSONException) {
                    runOnUiThread {
                        Toast.makeText(this@WhisperDetailActivity, "Comment Error parsing the response", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun postComment(commentText: String) {
        val requestBody = JSONObject().apply {
            put("userId", myApp.loginUserId)
            put("whisperNo", whisperNo)
            put("content", commentText)
        }.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("${myApp.apiUrl}addComment.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@WhisperDetailActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                try {
                    val jsonResponse = JSONObject(responseBody)
                    if (jsonResponse.has("error")) {
                        runOnUiThread {
                            Toast.makeText(this@WhisperDetailActivity, jsonResponse.getString("error"), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        runOnUiThread {
                            binding.commentEditText.text.clear()
                            fetchComments()  // Refresh comments after posting
                        }
                    }
                } catch (e: JSONException) {
                    runOnUiThread {
                        Toast.makeText(this@WhisperDetailActivity, "Error parsing the response", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun toggleLike() {
        val requestBody = JSONObject().apply {
            put("userId", myApp.loginUserId)
            put("whisperNo", whisperNo)
            put("goodFlg", !whisper.goodFlg) // Toggle the current goodFlg status
        }.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("${myApp.apiUrl}goodCtl.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@WhisperDetailActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                try {
                    val jsonResponse = JSONObject(responseBody)
                    if (jsonResponse.has("error")) {
                        runOnUiThread {
                            Toast.makeText(this@WhisperDetailActivity, jsonResponse.getString("error"), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        runOnUiThread {
                            whisper.goodFlg = !whisper.goodFlg
                            binding.goodImage.setImageResource(if (whisper.goodFlg) R.drawable.ic_star_filled else R.drawable.ic_star_placeholder)
                            whisper.goodCount = if (whisper.goodFlg) whisper.goodCount + 1 else whisper.goodCount - 1
                            binding.goodCountText.text = whisper.goodCount.toString()
                        }
                    }
                } catch (e: JSONException) {
                    runOnUiThread {
                        Toast.makeText(this@WhisperDetailActivity, "Error parsing the response", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun deleteWhisper(whisperNo: Int) {
        val requestBody = JSONObject().apply {
            put("whisperNo", whisperNo)
        }.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("${myApp.apiUrl}deleteWhisper.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@WhisperDetailActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                try {
                    val jsonResponse = JSONObject(responseBody)
                    if (jsonResponse.has("error")) {
                        runOnUiThread {
                            Toast.makeText(this@WhisperDetailActivity, jsonResponse.getString("error"), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        runOnUiThread {
                            // Xử lý logic xóa bài viết
                        }
                    }
                } catch (e: JSONException) {
                    runOnUiThread {
                        Toast.makeText(this@WhisperDetailActivity, "Error parsing the response", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun reportWhisper(whisperNo: Int) {
        Toast.makeText(this, "Reported whisper #$whisperNo", Toast.LENGTH_SHORT).show()
    }
}
