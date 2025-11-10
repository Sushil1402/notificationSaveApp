package com.jetpackComposeTest1.ui.screens.dashboard

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.size
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.jetpackComposeTest1.R
import com.jetpackComposeTest1.model.setting.SettingsData
import com.jetpackComposeTest1.ui.components.AppAlertDialog
import com.jetpackComposeTest1.ui.screens.dashboard.viewmodel.SettingsViewModel
import com.jetpackComposeTest1.ui.theme.JetpackComposeTest1Theme
import com.jetpackComposeTest1.ui.theme.main_appColor
import com.jetpackComposeTest1.ui.navigation.AppNavigationRoute
import com.jetpackComposeTest1.ui.navigation.PasscodeScreenRoute
import com.jetpackComposeTest1.data.local.preferences.AppPreferences
import com.jetpackComposeTest1.ui.utils.Utils.shareExcelFile
import com.jetpackComposeTest1.model.setting.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenView(
    navToScreen: (AppNavigationRoute) -> Unit,
    onNavigateBack: (() -> Unit),
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    
    // Provide initial value to prevent NPE during initialization
    val initialSettings = remember {
        SettingsData(
            hasNotificationAccess = false,
            autoCleanup = false,
            retentionDays = 30,
            storageUsed = 0f,
            storagePercentage = 0f,
            themeMode = ThemeMode.SYSTEM,
            notificationSound = true
        )
    }
    
    val settings by viewModel.settings.collectAsState(initial = initialSettings)
    val exportState by viewModel.exportState.collectAsState()
    
    val autoCleanup = settings.autoCleanup
    val retentionDays = settings.retentionDays
    val storageUsedMb = 45.6f
    val storagePercent = 65.2f
    val themeMode = settings.themeMode
    
    // Load passcode state
    val appPreferences = remember { AppPreferences(context) }
    var passcodeEnabled by remember { mutableStateOf(appPreferences.isPasscodeEnabled()) }
    
    // Refresh passcode state when screen becomes visible
    LaunchedEffect(Unit) {
        passcodeEnabled = appPreferences.isPasscodeEnabled()
    }
    
    // Privacy settings
    var lockscreenWidgetEnabled by remember { mutableStateOf(true) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }

    var showRetentionBottomSheet by remember { mutableStateOf(false) }
    var showClearAllDialog by remember { mutableStateOf(false) }
    var showAutoCleanupDialog by remember { mutableStateOf(false) }
    var selectedRetentionDays by remember { mutableStateOf(retentionDays) }
    
    val retentionOptions = listOf(30, 60, 90)

    // Handle export state changes
    LaunchedEffect(exportState) {
        when (exportState) {
            is SettingsViewModel.ExportState.Success -> {
                // Share the file using common utility function
                shareExcelFile(context, (exportState as SettingsViewModel.ExportState.Success).fileUri)
                viewModel.resetExportState()
            }
            is SettingsViewModel.ExportState.Error -> {
                // Error is shown in dialog
            }
            else -> {}
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = context.getString(R.string.settings),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack.invoke() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = context.getString(R.string.back),
                            tint = Color.White
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = main_appColor
                )
            )
        }
    ){  paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(main_appColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        SectionCard(modifier = Modifier.padding(top = 20.dp), title = context.getString(R.string.storage_management)) {
                            SettingsSwitchItemContent(
                                icon = Icons.Filled.CleaningServices,
                                title = context.getString(R.string.storage_auto_cleanup),
                                subtitle = {
                                    if (autoCleanup) {
                                        val annotatedText = buildAnnotatedString {
                                            append(context.getString(R.string.delete_notifications_old_than))
                                            withStyle(style = SpanStyle(color = if (retentionDays == 30) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurface)) {
                                                append(context.getString(R.string.days_count,"$retentionDays"))
                                            }
                                        }
                                        Text(annotatedText)
                                    } else {
                                        Text(context.getString(R.string.automatically_delete_old_notifications))
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
                        SectionCard(modifier = Modifier, title = context.getString(R.string.appearance)) {
                            ThemeModeSelector(
                                context = context,
                                selectedMode = themeMode,
                                onModeSelected = viewModel::setThemeMode
                            )
                        }
                    }

                    item {
                        SectionCard(modifier = Modifier,title = context.getString(R.string.export_and_backup)) {
                            SettingsNavItem(
                                icon = Icons.Filled.FileUpload,
                                title = context.getString(R.string.export_all_data),
                                subtitle = context.getString(R.string.export_all_notifications_to_excel_file),
                                onClick = {
                                    viewModel.exportAllData(context)
                                },
                                trailingContent = {
                                    if (exportState is SettingsViewModel.ExportState.Exporting) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = main_appColor,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(Icons.Filled.Info, contentDescription = null, tint = main_appColor)
                                    }
                                }
                            )
                            HorizontalDivider()
                            SettingsActionItem(
                                icon = Icons.Filled.Delete,
                                title = context.getString(R.string.clear_all_data),
                                subtitle = context.getString(R.string.clear_all_data_subtitle),
                                onClick = { showClearAllDialog = true }
                            )
                        }
                    }

                    item {
                        SectionCard(modifier = Modifier, title = context.getString(R.string.privacy)) {
                            SettingsSwitchItem(
                                icon = Icons.Filled.Password,
                                title = context.getString(R.string.passcode),
                                subtitle = context.getString(R.string.use_passcode_to_unlock,"${context.getString(R.string.app_name)}"),
                                checked = passcodeEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled) {
                                        // Clear disable intent and navigate to setup
                                        appPreferences.setPasscodeDisableIntent(false)
                                        navToScreen(PasscodeScreenRoute)
                                    } else {
                                        // Set disable intent and navigate to verification
                                        val savedPasscode = appPreferences.getPasscode()
                                        if (savedPasscode != null) {
                                            appPreferences.setPasscodeDisableIntent(true)
                                            navToScreen(PasscodeScreenRoute)
                                        } else {
                                            // No passcode set, just disable
                                            appPreferences.setPasscodeEnabled(false)
                                            passcodeEnabled = false
                                        }
                                    }
                                }
                            )
                            HorizontalDivider()
                            SettingsSwitchItem(
                                icon = Icons.Filled.Info,
                                title = context.getString(R.string.lockScreen_widget),
                                subtitle = context.getString(R.string.show_the_widget_on_lockscreen),
                                checked = lockscreenWidgetEnabled,
                                onCheckedChange = { lockscreenWidgetEnabled = it }
                            )
                            HorizontalDivider()
                            SettingsNavItem(
                                icon = Icons.Filled.Policy,
                                title = context.getString(R.string.privacy_policy),
                                subtitle = context.getString(R.string.how_data_is_stored),
                                onClick = { showPrivacyPolicyDialog = true }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.padding(bottom = 200.dp))
                    }
                }

                if (showRetentionBottomSheet) {
                    RetentionPeriodBottomSheet(
                        context = context,
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
                    ClearAllDataConfirmationDialog(
                        context = context,
                        onDismiss = { showClearAllDialog = false },
                        onConfirm = {
                            viewModel.clearAllData()
                            showClearAllDialog = false
                        }
                    )
                }

                if (showAutoCleanupDialog) {
                    AppAlertDialog(
                        onDismissRequest = { showAutoCleanupDialog = false },
                        title = context.getString(R.string.enable_auto_cleanup),
                        text = context.getString(R.string.auto_cleanup_will_automatically),
                        confirmButtonText = context.getString(R.string.enable),
                        dismissButtonText = context.getString(R.string.cancel),
                        confirmButton = {
                            viewModel.setAutoCleanupEnabled(true)
                            showAutoCleanupDialog = false
                        },
                        dismissButton = {
                            showAutoCleanupDialog = false
                        }
                    )
                }

                // Export error dialog
                if (exportState is SettingsViewModel.ExportState.Error) {
                    AlertDialog(
                        onDismissRequest = { viewModel.resetExportState() },
                        title = { Text(context.getString(R.string.export_failed)) },
                        text = { Text((exportState as SettingsViewModel.ExportState.Error).message) },
                        confirmButton = {
                            TextButton(onClick = { viewModel.resetExportState() }) {
                                Text(context.getString(R.string.ok))
                            }
                        }
                    )
                }

                // Privacy Policy dialog
                if (showPrivacyPolicyDialog) {
                    AlertDialog(
                        onDismissRequest = { showPrivacyPolicyDialog = false },
                        title = {
                            Text(
                                context.getString(R.string.privacy_policy),
                                fontWeight = FontWeight.Bold
                            )
                        },
                        text = {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    context.getString(R.string.how_data_is_stored),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(context.getString(R.string.all_notification_data_store_locally_desc))
                                Text(
                                    context.getString(R.string.data_security),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(context.getString(R.string.all_notification_are_encrypted_desc))
                                Text(
                                    context.getString(R.string.data_usage),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(context.getString(R.string.notification_data_is_used_solely_for_displaying_desc))
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = { showPrivacyPolicyDialog = false }
                            ) {
                                Text(context.getString(R.string.ok), color = main_appColor)
                            }
                        }
                    )
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = main_appColor,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
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
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
        leadingContent = {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
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
                    checkedBorderColor = Color.Transparent,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    uncheckedBorderColor = Color.Transparent
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
    onClick: () -> Unit,
    trailingContent: @Composable (() -> Unit)? = null
) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.clickable(role = Role.Button) { onClick() },
        leadingContent = {
            Icon(icon, contentDescription = null,  tint = MaterialTheme.colorScheme.onSurface)
        },
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (trailingText != null) {
                    Text(trailingText, modifier = Modifier.padding(end = 8.dp))
                }
                trailingContent?.invoke() ?: Icon(Icons.Filled.Info, contentDescription = null, tint = main_appColor)
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
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
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
    context: Context,
    currentDays: Int,
    retentionOptions: List<Int>,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var selectedDays by remember { mutableStateOf(currentDays) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Header
            Text(
                text =context.getString(R.string.retention_period),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            Text(
                text = context.getString(R.string.choose_how_long_to_keep_notification),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                color = if (isSelected) main_appColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
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
                                    color = when {
                                        isSelected && isRecommended -> Color(0xFF16A34A)
                                        isSelected -> main_appColor
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                Text(
                                    text = context.getString(R.string.days),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    context.getString(R.string.enable_auto_cleanup_str),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ThemeModeSelector(
    context: Context,
    selectedMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = context.getString(R.string.theme),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = context.getString(R.string.choose_how_the_app_appearance),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        SingleChoiceSegmentedButtonRow {
            val options = listOf(
                ThemeMode.SYSTEM to Pair(Icons.Filled.Settings, context.getString(R.string.system)),
                ThemeMode.LIGHT to Pair(Icons.Filled.LightMode, context.getString(R.string.light)),
                ThemeMode.DARK to Pair(Icons.Filled.DarkMode, context.getString(R.string.dark)),
            )
            options.forEachIndexed { index, option ->
                val (mode, iconAndLabel) = option
                val (icon, label) = iconAndLabel
                SegmentedButton(
                    selected = selectedMode == mode,
                    onClick = { onModeSelected(mode) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = options.size
                    ),
                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = label
                        )
                    },
                    label = { Text(label) }
                )
            }
        }
    }
}



@Preview
@Composable
fun SettingsScreenPreview() {
    JetpackComposeTest1Theme(darkTheme = false) {
        SettingsScreenView(navToScreen = {}, onNavigateBack = {})
    }
}

@Preview
@Composable
fun SettingsScreenPreviewDark() {
    JetpackComposeTest1Theme(darkTheme = true) {
        SettingsScreenView(navToScreen = {}, onNavigateBack = {})
    }
}

@Composable
private fun ClearAllDataConfirmationDialog(
    context: Context,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var confirmationText by remember { mutableStateOf(TextFieldValue("")) }
    val requiredText = context.getString(R.string.delete_txt_upper)
    val isConfirmEnabled = confirmationText.text.trim().equals(requiredText, ignoreCase = false)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = context.getString(R.string.clear_all_data_icon_txt),
                fontWeight = FontWeight.Bold,
                color = Color(0xFFDC2626)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = context.getString(R.string.clear_all_data_desc),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(

                    text = context.getString(R.string.confirm_please_type, requiredText),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                OutlinedTextField(
                    value = confirmationText,
                    onValueChange = { confirmationText = it },
                    label = { Text(context.getString(R.string.type_text, requiredText)) },
                    placeholder = { Text(context.getString(R.string.enter_text, requiredText)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isConfirmEnabled) Color(0xFF16A34A) else Color(0xFFDC2626),
                        unfocusedBorderColor = if (isConfirmEnabled) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedLabelColor = if (isConfirmEnabled) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (isConfirmEnabled) {
                                onConfirm()
                            }
                        }
                    )
                )
                
                if (confirmationText.text.isNotEmpty() && !isConfirmEnabled) {
                    Text(
                        text = context.getString(R.string.text_does_not_match,requiredText),
                        color = Color(0xFFDC2626),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = isConfirmEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDC2626),
                    contentColor = Color.White,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            ) {
                Text(context.getString(R.string.delete_all_data))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text( context.getString(R.string.cancel), color = main_appColor)
            }
        }
    )
}
