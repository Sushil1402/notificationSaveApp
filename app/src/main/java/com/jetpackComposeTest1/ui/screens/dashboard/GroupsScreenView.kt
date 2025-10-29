package com.jetpackComposeTest1.ui.screens.dashboard

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jetpackComposeTest1.R
import com.jetpackComposeTest1.model.notification.NotificationGroupData
import com.jetpackComposeTest1.ui.components.GroupNameDialog
import com.jetpackComposeTest1.ui.components.AppAlertDialog
import com.jetpackComposeTest1.ui.navigation.AppNavigationRoute
import com.jetpackComposeTest1.ui.navigation.GroupAppSelectionRoute
import com.jetpackComposeTest1.ui.navigation.GroupAppsRoute
import com.jetpackComposeTest1.ui.screens.dashboard.viewmodel.GroupsViewModel
import com.jetpackComposeTest1.ui.theme.main_appColor
import com.jetpackComposeTest1.ui.utils.GroupIconUtils

@Composable
fun GroupsScreenView(
    navToScreen: (AppNavigationRoute) -> Unit,
    viewModel: GroupsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val groups by viewModel.notificationGroups.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showGroupDialog by remember { mutableStateOf(false) }
    var groupName by remember { mutableStateOf("") }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameGroupName by remember { mutableStateOf("") }
    var selectedGroupId by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteGroupName by remember { mutableStateOf("") }



    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        )
        {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = context.getString(R.string.notification_group),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${groups.size} groups",
                    style = MaterialTheme.typography.bodyMedium,
                    color = main_appColor
                )
            }

            // Groups List
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = main_appColor
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(groups) { index, group ->
                        NotificationGroupItem(
                            context=context,
                            group = group,
                            onGroupClick = { 
                                // Navigate to group apps screen
                                navToScreen.invoke(
                                    GroupAppsRoute(groupId = group.id, groupName = group.name)
                                )
                            },
                            onToggleMute = { viewModel.toggleGroupMute(group.id) },
                            onRenameGroup = { 
                                selectedGroupId = group.id
                                renameGroupName = group.name
                                showRenameDialog = true 
                            },
                            onDeleteGroup = { 
                                selectedGroupId = group.id
                                deleteGroupName = group.name
                                showDeleteDialog = true 
                            },
                            modifier = if (index == groups.size - 1) Modifier.padding(
                                bottom = 200.dp
                            ) else Modifier
                        )
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { showGroupDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp, bottom = 150.dp, top = 0.dp, end = 16.dp),
            containerColor = main_appColor
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = Color.White
            )
        }

        // Group Name Dialog
        GroupNameDialog(
            isVisible = showGroupDialog,
            onDismiss = {
                showGroupDialog = false
                groupName = ""
            },
            onConfirm = { name ->
                groupName = name
                showGroupDialog = false
                // Navigate to AppSelectionScreen for group creation
                navToScreen.invoke(
                    GroupAppSelectionRoute(
                        groupName = name
                    )
                )
            }
        )

        // Rename Group Dialog
        GroupNameDialog(
            isVisible = showRenameDialog,
            initialName = renameGroupName,
            onDismiss = {
                showRenameDialog = false
                renameGroupName = ""
                selectedGroupId = ""
            },
            onConfirm = { name ->
                viewModel.renameGroup(selectedGroupId, name)
                showRenameDialog = false
                renameGroupName = ""
                selectedGroupId = ""
            }
        )

        // Delete Confirmation Dialog
        if (showDeleteDialog) {
            AppAlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    deleteGroupName = ""
                    selectedGroupId = ""
                },
                title = context.getString(R.string.delete_group),
                text = context.getString(R.string.are_you_sure_delete_group, deleteGroupName),
                confirmButtonText = context.getString(R.string.delete),
                dismissButtonText = context.getString(R.string.cancel),
                confirmButton = {
                    viewModel.deleteGroup(selectedGroupId)
                    showDeleteDialog = false
                    deleteGroupName = ""
                    selectedGroupId = ""
                },
                dismissButton = {
                    showDeleteDialog = false
                    deleteGroupName = ""
                    selectedGroupId = ""
                }
            )
        }
    }
}

@Composable
fun NotificationGroupItem(
    context: Context,
    modifier: Modifier,
    group: NotificationGroupData,
    onGroupClick: () -> Unit,
    onToggleMute: () -> Unit,
    onRenameGroup: () -> Unit,
    onDeleteGroup: () -> Unit
) {
    val groupIds = arrayListOf(context.getString(R.string.group_label_unread),context.getString(R.string.group_label_muted))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onGroupClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Group Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Show icon for system groups, initials for custom groups
                    if (group.id in groupIds) {
                        Icon(
                            imageVector = group.icon,
                            contentDescription = group.name,
                            tint = group.color,
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        // Custom group - show initials
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    color = group.color.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = GroupIconUtils.generateInitials(group.name),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = group.color
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = group.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = group.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Show count badge for system groups
                    if (group.id in groupIds) {
                        SmallCountBadge(
                            count = group.totalNotifications,
                            color = group.color
                        )
                    }
                    
                    // Show mute/unmute toggle for custom groups only
                    if (group.id !in groupIds) {
                        IconButton(onClick = onToggleMute) {
                            Icon(
                                imageVector = if (group.isMuted) Icons.Default.Notifications else Icons.Default.Notifications,
                                contentDescription = if (group.isMuted) context.getString(R.string.unmute) else context.getString(R.string.muted),
                                tint = if (group.isMuted) Color.Red else Color.Gray
                            )
                        }
                    }
                    
                    // Show three-dot menu for custom groups only
                    if (group.id !in groupIds) {
                        var showDropdownMenu by remember { mutableStateOf(false) }
                        
                        Box {
                            IconButton(onClick = { showDropdownMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More options",
                                    tint = Color.Gray
                                )
                            }
                            
                            DropdownMenu(
                                expanded = showDropdownMenu,
                                onDismissRequest = { showDropdownMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(context.getString(R.string.rename)) },
                                    onClick = {
                                        showDropdownMenu = false
                                        onRenameGroup()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Rename"
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(context.getString(R.string.delete)) },
                                    onClick = {
                                        showDropdownMenu = false
                                        onDeleteGroup()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete"
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Group Stats - only for custom groups
            if (group.id !in groupIds) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    GroupStatItem(context.getString(R.string.total), group.totalNotifications.toString())
                    GroupStatItem(context.getString(R.string.unread), group.unreadNotifications.toString())
                    GroupStatItem(context.getString(R.string.today), group.todayNotifications.toString())
                    GroupStatItem(context.getString(R.string.apps), group.appCount.toString())
                }
            }

            // Group Type Badge
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (group.id == context.getString(R.string.group_label_muted)) {
                    GroupTypeChip(context.getString(R.string.group_label_muted), Color.Red)
                } else if (group.id !in groupIds && group.isMuted) {
                    GroupTypeChip(context.getString(R.string.group_label_muted), Color.Red)
                }
            }
        }
    }
}

@Composable
fun GroupStatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = main_appColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun GroupTypeChip(
    type: String,
    color: Color = main_appColor
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = type,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}

@Composable
fun SmallCountBadge(
    count: Int,
    color: Color
) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .background(
                color = color,
                shape = RoundedCornerShape(10.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}



