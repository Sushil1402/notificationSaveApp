package com.notistorex.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notistorex.app.R
import com.notistorex.app.model.notification.NotificationGroup
import com.notistorex.app.model.notification.NotificationItem
import com.notistorex.app.ui.navigation.AllUnreadNotificationsRoute
import com.notistorex.app.ui.navigation.AppNavigationRoute
import com.notistorex.app.ui.theme.main_appColor
import com.notistorex.app.ui.theme.unreadIndicatorColor
import com.notistorex.app.ui.utils.PermissionState
import com.notistorex.app.ui.utils.toImageBitmap

@Composable
fun HomeHeader(
    navToScreen: (AppNavigationRoute) -> Unit,
    unreadCount: Int,
    onSearchClick: () -> Unit,
) {
    val title = stringResource(R.string.homeScreenTitle)
    val unreadText = stringResource(R.string.unread_notification_holder, unreadCount)
    val showAllUnreadContentDescription =
        stringResource(R.string.home_all_unread_content_description)
    val searchContentDescription = stringResource(R.string.home_search_content_description)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = unreadText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
        }
        Box {
            IconButton(onClick = { navToScreen.invoke(AllUnreadNotificationsRoute) }) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = showAllUnreadContentDescription,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
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
                contentDescription = searchContentDescription,
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun NotificationGroupCard(
    group: NotificationGroup,
    onGroupClick: () -> Unit,
    onNotificationClick: (NotificationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        onClick = onGroupClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = stringResource(
                        R.string.notification_count,
                        group.notificationCount
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            group.recentNotifications.take(5).forEach { notification ->
                NotificationItemRow(
                    notification = notification,
                    onClick = { onNotificationClick(notification) }
                )
                if (notification != group.recentNotifications.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            if (group.notificationCount > 5) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onGroupClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(
                            R.string.view_more_notification_holder,
                            group.notificationCount - 5
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

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = notification.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Medium,
                maxLines = 1,
                color = if (notification.isRead) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            if (notification.message.isNotEmpty()) {
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }

        Text(
            text = notification.timeAgo,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PermissionStatusBanner(
    permissionState: PermissionState,
    onPermissionClick: () -> Unit
) {
    val bannerColor = if (permissionState.allGranted) Color(0xFF4CAF50) else Color(0xFFFF9800)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = bannerColor
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
                    text = if (permissionState.allGranted) stringResource(R.string.notification_ready)
                    else stringResource(R.string.permission_required),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )

                Text(
                    text = if (permissionState.allGranted)
                        stringResource(R.string.your_notifications_are_bing_saved_automatically)
                    else
                        stringResource(R.string.tap_to_enable_notification_access),
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
                    Text(stringResource(R.string.enable), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun HomeEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Image(
            painter = painterResource(id = R.drawable.no_notification),
            contentDescription = stringResource(R.string.no_notifications_found),
            modifier = Modifier.size(54.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.empty_notification_description),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.empty_notification_small_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

