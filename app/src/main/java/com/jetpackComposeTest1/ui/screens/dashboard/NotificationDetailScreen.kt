package com.jetpackComposeTest1.ui.screens.dashboard


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
import com.jetpackComposeTest1.ads.BannerAD
import com.jetpackComposeTest1.ads.rememberRewardedAdManager
import com.jetpackComposeTest1.model.notification.NotificationItem
import com.jetpackComposeTest1.ui.components.AppAlertDialog
import com.jetpackComposeTest1.ui.components.MenuOption
import com.jetpackComposeTest1.ui.components.MoreOptionsMenu
import com.jetpackComposeTest1.ui.components.SearchEmptyStateMessage
import com.jetpackComposeTest1.ui.components.SearchToolBar
import com.jetpackComposeTest1.ui.screens.dashboard.viewmodel.NotificationDetailViewModel
import com.jetpackComposeTest1.ui.screens.dashboard.viewmodel.PremiumViewModel
import com.jetpackComposeTest1.ui.theme.main_appColor
import com.jetpackComposeTest1.ui.theme.unreadIndicatorColor
import com.jetpackComposeTest1.ui.utils.Utils.shareExcelFile
import com.jetpackComposeTest1.ui.utils.toImageBitmap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDetailScreen(
    packageName: String,
    appName: String,
    isFromNotification:Boolean,
    selectedDate:Long?,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: ((String) -> Unit)? = null,
    viewModel: NotificationDetailViewModel = hiltViewModel(),
    premiumViewModel: PremiumViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val notifications by viewModel.notifications.collectAsState()
    val grouped by viewModel.groupedNotifications.collectAsState()
    val dateFilter by viewModel.selectedDateFilter.collectAsState()
    val appIcon by viewModel.appIcon.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val exportState by viewModel.exportState.collectAsState()
    val isPremium by premiumViewModel.isPremium.collectAsState()

    var showMarkAllDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedNotification by remember { mutableStateOf<NotificationItem?>(null) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showAdLoadingDialog by remember { mutableStateOf(false) }
    var adLoadError by remember { mutableStateOf<String?>(null) }
    var isExportCanceled by remember { mutableStateOf(false) }
    
    // Reward ad manager
    val rewardedAdManager = rememberRewardedAdManager()
    val coroutineScope = rememberCoroutineScope()

    // Load notifications when screen is first displayed
    LaunchedEffect(packageName, appName) {
        viewModel.loadNotificationsForApp(context, packageName, appName)
    }
    
    // Preload reward ad when screen loads (only if not premium)
    LaunchedEffect(isPremium) {
        if (!isPremium) {
            activity?.let {
                rewardedAdManager.preloadAd(it)
            }
        }
    }
    
    // Timeout mechanism for ad loading dialog - close after 15 seconds if still loading
    LaunchedEffect(showAdLoadingDialog) {
        if (showAdLoadingDialog) {
            Log.d("RewardedAd", "Starting timeout timer for ad loading")
            kotlinx.coroutines.delay(15000) // 15 seconds timeout
            // Check if dialog is still showing (hasn't been closed by callbacks)
            if (showAdLoadingDialog) {
                Log.e("RewardedAd", "Ad loading timeout after 15 seconds - forcing dialog close")
                showAdLoadingDialog = false
                if (!isExportCanceled) {
                    adLoadError = "Ad loading timed out. Please check your internet connection and try again."
                }
                isExportCanceled = true // Mark as canceled to prevent export
            }
        }
    }

    // Apply incoming selectedDate as initial filter if provided
    LaunchedEffect(selectedDate) {
        if (selectedDate != null && selectedDate > 0L) {
            viewModel.setDateFilter(selectedDate)
        }
    }

    // Handle export state changes
    LaunchedEffect(exportState) {
        when (exportState) {
            is NotificationDetailViewModel.ExportState.Success -> {
                // Share the file using common utility function
                shareExcelFile(context, (exportState as NotificationDetailViewModel.ExportState.Success).fileUri)
                viewModel.resetExportState()
            }
            is NotificationDetailViewModel.ExportState.Error -> {
                // Error is shown in dialog
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = appName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (unreadCount > 0) {
                                Text(
                                    text = context.getString(
                                        R.string.unread_count,
                                        unreadCount
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
                    MoreOptionsMenu(
                        options = listOf(
                            MenuOption(
                                title = context.getString(R.string.filter),
                                icon = Icons.Default.FilterList,
                                iconTint = main_appColor,
                                onClick = { showFilterSheet = true }
                            ),
                            MenuOption(
                                title = context.getString(R.string.export),
                                icon = Icons.Default.FileUpload,
                                iconTint = main_appColor,
                                onClick = {
                                    // If user is premium, export directly without showing ad
                                    if (isPremium) {
                                        Log.d("Export", "Premium user - exporting directly without ad")
                                        viewModel.exportAppNotifications(context, packageName, appName)
                                    } else {
                                        // Reset cancel flag
                                        isExportCanceled = false
                                        // Show reward ad before export
                                        if (activity != null) {
                                            if (rewardedAdManager.isAdLoaded()) {
                                                // Show ad immediately
                                                rewardedAdManager.showRewardedAd(
                                                    activity = activity,
                                                    onUserEarnedReward = {
                                                        // User watched ad completely, proceed with export only if not canceled
                                                        if (!isExportCanceled) {
                                                            Log.d("RewardedAd", "User earned reward, starting export")
                                                            viewModel.exportAppNotifications(context, packageName, appName)
                                                        }
                                                    },
                                                    onAdFailedToShow = { error ->
                                                        // If ad fails to show, don't export
                                                        Log.d("RewardedAd", "Ad failed to show: $error")
                                                    },
                                                    onAdDismissed = {
                                                        // Ad was dismissed, don't export if user didn't earn reward
                                                        Log.d("RewardedAd", "Ad dismissed without reward")
                                                    }
                                                )
                                            } else {
                                                // Ad not loaded, show loading dialog and load ad
                                                Log.d("RewardedAd", "Ad not loaded, starting load process")
                                                showAdLoadingDialog = true
                                                adLoadError = null
                                                isExportCanceled = false
                                                
                                                // Load the ad with proper error handling
                                                try {
                                                    Log.d("RewardedAd", "Calling loadRewardedAd on activity: ${activity.javaClass.simpleName}")
                                                    rewardedAdManager.loadRewardedAd(
                                                        activity = activity,
                                                        onAdLoaded = {
                                                            Log.d("RewardedAd", "✓ onAdLoaded callback fired - ad loaded successfully")
                                                            // Close dialog immediately
                                                            showAdLoadingDialog = false
                                                            
                                                            // Check if user canceled - if so, don't show ad
                                                            if (!isExportCanceled) {
                                                                // Show ad after small delay to allow dialog to close
                                                                Log.d("RewardedAd", "Preparing to show ad in 300ms")
                                                                coroutineScope.launch {
                                                                    delay(300)
                                                                    
                                                                    // Double-check cancel status and activity
                                                                    if (isExportCanceled) {
                                                                        Log.d("RewardedAd", "Export was canceled during delay, skipping ad")
                                                                        return@launch
                                                                    }
                                                                    
                                                                    val currentActivity = context as? Activity
                                                                    if (currentActivity == null || currentActivity.isFinishing) {
                                                                        Log.e("RewardedAd", "Activity is invalid, cannot show ad")
                                                                        adLoadError = "Unable to show ad. Please try again."
                                                                        return@launch
                                                                    }
                                                                    
                                                                    Log.d("RewardedAd", "Showing rewarded ad now")
                                                                    try {
                                                                        rewardedAdManager.showRewardedAd(
                                                                            activity = currentActivity,
                                                                            onUserEarnedReward = {
                                                                                if (!isExportCanceled) {
                                                                                    Log.d("RewardedAd", "✓ User earned reward, starting export")
                                                                                    viewModel.exportAppNotifications(context, packageName, appName)
                                                                                } else {
                                                                                    Log.d("RewardedAd", "Export was canceled, not proceeding")
                                                                                }
                                                                            },
                                                                            onAdFailedToShow = { error ->
                                                                                Log.e("RewardedAd", "✗ Ad failed to show: $error")
                                                                                adLoadError = "Failed to show ad: $error"
                                                                            },
                                                                            onAdDismissed = {
                                                                                Log.d("RewardedAd", "Ad dismissed without reward")
                                                                            }
                                                                        )
                                                                    } catch (e: Exception) {
                                                                        Log.e("RewardedAd", "Exception showing ad: ${e.message}", e)
                                                                        adLoadError = "Failed to show ad: ${e.message}"
                                                                    }
                                                                }
                                                            } else {
                                                                Log.d("RewardedAd", "Export was canceled, not showing ad")
                                                            }
                                                        },
                                                        onAdFailed = { error ->
                                                            Log.e("RewardedAd", "✗ onAdFailed callback fired: $error")
                                                            // Always close dialog on failure
                                                            showAdLoadingDialog = false
                                                            // Show error only if not canceled
                                                            if (!isExportCanceled) {
                                                                adLoadError = "Failed to load ad. Please check your internet connection and try again."
                                                            }
                                                        }
                                                    )
                                                    Log.d("RewardedAd", "loadRewardedAd call completed, waiting for callbacks...")
                                                } catch (e: Exception) {
                                                    Log.e("RewardedAd", "Exception in loadRewardedAd call: ${e.message}", e)
                                                    showAdLoadingDialog = false
                                                    if (!isExportCanceled) {
                                                        adLoadError = "Failed to load ad: ${e.message}"
                                                    }
                                                }
                                            }
                                        } else {
                                            // No activity context, can't show ad - don't export
                                            Log.d("RewardedAd", "No activity context available")
                                        }
                                    }
                                }
                            ),
                            MenuOption(
                                title = context.getString(R.string.app_notification_settings),
                                icon = Icons.Default.Settings,
                                iconTint = main_appColor,
                                onClick = {
                                    openAppNotificationSettings(context, packageName)
                                }
                            )
                        ),
                        iconTint = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = main_appColor
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f, fill = true)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    // Export Progress Indicator
                    val isExporting = exportState is NotificationDetailViewModel.ExportState.Exporting
                    AnimatedVisibility(
                        visible = isExporting,
                        enter = slideInVertically(
                            initialOffsetY = { -it },
                            animationSpec = tween(durationMillis = 300)
                        ) + fadeIn(animationSpec = tween(durationMillis = 300)),
                        exit = slideOutVertically(
                            targetOffsetY = { -it },
                            animationSpec = tween(durationMillis = 300)
                        ) + fadeOut(animationSpec = tween(durationMillis = 300))
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = main_appColor.copy(alpha = 0.95f),
                            shadowElevation = 4.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                        Text(
                                            text = "Exporting notifications...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(3.dp),
                                    color = Color.White,
                                    trackColor = Color.White.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }

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
                                },
                                onNotificationClick = { notificationId ->
                                    onNavigateToDetail?.invoke(notificationId)
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

            // Only show banner ad if user is not premium
            if (!isPremium) {
                BannerAD(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
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

    // Export error dialog
    if (exportState is NotificationDetailViewModel.ExportState.Error) {
        AlertDialog(
            onDismissRequest = { viewModel.resetExportState() },
            title = { Text("Export Failed") },
            text = { Text((exportState as NotificationDetailViewModel.ExportState.Error).message) },
            confirmButton = {
                TextButton(onClick = { viewModel.resetExportState() }) {
                    Text("OK")
                }
            }
        )
    }
    
    // Export loading dialog (shown while preparing export file/ad)
    if (showAdLoadingDialog) {
        AlertDialog(
            onDismissRequest = { 
                showAdLoadingDialog = false
                adLoadError = null
                isExportCanceled = true
                // Cancel export - user dismissed dialog
            },
            title = { Text("Loading Export File") },
            text = { 
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(16.dp),
                        color = main_appColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Please wait while we prepare your export file...")
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    showAdLoadingDialog = false
                    adLoadError = null
                    isExportCanceled = true
                    // Cancel export - user clicked cancel
                }) {
                    Text(context.getString(R.string.cancel))
                }
            }
        )
    }
    
    // Ad load error dialog
    adLoadError?.let { error ->
        AlertDialog(
            onDismissRequest = { 
                adLoadError = null
            },
            title = { Text("Export Unavailable") },
            text = { 
                Text("Unable to prepare export. Please try again later.")
            },
            confirmButton = {
                TextButton(onClick = { 
                    adLoadError = null
                }) {
                    Text("OK")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NotificationsGroupedList(
    groups: List<NotificationDetailViewModel.DateGroup>,
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
                        color = MaterialTheme.colorScheme.surfaceVariant,
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
                        onDelete = { onDelete(notification) },
                        onClick = { onNotificationClick?.invoke(notification.id) }
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
        dragHandle = {},
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        // Sheet background
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
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
                        .background(
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                        )
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
                        tint = MaterialTheme.colorScheme.onSurface,
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Date select-like field
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
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
                                color = if (currentDateFilter == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
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
    onDelete: () -> Unit,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick?.invoke() ?: run {
                    if (!notification.isRead) {
                        onMarkAsRead()
                    }
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
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
                    color = if (notification.isRead) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (notification.message.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = notification.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = notification.timeAgo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = context.getString(R.string.no_notifications_found),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = context.getString(R.string.all_notifications_cleared),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

/**
 * Opens the notification settings for a specific app
 */
private fun openAppNotificationSettings(context: Context, packageName: String) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // For Android 8.0 (API 26) and above, use ACTION_APP_NOTIFICATION_SETTINGS
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    } else {
        // For older versions, open app details settings
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.parse("package:$packageName")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
    
    try {
        ContextCompat.startActivity(context, intent, null)
    } catch (e: Exception) {
        // If the specific notification settings can't be opened, fall back to app details
        val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.parse("package:$packageName")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        ContextCompat.startActivity(context, fallbackIntent, null)
    }
}




