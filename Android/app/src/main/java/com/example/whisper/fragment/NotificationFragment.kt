package com.example.whisper.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whisper.MainActivity
import com.example.whisper.MyApplication.MyApplication
import com.example.whisper.adapter.NotificationsAdapter
import com.example.whisper.databinding.FragmentNotificationBinding
import com.example.whisper.model.Notification
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class NotificationFragment : Fragment() {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var myApp: MyApplication

    @Inject
    lateinit var client: OkHttpClient

    @Inject
    lateinit var mediaType: MediaType

    private lateinit var notificationsAdapter: NotificationsAdapter
    private val notificationsList = mutableListOf<Notification>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)

        setupRecyclerView()
        fetchNotifications()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        notificationsAdapter = NotificationsAdapter(requireContext(), notificationsList,client,myApp.apiUrl)
        binding.notificationsRecyclerView.layoutManager = LinearLayoutManager(activity)
        binding.notificationsRecyclerView.adapter = notificationsAdapter
    }

    private fun fetchNotifications() {
        val requestBody = JSONObject().apply {
            put("userId", myApp.loginUserId)
        }.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("${myApp.apiUrl}getNotifications.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(activity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("getNotifications", "onResponse: $responseBody")
                try {
                    val jsonResponse = JSONObject(responseBody)
                    if (jsonResponse.has("error")) {
                        activity?.runOnUiThread {
                            Toast.makeText(activity, jsonResponse.getString("error"), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        val likes = jsonResponse.getJSONArray("likes")
                        val comments = jsonResponse.getJSONArray("comments")
                        parseNotifications(likes, comments)
                    }
                } catch (e: JSONException) {
                    activity?.runOnUiThread {
                        Toast.makeText(activity, "Error parsing the response", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun parseNotifications(likes: JSONArray, comments: JSONArray) {
        notificationsList.clear()

        var unreadCount = 0

        for (i in 0 until likes.length()) {
            val like = likes.getJSONObject(i)
            val isRead = like.getInt("isRead") == 1
            if (!isRead) unreadCount++
            notificationsList.add(Notification(
                type = "like",
                whisperNo = like.getInt("whisperNo"),
                userName = like.getString("likerName"),
                userIcon = like.getString("likerIcon"),
                content = like.getString("whisperContent"),
                imagePath = like.optString("whisperImage", null),
                userId = like.getString("likerId"),
                isRead = isRead
            ))
        }

        for (i in 0 until comments.length()) {
            val comment = comments.getJSONObject(i)
            val isRead = comment.getInt("isRead") == 1
            if (!isRead) unreadCount++
            notificationsList.add(Notification(
                type = "comment",
                whisperNo = comment.getInt("whisperNo"),
                userName = comment.getString("commenterName"),
                userIcon = comment.getString("commenterIcon"),
                content = comment.getString("commentContent"),
                imagePath = comment.optString("whisperImage", null),
                userId = comment.getString("commenterId"),
                isRead = isRead
            ))
        }

        activity?.runOnUiThread {
            notificationsAdapter.notifyDataSetChanged()
            (activity as MainActivity).updateNotificationBadge(unreadCount)
        }
    }
}
