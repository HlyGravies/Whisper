package com.example.whisper.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.whisper.CommentsBottomSheetFragment
import com.example.whisper.MyApplication.MyApplication
import com.example.whisper.R
import com.example.whisper.UserInfoActivity
import com.example.whisper.databinding.RecycleCommentBinding
import com.example.whisper.model.Comment
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class CommentsAdapter(
    private val fragment: CommentsBottomSheetFragment?,
    private val activity: Activity,
    private val comments: List<Comment>
) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    private val myApp = activity.application as MyApplication

    inner class CommentViewHolder(val binding: RecycleCommentBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.dotImage.setOnClickListener { view ->
                val position = adapterPosition
                val comment = comments[position]

                // Create a PopupMenu
                val popup = PopupMenu(activity, view)
                // Inflate the menu from xml
                popup.menuInflater.inflate(R.menu.dot_image_menu, popup.menu)

                // Check ownership
                if (comment.userId == myApp.loginUserId) {
                    // User is the owner of the whisper
                    popup.menu.findItem(R.id.delete_post).isVisible = true
                    popup.menu.findItem(R.id.report_post).isVisible = false
                } else {
                    // User is not the owner of the whisper
                    popup.menu.findItem(R.id.delete_post).isVisible = false
                    popup.menu.findItem(R.id.report_post).isVisible = true
                }

                // Setup menu item click
                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.delete_post -> {
                            // Handle delete post action here
                            deleteComment(comment.whisperNo, comment.commentId, comment.userId)
                            true
                        }
                        R.id.report_post -> {
                            // Handle report post action here
                            reportComment(comment.whisperNo)
                            true
                        }
                        else -> false
                    }
                }
                // Show the PopupMenu
                popup.show()
            }
        }
    }

    private fun reportComment(whisperNo: Any) {
        // TODO: Implement report comment functionality
    }

    private fun deleteComment(whisperNo: Long, commentId: Long, userId: String) {
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = JSONObject().apply {
            put("whisperNo", whisperNo)
            put("commentId", commentId)
            put("userId", userId)
        }.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url(myApp.apiUrl + "deleteComment.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                fragment?.activity?.runOnUiThread {
                    Toast.makeText(fragment.activity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                try {
                    val jsonResponse = JSONObject(responseBody)
                    if (jsonResponse.has("error")) {
                        fragment?.activity?.runOnUiThread {
                            Toast.makeText(fragment.activity, jsonResponse.getString("error"), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        fragment?.activity?.runOnUiThread {
                            // Refresh the comments list after deletion
                            fragment.commentsList.clear()
                            fragment.loadComments()
                        }
                    }
                } catch (e: JSONException) {
                    activity?.runOnUiThread {
                        Toast.makeText(activity, "Error parsing the response", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = RecycleCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.binding.userNameText.text = comment.userName
        holder.binding.cmtText.text = comment.content
        Glide.with(holder.itemView.context)
            .load(myApp.apiUrl + comment.iconPath)
            .diskCacheStrategy(DiskCacheStrategy.NONE) // Disable cache
            .skipMemoryCache(true)
            .into(holder.binding.userImage)
        holder.binding.userImage.setOnClickListener {
            val intent = Intent(activity, UserInfoActivity::class.java).apply {
                putExtra("userId", comment.userId)
            }
            activity.startActivity(intent)
        }
    }

    override fun getItemCount() = comments.size
}
