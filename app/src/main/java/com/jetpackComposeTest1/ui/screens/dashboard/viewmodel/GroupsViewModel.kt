package com.jetpackComposeTest1.ui.screens.dashboard.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetpackComposeTest1.db.NotificationDao
import com.jetpackComposeTest1.model.notification.NotificationGroupData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val notificationDao: NotificationDao
) : ViewModel() {

    private val _notificationGroups = MutableStateFlow<List<NotificationGroupData>>(emptyList())
    val notificationGroups: StateFlow<List<NotificationGroupData>> = _notificationGroups.asStateFlow()

    init {
        loadNotificationGroups()
    }

    private fun loadNotificationGroups() {
        viewModelScope.launch {
            // Create default groups
            val groups = listOf(
                NotificationGroupData(
                    id = "unread",
                    name = "Unread Notifications",
                    description = "Notifications you haven't read yet",
                    icon = Icons.Default.Settings,
                    color = Color(0xFFFF6B6B),
                    type = "Unread",
                    isMuted = false,
                    totalNotifications = 23,
                    unreadNotifications = 23,
                    todayNotifications = 5,
                    appCount = 8
                ),
                NotificationGroupData(
                    id = "read",
                    name = "Read Notifications",
                    description = "Notifications you've already read",
                    icon = Icons.Default.Settings,
                    color = Color(0xFF4CAF50),
                    type = "Read",
                    isMuted = false,
                    totalNotifications = 156,
                    unreadNotifications = 0,
                    todayNotifications = 12,
                    appCount = 15
                ),
                NotificationGroupData(
                    id = "muted",
                    name = "Muted Notifications",
                    description = "Notifications from muted apps",
                    icon = Icons.Default.Settings,
                    color = Color(0xFF9E9E9E),
                    type = "Muted",
                    isMuted = true,
                    totalNotifications = 45,
                    unreadNotifications = 0,
                    todayNotifications = 3,
                    appCount = 5
                ),
                NotificationGroupData(
                    id = "custom1",
                    name = "Work Notifications",
                    description = "Notifications from work-related apps",
                    icon = Icons.Default.Settings,
                    color = Color(0xFF2196F3),
                    type = "Custom",
                    isMuted = false,
                    totalNotifications = 78,
                    unreadNotifications = 12,
                    todayNotifications = 8,
                    appCount = 6
                ),
                NotificationGroupData(
                    id = "custom2",
                    name = "Social Media",
                    description = "Notifications from social media apps",
                    icon = Icons.Default.Settings,
                    color = Color(0xFFE91E63),
                    type = "Custom",
                    isMuted = false,
                    totalNotifications = 92,
                    unreadNotifications = 8,
                    todayNotifications = 15,
                    appCount = 4
                )
            )
            
            _notificationGroups.value = groups
        }
    }

    fun toggleGroupMute(groupId: String) {
        viewModelScope.launch {
            val groups = _notificationGroups.value.toMutableList()
            val groupIndex = groups.indexOfFirst { it.id == groupId }
            if (groupIndex != -1) {
                groups[groupIndex] = groups[groupIndex].copy(isMuted = !groups[groupIndex].isMuted)
                _notificationGroups.value = groups
            }
        }
    }
}

