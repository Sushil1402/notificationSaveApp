package com.jetpackComposeTest1.ui.screens.dashboard


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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    selectedDate:Long?,
    onNavigateBack: () -> Unit,
    viewModel: NotificationDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val notifications by viewModel.notifications.collectAsState()
    val grouped by viewModel.groupedNotifications.collectAsState()
    val dateFilter by viewModel.selectedDateFilter.collectAsState()
    val appIcon by viewModel.appIcon.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()

    var showMarkAllDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedNotification by remember { mutableStateOf<NotificationItem?>(null) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Load notifications when screen is first displayed
    LaunchedEffect(packageName, appName) {
        viewModel.loadNotificationsForApp(context, packageName, appName)
    }

    // Apply incoming selectedDate as initial filter if provided
    LaunchedEffect(selectedDate) {
        if (selectedDate != null && selectedDate > 0L) {
            viewModel.setDateFilter(selectedDate)
        }
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
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Filter",
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
            // state moved to top scope so TopAppBar can modify it

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
                        NotificationsGroupedList(
                            groups = grouped,
                            onMarkAsRead = { viewModel.markAsRead(it) },
                            onDelete = { notif ->
                                selectedNotification = notif
                                showDeleteDialog = true
                            }
                        )
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

            // Filter Bottom Sheet
            if (showFilterSheet) {
                FilterBottomSheet(
                    currentDateFilter = dateFilter,
                    onPickDate = {
                        showDatePicker = true
                    },
                    onClearDate = {
                        viewModel.setDateFilter(null)
                        showFilterSheet = false
                    },
                    onDismiss = { showFilterSheet = false }
                )
            }

            // Date Picker Dialog
            if (showDatePicker) {
                DatePickerDialogComposable(
                    selectedDate = dateFilter ?: System.currentTimeMillis(),
                    onDateSelected = { millis ->
                        viewModel.setDateFilter(millis)
                        showDatePicker = false
                        showFilterSheet = false
                    },
                    onDismiss = { showDatePicker = false }
                )
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NotificationsGroupedList(
    groups: List<NotificationDetailViewModel.DateGroup>,
    onMarkAsRead: (String) -> Unit,
    onDelete: (NotificationItem) -> Unit
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
                    NotificationDetailItem(
                        notification = notification,
                        onMarkAsRead = { onMarkAsRead(notification.id) },
                        onDelete = { onDelete(notification) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    currentDateFilter: Long?,
    onPickDate: () -> Unit,
    onClearDate: () -> Unit,
    onDismiss: () -> Unit
) {
    val dateText = remember(currentDateFilter) {
        currentDateFilter?.let {
            java.text.SimpleDateFormat("EEEE, d MMMM", java.util.Locale.getDefault())
                .format(java.util.Date(it))
        } ?: "All dates"
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = {}
    ) {
        // Sheet background
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF3F7FC))
        ) {
            // Small top handle line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .background(Color(0x33000000), shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
                )
            }
            // Header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filters",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Content
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)) {
                // Date label
                Text(
                    text = "Date",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Date select-like field
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                        .background(Color(0xFFE0E3E7))
                        .clickable { onPickDate() }
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (currentDateFilter == null) "Select value" else dateText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (currentDateFilter == null) Color(0xFF9EA3AE) else Color.Black,
                                maxLines = 1
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = Color(0xFF9EA3AE)
                        )
                    }
                }

                // Clear control
                if (currentDateFilter != null) {
                    Button(
                        onClick = onClearDate,
                        modifier = Modifier
                            .padding(top = 12.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = main_appColor,
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = "Clear date")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
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




