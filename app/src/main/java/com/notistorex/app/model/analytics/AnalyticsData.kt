package com.notistorex.app.model.analytics

data class AnalyticsData(
    val totalNotifications: Int,
    val activeApps: Int,
    val todayCount: Int,
    val weekCount: Int,
    val monthCount: Int,
    val databaseSize: Float,
    val imageCount: Int,
    val storagePercentage: Float,
    val topApps: List<AppUsageData>
)
