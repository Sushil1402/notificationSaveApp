package com.jetpackComposeTest1.ui.screens.dashboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jetpackComposeTest1.R
import com.jetpackComposeTest1.ui.components.AppAlertDialog
import com.jetpackComposeTest1.ui.components.MenuOption
import com.jetpackComposeTest1.ui.components.MoreOptionsMenu
import com.jetpackComposeTest1.ui.screens.dashboard.viewmodel.NotificationDetailViewViewModel
import com.jetpackComposeTest1.ui.theme.main_appColor
import com.jetpackComposeTest1.ui.utils.NotificationUtils
import com.jetpackComposeTest1.ui.utils.toImageBitmap
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDetailViewScreen(
    notificationId: String,
    onNavigateBack: () -> Unit,
    onNotificationDeleted: () -> Unit = {},
    viewModel: NotificationDetailViewViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val notification by viewModel.notification.collectAsState()
    val appIcon by viewModel.appIcon.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    // Track if notification was loaded at least once
    var wasLoaded by remember { mutableStateOf(false) }

    // Load notification when screen is first displayed
    LaunchedEffect(notificationId) {
        viewModel.loadNotification(context, notificationId)
    }

    // Track when notification is successfully loaded
    LaunchedEffect(notification, isLoading) {
        if (notification != null && !isLoading) {
            wasLoaded = true
        }
    }

    // Handle navigation back if notification is deleted
    LaunchedEffect(notification, isLoading, error, wasLoaded) {
        // Only navigate back if:
        // 1. Notification was loaded at least once (to avoid navigating on initial load)
        // 2. Notification is now null (deleted)
        // 3. Not currently loading
        // 4. No error occurred
        if (wasLoaded && notification == null && !isLoading && error == null) {
            // Notification was deleted, navigate back
            onNotificationDeleted()
        }
    }

    // Function to open the app
    fun openApp(packageName: String) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.app_not_found),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                context.getString(R.string.failed_to_open_app),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = context.getString(R.string.notification_details),
                        color = Color.White
                    )
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
                    notification?.let { notif ->
                        MoreOptionsMenu(
                            options = listOf(
                                MenuOption(
                                    title = if (notif.isRead) {
                                        context.getString(R.string.mark_as_unread)
                                    } else {
                                        context.getString(R.string.mark_as_read)
                                    },
                                    icon = Icons.Default.CheckCircle,
                                    iconTint = main_appColor,
                                    onClick = {
                                        viewModel.toggleReadStatus(notif.id.toString())
                                        showMenu = false
                                    }
                                ),
                                MenuOption(
                                    title = context.getString(R.string.copy_text),
                                    icon = Icons.Default.ContentCopy,
                                    iconTint = main_appColor,
                                    onClick = {
                                        val textToCopy = "${notif.title}\n\n${notif.text}"
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Notification", textToCopy)
                                        clipboard.setPrimaryClip(clip)
                                        showMenu = false
                                    }
                                ),
                                MenuOption(
                                    title = context.getString(R.string.delete),
                                    icon = Icons.Default.Delete,
                                    iconTint = Color.Red,
                                    onClick = {
                                        showDeleteDialog = true
                                        showMenu = false
                                    }
                                )
                            ),
                            iconTint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = main_appColor
                )
            )
        },
        floatingActionButton = {
            notification?.let { notif ->
                FloatingActionButton(
                    onClick = { openApp(notif.packageName) },
                    containerColor = main_appColor,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = context.getString(R.string.open_app),
                        tint = Color.White
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = main_appColor)
                    }
                }
                error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onNavigateBack,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = main_appColor
                            )
                        ) {
                            Text("Go Back")
                        }
                    }
                }
                notification != null -> {
                    NotificationDetailContent(
                        notification = notification!!,
                        appIcon = appIcon,
                        formatTimestamp = { viewModel.formatTimestamp(it) },
                        formatTimeAgo = { viewModel.formatTimeAgo(it) }
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog && notification != null) {
        AppAlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = context.getString(R.string.delete_notification),
            text = context.getString(R.string.delete_notification_confirmation),
            confirmButtonText = context.getString(R.string.delete),
            dismissButtonText = context.getString(R.string.cancel),
            confirmButton = {
                notification?.let {
                    viewModel.deleteNotification(it.id.toString())
                }
                showDeleteDialog = false
            },
            dismissButton = {
                showDeleteDialog = false
            }
        )
    }
}

@Composable
private fun NotificationDetailContent(
    notification: com.jetpackComposeTest1.db.NotificationEntity,
    appIcon: android.graphics.drawable.Drawable?,
    formatTimestamp: (Long) -> String,
    formatTimeAgo: (Long) -> String
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    // Convert base64 image to ImageBitmap
    val bigPictureBitmap = remember(notification.largeIcon) {
        notification.largeIcon?.let { base64String ->
            NotificationUtils.base64ToBitmap(base64String)?.asImageBitmap()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // App Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App Icon
                appIcon?.let { drawable ->
                    Image(
                        bitmap = drawable.toImageBitmap(),
                        contentDescription = notification.appName,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } ?: Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = notification.appName,
                    tint = main_appColor,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = notification.appName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = notification.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Read/Unread indicator
                Surface(
                    shape = CircleShape,
                    color = if (notification.isRead) MaterialTheme.colorScheme.onSurfaceVariant else main_appColor,
                    modifier = Modifier.size(12.dp)
                ) {}
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Title Section
        Text(
            text = context.getString(R.string.title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = notification.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Big Picture Image Section
        bigPictureBitmap?.let { imageBitmap ->
            var showFullImage by remember { mutableStateOf(false) }
            
            Text(
                text = "Image",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showFullImage = true },
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "Big picture - Tap to view full size",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            // Full screen image dialog
            if (showFullImage) {
                FullScreenImageDialog(
                    imageBitmap = imageBitmap,
                    base64String = notification.largeIcon,
                    onDismiss = { showFullImage = false }
                )
            }
        }

        // Message/Content Section
        if (notification.text.isNotEmpty()) {
            Text(
                text = context.getString(R.string.message),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = notification.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(16.dp),
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5
                )
            }
        }

        // Big Text (if available)
        notification.bigText?.let { bigText ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = context.getString(R.string.full_content),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = bigText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(16.dp),
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5
                )
            }
        }

        // Sub Text (if available)
        notification.subText?.let { subText ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = context.getString(R.string.sub_text),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Metadata Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = context.getString(R.string.details),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Time information
                MetadataRow(
                    label = context.getString(R.string.received),
                    value = formatTimestamp(notification.timestamp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                MetadataRow(
                    label = context.getString(R.string.time_ago),
                    value = formatTimeAgo(notification.timestamp)
                )

                // Additional metadata
                if (notification.channelName != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    MetadataRow(
                        label = context.getString(R.string.channel),
                        value = notification.channelName
                    )
                }

                if (notification.category != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    MetadataRow(
                        label = context.getString(R.string.category),
                        value = notification.category
                    )
                }

                if (notification.priority > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    MetadataRow(
                        label = context.getString(R.string.priority),
                        value = notification.priority.toString()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun FullScreenImageDialog(
    imageBitmap: androidx.compose.ui.graphics.ImageBitmap,
    base64String: String?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
        ) {
            // Top bar with Close and Save buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Save button
                IconButton(
                    onClick = {
                        if (base64String != null && !isSaving) {
                            isSaving = true
                            scope.launch {
                                val fileName = "notification_image_${System.currentTimeMillis()}.jpg"
                                NotificationUtils.saveBase64ImageToGallery(context, base64String, fileName)
                                isSaving = false
                            }
                        }
                    },
                    modifier = Modifier
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                        .size(48.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Save image",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                // Close button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // Image centered - clickable to dismiss
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onDismiss)
                    .padding(horizontal = 16.dp, vertical = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "Full size image - Tap to close",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 800.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

