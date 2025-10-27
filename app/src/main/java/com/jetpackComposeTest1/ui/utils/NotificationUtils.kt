package com.jetpackComposeTest1.ui.utils


import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.jetpackComposeTest1.R
import com.jetpackComposeTest1.db.NotificationEntity
import com.jetpackComposeTest1.services.MyNotificationListener

object NotificationUtils {

    const val NOTIFICATION_ID = 1

    // Check if notification service enabled
    fun isNotificationServiceEnabled(context: Context): Boolean {
        val pkgName = context.packageName
        val flat =
            Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        if (!TextUtils.isEmpty(flat)) {
            val names = flat.split(":").toTypedArray()
            for (name in names) {
                val cn = ComponentName.unflattenFromString(name)
                if (cn != null && TextUtils.equals(pkgName, cn.packageName)) {
                    return true
                }
            }
        }
        return false
    }

    // Open notification access settings
    fun openNotificationAccessSettings(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        ContextCompat.startActivity(context, intent, null)
    }

    // Restart the listener manually (optional)
    fun restartNotificationListenerService(context: Context) {
        val pm = context.packageManager
        val componentName = ComponentName(context, MyNotificationListener::class.java)

        pm.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        pm.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }


    // Step 1: Create channel (only once, usually in Application or MainActivity)
    fun createNotificationAliveServiceChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "NotificationAliveChannel"
            val channelDescription = "Notifications from Jetpack Compose example"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(
                Constants.channelNotificationAliveId,
                channelName,
                importance
            ).apply {
                description = channelDescription
            }

            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    // Step 1: Create channel (only once, usually in Application or MainActivity)
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Compose Notifications"
            val channelDescription = "Notifications from Jetpack Compose example"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(Constants.channelId, channelName, importance).apply {
                description = channelDescription
            }

            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    // Step 2: Function to show notification
    @SuppressLint("MissingPermission")
    fun showNotification(context: Context, title: String, message: String) {

        val builder = NotificationCompat.Builder(context, Constants.channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            Log.d("find_error", "showNotification: ====> notification ")
            notify(10101, builder.build())
        }
    }


    fun buildForegroundNotification(context: Context, notifications: List<String>): Notification {
        val builder = NotificationCompat.Builder(context, Constants.channelNotificationAliveId)
            .setSmallIcon(R.drawable.flower1)
            .setContentTitle("Notification Service Running")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)

        Log.d("find_error", "buildForegroundNotification: ====> ${notifications}")
        // Use InboxStyle to show multiple messages
        val inboxStyle = NotificationCompat.InboxStyle()
        if (notifications.isEmpty()) {
            inboxStyle.addLine("this i calling ")
        } else {
            notifications.forEach { inboxStyle.addLine(it) }
        }

        builder.setStyle(inboxStyle)
        return builder.build()
    }

    fun updateForegroundNotification(context: Context, notifications: List<String>) {
        val notification = buildForegroundNotification(context, notifications)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }


    fun base64ToBitmap(base64Str: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun buildForegroundNotificationWithIconsAndCount(
        context: Context,
        notifications: List<NotificationEntity>
    ): Notification {

        val remoteViews = RemoteViews(context.packageName, R.layout.custom_notification_horizontal)
        remoteViews.removeAllViews(R.id.notification_icons_container)
        
        if (notifications.isNotEmpty()) {
            notifications.forEach { notif ->
                val item = RemoteViews(context.packageName, R.layout.custom_notification_icon_item)
                val bitmap = getAppIconBitmap(context, notif.packageName)
                if (bitmap != null) {
                    item.setImageViewBitmap(R.id.app_icon, bitmap)
                } else {
                    item.setImageViewResource(R.id.app_icon, R.drawable.flower1)
                }
                remoteViews.addView(R.id.notification_icons_container, item)
            }
        } else {
            val item = RemoteViews(context.packageName, R.layout.custom_notification_icon_item)
            item.setImageViewResource(R.id.app_icon, R.drawable.flower1)
            remoteViews.addView(R.id.notification_icons_container, item)
        }

        val countText = if (notifications.isEmpty()) "No new notifications"
        else "${notifications.size} new notifications"
        remoteViews.setTextViewText(R.id.notification_count_text, countText)

        return NotificationCompat.Builder(context, Constants.channelNotificationAliveId)
            .setSmallIcon(R.drawable.flower1)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(remoteViews)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    fun getAppIconBitmap(context: Context, packageName: String): Bitmap? {
        return try {
            val drawable = context.packageManager.getApplicationIcon(packageName)
            if (drawable is BitmapDrawable) {
                drawable.bitmap
            } else {
                // Convert other drawable types to Bitmap
                val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 48
                val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 48
                
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, width, height)
                drawable.draw(canvas)
                bitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // fallback icon
            BitmapFactory.decodeResource(context.resources, R.drawable.flower1)
        }
    }


}