package com.example.whisper.model

data class Whisper(
    val whisperNo: Int,
    val userId: String,
    val userName: String,
    val postDate: String,
    val content: String,
    val goodFlg: Boolean
)

