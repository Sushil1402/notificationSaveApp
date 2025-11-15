package com.notistorex.app.model.analytics

import androidx.compose.ui.graphics.Color
import android.graphics.drawable.Drawable

data class AppUsageDataWithIcon(
    val packageName: String,
    val name: String,
    val count: Int,
    val percentage: Float,
    val color: Color,
    val icon: Drawable? = null
)

