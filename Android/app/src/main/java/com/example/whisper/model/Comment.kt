package com.example.whisper.model

data class Comment(
    val commentId: Long,
    val whisperNo: Long,
    val userId: String,
    val userName: String,
    val iconPath: String,
    val content: String,
    val commentDate: String,
    val likeCount: Int
)
