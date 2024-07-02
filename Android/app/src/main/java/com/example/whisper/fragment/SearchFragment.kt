package com.example.whisper.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whisper.GoodListAdapter
import com.example.whisper.MyApplication.MyApplication
import com.example.whisper.adapter.UserListAdapter
import com.example.whisper.databinding.FragmentSearchBinding
import com.example.whisper.model.Good
import com.example.whisper.model.User
import com.google.android.material.tabs.TabLayout
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val myApp: MyApplication by lazy { activity?.application as MyApplication }

    private var selectedSection: String = "1" // Default to "User" tab

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        val view = binding.root

        // Thêm các tab vào TabLayout
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("User"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Whisper"))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                selectedSection = when (tab.position) {
                    0 -> "1" // User tab
                    1 -> "2" // Whisper tab
                    else -> "1"
                }
                val query = binding.searchEdit.text.toString()
                if (query.isNotEmpty()) {
                    fetchSearchResults(selectedSection, query)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {
                val query = binding.searchEdit.text.toString()
                if (query.isNotEmpty()) {
                    fetchSearchResults(selectedSection, query)
                }
            }
        })

        binding.searchButton.setOnClickListener {
            val query = binding.searchEdit.text.toString()
            if (query.isEmpty()) {
                Toast.makeText(activity, "Please enter search text.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            fetchSearchResults(selectedSection, query)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchSearchResults(section: String, query: String) {
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = JSONObject().apply {
            put("section", section)
            put("string", query)
        }.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("${myApp.apiUrl}search.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(activity, "Request failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("SearchFragment", "onResponse: $responseBody")
                try {
                    val jsonResponse = JSONObject(responseBody)
                    if (jsonResponse.has("error")) {
                        activity?.runOnUiThread {
                            Toast.makeText(activity, jsonResponse.getString("error"), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        updateUI(jsonResponse, section)
                    }
                } catch (e: JSONException) {
                    activity?.runOnUiThread {
                        Toast.makeText(activity, "Error parsing the response", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun updateUI(json: JSONObject, section: String) {
        val listUser = mutableListOf<User>()
        val listWhisper = mutableListOf<Good>()

        activity?.runOnUiThread {
            binding.searchRecycle.layoutManager = LinearLayoutManager(activity)
            if (section == "1") {
                if (json.has("userList")) {
                    val userArray = json.getJSONArray("userList")
                    for (i in 0 until userArray.length()) {
                        val user = userArray.getJSONObject(i)
                        listUser.add(
                            User(
                                userId = user.getString("userId"),
                                userName = user.getString("userName"),
                                whisperCount = user.getInt("whisperCount"),
                                followCount = user.getInt("followCount"),
                                followerCount = user.getInt("followerCount"),
                                iconPath = user.getString("iconPath")
                            )
                        )
                    }
                }
                if (listUser.isEmpty()) {
                    binding.errorText.visibility = View.VISIBLE
                    binding.errorText.text = "No users found"
                } else {
                    binding.errorText.visibility = View.GONE
                }
                val adapter = UserListAdapter(activity!!, listUser)
                binding.searchRecycle.adapter = adapter
                adapter.notifyDataSetChanged()
            } else if (section == "2") {
                if (json.has("whisperList")) {
                    val whisperArray = json.getJSONArray("whisperList")
                    for (i in 0 until whisperArray.length()) {
                        val whisper = whisperArray.getJSONObject(i)
                        listWhisper.add(
                            Good(
                                whisperNo = whisper.getInt("whisperNo"),
                                userId = whisper.getString("userId"),
                                userName = whisper.getString("userName"),
                                content = whisper.getString("content"),
                                postDate = whisper.getString("postDate"),
                                goodCount = if (whisper.has("goodCount")) whisper.getInt("goodCount") else 0,
                                iconPath = whisper.getString("iconPath")
                            )
                        )
                    }
                }
                if (listWhisper.isEmpty()) {
                    binding.errorText.visibility = View.VISIBLE
                    binding.errorText.text = "No whispers found"
                } else {
                    binding.errorText.visibility = View.GONE
                }
                val adapter = GoodListAdapter(activity!!, listWhisper)
                binding.searchRecycle.adapter = adapter
                adapter.notifyDataSetChanged()
            }
        }
    }}
