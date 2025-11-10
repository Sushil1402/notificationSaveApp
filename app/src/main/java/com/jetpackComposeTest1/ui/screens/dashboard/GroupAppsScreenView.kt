package com.jetpackComposeTest1.ui.screens.dashboard

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.jetpackComposeTest1.db.NotificationDao
import com.jetpackComposeTest1.data.repository.database.NotificationGroupRepository
import com.jetpackComposeTest1.ui.components.SearchToolBar
import com.jetpackComposeTest1.ui.components.SearchEmptyStateMessage
import com.jetpackComposeTest1.ui.navigation.AppNavigationRoute
import com.jetpackComposeTest1.ui.navigation.NotificationDetailRoute
import com.jetpackComposeTest1.ui.theme.main_appColor
import com.jetpackComposeTest1.ui.utils.toImageBitmap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupAppsScreenView(
    groupId: String,
    groupName: String,
    navToScreen: (AppNavigationRoute) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: GroupAppsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val apps by viewModel.apps.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()

    LaunchedEffect(groupId) {
        viewModel.loadGroupApps(context, groupId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = groupName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleSearch() }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = main_appColor
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Search Bar
                AnimatedVisibility(
                    visible = isSearchActive,
                    enter = slideInVertically(
                        initialOffsetY = { -it },
                        animationSpec = tween(durationMillis = 300)
                    ) + fadeIn(animationSpec = tween(durationMillis = 300)),
                    exit = slideOutVertically(
                        targetOffsetY = { -it },
                        animationSpec = tween(durationMillis = 300)
                    ) + fadeOut(animationSpec = tween(durationMillis = 300))
                ) {
                    Column {
                        SearchToolBar(
                            context = context,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                            onClearSearch = { viewModel.clearSearch() }
                        )

                        // Search results count
                        if (searchQuery.isNotEmpty()) {
                            Text(
                                text = "Found ${apps.size} apps",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = main_appColor
                            )
                        }
                    } else if (apps.isEmpty()) {
                        if (isSearchActive && searchQuery.isNotEmpty()) {
                            SearchEmptyStateMessage(context)
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No apps found",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(apps) { app ->
                                AppItem(
                                    app = app,
                                    onAppClick = {
                                        navToScreen.invoke(
                                            NotificationDetailRoute(
                                                packageName = app.packageName,
                                                appName = app.appName
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppItem(
    app: AppWithNotificationCount,
    onAppClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAppClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon
            Image(
                bitmap = app.appIcon.toImageBitmap(),
                contentDescription = app.appName,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // App Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Notification Count Badge - only show if count > 0
            if (app.notificationCount > 0) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = main_appColor,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = app.notificationCount.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@HiltViewModel
class GroupAppsViewModel @Inject constructor(
    private val notificationDao: NotificationDao,
    private val groupRepository: NotificationGroupRepository
) : androidx.lifecycle.ViewModel() {

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
data class AppWithNotificationCount(
    val packageName: String,
    val appName: String,
    val appIcon: android.graphics.drawable.Drawable,
    val notificationCount: Int
)

