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

    init {
        // Combine search query with all notifications to filter
        viewModelScope.launch {
            combine(_searchQuery, _allNotifications) { query, notifications ->
                if (query.isBlank()) {
                    notifications
                } else {
                    filterNotifications(notifications, query)
                }
            }.collect { filtered ->
                _filteredNotifications.value = filtered
                _notifications.value = filtered
            }
        }
    }

    fun loadAllUnreadNotifications(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true

            notificationDBRepo.getNotificationsByReadStatus(isRead = false).collect { entities ->
                val notificationItems = entities
                    .sortedByDescending { it.timestamp }
                    .map { entity ->
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

                        NotificationItem(
                            id = entity.id.toString(),
                            title = entity.title,
                            message = entity.text,
                            isRead = entity.isRead,
                            timeAgo = formatTimeAgo(entity.timestamp),
                            packageName = entity.packageName,
                            appName = appName,
                            appIcon = appIcon
                        )
                    }

                _allNotifications.value = notificationItems
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
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                val id = notificationId.toLong()
                notificationDBRepo.deleteNotification(id)

                // Update local state
                _allNotifications.value = _allNotifications.value.filter { it.id != notificationId }
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
}
