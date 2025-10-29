package com.jetpackComposeTest1.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetpackComposeTest1.model.analytics.DayData
import com.jetpackComposeTest1.model.analytics.HourData
import kotlin.math.max

@Composable
fun WeeklyBarChart(
    weeklyTrend: List<DayData>,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFFEA4335),
    selectedBarColor: Color = Color(0xFFEA4335),
    averageLineColor: Color = Color.Gray.copy(alpha = 0.5f),
    showAverage: Boolean = true
) {
    if (weeklyTrend.isEmpty()) return
    
    val maxCount = max(weeklyTrend.maxOfOrNull { it.count } ?: 1, 1)
    val average = weeklyTrend.map { it.count }.average()
    val chartHeight = 140.dp
    
    Column(modifier = modifier.padding(16.dp)) {
        Box(modifier = Modifier.height(chartHeight)) {
            // Draw average line across all bars
            if (showAverage) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val avgHeight = (average.toFloat() / maxCount) * (size.height - 40.dp.toPx())
                    val y = size.height - avgHeight - 20.dp.toPx()
                    
                    drawLine(
                        color = averageLineColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyTrend.forEachIndexed { index, dayData ->
                    val color = if (dayData.isSelected) selectedBarColor else Color(0xFF424242)
                    
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(chartHeight)
                        ) {
                            val barHeight = if (maxCount > 0) {
                                (dayData.count.toFloat() / maxCount) * (size.height - 40.dp.toPx())
                            } else 0f
                            
                            val barWidth = size.width * 0.6f
                            val x = (size.width - barWidth) / 2
                            val y = size.height - barHeight - 10.dp.toPx()
                            
                            drawRect(
                                color = color,
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight)
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weeklyTrend.forEach { dayData ->
                Text(
                    text = dayData.dayLabel,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    color = Color.Black
                )
            }
        }
        
        // Average label
        if (showAverage) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "avg ${average.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun HourlyBarChart(
    hourlyData: List<HourData>,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFFEA4335)
) {
    if (hourlyData.isEmpty()) return
    
    val maxCount = max(hourlyData.maxOfOrNull { it.count } ?: 1, 1)
    val chartHeight = 120.dp
    
    Column(modifier = modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            hourlyData.forEachIndexed { index, hourData ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.height(chartHeight)) {
                        Canvas(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val barHeight = if (maxCount > 0) {
                                (hourData.count.toFloat() / maxCount) * (size.height - 20.dp.toPx())
                            } else 0f
                            
                            val barWidth = size.width * 0.7f
                            val x = (size.width - barWidth) / 2
                            val y = size.height - barHeight - 10.dp.toPx()
                            
                            drawRect(
                                color = barColor,
                                topLeft = Offset(x, y),
                                size = Size(barWidth, maxOf(barHeight, 2.dp.toPx()))
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    if (index % 6 == 0 || index == 23) {
                        Text(
                            text = String.format("%02d", hourData.hour),
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
        
        // Y-axis labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Column {
                Text(
                    text = maxCount.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
                Text(
                    text = "0",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

