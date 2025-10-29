package com.jetpackComposeTest1.model.analytics

import androidx.compose.ui.graphics.Color
import java.util.Calendar

data class DailyAnalyticsData(
    val date: Long, // Selected date timestamp
    val totalNotifications: Int,
    val changeFromYesterday: Float, // Percentage change
    val mostActiveHour: Int, // Hour (0-23) with most notifications
    val mostActiveHourRange: String, // e.g., "18:00-19:00"
    val topApp: AppUsageData?,
    val weeklyTrend: List<DayData>, // 7 days of data
    val hourlyData: List<HourData>, // 24 hours of data
    val appBreakdown: List<AppUsageData>, // All apps for the day
    val insights: List<String>
)

data class DayData(
    val dayOfWeek: Int, // Calendar.SUNDAY = 1, Calendar.MONDAY = 2, etc.
    val dayLabel: String, // "S", "M", "T", "W", "T", "F", "S"
    val count: Int,
    val isSelected: Boolean = false
)

data class HourData(
    val hour: Int, // 0-23
    val count: Int
)

