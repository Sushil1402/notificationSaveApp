package com.jetpackComposeTest1.ui.screens.dashboard.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetpackComposeTest1.data.repository.database.NotificationDBRepository
import com.jetpackComposeTest1.data.repository.preferences.SharedPreferencesRepository
import com.jetpackComposeTest1.model.notification.NotificationGroup
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
class NotificationHomeViewModel @Inject constructor(
    private val notificationDBRepo: NotificationDBRepository,
    private val sharedPreferences: SharedPreferencesRepository
) : ViewModel() {

    private val _groupedNotifications = MutableStateFlow<List<NotificationGroup>>(emptyList())
    val groupedNotifications: StateFlow<List<NotificationGroup>> = _groupedNotifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    // Search functionality
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    private val _filteredNotifications = MutableStateFlow<List<NotificationGroup>>(emptyList())
    val filteredNotifications: StateFlow<List<NotificationGroup>> = _filteredNotifications.asStateFlow()

    // Original notifications for filtering
    private val _allGroupedNotifications = MutableStateFlow<List<NotificationGroup>>(emptyList())

    init {
        // Combine search query with all notifications to filter
        viewModelScope.launch {
            combine(_searchQuery, _allGroupedNotifications) { query, notifications ->
                if (query.isBlank()) {
                    notifications
                } else {
                    filterNotifications(notifications, query)
                }
            }.collect { filtered ->
                _filteredNotifications.value = filtered
                _groupedNotifications.value = filtered
            }
        }
    }

    fun loadGroupedNotifications(context: Context) {
        viewModelScope.launch {
            notificationDBRepo.getNotificationsByReadStatus(isRead = false).collect { allNotifications ->
                // Filter to show only unread notifications in the list
                val unreadNotifications = allNotifications.filter { !it.isRead }
                
                val grouped = unreadNotifications
                    .groupBy { it.packageName }
                    .map { (packageName, appNotifications) ->
                        // Get the actual app name from package manager
                        val appName = try {
                            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
                            context.packageManager.getApplicationLabel(appInfo).toString()
                        } catch (e: Exception) {
                            packageName // Fallback to package name if app name can't be retrieved
                        }
                        
                        NotificationGroup(
                            packageName = packageName,
                            appName = appName, // This is now the actual app name
                            appIcon = Utils.getAppIcon(context, packageName),
                            appColor = getAppColor(appName),
                            notificationCount = appNotifications.size,
                            recentNotifications = appNotifications
                                .sortedByDescending { it.timestamp }
                                .take(5)
                                .map { notification ->
                                    NotificationItem(
                                        id = notification.id.toString(),
                                        title = notification.title,
                                        message = notification.text,
                                        isRead = notification.isRead,
                                        timeAgo = formatTimeAgo(notification.timestamp)
                                    )
                                }
                        )
                    }
                    .sortedByDescending { it.notificationCount }

                _allGroupedNotifications.value = grouped
                _unreadCount.value = unreadNotifications.size
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            // Update notification as read
            // You'll need to implement this in your DAO
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            // Delete notification
            // You'll need to implement this in your DAO
        }
    }

    fun refreshNotifications(context: Context) {
        loadGroupedNotifications(context)
    }

    fun shareNotification(notification: NotificationItem) {
        // Implement sharing functionality
    }


    private fun getAppColor(appName: String): androidx.compose.ui.graphics.Color {
        // Return appropriate color based on app name
        return androidx.compose.ui.graphics.Color(0xFF00a77a)
    }

    private fun formatTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000}m ago"
            diff < 86400000 -> "${diff / 3600000}h ago"
            else -> "${diff / 86400000}d ago"
        }
    }

    fun isAppSelectionCompleted():Boolean = sharedPreferences.isAppSelectionCompleted()

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

    private fun filterNotifications(notifications: List<NotificationGroup>, query: String): List<NotificationGroup> {
        val lowercaseQuery = query.lowercase().trim()
        if (lowercaseQuery.isEmpty()) return notifications

        return notifications.filter { group ->
            // Filter by app name
            val appNameMatches = group.appName.lowercase().contains(lowercaseQuery)
            
            // Filter by notification content
            val notificationMatches = group.recentNotifications.any { notification ->
                notification.title.lowercase().contains(lowercaseQuery) ||
                notification.message.lowercase().contains(lowercaseQuery)
            }
            
            appNameMatches || notificationMatches
        }.map { group ->
            // If app name doesn't match but notifications do, filter the notifications within the group
            if (group.appName.lowercase().contains(lowercaseQuery)) {
                group
            } else {
                val filteredNotifications = group.recentNotifications.filter { notification ->
                    notification.title.lowercase().contains(lowercaseQuery) ||
                    notification.message.lowercase().contains(lowercaseQuery)
                }
                group.copy(
                    recentNotifications = filteredNotifications,
                    notificationCount = filteredNotifications.size
                )
            }
        }.filter { it.notificationCount > 0 }
    }
}

