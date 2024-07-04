package com.example.whisper.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whisper.MyApplication.MyApplication
import com.example.whisper.WhisperListAdapter
import com.example.whisper.databinding.FragmentTimelineBinding
import com.example.whisper.model.Whisper
import com.example.whisper.Interface.OnDataRefreshNeededListener
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class TimelineFragment : Fragment(), OnDataRefreshNeededListener {

    private var _binding: FragmentTimelineBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var myApp: MyApplication

    @Inject
    lateinit var client: OkHttpClient

    @Inject
    lateinit var mediaType: MediaType

    private lateinit var adapter: WhisperListAdapter
    private var isRefreshing = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTimelineBinding.inflate(inflater, container, false)

        setupUI()
        getTimelineInfoApiCall() // Load initial data

        return binding.root
    }

    private fun setupUI() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            isRefreshing = true
            getTimelineInfoApiCall()
        }

        binding.timelineRecycle.layoutManager = LinearLayoutManager(activity)
        adapter = WhisperListAdapter(requireActivity(), mutableListOf(), myApp.loginUserId, this@TimelineFragment)
        binding.timelineRecycle.adapter = adapter
    }

    override fun onDataRefreshNeeded() {
        isRefreshing = true
        getTimelineInfoApiCall()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getTimelineInfoApiCall() {
        val requestBody = JSONObject().apply {
            put("userId", myApp.loginUserId)
        }.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("${myApp.apiUrl}timelineInfo.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(activity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    if (isRefreshing) {
                        binding.swipeRefreshLayout.isRefreshing = false
                        isRefreshing = false
                    }
                    binding.progressBar.visibility = View.GONE
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("TimelineApi", "onResponse: $responseBody")
                try {
                    val jsonResponse = JSONObject(responseBody)
                    if (jsonResponse.has("whisperList")) {
                        val timelineList = jsonResponse.getJSONArray("whisperList")
                        val whispers = mutableListOf<Whisper>()

                        for (i in 0 until timelineList.length()) {
                            val item = timelineList.getJSONObject(i)
                            val whisper = Whisper(
                                item.getInt("whisperNo"),
                                item.getString("userId"),
                                item.getString("userName"),
                                item.getString("postDate"),
                                item.getString("content"),
                                item.getInt("goodCount"),
                                item.getBoolean("goodFlg"),
                                item.getString("iconPath"),
                                item.getInt("commentCount")
                            )
                            whispers.add(whisper)
                        }

                        activity?.runOnUiThread {
                            if (isRefreshing) {
                                adapter.updateData(whispers)
                                binding.swipeRefreshLayout.isRefreshing = false
                                isRefreshing = false
                            } else {
                                adapter.addData(whispers)
                            }
                        }
                    }
                } catch (e: JSONException) {
                    activity?.runOnUiThread {
                        Toast.makeText(activity, "Error parsing the response", Toast.LENGTH_LONG).show()
                        if (isRefreshing) {
                            binding.swipeRefreshLayout.isRefreshing = false
                            isRefreshing = false
                        }
                    }
                }
            }
        })
    }
}
