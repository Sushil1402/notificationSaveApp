package com.notistorex.app.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.notistorex.app.R
import com.notistorex.app.model.notification.NotificationItem
import com.notistorex.app.ui.components.AppAlertDialog
import com.notistorex.app.ui.components.SearchEmptyStateMessage
import com.notistorex.app.ui.components.SearchToolBar
import com.notistorex.app.ui.screens.dashboard.viewmodel.AllUnreadNotificationsViewModel
import com.notistorex.app.ui.theme.main_appColor
import com.notistorex.app.ui.utils.toImageBitmap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllUnreadNotificationsScreen(
    onNavigateBack:()->Unit,
    onNavigateToDetail: ((String) -> Unit)? = null,
    viewModel: AllUnreadNotificationsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val notifications by viewModel.notifications.collectAsState()
    val grouped by viewModel.groupedNotifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()

    var showMarkAllDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedNotification by remember { mutableStateOf<NotificationItem?>(null) }

    // Load notifications when screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.loadAllUnreadNotifications(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = context.getString(R.string.all_unread_notifications),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack.invoke() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = context.getString(R.string.back),
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleSearch() }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = context.getString(R.string.search),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = main_appColor
                )
            )
        }
    )
    { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
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
                                text = context.getString(
                                    R.string.found_search_notification,
                                    "${notifications.size}"
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
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
                            CircularProgressIndicator(color = main_appColor)
                        }
                    } else if (notifications.isEmpty()) {
                        if (isSearchActive && searchQuery.isNotEmpty()) {
                            SearchEmptyStateMessage(context)
                        } else {
                            EmptyUnreadNotificationsMessage()
                        }
                    } else {
                        AllUnreadNotificationsGroupedList(
                            groups = grouped,
                            onMarkAsRead = { viewModel.markAsRead(it) },
                            onDelete = { notification ->
                                selectedNotification = notification
                                showDeleteDialog = true
                            },
                            onNotificationClick = { notificationId ->
                                onNavigateToDetail?.invoke(notificationId)
                            }
                        )
                    }
                }
            }

            // Floating Action Button for Mark All Read
            if (notifications.isNotEmpty() && !isLoading) {
                FloatingActionButton(
                    onClick = { showMarkAllDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = main_appColor
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = context.getString(R.string.mark_all_as_read),
                        tint = Color.White
                    )
                }
            }
        }
    }

    // Mark all as read dialog
    if (showMarkAllDialog) {
        AppAlertDialog(
            onDismissRequest = { showMarkAllDialog = false },
            title = context.getString(R.string.mark_all_as_read),
            text = context.getString(R.string.mark_all_as_read_confirmation),
            confirmButtonText = context.getString(R.string.mark_all),
            dismissButtonText = context.getString(R.string.cancel),
            confirmButton = {
                viewModel.markAllAsRead()
                showMarkAllDialog = false
            },
            dismissButton = {
                showMarkAllDialog = false
            }
        )
    }

    // Delete notification dialog
    if (showDeleteDialog && selectedNotification != null) {
        AppAlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                selectedNotification = null
            },
            title = context.getString(R.string.delete_notification),
            text = context.getString(R.string.delete_notification_confirmation),
            confirmButtonText = context.getString(R.string.delete),
            dismissButtonText = context.getString(R.string.cancel),
            confirmButton = {
                selectedNotification?.let { notification ->
                    viewModel.deleteNotification(notification.id)
                }
                showDeleteDialog = false
                selectedNotification = null
            },
            dismissButton = {
                showDeleteDialog = false
                selectedNotification = null
            }
        )
    }
}

@Composable
private fun AllUnreadNotificationsGroupedList(
    groups: List<AllUnreadNotificationsViewModel.DateGroup>,
    onMarkAsRead: (String) -> Unit,
    onDelete: (NotificationItem) -> Unit,
    onNotificationClick: ((String) -> Unit)? = null
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        groups.forEach { group ->
            stickyHeader {
                // Centered rounded date chip
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .padding(top = 8.dp)
                ) {
                    Surface(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                        color = Color.White,
                        tonalElevation = 2.dp,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .align(Alignment.Center)
                    ) {
                        Text(
                            text = group.dateLabel,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            items(group.items, key = { it.id }) { notification ->
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)) {
                    AllUnreadNotificationItem(
                        notification = notification,
                        onMarkAsRead = { onMarkAsRead(notification.id) },
                        onDelete = { onDelete(notification) },
                        onClick = { onNotificationClick?.invoke(notification.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun AllUnreadNotificationItem(
    notification: NotificationItem,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                onClick?.invoke() ?: run {
                    if (!notification.isRead) {
                        onMarkAsRead()
                    }
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // App info row at the top
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App icon
                if (notification.appIcon != null) {
                    Image(
                        bitmap = notification.appIcon.toImageBitmap(),
                        contentDescription = notification.appName,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                // App name
                Text(
                    text = notification.appName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
               
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            // Notification content with menu button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (notification.message.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = notification.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = notification.timeAgo,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                // Menu button
                Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = context.getString(R.string.more_options),
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(context.getString(R.string.mark_as_read)) },
                        onClick = {
                            onMarkAsRead()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = main_appColor
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(context.getString(R.string.delete)) },
                        onClick = {
                            onDelete()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color.Red
                            )
                        }
                    )
                }
            }
            }
        }
    }
}

@Composable
fun EmptyUnreadNotificationsMessage() {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = context.getString(R.string.no_notifications_found),
            tint = Color.Gray,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = context.getString(R.string.no_unread_notifications),
            style = MaterialTheme.typography.headlineSmall,
            color = Color.Black,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = context.getString(R.string.all_notifications_read),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}
