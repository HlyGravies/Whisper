package com.example.whisper.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.whisper.MainActivity
import com.example.whisper.MyApplication.MyApplication
import com.example.whisper.R
import com.example.whisper.UserInfoActivity
import com.example.whisper.WhisperDetailActivity
import com.example.whisper.model.Notification
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class NotificationsAdapter(private val context: Context, private val notifications: MutableList<Notification>) :
    RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>() {
    private val myApp = context.applicationContext as MyApplication
    private val client = OkHttpClient()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.bind(notification)
    }

    override fun getItemCount() = notifications.size

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userIcon: ImageView = itemView.findViewById(R.id.userIcon)
        private val userName: TextView = itemView.findViewById(R.id.userName)
        private val notificationContent: TextView = itemView.findViewById(R.id.notificationContent)
        private val whisperImage: ImageView = itemView.findViewById(R.id.whisperImage)

        fun bind(notification: Notification) {
            userName.text = notification.userName
            notificationContent.text = when (notification.type) {
                "like" -> "liked your whisper"
                "comment" -> "commented on your whisper"
                else -> ""
            }
            Glide.with(context).load(myApp.apiUrl + notification.userIcon).into(userIcon)
            if (notification.imagePath != null) {
                whisperImage.visibility = View.VISIBLE
                Glide.with(context).load(notification.imagePath).into(whisperImage)
            } else {
                whisperImage.visibility = View.GONE
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
                (context as MainActivity).updateNotificationBadge(notifications.count { !it.isRead })
            }

            userIcon.setOnClickListener {
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
                .url("${myApp.apiUrl}updateNotificationStatus.php")
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
}
