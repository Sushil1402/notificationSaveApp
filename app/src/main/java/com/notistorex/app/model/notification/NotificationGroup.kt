package com.notistorex.app.model.notification

import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.Color

data class NotificationGroup(
    val packageName: String,
    val appName: String,
    val appIcon: Drawable?,
    val appColor: Color,
    val notificationCount: Int,
    val recentNotifications: List<NotificationItem>
)