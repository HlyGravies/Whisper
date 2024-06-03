package com.example.whisper.model

data class Good(
    val whisperNo: Int,
    val userId: String,
    val userName: String,
    val postDate: String,
    val content: String,
    var goodCount: Int
)

