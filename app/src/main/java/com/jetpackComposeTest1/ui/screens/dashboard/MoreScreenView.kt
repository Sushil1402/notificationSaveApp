package com.jetpackComposeTest1.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.jetpackComposeTest1.ui.navigation.AppNavigationRoute
import com.jetpackComposeTest1.ui.navigation.AppSelectionScreenRoute
import com.jetpackComposeTest1.ui.navigation.SettingScreenRoute
import com.jetpackComposeTest1.ui.theme.JetpackComposeTest1Theme
import com.jetpackComposeTest1.ui.theme.main_appColor
import com.jetpackComposeTest1.ui.utils.PermissionChecker
import androidx.compose.runtime.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreenView(
    navToScreen: (AppNavigationRoute) -> Unit
) {
    val context = LocalContext.current
    var hasNotificationAccess by remember { 
        mutableStateOf(PermissionChecker.isNotificationListenerPermissionGranted(context)) 
    }
    var darkModeEnabled by remember { mutableStateOf(false) }
    var notificationSound by remember { mutableStateOf(true) }
    
    // Re-check notification access when screen is composed
    LaunchedEffect(Unit) {
        hasNotificationAccess = PermissionChecker.isNotificationListenerPermissionGranted(context)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(main_appColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 10.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "More",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    SectionCard(modifier = Modifier.padding(top = 20.dp), title = "Notification Access") {
                        MoreNotificationAccessItem(
                            hasNotificationAccess = hasNotificationAccess,
                            onClick = { navToScreen(AppSelectionScreenRoute) }
                        )
                    }
                }

                item {
                    SectionCard(modifier = Modifier, title = "App Preferences") {
                        MoreSwitchItem(
                            icon = Icons.Filled.Info,
                            title = "Dark Mode",
                            subtitle = "Switch to dark theme",
                            checked = darkModeEnabled,
                            onCheckedChange = { darkModeEnabled = it }
                        )
                    }
                }

                item {
                    SectionCard(modifier = Modifier, title = "Options") {
                        MoreNavItem(
                            icon = Icons.Filled.Settings,
                            title = "Settings",
                            subtitle = "App settings and preferences",
                            onClick = { navToScreen(SettingScreenRoute) }
                        )
                        Divider()
                        MoreNavItem(
                            icon = Icons.Filled.Info,
                            title = "About",
                            subtitle = "App information and version",
                            onClick = { /* Navigate to About */ }
                        )
                        Divider()
                        MoreNavItem(
                            icon = Icons.Filled.Info,
                            title = "Help & Support",
                            subtitle = "Get help and contact support",
                            onClick = { /* Navigate to Help */ }
                        )
                    }
                }



                item {
                    SectionCard(modifier = Modifier.padding(bottom = 200.dp), title = "Share") {
                        MoreNavItem(
                            icon = Icons.Filled.Share,
                            title = "Share App",
                            subtitle = "Share this app with friends",
                            onClick = { /* Share app */ }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    modifier: Modifier,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = main_appColor,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun MoreNavItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = Color.White),
        modifier = Modifier.clickable(role = Role.Button) { onClick() },
        leadingContent = {
            Icon(icon, contentDescription = null, tint = Color.Black)
        },
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        trailingContent = {
            Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = main_appColor)
        }
    )
}

@Composable
private fun MoreNotificationAccessItem(
    hasNotificationAccess: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable(role = Role.Button) { onClick() },
        colors = ListItemDefaults.colors(containerColor = Color.White),
        leadingContent = {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = "Notification access",
                tint = Color.Black
            )
        },
        headlineContent = { Text("Notification Access") },
        supportingContent = { Text("Allow app to read notifications") },
        trailingContent = {
            if (hasNotificationAccess) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Enabled",
                    tint = Color(0xFF16A34A)
                )
            } else {
                TextButton(onClick = onClick) {
                    Text("Enable")
                }
            }
        }
    )
}

@Composable
private fun MoreSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = Color.White),
        leadingContent = {
            Icon(icon, contentDescription = null, tint = Color.Black)
        },
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = main_appColor,
                    checkedTrackColor = main_appColor.copy(alpha = 0.35f),
                    checkedBorderColor = Color.White,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    uncheckedBorderColor = Color.White
                )
            )
        }
    )
}

@Preview
@Composable
fun MoreScreenPreview() {
    JetpackComposeTest1Theme(darkTheme = false) {
        MoreScreenView(navToScreen = {})
    }
}

@Preview
@Composable
fun MoreScreenPreviewDark() {
    JetpackComposeTest1Theme(darkTheme = true) {
        MoreScreenView(navToScreen = {})
    }
}

