package com.jetpackComposeTest1.services

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.jetpackComposeTest1.db.NotificationDao
import com.jetpackComposeTest1.db.NotificationEntity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyNotificationListener :
    NotificationListenerService() {

    @Inject
    lateinit var notificationDao: NotificationDao

    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        val notificationsFlow = MutableSharedFlow<String>(replay = 0)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("Notification", "Listener connected ✅")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val title = sbn.notification.extras.getString("android.title")
        val text = sbn.notification.extras.getCharSequence("android.text")?.toString()
        val pm = applicationContext.packageManager
        val appIcon = try {
            pm.getApplicationIcon(packageName)
        } catch (e: Exception) {
            null
        }

        // Optional: Get app name
        val appName = try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }

        scope.launch {
            if(!isSystemNotification(packageName)){
                notificationDao.insertNotification(
                    NotificationEntity(
                        packageName =appName,
                        packageId =packageName,
                        icon =appIcon.toString(),
                        messageTitle =title?:"",
                        message = text?:""
                    )
                )
            }

        }


        // Emit notification to Flow
        notificationsFlow.tryEmit("Message")
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.d("Notification", "Removed: ${sbn.packageName}")
    }


    private fun isSystemNotification(packageName: String): Boolean {
        return packageName == "com.android.systemui"

    }
}
