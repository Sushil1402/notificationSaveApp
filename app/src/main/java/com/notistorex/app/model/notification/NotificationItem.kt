package com.notistorex.app.model.notification

import android.graphics.drawable.Drawable

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val isRead: Boolean,
    val timeAgo: String,
    val packageName: String = "",
    val appName: String = "",
    val appIcon: Drawable? = null
)