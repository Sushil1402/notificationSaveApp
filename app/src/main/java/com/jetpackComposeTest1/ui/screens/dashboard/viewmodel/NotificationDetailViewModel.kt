package com.jetpackComposeTest1.ui.screens.dashboard.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetpackComposeTest1.data.repository.database.NotificationDBRepository
import com.jetpackComposeTest1.model.notification.NotificationItem
import com.jetpackComposeTest1.ui.utils.Utils
import com.jetpackComposeTest1.utils.NotificationExportManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationDetailViewModel @Inject constructor(
    private val notificationDBRepo: NotificationDBRepository
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _appName = MutableStateFlow("")
    val appName: StateFlow<String> = _appName.asStateFlow()

    private val _appIcon = MutableStateFlow<android.graphics.drawable.Drawable?>(null)
    val appIcon: StateFlow<android.graphics.drawable.Drawable?> = _appIcon.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

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

    // Keep timestamp to support date filtering/grouping without changing NotificationItem
    private val _allWithTime = MutableStateFlow<List<Pair<Long, NotificationItem>>>(emptyList())

    // Date filter (startOfDay millis); when null -> show all dates
    private val _selectedDateFilter = MutableStateFlow<Long?>(null)
    val selectedDateFilter: StateFlow<Long?> = _selectedDateFilter.asStateFlow()

    // Grouped notifications for sticky date headers
    data class DateGroup(
        val dateStartMillis: Long,
        val dateLabel: String,
        val items: List<NotificationItem>
    )

    private val _groupedNotifications = MutableStateFlow<List<DateGroup>>(emptyList())
    val groupedNotifications: StateFlow<List<DateGroup>> = _groupedNotifications.asStateFlow()

    init {
        // Combine search query, raw list, and date filter to produce filtered list and groups
        viewModelScope.launch {
            combine(_searchQuery, _allWithTime, _selectedDateFilter) { query, allWithTime, dateFilter ->
                // Apply date filter first
                val filteredByDate = if (dateFilter == null) {
                    allWithTime
                } else {
                    val dayRange = dayStartEnd(dateFilter)
                    allWithTime.filter { (ts, _) -> ts in dayRange.first..dayRange.second }
                }

                // Map to items for search
                val items = filteredByDate.map { it.second }
                val afterSearch = if (query.isBlank()) items else filterNotifications(items, query)

                // Update flat lists
                _filteredNotifications.value = afterSearch
                _notifications.value = afterSearch

                // Build groups by day from filteredByDate but only retaining items in afterSearch
                val allowedIds = afterSearch.map { it.id }.toSet()
                val grouped = filteredByDate
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

    fun loadNotificationsForApp(context: Context, packageName: String, appName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _appName.value = appName
            
            // Get app icon
            _appIcon.value = Utils.getAppIcon(context, packageName)
            
            // Load all notifications for this app (both read and unread)
            notificationDBRepo.getNotificationsByPackageName(packageName).collect { notificationEntities ->
                val sorted = notificationEntities.sortedByDescending { it.timestamp }
                val withTime = sorted.map { entity ->
                    val item = NotificationItem(
                        id = entity.id.toString(),
                        title = entity.title,
                        message = entity.text,
                        isRead = entity.isRead,
                        timeAgo = formatTimeAgo(entity.timestamp)
                    )
                    entity.timestamp to item
                }
                val notificationItems = withTime.map { it.second }
                
                _allNotifications.value = notificationItems
                _allWithTime.value = withTime
                _unreadCount.value = notificationItems.count { !it.isRead }
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
                
                // Update unread count
                _unreadCount.value = _allNotifications.value.count { !it.isRead }
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
            
            _unreadCount.value = 0
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
                _unreadCount.value = _allNotifications.value.count { !it.isRead }
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
            notification.message.lowercase().contains(lowercaseQuery)
        }
    }

    // Utilities for date grouping/filtering
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

    private fun dayStartEnd(dayStartMillis: Long): Pair<Long, Long> {
        val start = dayStart(dayStartMillis)
        val end = start + 24L * 60L * 60L * 1000L - 1L
        return start to end
    }

    private fun formatDateHeader(dayStartMillis: Long): String {
        val sdf = java.text.SimpleDateFormat("EEEE, d MMMM", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(dayStartMillis))
    }

    fun setDateFilter(dateMillis: Long?) {
        _selectedDateFilter.value = dateMillis?.let { dayStart(it) }
    }

    // Export state for current app notifications
    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    private var currentPackageName: String = ""

    fun exportAppNotifications(context: Context, packageName: String, appName: String) {
        viewModelScope.launch {
            _exportState.value = ExportState.Exporting
            currentPackageName = packageName
            try {
                // Get all notifications for this app from database
                val appNotifications = notificationDBRepo.getNotificationsByPackageName(packageName).first()
                
                if (appNotifications.isEmpty()) {
                    _exportState.value = ExportState.Error("No notifications to export for this app")
                    return@launch
                }
                
                // Create export manager and export to Excel with custom file name prefix (app name)
                val exportManager = NotificationExportManager(context)
                val result = exportManager.exportToExcel(appNotifications, customFileNamePrefix = appName)
                
                result.fold(
                    onSuccess = { uri ->
                        _exportState.value = ExportState.Success(uri)
                    },
                    onFailure = { exception ->
                        _exportState.value = ExportState.Error(exception.message ?: "Export failed")
                    }
                )
            } catch (e: Exception) {
                _exportState.value = ExportState.Error(e.message ?: "Export failed")
            }
        }
    }

    fun resetExportState() {
        _exportState.value = ExportState.Idle
    }

    sealed class ExportState {
        object Idle : ExportState()
        object Exporting : ExportState()
        data class Success(val fileUri: Uri) : ExportState()
        data class Error(val message: String) : ExportState()
    }
}
