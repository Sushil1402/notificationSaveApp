package com.notistorex.app.ui.screens.dashboard.viewmodel

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.notistorex.app.data.repository.database.NotificationGroupRepository
import com.notistorex.app.db.NotificationDao
import com.notistorex.app.model.group.AppWithNotificationCount

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupAppsViewModel @Inject constructor(
    private val notificationDao: NotificationDao,
    private val groupRepository: NotificationGroupRepository
) : androidx.lifecycle.ViewModel()
{

    private val _apps = MutableStateFlow<List<AppWithNotificationCount>>(emptyList())
    val apps: StateFlow<List<AppWithNotificationCount>> = _apps.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Search functionality
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    private val _filteredApps = MutableStateFlow<List<AppWithNotificationCount>>(emptyList())
    val filteredApps: StateFlow<List<AppWithNotificationCount>> = _filteredApps.asStateFlow()

    // Original apps for filtering
    private val _allApps = MutableStateFlow<List<AppWithNotificationCount>>(emptyList())

    init {
        // Combine search query with all apps to filter
        viewModelScope.launch {
            combine(_searchQuery, _allApps) { query, apps ->
                if (query.isBlank()) {
                    apps
                } else {
                    filterApps(apps, query)
                }
            }.collect { filtered ->
                _filteredApps.value = filtered
                _apps.value = filtered
            }
        }
    }

    fun loadGroupApps(context: Context, groupId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                when (groupId) {
                    "unread" -> {
                        // Get all apps with unread notifications
                        notificationDao.getNotificationsByReadStatus(isRead = false)
                            .collect { notifications ->
                                val appGroups = notifications.groupBy { it.packageName }
                                val appsList = appGroups.map { (packageName, appNotifications) ->
                                    val appInfo = try {
                                        val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
                                        context.packageManager.getApplicationLabel(appInfo).toString()
                                    } catch (e: Exception) {
                                        packageName
                                    }

                                    AppWithNotificationCount(
                                        packageName = packageName,
                                        appName = appInfo,
                                        appIcon = try {
                                            context.packageManager.getApplicationIcon(packageName)
                                        } catch (e: Exception) {
                                            context.packageManager.getDefaultActivityIcon()
                                        },
                                        notificationCount = appNotifications.size
                                    )
                                }.sortedByDescending { it.notificationCount }

                                _allApps.value = appsList
                                _isLoading.value = false
                            }
                    }
                    "all_apps" -> {
                        notificationDao.getAllNotifications()
                            .collect { notifications ->
                                val appGroups = notifications.groupBy { it.packageName }
                                val appsList = appGroups.map { (packageName, appNotifications) ->
                                    val appInfo = try {
                                        val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
                                        context.packageManager.getApplicationLabel(appInfo).toString()
                                    } catch (e: Exception) {
                                        packageName
                                    }

                                    AppWithNotificationCount(
                                        packageName = packageName,
                                        appName = appInfo,
                                        appIcon = try {
                                            context.packageManager.getApplicationIcon(packageName)
                                        } catch (e: Exception) {
                                            context.packageManager.getDefaultActivityIcon()
                                        },
                                        notificationCount = appNotifications.size
                                    )
                                }.sortedByDescending { it.notificationCount }

                                _allApps.value = appsList
                                _isLoading.value = false
                            }
                    }
                    "read" -> {
                        // Get all apps with read notifications
                        notificationDao.getNotificationsByReadStatus(isRead = true)
                            .collect { notifications ->
                                val appGroups = notifications.groupBy { it.packageName }
                                val appsList = appGroups.map { (packageName, appNotifications) ->
                                    val appInfo = try {
                                        val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
                                        context.packageManager.getApplicationLabel(appInfo).toString()
                                    } catch (e: Exception) {
                                        packageName
                                    }

                                    AppWithNotificationCount(
                                        packageName = packageName,
                                        appName = appInfo,
                                        appIcon = try {
                                            context.packageManager.getApplicationIcon(packageName)
                                        } catch (e: Exception) {
                                            context.packageManager.getDefaultActivityIcon()
                                        },
                                        notificationCount = appNotifications.size
                                    )
                                }.sortedByDescending { it.notificationCount }

                                _allApps.value = appsList
                                _isLoading.value = false
                            }
                    }
                    "muted" -> {
                        // Get all apps with muted notifications
                        notificationDao.getNotificationsByMuteStatus(isMuted = true)
                            .collect { notifications ->
                                val appGroups = notifications.groupBy { it.packageName }
                                val appsList = appGroups.map { (packageName, appNotifications) ->
                                    val appInfo = try {
                                        val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
                                        context.packageManager.getApplicationLabel(appInfo).toString()
                                    } catch (e: Exception) {
                                        packageName
                                    }

                                    AppWithNotificationCount(
                                        packageName = packageName,
                                        appName = appInfo,
                                        appIcon = try {
                                            context.packageManager.getApplicationIcon(packageName)
                                        } catch (e: Exception) {
                                            context.packageManager.getDefaultActivityIcon()
                                        },
                                        notificationCount = appNotifications.size
                                    )
                                }.sortedByDescending { it.notificationCount }

                                _allApps.value = appsList
                                _isLoading.value = false
                            }
                    }
                    else -> {
                        // Custom group - get apps from group membership
                        groupRepository.getAppsInGroup(groupId)
                            .collect { memberships ->
                                // Get all notifications to count them
                                notificationDao.getAllNotifications()
                                    .collect { allNotifications ->
                                        val appsList = memberships.map { membership ->
                                            val appInfo = try {
                                                val appInfo = context.packageManager.getApplicationInfo(membership.packageName, 0)
                                                context.packageManager.getApplicationLabel(appInfo).toString()
                                            } catch (e: Exception) {
                                                membership.packageName
                                            }

                                            // Count notifications for this package
                                            val notificationCount = allNotifications.count { it.packageName == membership.packageName }

                                            AppWithNotificationCount(
                                                packageName = membership.packageName,
                                                appName = appInfo,
                                                appIcon = try {
                                                    context.packageManager.getApplicationIcon(membership.packageName)
                                                } catch (e: Exception) {
                                                    context.packageManager.getDefaultActivityIcon()
                                                },
                                                notificationCount = notificationCount
                                            )
                                        }.sortedByDescending { it.notificationCount }

                                        _apps.value = appsList
                                        _isLoading.value = false
                                    }
                            }
                    }
                }
            } catch (e: Exception) {
                _apps.value = emptyList()
                _isLoading.value = false
            }
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

    private fun filterApps(apps: List<AppWithNotificationCount>, query: String): List<AppWithNotificationCount> {
        val lowercaseQuery = query.lowercase().trim()
        if (lowercaseQuery.isEmpty()) return apps

        return apps.filter { app ->
            app.appName.lowercase().contains(lowercaseQuery) ||
                    app.packageName.lowercase().contains(lowercaseQuery)
        }
    }
}