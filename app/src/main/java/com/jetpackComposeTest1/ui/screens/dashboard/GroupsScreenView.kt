package com.jetpackComposeTest1.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.jetpackComposeTest1.model.notification.NotificationGroupData
import com.jetpackComposeTest1.ui.theme.main_appColor

@Composable
fun GroupsScreenView() {
    // Mock data for now - replace with actual ViewModel later
    val groups = remember {
        listOf(
            NotificationGroupData(
                id = "unread",
                name = "Unread Notifications",
                description = "Notifications you haven't read yet",
                icon = Icons.Default.Email,
                color = Color(0xFFFF6B6B),
                type = "Unread",
                isMuted = false,
                totalNotifications = 23,
                unreadNotifications = 23,
                todayNotifications = 5,
                appCount = 8
            ),
            NotificationGroupData(
                id = "read",
                name = "Read Notifications",
                description = "Notifications you've already read",
                icon = Icons.Default.Done,
                color = Color(0xFF4CAF50),
                type = "Read",
                isMuted = false,
                totalNotifications = 156,
                unreadNotifications = 0,
                todayNotifications = 12,
                appCount = 15
            ),
            NotificationGroupData(
                id = "muted",
                name = "Muted Notifications",
                description = "Notifications from muted apps",
                icon = Icons.Default.Info,
                color = Color(0xFF9E9E9E),
                type = "Muted",
                isMuted = true,
                totalNotifications = 45,
                unreadNotifications = 0,
                todayNotifications = 3,
                appCount = 5
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
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
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(groups) { group ->
                NotificationGroupItem(
                    group = group,
                    onGroupClick = { /* Open group details */ },
                    onToggleMute = { /* Toggle group mute */ },
                    onEditGroup = { /* Edit group settings */ }
                )
            }
        }
    }
}

@Composable
fun NotificationGroupItem(
    group: NotificationGroupData,
    onGroupClick: () -> Unit,
    onToggleMute: () -> Unit,
    onEditGroup: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    Icon(
                        imageVector = group.icon,
                        contentDescription = group.name,
                        tint = group.color,
                        modifier = Modifier.size(32.dp)
                    )
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



@Preview
@Composable
fun GroupsScreenPreview() {
    GroupsScreenView()
}
