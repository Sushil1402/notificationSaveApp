package com.jetpackComposeTest1.model.notification

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class NotificationGroupData(
    val id: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val type: String, // "Unread", "Read", "Muted", "Custom"
    val isMuted: Boolean,
    val totalNotifications: Int,
    val unreadNotifications: Int,
    val todayNotifications: Int,
    val appCount: Int
)

