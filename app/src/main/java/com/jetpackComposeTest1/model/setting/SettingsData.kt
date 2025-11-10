package com.jetpackComposeTest1.model.setting

data class SettingsData(
    val hasNotificationAccess: Boolean,
    val autoCleanup: Boolean,
    val retentionDays: Int,
    val storageUsed: Float,
    val storagePercentage: Float,
    val themeMode: ThemeMode,
    val notificationSound: Boolean
)