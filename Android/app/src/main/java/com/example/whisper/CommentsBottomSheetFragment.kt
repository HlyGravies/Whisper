package com.example.whisper

import android.app.Dialog
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whisper.MyApplication.MyApplication
import com.example.whisper.adapter.CommentsAdapter
import com.example.whisper.databinding.FragmentCommentsBottomSheetBinding
import com.example.whisper.model.Comment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class CommentsBottomSheetFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var myApp: MyApplication

    @Inject
    lateinit var client: OkHttpClient

    @Inject
    lateinit var mediaType: MediaType

    private var _binding: FragmentCommentsBottomSheetBinding? = null
    private val binding get() = _binding!!

    private lateinit var commentsAdapter: CommentsAdapter
    private var whisperNo: Long? = null
    internal val commentsList = mutableListOf<Comment>()

    companion object {
        const val WHISPER_NO = "whisperNo"

        fun newInstance(whisperNo: Long): CommentsBottomSheetFragment {
            val fragment = CommentsBottomSheetFragment()
            val args = Bundle()
            args.putLong(WHISPER_NO, whisperNo)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            val behavior = BottomSheetBehavior.from(bottomSheet!!)
            val layoutParams = bottomSheet.layoutParams
            val windowHeight = Resources.getSystem().displayMetrics.heightPixels
            layoutParams?.height = windowHeight * 70 / 100
            bottomSheet.layoutParams = layoutParams
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isFitToContents = true
            behavior.skipCollapsed = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommentsBottomSheetBinding.inflate(inflater, container, false)

        binding.commentsRecyclerView.layoutManager = LinearLayoutManager(context)
        commentsAdapter = CommentsAdapter(this, requireActivity(), commentsList, client, myApp)
        binding.commentsRecyclerView.adapter = commentsAdapter

        whisperNo = arguments?.getLong(WHISPER_NO)

        loadComments()

        binding.postCommentButton.setOnClickListener {
            postComment()
        }
        binding.closeButton.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

    internal fun loadComments() {
        val requestBody = JSONObject().apply {
            put("whisperNo", whisperNo)
        }.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url(myApp.apiUrl + "commentInfo.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("comm", "loadComments: $responseBody")
                try {
                    val jsonResponse = JSONObject(responseBody)
                    val comments = jsonResponse.getJSONArray("comments")
                    for (i in 0 until comments.length()) {
                        val comment = comments.getJSONObject(i)
                        commentsList.add(
                            Comment(
                                comment.getLong("commentId"),
                                comment.getLong("whisperNo"),
                                comment.getString("userId"),
                                comment.getString("userName"),
                                comment.getString("iconPath"),
                                comment.getString("content"),
                                comment.getString("commentDate"),
                                comment.getInt("likeCount")
                            )
                        )
                    }
                    activity?.runOnUiThread {
                        commentsAdapter.notifyDataSetChanged()
                    }
                } catch (e: JSONException) {
                    activity?.runOnUiThread {
                        Toast.makeText(context, "Error parsing the response", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun postComment() {
        val commentText = binding.commentEditText.text.toString()
        if (commentText.isBlank()) return

        val requestBody = JSONObject().apply {
            put("userId", myApp.loginUserId) // Replace with actual user ID
            put("whisperNo", whisperNo)
            put("content", commentText)
        }.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url(myApp.apiUrl + "addComment.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                activity?.runOnUiThread {
                    commentsList.clear() // Clear the current list of comments
                    binding.commentEditText.text.clear() // Clear the comment text field
                    loadComments() // Load the comments again
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
