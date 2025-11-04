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
import com.jetpackComposeTest1.ui.theme.main_appColor
import kotlin.math.max
import kotlin.math.roundToInt

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
    
    Column(modifier = modifier.padding(2.dp)) {
        // Chart area with Y-axis labels on the right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Chart area (takes most of the width)
            Box(
                modifier = Modifier
                    .height(chartHeight)
                  
            ) {
                // Draw grid background
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val gridColor = Color.Gray.copy(alpha = 0.2f)
                    val gridStrokeWidth = 1.dp.toPx()
                    
                    // Draw only 3 horizontal grid lines: top, center, bottom
                    val gridPositions = listOf(0f, 0.5f, 1f) // Top, center, bottom
                    gridPositions.forEach { position ->
                        val y = size.height * position
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = gridStrokeWidth
                        )
                    }
                    
                    // Draw vertical grid lines at segment boundaries (creating columns for each day)
                    val barCount = weeklyTrend.size
                    if (barCount > 0) {
                        val segmentWidth = size.width / barCount
                        // Draw vertical lines at the boundaries of each segment (including edges)
                        for (i in 0..barCount) {
                            val x = segmentWidth * i
                            drawLine(
                                color = gridColor,
                                start = Offset(x, 0f),
                                end = Offset(x, size.height),
                                strokeWidth = gridStrokeWidth
                            )
                        }
                    }
                }
                
                // Draw average line across all bars
                if (showAverage) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val avgHeight = (average.toFloat() / maxCount) * size.height
                        val y = size.height - avgHeight
                        
                        drawLine(
                            color = averageLineColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                        )
                    }
                }
                
                // Draw bars
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
                                    (dayData.count.toFloat() / maxCount) * size.height
                                } else 0f
                                
                                val barWidth = size.width * 0.6f
                                val x = (size.width - barWidth) / 2
                                val y = size.height - barHeight
                                
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
            
            // Y-axis labels - positioned to the right of the chart, aligned with evenly spaced grid lines
            Box(
                modifier = Modifier
                    .height(chartHeight)
                    .width(20.dp)
            ) {
                // Show only 3 Y-axis labels: top (maxCount), center, bottom (0)
                val centerValue = (maxCount.toFloat() / 2f).roundToInt()
                val labelValues = listOf(maxCount, centerValue, 0)
                
                // Position labels: top, center, bottom
                labelValues.forEachIndexed { index, labelValue ->
                    val yPosition = when (index) {
                        0 -> 0f // Top
                        1 -> 0.5f // Center
                        else -> 1f // Bottom
                    }
                    val yOffset = (chartHeight.value * yPosition - 7).dp
                    Text(
                        text = labelValue.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(y = yOffset)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // X-axis labels - align with chart width (excluding Y-axis labels area)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Box(modifier = Modifier) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weeklyTrend.forEachIndexed { index, dayData ->
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayData.dayLabel,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 12.sp,
                                color = Color.Black,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
            // Spacer for Y-axis labels area
            Spacer(modifier = Modifier.width(28.dp))
        }
        
        // Average label
        if (showAverage) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "avg ${average.toInt()}",
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = 10.sp,
                    color = main_appColor
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

    // Calculate rounded max for Y-axis labels (round up to nearest 5 or 10)
    val roundedMax = when {
        maxCount <= 5 -> 5
        maxCount <= 10 -> 10
        maxCount <= 20 -> 20
        maxCount <= 50 -> ((maxCount + 9) / 10) * 10
        else -> ((maxCount + 49) / 50) * 50
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        // Chart area with Y-axis labels on the right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Chart area (takes most of the width)
            Box(
                modifier = Modifier
                    .height(chartHeight).padding(start = 5.dp)
            ) {
                // Draw grid background
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val gridColor = Color.Gray.copy(alpha = 0.2f)
                    val gridStrokeWidth = 1.dp.toPx()

                    // Draw horizontal grid lines at intervals (5 lines = 6 positions)
                    val numHorizontalLines = 5
                    for (i in 0..numHorizontalLines) {
                        val y = (size.height * i.toFloat() / numHorizontalLines)
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = gridStrokeWidth
                        )
                    }

                    // Draw vertical grid lines at each hour boundary
                    val hourCount = hourlyData.size
                    if (hourCount > 0) {
                        val hourWidth = size.width / hourCount
                        for (i in 0..hourCount) {
                            val x = hourWidth * i
                            drawLine(
                                color = gridColor,
                                start = Offset(x, 0f),
                                end = Offset(x, size.height),
                                strokeWidth = gridStrokeWidth
                            )
                        }
                    }
                }

                // Draw bars
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    hourlyData.forEachIndexed { index, hourData ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(chartHeight)
                            ) {
                                val barHeight = if (roundedMax > 0) {
                                    (hourData.count.toFloat() / roundedMax) * size.height
                                } else 0f

                                val barWidth = size.width * 0.7f
                                val x = (size.width - barWidth) / 2
                                val y = size.height - barHeight

                                // Only draw bar if height is greater than 0 to avoid bottom line
                                if (barHeight > 0) {
                                    drawRect(
                                        color = barColor,
                                        topLeft = Offset(x, y),
                                        size = Size(barWidth, maxOf(barHeight, 2.dp.toPx()))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Y-axis labels - positioned to the right of the chart, aligned with grid lines
            Box(
                modifier = Modifier
                    .height(chartHeight)
                    .width(20.dp)
            ) {
                // Calculate label values based on roundedMax to match 5 grid lines
                val numGridLines = 5
                val interval = roundedMax.toFloat() / numGridLines
                val labelValues = (0..numGridLines).map { roundedMax - (it * interval) }
                
                // Position labels aligned with grid lines (5 grid lines = 6 positions: 0 to 5)
                labelValues.forEachIndexed { index, labelValue ->
                    val yPosition = index.toFloat() / numGridLines
                    Text(
                        text = labelValue.toInt().toString(),
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(y = (chartHeight * yPosition) - 7.dp) // Center text on grid line
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // X-axis labels - show only key hours (00, 06, 12, 18)
        // Align with chart width (excluding Y-axis labels area)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Box(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    hourlyData.forEachIndexed { index, hourData ->
                        // Only show labels at 00, 06, 12, 18
                        if (hourData.hour % 6 == 0) {
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = when {
                                    hourData.hour == 0 -> Alignment.CenterStart
                                    hourData.hour == 18 -> Alignment.CenterEnd
                                    else -> Alignment.Center
                                }
                            ) {
                                Text(
                                    text = String.format("%02d", hourData.hour),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        } else {
                            // Empty space for hours without labels
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
                // Spacer for Y-axis labels area
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

