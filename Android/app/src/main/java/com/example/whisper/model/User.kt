package com.example.whisper.model

data class User(
    val userId: String,
    val userName: String,
    val whisperCount: Int,
    val followCount: Int,
    val followerCount: Int,
    val iconPath : String
)