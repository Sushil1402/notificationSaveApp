package com.notistorex.app.ui.components.charts

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notistorex.app.model.analytics.DayData
import com.notistorex.app.model.analytics.HourData
import com.notistorex.app.ui.theme.main_appColor
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

    // Calculate dynamic Y-axis label width
    val maxValueDigits = maxCount.toString().length
    val centerValue = (maxCount.toFloat() / 2f).roundToInt()
    val centerValueDigits = centerValue.toString().length
    val maxDigits = maxOf(maxValueDigits, centerValueDigits, 1)
    val calculatedWidth = (maxDigits * 10).dp
    val yAxisLabelWidth = maxOf(calculatedWidth, 15.dp)

    Column(modifier = modifier.padding(2.dp)) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 2.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // --- Chart Area (Grid + Bars) ---
            Box(
                modifier = Modifier
                    .height(chartHeight)
                    .weight(1f)
            ) {
                // --- Grid Background ---
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val gridColor = Color.Gray.copy(alpha = 0.2f)
                    val gridStrokeWidth = 1.dp.toPx()
                    val barCount = weeklyTrend.size

                    // Horizontal lines (top, middle, bottom)
                    listOf(0f, 0.5f, 1f).forEach { position ->
                        val y = size.height * position
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = gridStrokeWidth
                        )
                    }

                    // Left edge line
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                        strokeWidth = gridStrokeWidth
                    )

                    // Vertical grid lines (including right edge)
                    if (barCount > 0) {
                        val segmentWidth = size.width / barCount
                        for (i in 1..barCount) { // include final edge
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

                // --- Average Line ---
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

                // --- Bars (Evenly spaced, no outer gaps) ---
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 0.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val barWidthFraction = 0.7f
                    weeklyTrend.forEach { dayData ->
                        val color =
                            if (dayData.isSelected) selectedBarColor else Color(0xFF424242)
                        val animatedHeight = remember(dayData.count) { Animatable(0f) }

                        // Animate bar height when value changes
                        LaunchedEffect(dayData.count) {
                            animatedHeight.animateTo(dayData.count.toFloat())
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth(barWidthFraction)
                                    .fillMaxHeight()
                            ) {
                                val barHeight = (animatedHeight.value / maxCount) * size.height
                                val y = size.height - barHeight
                                drawRect(
                                    color = color,
                                    topLeft = Offset(0f, y),
                                    size = Size(size.width, barHeight)
                                )
                            }
                        }
                    }
                }
            }

            // --- Y-Axis Labels ---
            Column(
                modifier = Modifier
                    .height(chartHeight)
                    .width(yAxisLabelWidth),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                listOf(maxCount, (maxCount / 2), 0).forEach { label ->
                    Text(
                        text = label.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- X-Axis Labels ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 0.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                weeklyTrend.forEach { dayData ->
                    Text(
                        text = dayData.dayLabel,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.width(yAxisLabelWidth))
        }

        // --- Average Label ---
        if (showAverage) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "avg ${average.toInt()}",
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = 8.sp,
                    color = Color(0xFF1A73E8), // Replace with your main_appColor
                    modifier = Modifier.padding(end = yAxisLabelWidth + 4.dp)
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
                    .height(chartHeight)
                    .padding(start = 5.dp)
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

