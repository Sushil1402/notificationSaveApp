package com.jetpackComposeTest1.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.size
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.jetpackComposeTest1.model.setting.SettingsData
import com.jetpackComposeTest1.ui.components.AppAlertDialog
import com.jetpackComposeTest1.ui.screens.dashboard.viewmodel.SettingsViewModel
import com.jetpackComposeTest1.ui.theme.JetpackComposeTest1Theme
import com.jetpackComposeTest1.ui.theme.main_appColor
import com.jetpackComposeTest1.ui.navigation.AppNavigationRoute
import com.jetpackComposeTest1.ui.navigation.AppSelectionScreenRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenView(
    navToScreen: (AppNavigationRoute) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    // Provide initial value to prevent NPE during initialization
    val initialSettings = remember {
        SettingsData(
            hasNotificationAccess = false,
            autoCleanup = false,
            retentionDays = 30,
            storageUsed = 0f,
            storagePercentage = 0f,
            darkMode = false,
            notificationSound = true
        )
    }
    
    val settings by viewModel.settings.collectAsState(initial = initialSettings)
    
    var hasNotificationAccess by remember { mutableStateOf(true) }
    val autoCleanup = settings.autoCleanup
    val retentionDays = settings.retentionDays
    val storageUsedMb = 45.6f
    val storagePercent = 65.2f
    var darkModeEnabled by remember { mutableStateOf(false) }
    var notificationSound by remember { mutableStateOf(true) }

    var showRetentionBottomSheet by remember { mutableStateOf(false) }
    var showClearAllDialog by remember { mutableStateOf(false) }
    var showAutoCleanupDialog by remember { mutableStateOf(false) }
    var selectedRetentionDays by remember { mutableStateOf(retentionDays) }
    
    val retentionOptions = listOf(30, 60, 90)

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
                    text = "Settings",
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
                    SectionCard(modifier = Modifier, title = "Notification Access") {
                        ListItem(
                            modifier = Modifier.clickable(role = Role.Button) { navToScreen(AppSelectionScreenRoute) },
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
                                    TextButton(onClick = { /* open notification access */ }) {
                                        Text("Enable")
                                    }
                                }
                            }
                        )
                    }
                }

                item {
                    SectionCard(modifier = Modifier,title = "Storage Management") {
                        SettingsSwitchItemContent(
                            icon = Icons.Filled.Delete,
                            title = "Auto Cleanup",
                            subtitle = {
                                if (autoCleanup) {
                                    val annotatedText = buildAnnotatedString {
                                        append("Delete notifications older than ")
                                        withStyle(style = SpanStyle(color = if (retentionDays == 30) Color(0xFF16A34A) else Color.Black)) {
                                            append("$retentionDays days")
                                        }
                                    }
                                    Text(annotatedText)
                                } else {
                                    Text("Automatically delete old notifications")
                                }
                            },
                            checked = autoCleanup,
                            onCheckedChange = { newValue ->
                                if (newValue && !autoCleanup) {
                                    // User is enabling auto cleanup - show retention period bottom sheet
                                    selectedRetentionDays = retentionDays
                                    showRetentionBottomSheet = true
                                } else {
                                    // User is disabling - allow directly
                                    viewModel.setAutoCleanupEnabled(newValue)
                                }
                            }
                        )
                    }
                }

                item {
                    SectionCard(modifier = Modifier,title = "Export & Backup") {
                        SettingsNavItem(
                            icon = Icons.Filled.Info,
                            title = "Export All Data",
                            subtitle = "Export all notifications to file",
                            onClick = { /* export */ }
                        )
                        Divider()
                        SettingsActionItem(
                            icon = Icons.Filled.Delete,
                            title = "Clear All Data",
                            subtitle = "Delete all saved notifications",
                            onClick = { showClearAllDialog = true }
                        )
                    }
                }

                item {
                    SectionCard(modifier = Modifier.padding(bottom = 200.dp),title = "App Preferences") {
                        SettingsSwitchItem(
                            icon = Icons.Filled.Info,
                            title = "Dark Mode",
                            subtitle = "Switch to dark theme",
                            checked = darkModeEnabled,
                            onCheckedChange = { darkModeEnabled = it }
                        )
                        Divider()
                        SettingsSwitchItem(
                            icon = Icons.Filled.Info,
                            title = "Notification Sound",
                            subtitle = "Play sound when saving notifications",
                            checked = notificationSound,
                            onCheckedChange = { notificationSound = it }
                        )
                    }
                }
            }

            if (showRetentionBottomSheet) {
                RetentionPeriodBottomSheet(
                    currentDays = selectedRetentionDays,
                    retentionOptions = retentionOptions,
                    onDismiss = { showRetentionBottomSheet = false },
                    onConfirm = { newDays ->
                        viewModel.setRetentionDays(newDays)
                        viewModel.setAutoCleanupEnabled(true)
                        showRetentionBottomSheet = false
                    }
                )
            }

            if (showClearAllDialog) {
                AlertDialog(
                    onDismissRequest = { showClearAllDialog = false },
                    title = { Text("Clear all data?") },
                    text = { Text("This will delete all saved notifications. This action cannot be undone.") },
                    confirmButton = {
                        TextButton(onClick = {
                            showClearAllDialog = false
                            // perform clear all
                        }) { Text("Delete") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showClearAllDialog = false }) { Text("Cancel") }
                    }
                )
            }

            if (showAutoCleanupDialog) {
                AppAlertDialog(
                    onDismissRequest = { showAutoCleanupDialog = false },
                    title = "Enable Auto Cleanup?",
                    text = "Auto cleanup will automatically delete notifications older than the retention period. This action cannot be undone.",
                    confirmButtonText = "Enable",
                    dismissButtonText = "Cancel",
                    confirmButton = {
                        viewModel.setAutoCleanupEnabled(true)
                        showAutoCleanupDialog = false
                    },
                    dismissButton = {
                        showAutoCleanupDialog = false
                    }
                )
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
                containerColor = Color.White // pure white
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
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    SettingsSwitchItemContent(
        icon = icon,
        title = title,
        subtitle = { Text(subtitle) },
        checked = checked,
        onCheckedChange = onCheckedChange
    )
}

@Composable
private fun SettingsSwitchItemContent(
    icon: ImageVector,
    title: String,
    subtitle: @Composable () -> Unit,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = Color.White),
        leadingContent = {
            Icon(icon, contentDescription = null, tint = Color.Black)
        },
        headlineContent = { Text(title) },
        supportingContent = subtitle,
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

@Composable
private fun SettingsNavItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailingText: String? = null,
    onClick: () -> Unit
) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = Color.White),
        modifier = Modifier.clickable(role = Role.Button) { onClick() },
        leadingContent = {
            Icon(icon, contentDescription = null,  tint = Color.Black)
        },
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (trailingText != null) {
                    Text(trailingText, modifier = Modifier.padding(end = 8.dp))
                }
                Icon(Icons.Filled.Info, contentDescription = null, tint = main_appColor)
            }
        }
    )
}

@Composable
private fun SettingsActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = Color.White),
        modifier = Modifier.clickable(role = Role.Button) { onClick() },
        leadingContent = {
            Icon(icon, contentDescription = null, tint = Color(0xFFDC2626))
        },
        headlineContent = { Text(title, color = Color(0xFFDC2626)) },
        supportingContent = { Text(subtitle) },
        trailingContent = {
            Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RetentionPeriodBottomSheet(
    currentDays: Int,
    retentionOptions: List<Int>,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var selectedDays by remember { mutableStateOf(currentDays) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Header
            Text(
                text = "Retention Period",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            Text(
                text = "Choose how long to keep notifications",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF666666),
                modifier = Modifier.padding(bottom = 20.dp)
            )
            
            // Retention period options
            retentionOptions.forEach { days ->
                val isSelected = selectedDays == days
                val isRecommended = days == 30
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable { selectedDays = days }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .background(
                                color = if (isSelected) main_appColor.copy(alpha = 0.08f) else Color(0xFFF5F5F5),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(vertical = 18.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "$days",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected && isRecommended) Color(0xFF16A34A) else if (isSelected) main_appColor else Color.Black
                                )
                                Text(
                                    text = "Days",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF666666)
                                )
                            }
                        }
                        
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Selected",
                                tint = if (isRecommended) Color(0xFF16A34A) else main_appColor
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Enable button
            Button(
                onClick = { onConfirm(selectedDays) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = main_appColor,
                    contentColor = Color.White
                )
            ) {
                Text(
                    "Enable Auto Cleanup",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}



@Preview
@Composable
fun SettingsScreenPreview() {
    JetpackComposeTest1Theme(darkTheme = false) {
        SettingsScreenView(navToScreen = {})
    }
}

@Preview
@Composable
fun SettingsScreenPreviewDark() {
    JetpackComposeTest1Theme(darkTheme = true) {
        SettingsScreenView(navToScreen = {})
    }
}
