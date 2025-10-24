package com.jetpackComposeTest1.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingScreenView(
    modifier: Modifier = Modifier,
    onBlockNotificationsClick: () -> Unit = {},
    onExportSettingsClick: () -> Unit = {},
    onClearDataClick: () -> Unit = {}
) {
    var enableTracking by remember { mutableStateOf(true) }
    var darkMode by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Enable Tracking
        SettingSwitchItem(
            title = "Enable Tracking",
            subtitle = "Collect notification data",
            checked = enableTracking,
            onCheckedChange = { enableTracking = it }
        )

        Divider()

        // Dark Mode
        SettingSwitchItem(
            title = "Dark Mode",
            subtitle = "Switch to dark theme",
            checked = darkMode,
            onCheckedChange = { darkMode = it }
        )

        Divider()

        // Block Notifications
        SettingClickableItem(
            title = "Block Notifications",
            subtitle = "Manage per app",
            onClick = onBlockNotificationsClick
        )

        Divider()

        // Export Settings
        SettingClickableItem(
            title = "Export Settings",
            subtitle = "Choose format and location",
            onClick = onExportSettingsClick
        )

        Divider()

        // Clear Data
        SettingClickableItem(
            title = "Clear Data",
            subtitle = "Remove all stored notifications",
            onClick = onClearDataClick
        )
    }
}

@Composable
fun SettingSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingClickableItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp)
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
