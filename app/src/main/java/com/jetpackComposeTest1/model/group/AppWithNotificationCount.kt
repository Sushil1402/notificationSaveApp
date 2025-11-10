package com.jetpackComposeTest1.model.group

data class AppWithNotificationCount(
    val packageName: String,
    val appName: String,
    val appIcon: android.graphics.drawable.Drawable,
    val notificationCount: Int
)