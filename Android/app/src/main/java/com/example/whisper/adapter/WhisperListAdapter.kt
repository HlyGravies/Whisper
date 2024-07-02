package com.example.whisper

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.whisper.Interface.OnDataRefreshNeededListener
import com.example.whisper.MyApplication.MyApplication
import com.example.whisper.model.Whisper
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class WhisperListAdapter(
    private val activity: Activity,
    private val whispers: MutableList<Whisper>,
    private val loginUserId: String,
    private val dataRefreshListener: OnDataRefreshNeededListener
) : RecyclerView.Adapter<WhisperListAdapter.WhisperViewHolder>() {

    private val myApp = activity.application as MyApplication

    inner class WhisperViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userImage: ImageView = itemView.findViewById(R.id.userImage)
        val userName: TextView = itemView.findViewById(R.id.userNameText)
        val whisperText: TextView = itemView.findViewById(R.id.whisperText)
        val goodImage: ImageView = itemView.findViewById(R.id.goodImage)
        val dotImage: ImageView = itemView.findViewById(R.id.dotImage)
        val goodCount: TextView = itemView.findViewById(R.id.goodCountText)
        val commentImage: ImageView = itemView.findViewById(R.id.cmtImage)
        val cmtCountText: TextView = itemView.findViewById(R.id.cmtCountText)

        init {
            dotImage.setOnClickListener { view ->
                val position = adapterPosition
                val whisper = whispers[position]

                // Create a PopupMenu
                val popup = PopupMenu(activity, view)
                // Inflate the menu from xml
                popup.menuInflater.inflate(R.menu.dot_image_menu, popup.menu)

                // Check ownership
                if (whisper.userId == loginUserId) {
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
                            deleteWhisper(whisper.whisperNo)
                            true
                        }
                        R.id.report_post -> {
                            // Handle report post action here
                            reportWhisper(whisper.whisperNo)
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


    fun clearData() {
        whispers.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WhisperViewHolder {
        val view = LayoutInflater.from(activity).inflate(R.layout.recycler_whisper, parent, false)
        return WhisperViewHolder(view)
    }

    override fun onBindViewHolder(holder: WhisperViewHolder, position: Int) {
        val whisper = whispers[position]

        holder.userName.text = whisper.userName
        holder.whisperText.text = whisper.content.replace("\\n", "\n")
        holder.goodCount.text = whisper.goodCount.toString()
        holder.cmtCountText.text = whisper.commentCount.toString()
        holder.goodImage.setImageResource(if (whisper.goodFlg) R.drawable.ic_star_filled else R.drawable.ic_star_placeholder)
        holder.goodImage.setOnClickListener {
            toggleLike(whisper, holder)
        }

        Glide.with(holder.userImage.context)
            .load(myApp.apiUrl + whisper.iconPath) // URL của hình ảnh
            .placeholder(R.drawable.loading)
            .error(R.drawable.avatar)
            .into(holder.userImage)

        // Set click listener for userImage
        holder.userImage.setOnClickListener {
            val intent = Intent(activity, UserInfoActivity::class.java).apply {
                putExtra("userId", whisper.userId)
            }
            activity.startActivity(intent)
        }
        holder.commentImage.setOnClickListener {
            val bottomSheetFragment = CommentsBottomSheetFragment.newInstance(whisper.whisperNo.toLong())
            if (activity is AppCompatActivity) {
                bottomSheetFragment.show(activity.supportFragmentManager, bottomSheetFragment.tag)
            }
        }
    }

    override fun getItemCount(): Int {
        return whispers.size
    }

    private fun deleteWhisper(whisperNo: Int) {
        val client = OkHttpClient()
        val mediaType: MediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = JSONObject().apply {
            put("whisperNo", whisperNo)
        }.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("${myApp.apiUrl}deleteWhisper.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity.runOnUiThread {
                    Toast.makeText(activity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                try {
                    val jsonResponse = JSONObject(responseBody)
                    if (jsonResponse.has("error")) {
                        activity.runOnUiThread {
                            Toast.makeText(activity, jsonResponse.getString("error"), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        activity.runOnUiThread {
                            // Remove the deleted whisper from the list and notify the adapter
                            whispers.removeIf { it.whisperNo == whisperNo }
                            notifyDataSetChanged()
                        }
                    }
                } catch (e: JSONException) {
                    activity.runOnUiThread {
                        Toast.makeText(activity, "Error parsing the response", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun reportWhisper(whisperNo: Int) {
        // Implement your report whisper logic here
        Toast.makeText(activity, "Reported whisper #$whisperNo", Toast.LENGTH_SHORT).show()
    }
    private fun toggleLike(whisper: Whisper, holder: WhisperViewHolder) {
        val client = OkHttpClient()
        val mediaType: MediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = JSONObject().apply {
            put("userId", loginUserId)
            put("whisperNo", whisper.whisperNo)
            put("goodFlg", !whisper.goodFlg) // Toggle the current goodFlg status
        }.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("${myApp.apiUrl}goodCtl.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity.runOnUiThread {
                    Toast.makeText(activity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("Goodcnt", "onResponse: $responseBody")
                try {
                    val jsonResponse = JSONObject(responseBody)
                    if (jsonResponse.has("error")) {
                        activity.runOnUiThread {
                            Toast.makeText(activity, jsonResponse.getString("error"), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        activity.runOnUiThread {
                            // Toggle the goodFlg status and update the image resource
                            val index = whispers.indexOf(whisper)
                            whispers[index].goodFlg = !whispers[index].goodFlg
                            holder.goodImage.setImageResource(if (whispers[index].goodFlg) R.drawable.ic_star_filled else R.drawable.ic_star_placeholder)
                        }
                    }
                } catch (e: JSONException) {
                    activity.runOnUiThread {
                        Toast.makeText(activity, "Error parsing the response", Toast.LENGTH_LONG).show()
                    }
                }
                dataRefreshListener.onDataRefreshNeeded()
            }
        })
    }
    fun updateData(newData: List<Whisper>) {
        whispers.clear()
        whispers.addAll(newData)
        notifyDataSetChanged()
    }

    fun addData(newData: List<Whisper>) {
        val startPosition = whispers.size
        whispers.addAll(newData)
        notifyItemRangeInserted(startPosition, newData.size)
    }
}
