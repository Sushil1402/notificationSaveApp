package com.notistorex.app.ui.screens.dashboard.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notistorex.app.db.NotificationDao
import com.notistorex.app.data.repository.database.NotificationGroupRepository
import com.notistorex.app.data.local.database.NotificationGroupEntity
import com.notistorex.app.model.notification.NotificationGroupData
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
                notificationDao.getAllNotifications(),                     // All notifications
                notificationDao.getNotificationsByReadStatus(isRead = false), // Unread notifications
                groupRepository.getAllGroups() // Custom groups from database
            ) { allNotifications, unreadNotifications, customGroups ->
                
                val now = System.currentTimeMillis()
                val today = now - (24 * 60 * 60 * 1000) // 24 hours ago
                
                val allStats = calculateNotificationStats(allNotifications, today)
                // Calculate statistics for system groups
                val unreadStats = calculateNotificationStats(unreadNotifications, today)
                
                // Create system groups
                val systemGroups = listOf(
                    NotificationGroupData(
                        id = "all_apps",
                        name = "All Apps Notifications",
                        description = "Notifications from every app you've received",
                        icon = Icons.Default.Notifications,
                        color = Color(0xFF42A5F5),
                        type = "All",
                        isMuted = false,
                        totalNotifications = allStats.totalCount,
                        unreadNotifications = unreadStats.totalCount,
                        todayNotifications = allStats.todayCount,
                        appCount = allStats.appCount
                    ),
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
    
    private fun calculateNotificationStats(notifications: List<com.notistorex.app.db.NotificationEntity>, todayTimestamp: Long): NotificationStats {
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

