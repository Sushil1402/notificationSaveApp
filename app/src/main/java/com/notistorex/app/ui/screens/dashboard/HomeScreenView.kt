package com.notistorex.app.ui.screens.dashboard

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.notistorex.app.R
import com.notistorex.app.ads.HomeScreenNotificationGroupCardAd
import com.notistorex.app.model.notification.NotificationGroup
import com.notistorex.app.services.MyNotificationListener
import com.notistorex.app.services.NotificationForegroundService
import com.notistorex.app.ui.components.HomeEmptyState
import com.notistorex.app.ui.components.HomeHeader
import com.notistorex.app.ui.components.NotificationGroupCard
import com.notistorex.app.ui.components.PermissionBottomSheet
import com.notistorex.app.ui.components.PermissionStatusBanner
import com.notistorex.app.ui.components.SearchEmptyStateMessage
import com.notistorex.app.ui.components.SearchToolBar
import com.notistorex.app.ui.navigation.AppNavigationRoute
import com.notistorex.app.ui.navigation.AppSelectionScreenRoute
import com.notistorex.app.ui.navigation.NotificationDetailRoute
import com.notistorex.app.ui.screens.dashboard.viewmodel.PremiumViewModel
import com.notistorex.app.ui.screens.dashboard.viewmodel.NotificationHomeViewModel
import com.notistorex.app.ui.theme.main_appColor
import com.notistorex.app.ui.utils.PermissionManager
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun HomeScreenView(
    navToScreen: (AppNavigationRoute) -> Unit,
    permissionManager: PermissionManager = hiltViewModel(),
    homeScreenVM: NotificationHomeViewModel = hiltViewModel(),
    premiumViewModel: PremiumViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val permissionState by permissionManager.permissionState.collectAsState()
    val showPermissionBottomSheet by permissionManager.showPermissionBottomSheet.collectAsState()
    val isPremium by premiumViewModel.isPremium.collectAsState()

    LaunchedEffect(Unit) {
        permissionManager.checkPermissions(context)
        homeScreenVM.loadGroupedNotifications(context)
    }

    LaunchedEffect(showPermissionBottomSheet) {
        if (!showPermissionBottomSheet) {
            permissionManager.recheckPermissions(context)
        }
    }

    LaunchedEffect(Unit) {
        MyNotificationListener.notificationsFlow.collect {
            homeScreenVM.refreshNotifications(context)
        }
    }

    val groupedNotifications by homeScreenVM.groupedNotifications.collectAsState()
    val unreadCount by homeScreenVM.unreadCount.collectAsState()
    val searchQuery by homeScreenVM.searchQuery.collectAsState()
    val isSearchActive by homeScreenVM.isSearchActive.collectAsState()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = main_appColor.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        Column {
            Surface(
                color = main_appColor,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                tonalElevation = 0.dp
            ) {
                Column {
                    HomeHeader(
                        navToScreen = navToScreen,
                        unreadCount = unreadCount,
                        onSearchClick = { homeScreenVM.toggleSearch() }
                    )

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

                            if (searchQuery.isNotEmpty()) {
                                Text(
                                    text = context.getString(
                                        R.string.found_search_notification,
                                        "${groupedNotifications.sumOf { it.notificationCount }}"
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (!permissionState.allGranted) {
                PermissionStatusBanner(
                    permissionState = permissionState,
                    onPermissionClick = { permissionManager.showPermissionDialog() }
                )
            }



            LaunchedEffect(permissionState.allGranted) {
                if (permissionState.allGranted && !homeScreenVM.isAppSelectionCompleted()) {
                    delay(500)
                    navToScreen.invoke(AppSelectionScreenRoute)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                val feedItems = remember(groupedNotifications, isPremium) {
                    if (groupedNotifications.isEmpty()) {
                        emptyList<HomeFeedItem>()
                    } else if (isPremium) {
                        groupedNotifications.mapIndexed { index, group ->
                            HomeFeedItem.Notification(group, index)
                        }
                    } else {
                        val notificationCount = groupedNotifications.size
                        
                        // Generate random ad positions (avoiding top and end)
                        // Valid positions are indices 1 to (notificationCount - 2), excluding 0 (top) and last index
                        val adPositions = if (notificationCount <= 2) {
                            // Not enough items to show ads (need at least 3 to have a valid middle position)
                            emptySet<Int>()
                        } else {
                            // Calculate how many ads to show (approximately 1 ad per 4 notifications, minimum 1)
                            val maxValidIndex = notificationCount - 2 // Last valid index (exclude top and end)
                            val adCount = maxOf(1, notificationCount / 4)
                                .coerceAtMost(maxValidIndex) // Don't show more ads than we have valid positions
                            
                            // Generate stable random positions using a seed based on notification groups
                            // This ensures positions are consistent across recompositions but random
                            val seed = groupedNotifications.joinToString { it.packageName }.hashCode().toLong()
                            val random = Random(seed)
                            
                            // Create a list of valid positions [1, 2, ..., maxValidIndex], shuffle it, and take adCount
                            (1..maxValidIndex).shuffled(random).take(adCount).toSet()
                        }
                        
                        buildList {
                            groupedNotifications.forEachIndexed { index, group ->
                                add(HomeFeedItem.Notification(group, index))
                                // Add ad after this notification if its index is in the random positions
                                if (index in adPositions) {
                                    add(HomeFeedItem.NativeAd("home-native-ad-after-${group.packageName}-$index"))
                                }
                            }
                        }
                    }
                }

                if (feedItems.isEmpty()) {
                    if (isSearchActive && searchQuery.isNotEmpty()) {
                        SearchEmptyStateMessage(context)
                    } else {
                        HomeEmptyState()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(
                            items = feedItems,
                            key = { _, item ->
                                when (item) {
                                    is HomeFeedItem.NativeAd -> item.id
                                    is HomeFeedItem.Notification -> "notification-${item.group.packageName}-${item.originalIndex}"
                                }
                            }
                        ) { _, item ->
                            when (item) {
                                is HomeFeedItem.NativeAd -> {
                                    HomeScreenNotificationGroupCardAd(
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                is HomeFeedItem.Notification -> {
                                    NotificationGroupCard(
                                        group = item.group,
                                        onGroupClick = {
                                            navToScreen.invoke(
                                                NotificationDetailRoute(
                                                    packageName = item.group.packageName,
                                                    appName = item.group.appName,
                                                    isFromNotification = true
                                                )
                                            )
                                        },
                                        onNotificationClick = { /* Open notification details */ },
                                        modifier = if (item.originalIndex == groupedNotifications.size - 1) {
                                            Modifier.padding(bottom = 200.dp)
                                        } else {
                                            Modifier
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

    if (showPermissionBottomSheet) {
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

private sealed interface HomeFeedItem {
    data class Notification(val group: NotificationGroup, val originalIndex: Int) : HomeFeedItem
    data class NativeAd(val id: String) : HomeFeedItem
}


