package com.jetpackComposeTest1.ui.screens.dashboard.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetpackComposeTest1.data.repository.database.NotificationDBRepository
import com.jetpackComposeTest1.model.notification.NotificationItem
import com.jetpackComposeTest1.ui.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllUnreadNotificationsViewModel @Inject constructor(
    private val notificationDBRepo: NotificationDBRepository
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Search functionality
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    private val _filteredNotifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val filteredNotifications: StateFlow<List<NotificationItem>> = _filteredNotifications.asStateFlow()

    // Original notifications for filtering
    private val _allNotifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    
    // Keep timestamp to support date grouping without changing NotificationItem
    private val _allWithTime = MutableStateFlow<List<Pair<Long, NotificationItem>>>(emptyList())

    // Grouped notifications for sticky date headers
    data class DateGroup(
        val dateStartMillis: Long,
        val dateLabel: String,
        val items: List<NotificationItem>
    )

    private val _groupedNotifications = MutableStateFlow<List<DateGroup>>(emptyList())
    val groupedNotifications: StateFlow<List<DateGroup>> = _groupedNotifications.asStateFlow()

    init {
        // Combine search query, raw list to produce filtered list and groups
        viewModelScope.launch {
            combine(_searchQuery, _allWithTime) { query, allWithTime ->
                // Map to items for search
                val items = allWithTime.map { it.second }
                val afterSearch = if (query.isBlank()) items else filterNotifications(items, query)

                // Update flat lists
                _filteredNotifications.value = afterSearch
                _notifications.value = afterSearch

                // Build groups by day from allWithTime but only retaining items in afterSearch
                val allowedIds = afterSearch.map { it.id }.toSet()
                val grouped = allWithTime
                    .filter { it.second.id in allowedIds }
                    .groupBy { dayStart(it.first) }
                    .toSortedMap(compareByDescending { it }) // newest day first
                    .map { (dayStart, pairs) ->
                        val itemsForDay = pairs
                            .sortedByDescending { it.first }
                            .map { it.second }
                        DateGroup(
                            dateStartMillis = dayStart,
                            dateLabel = formatDateHeader(dayStart),
                            items = itemsForDay
                        )
                    }

                grouped
            }.collect { groups ->
                _groupedNotifications.value = groups
            }
        }
    }

    fun loadAllUnreadNotifications(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true

            notificationDBRepo.getNotificationsByReadStatus(isRead = false).collect { entities ->
                val sorted = entities.sortedByDescending { it.timestamp }
                val withTime = sorted.map { entity ->
                    // Get app name and icon
                    val appName = try {
                        val appInfo = context.packageManager.getApplicationInfo(entity.packageName, 0)
                        context.packageManager.getApplicationLabel(appInfo).toString()
                    } catch (e: Exception) {
                        entity.packageName
                    }
                    
                    val appIcon = try {
                        Utils.getAppIcon(context, entity.packageName)
                    } catch (e: Exception) {
                        null
                    }

                    val item = NotificationItem(
                        id = entity.id.toString(),
                        title = entity.title,
                        message = entity.text,
                        isRead = entity.isRead,
                        timeAgo = formatTimeAgo(entity.timestamp),
                        packageName = entity.packageName,
                        appName = appName,
                        appIcon = appIcon
                    )
                    entity.timestamp to item
                }
                val notificationItems = withTime.map { it.second }

                _allNotifications.value = notificationItems
                _allWithTime.value = withTime
                _isLoading.value = false
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                val id = notificationId.toLong()
                notificationDBRepo.markAsRead(id)

                // Update local state
                _allNotifications.value = _allNotifications.value.map { notification ->
                    if (notification.id == notificationId) {
                        notification.copy(isRead = true)
                    } else {
                        notification
                    }
                }
                _allWithTime.value = _allWithTime.value.map { (ts, notification) ->
                    if (notification.id == notificationId) ts to notification.copy(isRead = true) else ts to notification
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            val unreadNotifications = _allNotifications.value.filter { !it.isRead }
            unreadNotifications.forEach { notification ->
                try {
                    val id = notification.id.toLong()
                    notificationDBRepo.markAsRead(id)
                } catch (e: Exception) {
                    // Handle error
                }
            }

            // Update local state
            _allNotifications.value = _allNotifications.value.map { notification ->
                notification.copy(isRead = true)
            }
            _allWithTime.value = _allWithTime.value.map { (ts, notification) -> ts to notification.copy(isRead = true) }
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                val id = notificationId.toLong()
                notificationDBRepo.deleteNotification(id)

                // Update local state
                _allNotifications.value = _allNotifications.value.filter { it.id != notificationId }
                _allWithTime.value = _allWithTime.value.filter { it.second.id != notificationId }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun formatTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000}m ago"
            diff < 86400000 -> "${diff / 3600000}h ago"
            diff < 604800000 -> "${diff / 86400000}d ago"
            else -> "${diff / 604800000}w ago"
        }
    }

    // Search functionality methods
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSearch() {
        _isSearchActive.value = !_isSearchActive.value
        if (!_isSearchActive.value) {
            _searchQuery.value = ""
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _isSearchActive.value = false
    }

    private fun filterNotifications(notifications: List<NotificationItem>, query: String): List<NotificationItem> {
        val lowercaseQuery = query.lowercase().trim()
        if (lowercaseQuery.isEmpty()) return notifications

        return notifications.filter { notification ->
            notification.title.lowercase().contains(lowercaseQuery) ||
            notification.message.lowercase().contains(lowercaseQuery) ||
            notification.appName.lowercase().contains(lowercaseQuery)
        }
    }

    // Utilities for date grouping
    private fun dayStart(timeMillis: Long): Long {
        val cal = java.util.Calendar.getInstance().apply {
            timeInMillis = timeMillis
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    private fun formatDateHeader(dayStartMillis: Long): String {
        val sdf = java.text.SimpleDateFormat("EEEE, d MMMM", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(dayStartMillis))
    }
}
