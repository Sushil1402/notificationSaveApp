package com.jetpackComposeTest1.ui.screens.dashboard


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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jetpackComposeTest1.R
import com.jetpackComposeTest1.model.notification.NotificationItem
import com.jetpackComposeTest1.ui.components.AppAlertDialog
import com.jetpackComposeTest1.ui.components.SearchEmptyStateMessage
import com.jetpackComposeTest1.ui.components.SearchToolBar
import com.jetpackComposeTest1.ui.screens.dashboard.viewmodel.NotificationDetailViewModel
import com.jetpackComposeTest1.ui.theme.main_appColor
import com.jetpackComposeTest1.ui.theme.unreadIndicatorColor
import com.jetpackComposeTest1.ui.utils.toImageBitmap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDetailScreen(
    packageName: String,
    appName: String,
    isFromNotification:Boolean,
    onNavigateBack: () -> Unit,
    viewModel: NotificationDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val notifications by viewModel.notifications.collectAsState()
    val appIcon by viewModel.appIcon.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()

    var showMarkAllDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedNotification by remember { mutableStateOf<NotificationItem?>(null) }

    // Load notifications when screen is first displayed
    LaunchedEffect(packageName, appName) {
        viewModel.loadNotificationsForApp(context, packageName, appName)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // App icon
                        appIcon?.let { drawable ->
                            Image(
                                bitmap = drawable.toImageBitmap(),
                                contentDescription = appName,
                                modifier = Modifier.size(32.dp),
                                contentScale = ContentScale.Crop
                            )
                        } ?: Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = appName,
                            tint = main_appColor,
                            modifier = Modifier.size(32.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = appName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (unreadCount > 0) {
                                Text(
                                    text = context.getString(
                                        R.string.unread_count,
                                        unreadCount.toString()
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
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
    ) { paddingValues ->
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
                            EmptyNotificationsMessage()
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(notifications) { notification ->
                                NotificationDetailItem(
                                    notification = notification,
                                    onMarkAsRead = { viewModel.markAsRead(notification.id) },
                                    onDelete = {
                                        selectedNotification = notification
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Floating Action Button for Mark All Read
            if (unreadCount > 0 && !isLoading) {
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
            })
    }
}

@Composable
fun NotificationDetailItem(
    notification: NotificationItem,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (!notification.isRead) {
                    onMarkAsRead()
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) Color.White else Color(0xFFF8F9FA)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Unread indicator
            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = unreadIndicatorColor,
                            shape = CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(12.dp))
            } else {
                Spacer(modifier = Modifier.width(20.dp))
            }

            // Notification content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.SemiBold,
                    color = if (notification.isRead) Color.Gray else Color.Black,
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
                    if (!notification.isRead) {
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
                    }
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

@Composable
fun EmptyNotificationsMessage() {
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
            text = context.getString(R.string.no_notifications_found),
            style = MaterialTheme.typography.headlineSmall,
            color = Color.Black,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = context.getString(R.string.all_notifications_cleared),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}




