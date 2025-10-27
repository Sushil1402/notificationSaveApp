package com.jetpackComposeTest1.ui.components

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetpackComposeTest1.ui.utils.PermissionChecker
import com.jetpackComposeTest1.ui.theme.main_appColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onPermissionGranted: () -> Unit
) {
    val context = LocalContext.current
    
    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            containerColor = Color.White,
            contentColor = Color.Black
        ) {
            PermissionBottomSheetContent(
                context = context,
                onDismiss = onDismiss,
                onPermissionGranted = onPermissionGranted
            )
        }
    }
}

@Composable
private fun PermissionBottomSheetContent(
    context: Context,
    onDismiss: () -> Unit,
    onPermissionGranted: () -> Unit
) {
    var currentStep by remember { mutableStateOf(0) }
    val steps = getPermissionSteps(context)
    
    // Initialize step based on current permissions
    LaunchedEffect(Unit) {
        val isNotificationListenerGranted = PermissionChecker.isNotificationListenerPermissionGranted(context)
        val isNotificationGranted = PermissionChecker.isNotificationPermissionGranted(context)
        
        when {
            !isNotificationListenerGranted -> currentStep = 0 // Step 1: Enable notification access
            !isNotificationGranted -> currentStep = 1 // Step 2: Allow app notifications  
            else -> currentStep = 2 // Step 3: Verify setup
        }
    }
    
    // Re-check permissions when user returns from settings
    LaunchedEffect(Unit) {
        // This will run when the bottom sheet is first shown
        // We'll add a way to re-check when user returns
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Notification Access Required",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.Gray
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Progress indicator
        LinearProgressIndicator(
            progress = { (currentStep + 1).toFloat() / steps.size },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = main_appColor,
            trackColor = Color.LightGray
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Current step content
        val currentStepData = steps[currentStep]
        
        // Step icon and title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = currentStepData.icon,
                contentDescription = null,
                tint = main_appColor,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = currentStepData.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Step description
        Text(
            text = currentStepData.description,
            fontSize = 14.sp,
            color = Color.Gray,
            lineHeight = 20.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Step instructions
        currentStepData.instructions.forEach { instruction ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "• ",
                    fontSize = 14.sp,
                    color = main_appColor,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = instruction,
                    fontSize = 14.sp,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Check Again button for manual permission checking
        if (currentStep < 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(
                    onClick = {
                        // Re-check permissions and advance step if granted
                        val isNotificationListenerGranted = PermissionChecker.isNotificationListenerPermissionGranted(context)
                        val isNotificationGranted = PermissionChecker.isNotificationPermissionGranted(context)
                        
                        when (currentStep) {
                            0 -> {
                                // Step 1: Check if notification listener is granted
                                if (isNotificationListenerGranted) {
                                    currentStep = 1 // Move to step 2
                                }
                            }
                            1 -> {
                                // Step 2: Check if app notifications are granted
                                if (isNotificationGranted) {
                                    currentStep = 2 // Move to step 3
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = main_appColor
                    )
                ) {
                    Text("Check Again")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (currentStep > 0) {
                OutlinedButton(
                    onClick = { currentStep-- },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Previous")
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
            
             Button(
                 onClick = {
                     if (currentStepData.action != null) {
                         try {
                             context.startActivity(currentStepData.action)
                         } catch (e: Exception) {
                             // Fallback: try to open general settings
                             try {
                                 context.startActivity(Intent(Settings.ACTION_SETTINGS))

                             } catch (e2: Exception) {
                             }
                         }
                     } else if (currentStep < steps.size - 1) {
                         currentStep++
                     } else {
                         // Check permissions and close
                         if (PermissionChecker.areAllPermissionsGranted(context)) {
                             onPermissionGranted()
                         }
                         onDismiss()
                     }
                 },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = main_appColor
                )
            ) {
                Text(
                    text = if (currentStepData.action != null) "Open Settings" 
                           else if (currentStep < steps.size - 1) "Next" 
                           else "Done",
                    color = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Why this is important
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = main_appColor.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = main_appColor,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "Why is this important?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = main_appColor
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "This permission allows the app to save your notifications locally, so you can review them later even if you miss them. Your data stays on your device and is never shared.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

private data class PermissionStep(
    val title: String,
    val description: String,
    val instructions: List<String>,
    val icon: ImageVector,
    val action: Intent?
)

private fun getPermissionSteps(context: Context): List<PermissionStep> {
    return listOf(
        PermissionStep(
            title = "Step 1: Enable Notification Access",
            description = "We need permission to access your notifications to save them for you.",
            instructions = listOf(
                "Tap 'Open Settings' below",
                "Find 'Notification Saver' in the list",
                "Toggle the switch to enable access"
            ),
            icon = Icons.Default.Notifications,
            action = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        ),
        PermissionStep(
            title = "Step 2: Allow App Notifications",
            description = "Make sure notifications are enabled for this app.",
            instructions = listOf(
                "Tap 'Open Settings' below",
                "Go to 'Notifications' section",
                "Make sure notifications are enabled"
            ),
            icon = Icons.Default.Settings,
            action = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
            }
        ),
        PermissionStep(
            title = "Step 3: Verify Setup",
            description = "Let's make sure everything is working correctly.",
            instructions = listOf(
                "The app will check if permissions are granted",
                "You'll see a success message if everything is set up",
                "You can now start saving notifications!"
            ),
            icon = Icons.Default.Check,
            action = null
        )
    )
}
