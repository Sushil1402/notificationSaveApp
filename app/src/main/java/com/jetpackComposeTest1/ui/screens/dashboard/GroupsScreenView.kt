package com.jetpackComposeTest1.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.jetpackComposeTest1.model.notification.NotificationGroupData
import com.jetpackComposeTest1.ui.components.GroupNameDialog
import com.jetpackComposeTest1.ui.navigation.AppNavigationRoute
import com.jetpackComposeTest1.ui.navigation.GroupAppSelectionRoute
import com.jetpackComposeTest1.ui.screens.dashboard.viewmodel.GroupsViewModel
import com.jetpackComposeTest1.ui.theme.main_appColor
import com.jetpackComposeTest1.ui.utils.GroupIconUtils

@Composable
fun GroupsScreenView(
    navToScreen: (AppNavigationRoute) -> Unit,
    viewModel: GroupsViewModel = hiltViewModel()
) {
    val groups by viewModel.notificationGroups.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showGroupDialog by remember { mutableStateOf(false) }
    var groupName by remember { mutableStateOf("") }



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
                    text = "Notification Groups",
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
                            group = group,
                            onGroupClick = { /* Open group details */ },
                            onToggleMute = { viewModel.toggleGroupMute(group.id) },
                            onEditGroup = { /* Edit group settings */ },
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
    }
}

@Composable
fun NotificationGroupItem(
    modifier: Modifier,
    group: NotificationGroupData,
    onGroupClick: () -> Unit,
    onToggleMute: () -> Unit,
    onEditGroup: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                    if (group.id in listOf("unread", "read", "muted")) {
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
                    IconButton(onClick = onToggleMute) {
                        Icon(
                            imageVector = if (group.isMuted) Icons.Default.Info else Icons.Default.Info,
                            contentDescription = if (group.isMuted) "Unmute" else "Mute",
                            tint = if (group.isMuted) Color.Red else Color.Gray
                        )
                    }
                    IconButton(onClick = onEditGroup) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Group Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                GroupStatItem("Total", group.totalNotifications.toString())
                GroupStatItem("Unread", group.unreadNotifications.toString())
                GroupStatItem("Today", group.todayNotifications.toString())
                GroupStatItem("Apps", group.appCount.toString())
            }

            // Group Type Badge
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GroupTypeChip(group.type)
                if (group.isMuted) {
                    GroupTypeChip("Muted", Color.Red)
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



