package com.example.whisper.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.*
import okhttp3.MediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment(), OnDataRefreshNeededListener {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var myApp: MyApplication

    @Inject
    lateinit var okHttpClient: OkHttpClient

    @Inject
    lateinit var jsonMediaType: MediaType

    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        userId = myApp.userId
        Log.d("TAGcheckus", "onCreateView: $userId")

        initViews()
        initListeners()
        fetchData()

        return binding.root
    }

    private fun initViews() {
        binding.userRecycle.layoutManager = LinearLayoutManager(activity)
        binding.userRecycle.adapter = WhisperListAdapter(requireActivity(), mutableListOf(), myApp.loginUserId, this@ProfileFragment)
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Whisper"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Good Info"))
    }

    private fun initListeners() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                (binding.userRecycle.adapter as WhisperListAdapter).clearData()
                binding.errorText.text = ""
                when (tab.position) {
                    0 -> getUserWhispersApiCall()
                    1 -> getUserGoodWhispersApiCall()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        binding.followText.setOnClickListener { navigateToFollowListActivity(true) }
        binding.followerText.setOnClickListener { navigateToFollowListActivity(false) }
        binding.editInfoBtn.setOnClickListener { startActivity(Intent(activity, UserEditActivity::class.java)) }
        binding.settingBtn.setOnClickListener { showSignOutDialog() }
    }

    private fun fetchData() {
        getUserInfoApiCall()
        getFollowInfoApiCall()
        getUserWhispersApiCall()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDataRefreshNeeded() {
        getUserInfoApiCall()
    }

    private fun navigateToFollowListActivity(isFollow: Boolean) {
        val intent = Intent(activity, FollowListActivity::class.java).apply {
            putExtra("userId", userId)
            putExtra("isFollow", isFollow)
        }
        startActivity(intent)
    }

    private fun showSignOutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Sign Out")
            .setMessage("Do you want to sign out of your account?")
            .setPositiveButton("Sign out") { _, _ ->
                myApp.loginUserId = "lo"
                startActivity(Intent(activity, LoginActivity::class.java))
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun getUserGoodWhispersApiCall() {
        val requestBody = JSONObject().apply {
            put("userId", userId)
            put("loginUserId", myApp.loginUserId)
        }.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("${myApp.apiUrl}userWhisperInfo.php")
            .post(requestBody)
            .build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                showErrorText("Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                handleApiResponse(response, "No liked whispers!") { jsonResponse ->
                    val whispers = jsonResponse.getJSONObject("data").getJSONArray("allLikedWhisperList")
                    val list = List(whispers.length()) { index ->
                        whispers.getJSONObject(index).toWhisper()
                    }
                    updateRecyclerView(list)
                }
            }
        })
    }

    private fun getUserInfoApiCall() {
        val requestBody = JSONObject().apply {
            put("userId", userId)
            put("loginUserId", myApp.loginUserId)
        }.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("${myApp.apiUrl}userInfo.php")
            .post(requestBody)
            .build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                showErrorText("Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                try {
                    val jsonResponse = JSONObject(responseBody)
                    if (jsonResponse.has("error")) {
                        showErrorText(jsonResponse.getString("error"))
                    } else {
                        val userData = jsonResponse.getJSONObject("userData")
                        updateUserInfo(userData)
                    }
                } catch (e: JSONException) {
                    showErrorText("UserInfo Error parsing the response")
                }
            }
        })
    }

    private fun getFollowInfoApiCall() {
        val requestBody = JSONObject().apply {
            put("userId", myApp.loginUserId)
        }.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("${myApp.apiUrl}followerInfo.php")
            .post(requestBody)
            .build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                showErrorText("Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                try {
                    val jsonResponse = JSONObject(responseBody)
                    if (jsonResponse.has("error")) {
                        showErrorText(jsonResponse.getString("error"))
                    } else {
                        val data = jsonResponse.getJSONObject("data")
                        updateFollowCounts(data.getJSONArray("followList").length(), data.getJSONArray("followerList").length())
                    }
                } catch (e: JSONException) {
                    showErrorText("Follow Error parsing the response")
                }
            }
        })
    }

    private fun getUserWhispersApiCall() {
        val requestBody = JSONObject().apply {
            put("userId", userId)
            put("loginUserId", myApp.loginUserId)
        }.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("${myApp.apiUrl}userWhisperInfo.php")
            .post(requestBody)
            .build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                showErrorText("Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                handleApiResponse(response, "No whispers yet!") { jsonResponse ->
                    val whispers = jsonResponse.getJSONObject("data").getJSONArray("whisperList")
                    val list = List(whispers.length()) { index ->
                        whispers.getJSONObject(index).toWhisper()
                    }
                    updateRecyclerView(list)
                }
            }
        })
    }

    private fun handleApiResponse(response: Response, emptyMessage: String, onSuccess: (JSONObject) -> Unit) {
        val responseBody = response.body?.string()
        Log.d("API Response", responseBody ?: "No response body")

        try {
            val jsonResponse = JSONObject(responseBody)
            if (jsonResponse.getJSONObject("data").isNull("whisperList")) {
                showErrorText(emptyMessage)
            } else {
                onSuccess(jsonResponse)
            }
        } catch (e: JSONException) {
            showErrorText("Error parsing the response")
            Log.e("JSON Parsing Error", "Error parsing the response", e)
        }
    }

    private fun updateRecyclerView(list: List<Whisper>) {
        activity?.runOnUiThread {
            binding.userRecycle.layoutManager = LinearLayoutManager(activity)
            val adapter = WhisperListAdapter(requireActivity(), list.toMutableList(), myApp.loginUserId, this@ProfileFragment)
            binding.userRecycle.adapter = adapter
            adapter.notifyDataSetChanged()
        }
    }

    private fun updateUserInfo(userData: JSONObject) {
        activity?.runOnUiThread {
            binding.userNameText.text = userData.getString("userName")
            val profile = userData.optString("profile", "")
            binding.profileText.text = if (profile == "null") "" else profile
            val iconPath = userData.getString("iconPath")
            if (iconPath.isNotEmpty()) {
                myApp.iconPath = myApp.apiUrl + iconPath
                Glide.with(this@ProfileFragment)
                    .load(myApp.iconPath)
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // Disable cache
                    .skipMemoryCache(true) // Skip memory cache
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.avatar)
                    .into(binding.userImage)
            }
        }
    }

    private fun updateFollowCounts(followCount: Int, followerCount: Int) {
        activity?.runOnUiThread {
            binding.followCntText.text = followCount.toString()
            binding.followerCntText.text = followerCount.toString()
        }
    }

    private fun showErrorText(message: String) {
        activity?.runOnUiThread {
            binding.errorText.text = message
        }
    }

    private fun JSONObject.toWhisper(): Whisper {
        return Whisper(
            getInt("whisperNo"),
            getString("userId"),
            getString("userName"),
            getString("postDate"),
            getString("content"),
            getInt("goodCount"),
            getBoolean("goodFlg"),
            getString("iconPath"),
            getInt("commentCount")
        )
    }
}
