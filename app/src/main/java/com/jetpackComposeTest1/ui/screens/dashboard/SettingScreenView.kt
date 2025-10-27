package com.jetpackComposeTest1.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import com.jetpackComposeTest1.model.setting.SettingsData
import com.jetpackComposeTest1.ui.theme.main_appColor

@Composable
fun SettingsScreenView() {
    // Mock data for now - replace with actual ViewModel later
    val settings = remember {
        SettingsData(
            hasNotificationAccess = true,
            autoCleanup = true,
            retentionDays = 30,
            storageUsed = 45.6f,
            storagePercentage = 65.2f,
            darkMode = false,
            notificationSound = true
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
        Text(
            text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            SettingsSection("Notification Access") {
                SettingsItem(
                    title = "Notification Access",
                    subtitle = "Allow app to read notifications",
                    icon = Icons.Default.Notifications,
                    trailing = {
                        if (settings.hasNotificationAccess) {
                            Icon(Icons.Default.Check, contentDescription = "Enabled", tint = Color.Green)
                        } else {
                            Button(onClick = { /* Request notification access */ }) {
                                Text("Enable")
                            }
                        }
                    }
                )
            }
        }
        
        item {
            SettingsSection("Storage Management") {
                SettingsItem(
                    title = "Auto Cleanup",
                    subtitle = "Automatically delete old notifications",
                    icon = Icons.Default.Delete,
                    trailing = {
                        Switch(
                            checked = settings.autoCleanup,
                            onCheckedChange = { /* Toggle auto cleanup */ }
                        )
                    }
                )
                
                SettingsItem(
                    title = "Retention Period",
                    subtitle = "Keep notifications for ${settings.retentionDays} days",
                    icon = Icons.Default.Info,
                    trailing = {
                        Text(settings.retentionDays.toString())
                    },
                    onClick = { /* Show retention dialog */ }
                )
                
                SettingsItem(
                    title = "Storage Used",
                    subtitle = "${settings.storageUsed} MB used",
                    icon = Icons.Default.Info,
                    trailing = {
                        Text("${settings.storagePercentage}%")
                    }
                )
            }
        }
        
        item {
            SettingsSection("Export & Backup") {
                SettingsItem(
                    title = "Export All Data",
                    subtitle = "Export all notifications to file",
                    icon = Icons.Default.Info,
                    onClick = { /* Export all data */ }
                )
                
                SettingsItem(
                    title = "Import Data",
                    subtitle = "Import notifications from file",
                    icon = Icons.Default.Info,
                    onClick = { /* Import data */ }
                )
                
                SettingsItem(
                    title = "Clear All Data",
                    subtitle = "Delete all saved notifications",
                    icon = Icons.Default.Delete,
                    onClick = { /* Show clear data dialog */ }
                )
            }
        }
        
        item {
            SettingsSection("App Preferences") {
                SettingsItem(
                    title = "Dark Mode",
                    subtitle = "Switch to dark theme",
                    icon = Icons.Default.Info,
                    trailing = {
                        Switch(
                            checked = settings.darkMode,
                            onCheckedChange = { /* Toggle dark mode */ }
                        )
                    }
                )
                
                SettingsItem(
                    title = "Notification Sound",
                    subtitle = "Play sound when saving notifications",
                    icon = Icons.Default.Info,
                    trailing = {
                        Switch(
                            checked = settings.notificationSound,
                            onCheckedChange = { /* Toggle notification sound */ }
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = main_appColor,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        content()
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick?.invoke() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = main_appColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            
            trailing?.invoke()
        }
    }
}



@Preview
@Composable
fun SettingsScreenPreview() {
    SettingsScreenView()
}
