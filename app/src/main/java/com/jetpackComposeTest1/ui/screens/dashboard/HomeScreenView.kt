package com.jetpackComposeTest1.ui.screens.dashboard

import android.app.Activity
import android.content.Intent
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.jetpackComposeTest1.R
import com.jetpackComposeTest1.services.MyNotificationListener
import com.jetpackComposeTest1.services.NotificationForegroundService
import com.jetpackComposeTest1.ui.components.HomeEmptyState
import com.jetpackComposeTest1.ui.components.HomeHeader
import com.jetpackComposeTest1.ui.components.NotificationGroupCard
import com.jetpackComposeTest1.ui.components.PermissionBottomSheet
import com.jetpackComposeTest1.ui.components.PermissionStatusBanner
import com.jetpackComposeTest1.ui.components.SearchEmptyStateMessage
import com.jetpackComposeTest1.ui.components.SearchToolBar
import com.jetpackComposeTest1.ui.navigation.AppNavigationRoute
import com.jetpackComposeTest1.ui.navigation.AppSelectionScreenRoute
import com.jetpackComposeTest1.ui.navigation.NotificationDetailRoute
import com.jetpackComposeTest1.ui.screens.dashboard.viewmodel.NotificationHomeViewModel
import com.jetpackComposeTest1.ui.theme.main_appColor
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
                if (groupedNotifications.isEmpty()) {
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
                        itemsIndexed(groupedNotifications) { index, group ->
                            NotificationGroupCard(
                                group = group,
                                onGroupClick = {
                                    navToScreen.invoke(
                                        NotificationDetailRoute(
                                            packageName = group.packageName,
                                            appName = group.appName,
                                            isFromNotification = true
                                        )
                                    )
                                },
                                onNotificationClick = { /* Open notification details */ },
                                modifier = if (index == groupedNotifications.size - 1) {
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


