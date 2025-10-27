package com.jetpackComposeTest1.model.notification

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val isRead: Boolean,
    val timeAgo: String
)