package com.jetpackComposeTest1.ui.screens.dashboard.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetpackComposeTest1.db.NotificationDao
import com.jetpackComposeTest1.data.repository.database.NotificationGroupRepository
import com.jetpackComposeTest1.data.local.database.NotificationGroupEntity
import com.jetpackComposeTest1.model.notification.NotificationGroupData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val notificationDao: NotificationDao,
    private val groupRepository: NotificationGroupRepository
) : ViewModel() {

    private val _notificationGroups = MutableStateFlow<List<NotificationGroupData>>(emptyList())
    val notificationGroups: StateFlow<List<NotificationGroupData>> = _notificationGroups.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadNotificationGroups()
    }

    private fun loadNotificationGroups() {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Combine system groups (from notifications) with custom groups (from database)
            combine(
                notificationDao.getNotificationsByReadStatus(isRead = false), // Unread notifications
                notificationDao.getNotificationsByReadStatus(isRead = true),  // Read notifications
                notificationDao.getNotificationsByMuteStatus(isMuted = true), // Muted notifications
                groupRepository.getAllGroups() // Custom groups from database
            ) { unreadNotifications, readNotifications, mutedNotifications, customGroups ->
                
                val now = System.currentTimeMillis()
                val today = now - (24 * 60 * 60 * 1000) // 24 hours ago
                
                // Calculate statistics for system groups
                val unreadStats = calculateNotificationStats(unreadNotifications, today)
                val readStats = calculateNotificationStats(readNotifications, today)
                val mutedStats = calculateNotificationStats(mutedNotifications, today)
                
                // Create system groups
                val systemGroups = listOf(
                    NotificationGroupData(
                        id = "unread",
                        name = "Unread Notifications",
                        description = "Notifications you haven't read yet",
                        icon = Icons.Default.Email,
                        color = Color(0xFFFF6B6B),
                        type = "Unread",
                        isMuted = false,
                        totalNotifications = unreadStats.totalCount,
                        unreadNotifications = unreadStats.totalCount,
                        todayNotifications = unreadStats.todayCount,
                        appCount = unreadStats.appCount
                    ),
                    NotificationGroupData(
                        id = "muted",
                        name = "Muted Notifications",
                        description = "Notifications from muted apps",
                        icon = Icons.Default.Face,
                        color = Color(0xFF9E9E9E),
                        type = "Muted",
                        isMuted = true,
                        totalNotifications = mutedStats.totalCount,
                        unreadNotifications = 0,
                        todayNotifications = mutedStats.todayCount,
                        appCount = mutedStats.appCount
                    )
                )
                
                // Convert custom groups from database entities to UI models
                val customGroupsUI = customGroups.map { entity ->
                    NotificationGroupData(
                        id = entity.id,
                        name = entity.name,
                        description = entity.description ?: "Custom group with ${entity.appCount} apps",
                        icon = getIconFromName(entity.iconName),
                        color = Color(android.graphics.Color.parseColor(entity.colorHex)),
                        type = entity.groupType,
                        isMuted = entity.isMuted,
                        totalNotifications = entity.totalNotifications,
                        unreadNotifications = entity.unreadNotifications,
                        todayNotifications = entity.todayNotifications,
                        appCount = entity.appCount
                    )
                }
                
                // Combine system and custom groups
                _notificationGroups.value = systemGroups + customGroupsUI
                _isLoading.value = false
            }.collect { /* This will trigger whenever any of the flows emit */ }
        }
    }
    
    private data class NotificationStats(
        val totalCount: Int,
        val todayCount: Int,
        val appCount: Int
    )
    
    private fun calculateNotificationStats(notifications: List<com.jetpackComposeTest1.db.NotificationEntity>, todayTimestamp: Long): NotificationStats {
        val totalCount = notifications.size
        val todayCount = notifications.count { it.timestamp >= todayTimestamp }
        val appCount = notifications.map { it.packageName }.distinct().size
        
        return NotificationStats(
            totalCount = totalCount,
            todayCount = todayCount,
            appCount = appCount
        )
    }
    
    private fun getIconFromName(iconName: String) = when (iconName) {
        "email" -> Icons.Default.Email
        "done" -> Icons.Default.Done
        "face" -> Icons.Default.Face
        "group" -> Icons.Default.DateRange
        "work" -> Icons.Default.Build
        "home" -> Icons.Default.Home
        "settings" -> Icons.Default.Settings
        else -> Icons.Default.Face
    }

    fun toggleGroupMute(groupId: String) {
        viewModelScope.launch {
            if (groupId in listOf("unread", "muted")) {
                // Handle system groups - just update UI state for now
                val groups = _notificationGroups.value.toMutableList()
                val groupIndex = groups.indexOfFirst { it.id == groupId }
                if (groupIndex != -1) {
                    groups[groupIndex] = groups[groupIndex].copy(isMuted = !groups[groupIndex].isMuted)
                    _notificationGroups.value = groups
                }
            } else {
                // Handle custom groups - update database
                groupRepository.toggleGroupMute(groupId)
            }
        }
    }


    fun deleteGroup(groupId: String) {
        viewModelScope.launch {
            if (groupId !in listOf("unread", "muted")) {
                // Only allow deletion of custom groups
                groupRepository.deleteGroup(groupId)
            }
        }
    }

    fun renameGroup(groupId: String, newName: String) {
        viewModelScope.launch {
            if (groupId !in listOf("unread", "muted")) {
                // Only allow renaming of custom groups
                groupRepository.updateGroupName(groupId, newName)
            }
        }
    }

}

