package com.example.whisper.model

data class Notification(
    val type: String,
    val whisperNo: Int,
    val userName: String,
    val userIcon: String,
    val content: String,
    val imagePath: String?,
    val userId: String,
    var isRead: Boolean
)
