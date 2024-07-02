package com.example.whisper

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whisper.MyApplication.MyApplication
import com.example.whisper.adapter.CommentsAdapter
import com.example.whisper.model.Comment
import com.example.whisper.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class CommentsBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var myApp: MyApplication
    private lateinit var commentsRecyclerView: RecyclerView
    private lateinit var commentEditText: EditText
    private lateinit var postCommentButton: Button
    private lateinit var commentsAdapter: CommentsAdapter
    private lateinit var closebtn: ImageView
    private var whisperNo: Long? = null
    internal val commentsList = mutableListOf<Comment>()

    companion object {
        const val WHISPER_NO = ""

        fun newInstance(whisperNo: Long): CommentsBottomSheetFragment {
            val fragment = CommentsBottomSheetFragment()
            val args = Bundle()
            args.putLong(WHISPER_NO, whisperNo)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            val behavior = BottomSheetBehavior.from(bottomSheet!!)
            val layoutParams = bottomSheet.layoutParams
            val windowHeight = Resources.getSystem().displayMetrics.heightPixels
            layoutParams?.height = windowHeight * 90 / 100
            bottomSheet.layoutParams = layoutParams
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myApp = activity?.application as MyApplication
        val view = inflater.inflate(R.layout.fragment_comments_bottom_sheet, container, false)
        commentsRecyclerView = view.findViewById(R.id.commentsRecyclerView)
        commentEditText = view.findViewById(R.id.commentEditText)
        postCommentButton = view.findViewById(R.id.postCommentButton)
        closebtn = view.findViewById(R.id.close_button)

        commentsRecyclerView.layoutManager = LinearLayoutManager(context)
        commentsAdapter = CommentsAdapter(this ,requireActivity(),commentsList)
        commentsRecyclerView.adapter = commentsAdapter

        whisperNo = arguments?.getLong(WHISPER_NO)

        loadComments()

        postCommentButton.setOnClickListener {
            postComment()
        }
        closebtn.setOnClickListener {
            dismiss()
        }


        return view
    }

    internal fun loadComments() {
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()
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
                        Toast.makeText(context, "LoadComment Error parsing the response", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun postComment() {
        val commentText = commentEditText.text.toString()
        if (commentText.isBlank()) return

        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()
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
                    loadComments() // Load the comments again
                }
            }
        })
    }
}
