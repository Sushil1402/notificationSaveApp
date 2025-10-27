package com.jetpackComposeTest1.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import com.jetpackComposeTest1.model.analytics.AnalyticsData
import com.jetpackComposeTest1.model.analytics.AppUsageData
import com.jetpackComposeTest1.ui.theme.main_appColor

@Composable
fun AnalyticsScreenView() {
    // Mock data for now - replace with actual ViewModel later
    val analyticsData = remember {
        AnalyticsData(
            totalNotifications = 1247,
            activeApps = 23,
            todayCount = 45,
            weekCount = 89,
            monthCount = 234,
            databaseSize = 45.6f,
            imageCount = 23,
            storagePercentage = 65.2f,
            topApps = listOf(
                AppUsageData("WhatsApp", 450, 36.1f, Color(0xFF25D366)),
                AppUsageData("Gmail", 234, 18.8f, Color(0xFFEA4335)),
                AppUsageData("Facebook", 189, 15.2f, Color(0xFF1877F2)),
                AppUsageData("Instagram", 156, 12.5f, Color(0xFFE4405F)),
                AppUsageData("Twitter", 98, 7.9f, Color(0xFF1DA1F2))
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Analytics",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            // Overview Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnalyticsCard(
                    title = "Total Notifications",
                    value = analyticsData.totalNotifications.toString(),
                    icon = Icons.Default.Notifications,
                    color = main_appColor
                )
                AnalyticsCard(
                    title = "Active Apps",
                    value = analyticsData.activeApps.toString(),
                    icon = Icons.Default.Info,
                    color = Color(0xFF4ECDC4)
                )
            }
        }
        
        item {
            // App Usage Chart
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Top Apps by Notifications",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    analyticsData.topApps.forEach { app ->
                        AppUsageItem(
                            appName = app.name,
                            notificationCount = app.count,
                            percentage = app.percentage,
                            color = app.color
                        )
                    }
                }
            }
        }
        
        item {
            // Notification Trends
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Notification Trends",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TrendItem("Today", analyticsData.todayCount.toString(), Color(0xFF4CAF50))
                        TrendItem("This Week", analyticsData.weekCount.toString(), Color(0xFF2196F3))
                        TrendItem("This Month", analyticsData.monthCount.toString(), Color(0xFFFF9800))
                    }
                }
            }
        }
        
        item {
            // Storage Usage
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Storage Usage",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Database Size: ${analyticsData.databaseSize} MB")
                        Text("Images: ${analyticsData.imageCount} files")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LinearProgressIndicator(
                        progress = analyticsData.storagePercentage / 100f,
                        modifier = Modifier.fillMaxWidth(),
                        color = main_appColor
                    )
                    
                    Text(
                        text = "${analyticsData.storagePercentage}% of available storage",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun AnalyticsCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun AppUsageItem(
    appName: String,
    notificationCount: Int,
    percentage: Float,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = appName,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth()
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LinearProgressIndicator(
                progress = percentage / 100f,
                modifier = Modifier
                    .width(60.dp)
                    .height(8.dp),
                color = color
            )
            Text(
                text = "${percentage.toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun TrendItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}


@Preview
@Composable
fun AnalyticsScreenPreview() {
    AnalyticsScreenView()
}