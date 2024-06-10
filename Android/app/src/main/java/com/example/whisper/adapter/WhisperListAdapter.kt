package com.example.whisper

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
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
    private val loginUserId: String
) : RecyclerView.Adapter<WhisperListAdapter.WhisperViewHolder>() {

    private val myApp = activity.application as MyApplication

    inner class WhisperViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userImage: ImageView = itemView.findViewById(R.id.userImage)
        val userName: TextView = itemView.findViewById(R.id.userNameText)
        val whisperText: TextView = itemView.findViewById(R.id.whisperText)
        val goodImage: ImageView = itemView.findViewById(R.id.goodImage)
        val dotImage: ImageView = itemView.findViewById(R.id.dotImage)

        init {
            dotImage.setOnClickListener { view ->
                // Create a PopupMenu
                val popup = PopupMenu(activity, view)
                // Inflate the menu from xml
                popup.menuInflater.inflate(R.menu.dot_image_menu, popup.menu)
                // Setup menu item click
                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.delete_post -> {
                            // Handle delete post action here
                            deleteWhisper(whispers[adapterPosition].whisperNo)
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WhisperViewHolder {
        val view = LayoutInflater.from(activity).inflate(R.layout.recycler_whisper, parent, false)
        return WhisperViewHolder(view)
    }

    override fun onBindViewHolder(holder: WhisperViewHolder, position: Int) {
        val whisper = whispers[position]

        holder.userName.text = whisper.userName
        holder.whisperText.text = whisper.content
        holder.goodImage.setImageResource(if (whisper.goodFlg) R.drawable.ic_star_filled else R.drawable.ic_star_placeholder)
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
}