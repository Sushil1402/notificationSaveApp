package com.jetpackComposeTest1.ui.utils

import android.content.Context
import android.graphics.drawable.Drawable
import com.jetpackComposeTest1.R
import com.jetpackComposeTest1.model.FlowerItem

object Utils {

    fun getAppIcon(context: Context, packageName: String): Drawable? {
        return try {
            val packageManager = context.packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationIcon(appInfo)
        } catch (e: Exception) {
            e.printStackTrace()
            null // if package not found
        }
    }


}