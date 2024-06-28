package com.example.whisper.model

data class Whisper(
    val whisperNo: Int,
    val userId: String,
    val userName: String,
    val postDate: String,
    val content: String,
    var goodCount: Int,
    var goodFlg: Boolean,
    val iconPath : String
)

