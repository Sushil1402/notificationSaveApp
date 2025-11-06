package com.jetpackComposeTest1.ui.screens.dashboard.viewmodel

import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetpackComposeTest1.db.NotificationDao
import com.jetpackComposeTest1.db.NotificationEntity
import com.jetpackComposeTest1.model.analytics.AnalyticsData
import com.jetpackComposeTest1.model.analytics.AppUsageData
import com.jetpackComposeTest1.model.analytics.DailyAnalyticsData
import com.jetpackComposeTest1.model.analytics.DayData
import com.jetpackComposeTest1.model.analytics.HourData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val notificationDao: NotificationDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()
    
    val canGoToNextDay: StateFlow<Boolean> = _selectedDate.map { selectedDate ->
        val today = getStartOfDay(System.currentTimeMillis())
        val selected = getStartOfDay(selectedDate)
        selected < today
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    private val _analyticsData = MutableStateFlow(
        AnalyticsData(
            totalNotifications = 0,
            activeApps = 0,
            todayCount = 0,
            weekCount = 0,
            monthCount = 0,
            databaseSize = 0f,
            imageCount = 0,
            storagePercentage = 0f,
            topApps = emptyList()
        )
    )
    val analyticsData: StateFlow<AnalyticsData> = _analyticsData.asStateFlow()

    private val _dailyAnalytics = MutableStateFlow<DailyAnalyticsData?>(null)
    val dailyAnalytics: StateFlow<DailyAnalyticsData?> = _dailyAnalytics.asStateFlow()

    private val _weeklyAppBreakdown = MutableStateFlow<List<AppUsageData>>(emptyList())
    val weeklyAppBreakdown: StateFlow<List<AppUsageData>> = _weeklyAppBreakdown.asStateFlow()

    init {
        loadAnalyticsData()
        loadDailyAnalytics()
        loadWeeklyAppBreakdown()
    }

    fun selectDate(dateTimestamp: Long) {
        _selectedDate.value = dateTimestamp
        loadDailyAnalytics()
        loadWeeklyAppBreakdown()
    }

    fun selectPreviousDay() {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = _selectedDate.value
            add(Calendar.DAY_OF_MONTH, -1)
        }
        selectDate(calendar.timeInMillis)
    }

    fun selectNextDay() {
        val today = getStartOfDay(System.currentTimeMillis())
        val selectedDate = getStartOfDay(_selectedDate.value)
        
        // Don't allow navigation to future dates
        if (selectedDate >= today) {
            return
        }
        
        val calendar = Calendar.getInstance().apply {
            timeInMillis = _selectedDate.value
            add(Calendar.DAY_OF_MONTH, 1)
        }
        
        // Double check that the next day is not in the future
        val nextDay = getStartOfDay(calendar.timeInMillis)
        if (nextDay <= today) {
            selectDate(calendar.timeInMillis)
        }
    }

    fun selectToday() {
        selectDate(System.currentTimeMillis())
    }

    private fun loadAnalyticsData() {
        viewModelScope.launch {
            notificationDao.getAllNotifications().collect { notifications ->
                val now = System.currentTimeMillis()
                val today = getStartOfDay(now)
                val week = now - (7 * 24 * 60 * 60 * 1000)
                val month = now - (30 * 24 * 60 * 60 * 1000)

                val todayCount = notifications.count { it.timestamp >= today }
                val weekCount = notifications.count { it.timestamp >= week }
                val monthCount = notifications.count { it.timestamp >= month }

                val appGroups = notifications.groupBy { it.packageName }
                val topApps = appGroups.entries
                    .map { (packageName, appNotifications) ->
                        val appName = getAppName(packageName)
                        AppUsageData(
                            name = appName,
                            packageName = packageName,
                            count = appNotifications.size,
                            percentage = if (notifications.isNotEmpty()) {
                                (appNotifications.size.toFloat() / notifications.size * 100)
                            } else 0f,
                            color = getAppColor(packageName)
                        )
                    }
                    .sortedByDescending { it.count }
                    .take(5)

                _analyticsData.value = AnalyticsData(
                    totalNotifications = notifications.size,
                    activeApps = appGroups.size,
                    todayCount = todayCount,
                    weekCount = weekCount,
                    monthCount = monthCount,
                    databaseSize = 45.6f, // Mock data
                    imageCount = 23, // Mock data
                    storagePercentage = 65.2f, // Mock data
                    topApps = topApps
                )
            }
        }
    }

    private fun loadDailyAnalytics() {
        viewModelScope.launch {
            combine(
                notificationDao.getAllNotifications(),
                _selectedDate
            ) { notifications, selectedDate ->
                calculateDailyAnalytics(notifications, selectedDate)
            }.collect { dailyData ->
                _dailyAnalytics.value = dailyData
            }
        }
    }

    private fun loadWeeklyAppBreakdown() {
        viewModelScope.launch {
            combine(
                notificationDao.getAllNotifications(),
                _selectedDate
            ) { notifications, selectedDate ->
                calculateWeeklyAppBreakdown(notifications, selectedDate)
            }.collect { weeklyBreakdown ->
                _weeklyAppBreakdown.value = weeklyBreakdown
            }
        }
    }

    private fun calculateWeeklyAppBreakdown(
        allNotifications: List<NotificationEntity>,
        selectedDateTimestamp: Long
    ): List<AppUsageData> {
        // Find Sunday of the week containing the selected date
        val selectedCalendar = Calendar.getInstance().apply {
            timeInMillis = selectedDateTimestamp
        }
        val dayOfWeekSelected = selectedCalendar.get(Calendar.DAY_OF_WEEK)
        val daysFromSunday = if (dayOfWeekSelected == Calendar.SUNDAY) 0 else dayOfWeekSelected - Calendar.SUNDAY
        
        // Get the Sunday of the current week (start of week)
        val weekStartCalendar = Calendar.getInstance().apply {
            timeInMillis = selectedDateTimestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_MONTH, -daysFromSunday)
        }
        val weekStart = weekStartCalendar.timeInMillis
        
        // Get the Saturday of the current week (end of week)
        val weekEnd = weekStart + (7 * 24 * 60 * 60 * 1000) - 1
        
        // Get all notifications for the week
        val weekNotifications = allNotifications.filter {
            it.timestamp >= weekStart && it.timestamp <= weekEnd
        }
        
        // Calculate app breakdown for the week
        val appGroups = weekNotifications.groupBy { it.packageName }
        val weekTotalCount = weekNotifications.size
        
        val weeklyBreakdown = appGroups.entries
            .map { (packageName, appNotifications) ->
                val appName = getAppName(packageName)
                AppUsageData(
                    name = appName,
                    packageName = packageName,
                    count = appNotifications.size,
                    percentage = if (weekTotalCount > 0) {
                        (appNotifications.size.toFloat() / weekTotalCount * 100)
                    } else 0f,
                    color = getAppColor(packageName)
                )
            }
            .sortedByDescending { it.count }
            .take(5) // Top 5 apps
        
        return weeklyBreakdown
    }

    private fun calculateDailyAnalytics(
        allNotifications: List<NotificationEntity>,
        selectedDateTimestamp: Long
    ): DailyAnalyticsData {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedDateTimestamp
        }
        
        val startOfDay = getStartOfDay(selectedDateTimestamp)
        val endOfDay = startOfDay + (24 * 60 * 60 * 1000) - 1

        // Get notifications for selected day
        val dayNotifications = allNotifications.filter {
            it.timestamp >= startOfDay && it.timestamp <= endOfDay
        }
        
        // Get notifications for previous day (for comparison)
        val previousDayStart = startOfDay - (24 * 60 * 60 * 1000)
        val previousDayEnd = startOfDay - 1
        val previousDayNotifications = allNotifications.filter {
            it.timestamp >= previousDayStart && it.timestamp <= previousDayEnd
        }
        
        val todayCount = dayNotifications.size
        val yesterdayCount = previousDayNotifications.size
        
        // Calculate percentage change
        val changeFromYesterday = if (yesterdayCount > 0) {
            ((todayCount - yesterdayCount).toFloat() / yesterdayCount) * 100
        } else if (todayCount > 0) {
            100f
        } else {
            0f
        }
        
        // Calculate hourly breakdown
        val hourlyMap = mutableMapOf<Int, Int>()
        dayNotifications.forEach { notification ->
            val hour = Calendar.getInstance().apply {
                timeInMillis = notification.timestamp
            }.get(Calendar.HOUR_OF_DAY)
            hourlyMap[hour] = (hourlyMap[hour] ?: 0) + 1
        }
        
        val hourlyData = (0..23).map { hour ->
            HourData(hour = hour, count = hourlyMap[hour] ?: 0)
        }
        
        // Find most active hour
        val mostActiveHour = hourlyData.maxByOrNull { it.count }?.hour ?: 0
        val mostActiveHourRange = String.format(
            "%02d:00-%02d:00",
            mostActiveHour,
            (mostActiveHour + 1) % 24
        )
        
        // Calculate app breakdown
        val appGroups = dayNotifications.groupBy { it.packageName }
        val appBreakdown = appGroups.entries
            .map { (packageName, appNotifications) ->
                val appName = getAppName(packageName)
                AppUsageData(
                    name = appName,
                    packageName = packageName,
                    count = appNotifications.size,
                    percentage = if (todayCount > 0) {
                        (appNotifications.size.toFloat() / todayCount * 100)
                    } else 0f,
                    color = getAppColor(packageName)
                )
            }
            .sortedByDescending { it.count }
        
        val topApp = appBreakdown.firstOrNull()
        
        // Calculate weekly trend (7 days starting from Sunday of the current week)
        val weeklyTrend = mutableListOf<DayData>()
        val dayLabels = arrayOf("S", "M", "T", "W", "T", "F", "S")
        
        // Find Sunday of the week containing the selected date
        val selectedCalendar = Calendar.getInstance().apply {
            timeInMillis = selectedDateTimestamp
        }
        val dayOfWeekSelected = selectedCalendar.get(Calendar.DAY_OF_WEEK)
        val daysFromSunday = if (dayOfWeekSelected == Calendar.SUNDAY) 0 else dayOfWeekSelected - Calendar.SUNDAY
        
        // Get the Sunday of the current week
        val weekStartCalendar = Calendar.getInstance().apply {
            timeInMillis = selectedDateTimestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_MONTH, -daysFromSunday)
        }
        val weekStartSunday = weekStartCalendar.timeInMillis
        
        // Build weekly trend starting from Sunday through Saturday
        for (i in 0 until 7) {
            val dayStart = weekStartSunday + (i * 24 * 60 * 60 * 1000)
            val dayEnd = dayStart + (24 * 60 * 60 * 1000) - 1
            
            val dayNotifications = allNotifications.filter {
                it.timestamp >= dayStart && it.timestamp <= dayEnd
            }
            
            val cal = Calendar.getInstance().apply {
                timeInMillis = dayStart
            }
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val dayLabel = dayLabels[dayOfWeek - 1]
            
            // Check if this day is the selected date
            val isSelected = (dayStart >= startOfDay && dayStart < startOfDay + (24 * 60 * 60 * 1000))
            
            weeklyTrend.add(
                DayData(
                    dayOfWeek = dayOfWeek,
                    dayLabel = dayLabel,
                    count = dayNotifications.size,
                    isSelected = isSelected
                )
            )
        }
        
        // Calculate average for the week
        val weekAverage = weeklyTrend.map { it.count }.average().toInt()
        
        // Generate insights
        val insights = generateInsights(
            dayNotifications,
            weeklyTrend,
            hourlyData,
            appBreakdown,
            changeFromYesterday,
            weekAverage
        )
        
        return DailyAnalyticsData(
            date = selectedDateTimestamp,
            totalNotifications = todayCount,
            changeFromYesterday = changeFromYesterday,
            mostActiveHour = mostActiveHour,
            mostActiveHourRange = mostActiveHourRange,
            topApp = topApp,
            weeklyTrend = weeklyTrend,
            hourlyData = hourlyData,
            appBreakdown = appBreakdown,
            insights = insights
        )
    }

    private fun generateInsights(
        dayNotifications: List<NotificationEntity>,
        weeklyTrend: List<DayData>,
        hourlyData: List<HourData>,
        appBreakdown: List<AppUsageData>,
        changeFromYesterday: Float,
        weekAverage: Int
    ): List<String> {
        val insights = mutableListOf<String>()
        
        val todayCount = dayNotifications.size
        
        // Compare with yesterday
        if (changeFromYesterday > 0) {
            insights.add("You received ${String.format("%.0f", changeFromYesterday)}% more notifications than yesterday")
        } else if (changeFromYesterday < 0) {
            insights.add("You received ${String.format("%.0f", kotlin.math.abs(changeFromYesterday))}% fewer notifications than yesterday")
        }
        
        // Compare with weekly average
        if (weekAverage > 0) {
            val diffFromAverage = todayCount - weekAverage
            val percentageDiff = (diffFromAverage.toFloat() / weekAverage) * 100
            if (percentageDiff > 20) {
                insights.add("${String.format("%.0f", percentageDiff)}% above your weekly average")
            } else if (percentageDiff < -20) {
                insights.add("${String.format("%.0f", kotlin.math.abs(percentageDiff))}% below your weekly average")
            }
        }
        
        // Top apps
        if (appBreakdown.isNotEmpty()) {
            val topApps = appBreakdown.take(3).joinToString(", ") { it.name }
            insights.add("Most frequent apps: $topApps")
        }
        
        // Quietest period
        val quietestHour = hourlyData.minByOrNull { it.count }?.hour
        if (quietestHour != null && hourlyData[quietestHour].count == 0) {
            val quietEnd = (quietestHour + 1) % 24
            insights.add("Your quietest period: ${String.format("%02d:00-%02d:00", quietestHour, quietEnd)}")
        }
        
        // Peak hour
        val peakHour = hourlyData.maxByOrNull { it.count }
        if (peakHour != null && peakHour.count > 0) {
            val peakEnd = (peakHour.hour + 1) % 24
            insights.add("Peak activity: ${String.format("%02d:00-%02d:00", peakHour.hour, peakEnd)} (${peakHour.count} notifications)")
        }
        
        return insights
    }

    private fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    private fun getAppColor(appName: String): androidx.compose.ui.graphics.Color {
        return when (appName.lowercase()) {
            "com.whatsapp", "whatsapp" -> androidx.compose.ui.graphics.Color(0xFF25D366)
            "com.google.android.gm", "gmail" -> androidx.compose.ui.graphics.Color(0xFFEA4335)
            "com.facebook.katana", "facebook" -> androidx.compose.ui.graphics.Color(0xFF1877F2)
            "com.instagram.android", "instagram" -> androidx.compose.ui.graphics.Color(0xFFE4405F)
            "com.twitter.android", "twitter" -> androidx.compose.ui.graphics.Color(0xFF1DA1F2)
            "com.linkedin.android", "linkedin" -> androidx.compose.ui.graphics.Color(0xFF0077B5)
            else -> androidx.compose.ui.graphics.Color(0xFF00a77a)
        }
    }
    
    private fun getAppName(packageName: String): String {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
            context.packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            // Fallback: use last part of package name, capitalized
            packageName.substringAfterLast(".").replaceFirstChar { 
                it.uppercase() 
            }
        } catch (e: Exception) {
            packageName.substringAfterLast(".").replaceFirstChar { 
                it.uppercase() 
            }
        }
    }
}
