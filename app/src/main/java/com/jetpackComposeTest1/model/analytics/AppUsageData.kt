package com.jetpackComposeTest1.model.analytics

data class AppUsageData(
    val name: String,
    val packageName: String,
    val count: Int,
    val percentage: Float,
    val color: androidx.compose.ui.graphics.Color
)