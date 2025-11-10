package com.jetpackComposeTest1.ui.screens.dashboard

import android.app.DatePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jetpackComposeTest1.R
import com.jetpackComposeTest1.model.analytics.AppUsageData
import com.jetpackComposeTest1.ui.components.charts.HourlyBarChart
import com.jetpackComposeTest1.ui.components.charts.WeeklyBarChart
import com.jetpackComposeTest1.ui.navigation.AppNavigationRoute
import com.jetpackComposeTest1.ui.navigation.NotificationDetailRoute
import com.jetpackComposeTest1.ui.screens.dashboard.viewmodel.AnalyticsViewModel
import com.jetpackComposeTest1.ui.theme.main_appColor
import com.jetpackComposeTest1.ui.utils.NotificationUtils
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AnalyticsScreenView(
    viewModel: AnalyticsViewModel = hiltViewModel(),
    navToScreen: (AppNavigationRoute) -> Unit = {}
) {
    val context = LocalContext.current
    val selectedDate by viewModel.selectedDate.collectAsState()
    val dailyAnalytics by viewModel.dailyAnalytics.collectAsState()
    val weeklyAppBreakdown by viewModel.weeklyAppBreakdown.collectAsState()
    val canGoNext by viewModel.canGoToNextDay.collectAsState()

    val dateFormatter = remember { SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()) }
    val dateString = remember(selectedDate) {
        dateFormatter.format(Date(selectedDate))
    }

    val isToday = remember(selectedDate) {
        val today = Calendar.getInstance()
        val selected = Calendar.getInstance().apply { timeInMillis = selectedDate }
        today.get(Calendar.DAY_OF_YEAR) == selected.get(Calendar.DAY_OF_YEAR) &&
                today.get(Calendar.YEAR) == selected.get(Calendar.YEAR)
    }

    var showDatePicker by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(main_appColor)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
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
                    text = context.getString(R.string.analytics),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

            }

            DateSelectorHeader(
                context=context,
                dateString = dateString,
                onPreviousDay = { viewModel.selectPreviousDay() },
                onNextDay = { viewModel.selectNextDay() },
                onDateClick = { showDatePicker = true },
                onTodayClick = { viewModel.selectToday() },
                showTodayButton = !isToday,
                canGoNext = canGoNext
            )

            // Date Picker Dialog
            if (showDatePicker) {
                DatePickerDialogComposable(
                    selectedDate = selectedDate,
                    onDateSelected = { date ->
                        viewModel.selectDate(date)
                        showDatePicker = false
                    },
                    onDismiss = { showDatePicker = false }
                )
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {

                dailyAnalytics?.let { analytics ->
                    // Hero Stats Section
                    item {
                        HeroStatsSection(
                            context =context,
                            totalNotifications = analytics.totalNotifications,
                            changeFromYesterday = analytics.changeFromYesterday,
                            mostActiveHourRange = analytics.mostActiveHourRange,
                            topApp = analytics.topApp
                        )
                    }

                    // Weekly Trend Chart
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = context.getString(R.string.weekly_trend),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                WeeklyBarChart(
                                    weeklyTrend = analytics.weeklyTrend,
                                    barColor = Color(0xFF424242),
                                    selectedBarColor = Color(0xFFEA4335),
                                    averageLineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }



                    // Hourly Activity Chart
        item {
            Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),

                        ) {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                Text(
                                    text = context.getString(R.string.hourly_activity),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                HourlyBarChart(
                                    hourlyData = analytics.hourlyData,
                                    barColor = Color(0xFFEA4335)
                                )
                            }
                        }
                    }

                    // Today App Breakdown List
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                                    text = context.getString(R.string.today_app_breakdown),
                        style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,

                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                                if (analytics.appBreakdown.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = context.getString(R.string.no_notifications_for_this_day),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else {
                                    analytics.appBreakdown.take(10).forEachIndexed { index, app ->
                                        AppBreakdownRow(
                                            context = context,
                                            app = app,
                                            maxCount = analytics.appBreakdown.first().count,
                                            onClick = {
                                                navToScreen(
                                                    NotificationDetailRoute(
                                                        packageName = app.packageName,
                                                        appName = app.name,
                                                        isFromNotification = false,
                                                        selectedDate = selectedDate
                                                    )
                                                )
                                            }
                                        )
                                        if (index < analytics.appBreakdown.size - 1) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Weekly App Breakdown Section
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 200.dp, end = 16.dp, start = 16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = context.getString(R.string.weekly_app_breakdown),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = context.getString(R.string.top_5_apps_this_week),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                if (weeklyAppBreakdown.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = context.getString(R.string.no_notifications_for_this_week),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else {
                                    val maxCount = weeklyAppBreakdown.firstOrNull()?.count ?: 1
                                    weeklyAppBreakdown.forEachIndexed { index, app ->
                                        WeeklyAppBreakdownRow(
                                            context = context,
                                            app = app,
                                            maxCount = maxCount,
                                            onClick = {
                                                navToScreen(
                                                    NotificationDetailRoute(
                                                        packageName = app.packageName,
                                                        appName = app.name,
                                                        isFromNotification = false,
                                                        selectedDate = selectedDate
                                                    )
                                                )
                                            }
                                        )
                                        if (index < weeklyAppBreakdown.size - 1) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                } ?: item {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = main_appColor)
                    }
                }
            }
        }

    }
}

@Composable
fun DateSelectorHeader(
    context: Context,
    dateString: String,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onDateClick: () -> Unit,
    onTodayClick: () -> Unit,
    showTodayButton: Boolean,
    canGoNext: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 0.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous arrow button with circular background
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(main_appColor) // Blue background
                    .clickable { onPreviousDay() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Default.KeyboardArrowLeft,
                    contentDescription = "Previous day",
                    tint = Color.White, // Dark arrow
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Text(
                text = dateString,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onDateClick() },
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Next arrow button with circular background
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (canGoNext) main_appColor 
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                        .then(
                            if (canGoNext) {
                                Modifier.clickable { onNextDay() }
                            } else {
                                Modifier
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Default.KeyboardArrowRight,
                        contentDescription = "Next day",
                        tint = if (canGoNext) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Today button inline with navigation arrows
                if (showTodayButton) {
                    Button(
                        onClick = onTodayClick,
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = main_appColor
                        ),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text =context.getString(R.string.today),
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeroStatsSection(
    context: Context,
    totalNotifications: Int,
    changeFromYesterday: Float,
    mostActiveHourRange: String,
    topApp: AppUsageData?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = context.getString(R.string.total_notifications),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = totalNotifications.toString(),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 56.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (changeFromYesterday != 0f) {
                        // Calculate the actual difference (not percentage)
                        val difference = if (changeFromYesterday != -100f) {
                            ((changeFromYesterday * totalNotifications) / (100 + changeFromYesterday)).toInt()
                        } else {
                            // If change is -100%, means today is 0 and yesterday had some
                            -totalNotifications
                        }
                        
                        Text(
                            text = if (difference > 0) {
                                context.getString(R.string.plus_vs_yesterday,"${difference}")
                            } else {
                                context.getString(R.string.vs_yesterday,"${difference}")
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (difference > 0) Color(0xFF4CAF50) else Color.Red,
                            fontSize = 14.sp
                        )
                    }
                }
                
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = context.getString(R.string.most_active),
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = mostActiveHourRange,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = context.getString(R.string.top_app),
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    topApp?.let { app ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.End
                        ) {
                            // App name with proper truncation
                            Text(
                                text = app.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                            
                            // Count displayed separately with badge-like styling
                            Row(
                                modifier = Modifier.padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = app.count.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = main_appColor,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (app.count == 1)context.getString(R.string.notification) else context.getString(R.string.notifications),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppBreakdownRow(
    context: Context,
    app: AppUsageData,
    maxCount: Int,
    onClick: () -> Unit
) {
    val appIcon = remember(app.packageName) {
        try {
            val bitmap = NotificationUtils.getAppIconBitmap(context, app.packageName)
            bitmap?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
    
    val progress = if (maxCount > 0) app.count.toFloat() / maxCount else 0f
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon
            if (appIcon != null) {
                Image(
                    bitmap = appIcon,
                    contentDescription = app.name,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(
                modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = app.color.copy(alpha = 0.3f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = app.name.take(1).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = app.color
                    )
                }
            }
            
            Column(modifier = Modifier.weight(1f)) {
            Text(
                    text = app.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(1.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .fillMaxWidth(progress)
                            .background(Color(0xFFEA4335), shape = RoundedCornerShape(1.dp))
                    )
                }
            }
        }
        
        // Notification count on the right
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = app.count.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                contentDescription = "View details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DatePickerDialogComposable(
    selectedDate: Long,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply {
        timeInMillis = selectedDate
    }
    
    LaunchedEffect(Unit) {
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCalendar = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                
                // Validate that the selected date is not in the future
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                
                // Only proceed if the selected date is today or in the past
                if (newCalendar.timeInMillis <= today.timeInMillis) {
                    onDateSelected(newCalendar.timeInMillis)
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        // Set maximum date to today to prevent selecting future dates
        val today = Calendar.getInstance()
        datePickerDialog.datePicker.maxDate = today.timeInMillis
        
        datePickerDialog.setOnDismissListener {
            onDismiss()
        }
        
        datePickerDialog.show()
    }
}

@Composable
fun WeeklyAppBreakdownRow(
    context:Context,
    app: AppUsageData,
    maxCount: Int,
    onClick: () -> Unit
) {
    val appIcon = remember(app.packageName) {
        try {
            val bitmap = NotificationUtils.getAppIconBitmap(context, app.packageName)
            bitmap?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
    
    val progress = if (maxCount > 0) app.count.toFloat() / maxCount else 0f
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon
            if (appIcon != null) {
                Image(
                    bitmap = appIcon,
                    contentDescription = app.name,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = app.color.copy(alpha = 0.3f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = app.name.take(1).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = app.color
                    )
                }
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(1.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .fillMaxWidth(progress)
                            .background(app.color, shape = RoundedCornerShape(1.dp))
                    )
                }
            }
        }
        
        // Notification count on the right
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = app.count.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                contentDescription = "View details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview
@Composable
fun AnalyticsScreenPreview() {
    AnalyticsScreenView()
}
