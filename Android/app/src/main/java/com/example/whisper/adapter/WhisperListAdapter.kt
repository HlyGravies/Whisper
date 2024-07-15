package com.example.whisper

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.whisper.Interface.OnDataRefreshNeededListener
import com.example.whisper.MyApplication.MyApplication
import com.example.whisper.databinding.RecyclerWhisperBinding
import com.example.whisper.fragment.TimelineFragment
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

    inner class WhisperViewHolder(val binding: RecyclerWhisperBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.dotImage.setOnClickListener { view ->
                val position = adapterPosition
                val whisper = whispers[position]

                // Create a PopupMenu
                val popup = PopupMenu(activity, view)
                // Inflate the menu from xml
                popup.menuInflater.inflate(R.menu.dot_image_menu, popup.menu)

                // Check ownership
                if (whisper.userId == loginUserId) {
                    // User is the owner of the whisper
                    popup.menu.findItem(R.id.delete_post).isVisible = false
                    popup.menu.findItem(R.id.report_post).isVisible = false
                    showOptionsDialog(position)
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
            binding.cardView.setOnClickListener {
                val position = adapterPosition
                val whisper = whispers[position]
                val intent = Intent(activity, WhisperDetailActivity::class.java).apply {
                    putExtra("whisperNo", whisper.whisperNo)
                }
                activity.startActivity(intent)
            }
        }
    }

    fun clearData() {
        whispers.clear()
        notifyDataSetChanged()
    }
    private fun showOptionsDialog(position: Int) {
        val whisper = whispers[position]
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Options")
        builder.setMessage("What do you want to do?")
        builder.setPositiveButton("Delete") { _, _ ->
            deleteWhisper(whisper.whisperNo)
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WhisperViewHolder {
        val binding = RecyclerWhisperBinding.inflate(LayoutInflater.from(activity), parent, false)
        return WhisperViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WhisperViewHolder, position: Int) {
        val whisper = whispers[position]

        holder.binding.userNameText.text = whisper.userName
        holder.binding.whisperText.text = whisper.content.replace("\\n", "\n")
        holder.binding.goodCountText.text = whisper.goodCount.toString()
        holder.binding.cmtCountText.text = whisper.commentCount.toString()
        holder.binding.goodImage.setImageResource(if (whisper.goodFlg) R.drawable.ic_star_filled else R.drawable.ic_star_placeholder)
        holder.binding.goodImage.setOnClickListener {
            toggleLike(whisper, holder)
        }

        Glide.with(holder.binding.userImage.context)
            .load(myApp.apiUrl + whisper.iconPath)
            .diskCacheStrategy(DiskCacheStrategy.NONE) // Disable cache
            .skipMemoryCache(true)
            .placeholder(R.drawable.loading)
            .error(R.drawable.avatar)
            .into(holder.binding.userImage)

        // Set click listener for userImage
        holder.binding.userImage.setOnClickListener {
            val intent = Intent(activity, UserInfoActivity::class.java).apply {
                putExtra("userId", whisper.userId)
            }
            activity.startActivity(intent)
        }
        holder.binding.cmtImage.setOnClickListener {
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
                            holder.binding.goodImage.setImageResource(if (whispers[index].goodFlg) R.drawable.ic_star_filled else R.drawable.ic_star_placeholder)
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