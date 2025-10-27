package com.jetpackComposeTest1.ui.screens.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetpackComposeTest1.db.NotificationDao
import com.jetpackComposeTest1.model.analytics.AnalyticsData
import com.jetpackComposeTest1.model.analytics.AppUsageData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val notificationDao: NotificationDao
) : ViewModel() {

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

    init {
        loadAnalyticsData()
    }

    private fun loadAnalyticsData() {
        viewModelScope.launch {
            notificationDao.getAllNotifications().collect { notifications ->
                val now = System.currentTimeMillis()
                val today = now - (24 * 60 * 60 * 1000)
                val week = now - (7 * 24 * 60 * 60 * 1000)
                val month = now - (30 * 24 * 60 * 60 * 1000)

                val todayCount = notifications.count { it.timestamp >= today }
                val weekCount = notifications.count { it.timestamp >= week }
                val monthCount = notifications.count { it.timestamp >= month }

                val appGroups = notifications.groupBy { it.packageName }
                val topApps = appGroups.entries
                    .map { (appName, appNotifications) ->
                        AppUsageData(
                            name = appName,
                            count = appNotifications.size,
                            percentage = (appNotifications.size.toFloat() / notifications.size * 100),
                            color = getAppColor(appName)
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

    private fun getAppColor(appName: String): androidx.compose.ui.graphics.Color {
        return when (appName.lowercase()) {
            "whatsapp" -> androidx.compose.ui.graphics.Color(0xFF25D366)
            "gmail" -> androidx.compose.ui.graphics.Color(0xFFEA4335)
            "facebook" -> androidx.compose.ui.graphics.Color(0xFF1877F2)
            "instagram" -> androidx.compose.ui.graphics.Color(0xFFE4405F)
            "twitter" -> androidx.compose.ui.graphics.Color(0xFF1DA1F2)
            else -> androidx.compose.ui.graphics.Color(0xFF00a77a)
        }
    }
}



