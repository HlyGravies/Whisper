package com.example.whisper.adapter

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.whisper.MainActivity
import com.example.whisper.R
import com.example.whisper.UserInfoActivity
import com.example.whisper.WhisperDetailActivity
import com.example.whisper.databinding.ItemNotificationBinding
import com.example.whisper.model.Notification
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject

class NotificationsAdapter @Inject constructor(
    private val context: Context,
    private val notifications: MutableList<Notification>,
    private val client: OkHttpClient,
    private val apiUrl: String
) : RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: Notification) {
            binding.userName.text = notification.userName
            binding.notificationContent.text = when (notification.type) {
                "like" -> "liked your whisper"
                "comment" -> "commented on your whisper"
                else -> ""
            }
            Glide.with(context).load(apiUrl + notification.userIcon).into(binding.userIcon)
            if (notification.imagePath != null) {
                binding.whisperImage.visibility = View.VISIBLE
                Glide.with(context).load(notification.imagePath).into(binding.whisperImage)
            } else {
                binding.whisperImage.visibility = View.GONE
            }

            itemView.setBackgroundColor(
                if (notification.isRead) ContextCompat.getColor(context, R.color.notification_read)
                else ContextCompat.getColor(context, R.color.notification_unread)
            )

            itemView.setOnClickListener {
                val intent = Intent(context, WhisperDetailActivity::class.java)
                intent.putExtra("whisperNo", notification.whisperNo)
                context.startActivity(intent)
                markAsRead(notification)
                notification.isRead = true
                notifyItemChanged(adapterPosition)
                var baseContext = context
                while (baseContext is ContextWrapper) {
                    if (baseContext is MainActivity) {
                        baseContext.updateNotificationBadge(notifications.count { !it.isRead })
                        break
                    }
                    baseContext = baseContext.baseContext
                }            }

            binding.userIcon.setOnClickListener {
                val intent = Intent(context, UserInfoActivity::class.java)
                intent.putExtra("userId", notification.userId)
                context.startActivity(intent)
            }
        }

        private fun markAsRead(notification: Notification) {
            val requestBody = JSONObject().apply {
                put("userId", notification.userId)
                put("whisperNo", notification.whisperNo)
                put("type", notification.type)
            }.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url("$apiUrl/updateNotificationStatus.php")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // Handle failure
                }

                override fun onResponse(call: Call, response: Response) {
                    // Handle success
                }
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.bind(notification)
    }

    override fun getItemCount() = notifications.size
}
