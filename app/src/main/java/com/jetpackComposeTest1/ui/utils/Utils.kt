package com.jetpackComposeTest1.ui.utils

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
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

    /**
     * Common function to share Excel file
     * Used in multiple screens for exporting notifications
     */
    fun shareExcelFile(context: Context, uri: Uri) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Excel File"))
    }

}