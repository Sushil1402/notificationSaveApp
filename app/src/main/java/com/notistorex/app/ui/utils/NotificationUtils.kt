package com.notistorex.app.ui.utils


import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.app.TaskStackBuilder
import com.notistorex.app.R
import com.notistorex.app.db.NotificationEntity
import com.notistorex.app.services.MyNotificationListener
import com.notistorex.app.presentation.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream

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
            val importance = NotificationManager.IMPORTANCE_MIN

            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Delete and recreate channel if it already exists with different settings
            manager.getNotificationChannel(Constants.channelNotificationAliveId)?.let { existing ->
                if (existing.importance != importance || existing.sound != null || existing.shouldVibrate()) {
                    manager.deleteNotificationChannel(Constants.channelNotificationAliveId)
                }
            }

            val channel = NotificationChannel(
                Constants.channelNotificationAliveId,
                channelName,
                importance
            ).apply {
                description = channelDescription
                setSound(null, null)
                enableVibration(false)
                enableLights(false)
                setBypassDnd(false)
                lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            }

            manager.createNotificationChannel(channel)
        }
    }

    // Step 1: Create channel (only once, usually in Application or MainActivity)
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Compose Notifications"
            val channelDescription = "Notifications from Jetpack Compose example"
            val importance = NotificationManager.IMPORTANCE_LOW

            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Delete and recreate channel if it already exists with different settings
            manager.getNotificationChannel(Constants.channelId)?.let { existing ->
                if (existing.importance != importance || existing.sound != null || existing.shouldVibrate()) {
                    manager.deleteNotificationChannel(Constants.channelId)
                }
            }

            val channel = NotificationChannel(Constants.channelId, channelName, importance).apply {
                description = channelDescription
                setSound(null, null)
                enableVibration(false)
                enableLights(false)
                setBypassDnd(false)
                lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            }

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
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setSilent(true)
            .setContentIntent(createAppPendingIntent(context))

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
            .setSilent(true)
            .setContentIntent(createAppPendingIntent(context))

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
            // Group notifications by packageName to show only unique app icons
            val uniqueApps = notifications
                .groupBy { it.packageName }
                .map { (packageName, appNotifications) ->
                    // Get the most recent notification from each app to determine order
                    val mostRecentNotification = appNotifications.maxByOrNull { it.timestamp }
                    packageName to (mostRecentNotification?.timestamp ?: 0L)
                }
                .sortedByDescending { it.second } // Sort by timestamp (newest first)
                .take(10) // Limit to 10 apps max to avoid overcrowding
                .map { it.first } // Extract just the packageName
            
            uniqueApps.forEach { packageName ->
                val item = RemoteViews(context.packageName, R.layout.custom_notification_icon_item)
                val bitmap = getAppIconBitmap(context, packageName)
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
            .setSilent(true)
            .setContentIntent(createAppPendingIntent(context))
            .build()
    }

    private fun createAppPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0

        return TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(0, flags)
        } ?: PendingIntent.getActivity(context, 0, intent, flags)
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

    /**
     * Save bitmap to gallery using MediaStore API
     * Works on Android 10+ (API 29+) with scoped storage
     */
    suspend fun saveBitmapToGallery(context: Context, bitmap: Bitmap, fileName: String = "notification_image_${System.currentTimeMillis()}.jpg"): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/NotificationImages")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                }

                val uri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                ) ?: return@withContext false

                context.contentResolver.openOutputStream(uri)?.use { outputStream: OutputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    context.contentResolver.update(uri, contentValues, null, null)
                }

                // Notify media scanner
                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                mediaScanIntent.data = uri
                context.sendBroadcast(mediaScanIntent)

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.image_saved_to_gallery) ?: "Image saved to gallery",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                Log.d("ImageSave", "Image saved successfully: $uri")
                true
            } catch (e: Exception) {
                Log.e("ImageSave", "Error saving image to gallery", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.failed_to_save_image) ?: "Failed to save image: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                false
            }
        }
    }

    /**
     * Save bitmap from base64 string to gallery
     */
    suspend fun saveBase64ImageToGallery(context: Context, base64String: String, fileName: String = "notification_image_${System.currentTimeMillis()}.jpg"): Boolean {
        return try {
            val bitmap = base64ToBitmap(base64String)
            if (bitmap != null) {
                saveBitmapToGallery(context, bitmap, fileName)
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.failed_to_decode_image) ?: "Failed to decode image",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                false
            }
        } catch (e: Exception) {
            Log.e("ImageSave", "Error saving base64 image", e)
            false
        }
    }


}