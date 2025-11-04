package com.jetpackComposeTest1.ui.screens.dashboard

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.jetpackComposeTest1.R
import com.jetpackComposeTest1.ui.utils.toImageBitmap
import com.jetpackComposeTest1.model.notification.NotificationGroup
import com.jetpackComposeTest1.model.notification.NotificationItem
import com.jetpackComposeTest1.services.MyNotificationListener
import com.jetpackComposeTest1.services.NotificationForegroundService
import com.jetpackComposeTest1.ui.components.PermissionBottomSheet
import com.jetpackComposeTest1.ui.components.SearchEmptyStateMessage
import com.jetpackComposeTest1.ui.components.SearchToolBar
import com.jetpackComposeTest1.ui.navigation.AllUnreadNotificationsRoute
import com.jetpackComposeTest1.ui.navigation.AppNavigationRoute
import com.jetpackComposeTest1.ui.navigation.AppSelectionScreenRoute
import com.jetpackComposeTest1.ui.navigation.NotificationDetailRoute
import com.jetpackComposeTest1.ui.screens.dashboard.viewmodel.NotificationHomeViewModel
import com.jetpackComposeTest1.ui.theme.main_appColor
import com.jetpackComposeTest1.ui.theme.unreadIndicatorColor
import com.jetpackComposeTest1.ui.utils.NotificationUtils
import com.jetpackComposeTest1.ui.utils.PermissionManager
import kotlinx.coroutines.delay

@Composable
fun HomeScreenView(
    navToScreen: (AppNavigationRoute) -> Unit,
    permissionManager: PermissionManager = hiltViewModel(),
    homeScreenVM: NotificationHomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val permissionState by permissionManager.permissionState.collectAsState()
    val showPermissionBottomSheet by permissionManager.showPermissionBottomSheet.collectAsState()

    // Check permissions when the screen is first loaded
    LaunchedEffect(Unit) {
        permissionManager.checkPermissions(context)
        // Load notifications when screen first loads
        homeScreenVM.loadGroupedNotifications(context)
    }

    // Re-check permissions when the screen becomes visible again
    LaunchedEffect(showPermissionBottomSheet) {
        if (!showPermissionBottomSheet) {
            // User returned from settings, re-check permissions
            permissionManager.recheckPermissions(context)
        }
    }

    // Listen for new notifications and refresh data
    LaunchedEffect(Unit) {
        MyNotificationListener.notificationsFlow.collect {
            // Refresh notifications when new ones arrive
            homeScreenVM.refreshNotifications(context)
        }
    }

    // Collect reactive data from ViewModel
    val groupedNotifications by homeScreenVM.groupedNotifications.collectAsState()
    val unreadCount by homeScreenVM.unreadCount.collectAsState()
    val searchQuery by homeScreenVM.searchQuery.collectAsState()
    val isSearchActive by homeScreenVM.isSearchActive.collectAsState()


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = main_appColor)
    ) {
        Column {
            // Header Section
            NotificationHeader(
                navToScreen,
                context,
                unreadCount = unreadCount,
                onSearchClick = { homeScreenVM.toggleSearch() }
            )

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
                        onSearchQueryChange = { homeScreenVM.updateSearchQuery(it) },
                        onClearSearch = { homeScreenVM.clearSearch() }
                    )

                    // Search results count
                    if (searchQuery.isNotEmpty()) {
                        Text(
                            text = context.getString(
                                R.string.found_search_notification,
                                "${groupedNotifications.sumOf { it.notificationCount }}"
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Permission Status Banner
            if (!permissionState.allGranted) {
                PermissionStatusBanner(
                    context,
                    permissionState = permissionState,
                    onPermissionClick = { permissionManager.showPermissionDialog() }
                )
            }

            // Navigate to AppSelectionScreen when permissions are granted
            LaunchedEffect(permissionState.allGranted) {
                if (permissionState.allGranted && !homeScreenVM.isAppSelectionCompleted()) {
                   delay(500) // Small delay for better UX
                    navToScreen.invoke(AppSelectionScreenRoute)
                }
            }


            // Main Content Area
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    if (groupedNotifications.isEmpty()) {
                        // Empty state
                        if (isSearchActive && searchQuery.isNotEmpty()) {
                            SearchEmptyStateMessage(context)
                        } else {
                            EmptyStateMessage(context)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            itemsIndexed(groupedNotifications) { index, group ->

                                NotificationGroupCard(
                                    context,
                                    group = group,
                                    onGroupClick = {
                                        // Navigate to notification detail screen
                                        navToScreen.invoke(
                                            NotificationDetailRoute(
                                                packageName = group.packageName,
                                                appName = group.appName,
                                                isFromNotification = true,

                                            )
                                        )
                                    },
                                    onNotificationClick = { /* Open notification details */ },
                                    modifier = if (index == groupedNotifications.size - 1) Modifier.padding(
                                        bottom = 200.dp
                                    ) else Modifier
                                )
                            }
                        }
                    }
                }
            }
        }

        if (groupedNotifications.isNotEmpty()) {
            FloatingActionButton(
                onClick = { homeScreenVM.refreshNotifications(context) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp, bottom = 150.dp, top = 0.dp, end = 16.dp),
                containerColor = main_appColor
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.White
                )
            }
        }
    }

    if (showPermissionBottomSheet) {
        // Permission Bottom Sheet
        PermissionBottomSheet(
            isVisible = showPermissionBottomSheet,
            onDismiss = { permissionManager.hidePermissionDialog() },
            onPermissionGranted = { permissionManager.onPermissionGranted() }
        )
    } else {
        val serviceIntent = Intent(context, NotificationForegroundService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
    }

}

@Composable
fun NotificationHeader(
    navToScreen: (AppNavigationRoute) -> Unit,
    context: Context,
    unreadCount: Int,
    onSearchClick: () -> Unit
) {

    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = context.getString(R.string.homeScreenTitle),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = context.getString(
                    R.string.unread_notification_holder,
                    unreadCount.toString()
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
        IconButton(onClick = {
            NotificationUtils.showNotification(context, "Hello!", "This is from Jetpack Compose 🚀")
        }) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "notification Create",
                tint = Color.White
            )
        }
        Box {
            IconButton(onClick = { navToScreen.invoke(AllUnreadNotificationsRoute) }) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "show all unread notification",
                    tint = Color.White
                )
            }

            // Circular indicator for unread notifications - positioned to overlap the icon
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(unreadIndicatorColor, CircleShape)
                        .align(Alignment.TopEnd)
                        .padding(end = 2.dp, top = 2.dp)
                )
            }
        }


        IconButton(onClick = onSearchClick) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.White
            )
        }
    }
}

@Composable
fun NotificationGroupCard(
    context: Context,
    group: NotificationGroup,
    onGroupClick: () -> Unit,
    onNotificationClick: (NotificationItem) -> Unit,
    modifier: Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onGroupClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Group Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Display app icon as Image if available, otherwise use default icon
                    group.appIcon?.let { drawable ->
                        Image(
                            bitmap = drawable.toImageBitmap(),
                            contentDescription = group.appName,
                            modifier = Modifier.size(24.dp)
                        )
                    } ?: Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = group.appName,
                        tint = group.appColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = group.appName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = context.getString(
                        R.string.notification_count,
                        "${group.notificationCount}"
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Recent Notifications (max 5)
            group.recentNotifications.take(5).forEach { notification ->
                NotificationItemRow(
                    notification = notification,
                    onClick = { onNotificationClick(notification) }
                )
                if (notification != group.recentNotifications.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Show more indicator if there are more notifications
            if (group.notificationCount > 5) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onGroupClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = context.getString(
                            R.string.view_more_notification_holder,
                            "${group.notificationCount - 5}"
                        ),
                        color = main_appColor
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationItemRow(
    notification: NotificationItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Unread indicator
        if (!notification.isRead) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = unreadIndicatorColor,
                        shape = RoundedCornerShape(4.dp)
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
        } else {
            Spacer(modifier = Modifier.width(16.dp))
        }

        // Notification content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = notification.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Medium,
                maxLines = 1,
                color = if (notification.isRead) Color.Gray else Color.Black
            )
            if (notification.message.isNotEmpty()) {
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
        }

        // Timestamp
        Text(
            text = notification.timeAgo,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}


@Composable
fun PermissionStatusBanner(
    context: Context,
    permissionState: com.jetpackComposeTest1.ui.utils.PermissionState,
    onPermissionClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (permissionState.allGranted) Color(0xFF4CAF50) else Color(0xFFFF9800)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (permissionState.allGranted) Icons.Default.Notifications else Icons.Default.Warning,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (permissionState.allGranted) context.getString(R.string.notification_ready) else context.getString(
                        R.string.permission_required
                    ),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )

                Text(
                    text = if (permissionState.allGranted)
                        context.getString(R.string.your_notifications_are_bing_saved_automatically)
                    else
                        context.getString(R.string.tap_to_enable_notification_access),
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp
                )
            }

            if (!permissionState.allGranted) {
                TextButton(
                    onClick = onPermissionClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text(context.getString(R.string.enable), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun EmptyStateMessage(context: Context) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        Image(
            painter = painterResource(id = R.drawable.no_notification),
            contentDescription = "Description of image",
            modifier = Modifier.size(54.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = context.getString(R.string.empty_notification_description),
            style = MaterialTheme.typography.headlineSmall,
            color = Color.Black,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = context.getString(R.string.empty_notification_small_description),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}


