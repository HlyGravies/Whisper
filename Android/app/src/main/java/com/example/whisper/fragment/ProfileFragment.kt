package com.example.whisper.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.whisper.FollowListActivity
import com.example.whisper.Interface.OnDataRefreshNeededListener
import com.example.whisper.LoginActivity
import com.example.whisper.MyApplication.MyApplication
import com.example.whisper.R
import com.example.whisper.UserEditActivity
import com.example.whisper.WhisperListAdapter
import com.example.whisper.databinding.FragmentProfileBinding
import com.example.whisper.model.Whisper
import com.google.android.material.tabs.TabLayout
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class ProfileFragment : Fragment(), OnDataRefreshNeededListener {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val myApp: MyApplication by lazy { activity?.application as MyApplication }
    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        userId = myApp.userId
        Log.d("TAGcheckus", "onCreateView: $userId")

        getUserInfoApiCall()
        getFollowInfoApiCall()
        getUserWhispersApiCall()

        binding.userRecycle.layoutManager = LinearLayoutManager(activity)
        binding.userRecycle.adapter = WhisperListAdapter(requireActivity(), mutableListOf(), myApp.loginUserId,this@ProfileFragment)

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Whisper"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Good Info"))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                (binding.userRecycle.adapter as WhisperListAdapter).clearData()
                if (tab.position == 0) {
                    binding.errorText.text = "" // Clear the error text
                    getUserWhispersApiCall()
                } else if (tab.position == 1) {
                    binding.errorText.text = "" // Clear the error text
                    getUserGoodWhispersApiCall()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        binding.followText.setOnClickListener {
            val intent = Intent(activity, FollowListActivity::class.java)
            intent.putExtra("userId", userId)
            intent.putExtra("isFollow", true)
            startActivity(intent)
        }

        binding.followerText.setOnClickListener {
            val intent = Intent(activity, FollowListActivity::class.java)
            intent.putExtra("userId", userId)
            intent.putExtra("isFollow", false)
            startActivity(intent)
        }
        binding.editInfoBtn.setOnClickListener {
            val intent = Intent(activity, UserEditActivity::class.java)
            startActivity(intent)
        }
        binding.settingBtn.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Sign Out")
                .setMessage("Do you want to sign out of your account?")
                .setPositiveButton("Sign out") { dialog, which ->
                    myApp.loginUserId = "lo"
                    // Navigate to Login screen and clear previous screen info
                    val intent = Intent(activity, LoginActivity::class.java)
//                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    activity?.startActivity(intent)
                }
                .setNegativeButton("Cancel") { dialog, which ->
                    dialog.dismiss()
                }
                .show()
        }
        return binding.root
    }
    override fun onDataRefreshNeeded() {
        // Implementation of onDataRefreshNeeded
        // For example, you might want to refresh the user info
        getUserInfoApiCall()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getUserGoodWhispersApiCall() {
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = JSONObject().apply {
            put("userId", userId)
            put("loginUserId", myApp.loginUserId)
        }.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("${myApp.apiUrl}userWhisperInfo.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(myApp, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("API Response", responseBody ?: "No response body")

                try {
                    val jsonResponse = JSONObject(responseBody)
                    if (jsonResponse.getJSONObject("data").isNull("allLikedWhisperList")) {
                        activity?.runOnUiThread {
                            binding.errorText.text = "”いいね！”した囁やきがありません！"
                        }
                    } else {
                        val list = mutableListOf<Whisper>()
                        val whispers = jsonResponse.getJSONObject("data").getJSONArray("allLikedWhisperList")
                        for (i in 0 until whispers.length()) {
                            val whisper = whispers.getJSONObject(i)
                            list.add(
                                Whisper(
                                    whisper.getInt("whisperNo"),
                                    whisper.getString("userId"),
                                    whisper.getString("userName"),
                                    whisper.getString("postDate"),
                                    whisper.getString("content"),
                                    whisper.getInt("goodCount"),
                                    whisper.getBoolean("goodFlg"),
                                    whisper.getString("iconPath"),
                                    whisper.getInt("commentCount")
                                )
                            )
                        }

                        if (jsonResponse.has("error")) {
                            activity?.runOnUiThread {
                                Toast.makeText(myApp, jsonResponse.getString("error"), Toast.LENGTH_LONG).show()
                            }
                        } else {
                            activity?.runOnUiThread {
                                Log.d("GoodWhispers", list.toString())
                                binding.userRecycle.layoutManager = LinearLayoutManager(activity)
                                val adapter = WhisperListAdapter(activity!!, list, myApp.loginUserId, this@ProfileFragment)
                                binding.userRecycle.adapter = adapter
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
                } catch (e: JSONException) {
                    activity?.runOnUiThread {
                        Toast.makeText(myApp, "Error parsing the response", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun getUserInfoApiCall() {
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = JSONObject().apply {
            put("userId", userId)
            put("loginUserId", myApp.loginUserId)
        }.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("${myApp.apiUrl}userInfo.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(myApp, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                try {
                    val jsonResponse = JSONObject(responseBody)
                    if (jsonResponse.has("error")) {
                        activity?.runOnUiThread {
                            Toast.makeText(myApp, jsonResponse.getString("error"), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        if (jsonResponse.has("userData") && !jsonResponse.isNull("userData")) {
                            val userData = jsonResponse.getJSONObject("userData")
                            activity?.runOnUiThread {
                                binding.userNameText.text = userData.getString("userName")
                                binding.profileText.text = userData.getString("profile")
                                val iconPath = userData.getString("iconPath")
                                if (iconPath.isNotEmpty()) {
                                    myApp.iconPath = myApp.apiUrl + iconPath
                                    Glide.with(this@ProfileFragment)
                                        .load(myApp.iconPath)
                                        .diskCacheStrategy(DiskCacheStrategy.NONE) // Disable cache
                                        .skipMemoryCache(true) // Skip memory cache
                                        .placeholder(R.drawable.loading)
                                        .error(R.drawable.avatar)
                                        .circleCrop()
                                        .into(binding.userImage)
                                }
                            }
                        }
                    }
                } catch (e: JSONException) {
                    activity?.runOnUiThread {
                        Toast.makeText(myApp, "UserInfo Error parsing the response", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun getFollowInfoApiCall() {
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = JSONObject().apply {
            put("userId", myApp.loginUserId)
        }.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("${myApp.apiUrl}followerInfo.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(myApp, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("Followa Response", "Response: $responseBody")
                try {
                    val jsonResponse = JSONObject(responseBody)
                    if (jsonResponse.has("error")) {
                        activity?.runOnUiThread {
                            Toast.makeText(myApp, jsonResponse.getString("error"), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        val followList = jsonResponse.getJSONObject("data").getJSONArray("followList")
                        val followerList = jsonResponse.getJSONObject("data").getJSONArray("followerList")
                        activity?.runOnUiThread {
                            binding.followCntText.text = followList.length().toString()
                            binding.followerCntText.text = followerList.length().toString()
                        }
                    }
                } catch (e: JSONException) {
                    activity?.runOnUiThread {
                        Toast.makeText(myApp, "Follow Error parsing the response", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun getUserWhispersApiCall() {
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = JSONObject().apply {
            put("userId", userId)
            put("loginUserId", myApp.loginUserId)
        }.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("${myApp.apiUrl}userWhisperInfo.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(myApp, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("API Response", responseBody ?: "No response body")

                try {
                    val jsonResponse = JSONObject(responseBody)
                    if (jsonResponse.getJSONObject("data").isNull("whisperList")) {
                        activity?.runOnUiThread {
                            binding.errorText.text = "まだ囁やきがありません！"
                        }
                    } else {
                        val list = mutableListOf<Whisper>()
                        val whispers = jsonResponse.getJSONObject("data").getJSONArray("whisperList")
                        for (i in 0 until whispers.length()) {
                            val whisper = whispers.getJSONObject(i)
                            list.add(
                                Whisper(
                                    whisper.getInt("whisperNo"),
                                    whisper.getString("userId"),
                                    whisper.getString("userName"),
                                    whisper.getString("postDate"),
                                    whisper.getString("content"),
                                    whisper.getInt("goodCount"),
                                    whisper.getBoolean("goodFlg"),
                                    whisper.getString("iconPath"),
                                    whisper.getInt("commentCount")
                                )
                            )
                        }

                        if (jsonResponse.has("error")) {
                            activity?.runOnUiThread {
                                Toast.makeText(myApp, jsonResponse.getString("error"), Toast.LENGTH_LONG).show()
                            }
                        } else {
                            activity?.runOnUiThread {
                                Log.d("Whispers", list.toString())
                                binding.userRecycle.layoutManager = LinearLayoutManager(activity)
                                val adapter = WhisperListAdapter(activity!!, list, myApp.loginUserId, this@ProfileFragment)
                                binding.userRecycle.adapter = adapter
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
                } catch (e: JSONException) {
                    activity?.runOnUiThread {
                        Toast.makeText(myApp, "Error parsing the response", Toast.LENGTH_LONG).show()
                        Log.e("JSON Parsing Error", "Error parsing the response", e)
                    }
                }
            }
        })
    }

    private fun followManageApiCall(followUserId: String, followFlg: Boolean) {
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = JSONObject().apply {
            put("userId", myApp.loginUserId)
            put("followUserId", followUserId)
            put("followFlg", followFlg)
        }.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("${myApp.apiUrl}followCtl.php")
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
                Log.d("API follow Response", responseBody ?: "No response body")
                try {
                    val jsonResponse = JSONObject(responseBody)
                    if (jsonResponse.has("error")) {
                        activity?.runOnUiThread {
                            Toast.makeText(activity, jsonResponse.getString("error"), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        activity?.runOnUiThread {
                            getFollowInfoApiCall() // Update follow count
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
}
