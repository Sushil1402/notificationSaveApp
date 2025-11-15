package com.notistorex.app.services

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.notistorex.app.data.repository.database.NotificationDBRepository
import com.notistorex.app.db.NotificationDao
import com.notistorex.app.ui.utils.NotificationUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class NotificationForegroundService : Service() {

    @Inject
    lateinit var notificationDBRepo: NotificationDBRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundServiceWithObserver()
        return START_STICKY
    }

    private fun startForegroundServiceWithObserver() {
        // Start initial notification
        val initialNotification = NotificationUtils.buildForegroundNotificationWithIconsAndCount(this, emptyList())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NotificationUtils.NOTIFICATION_ID,
                initialNotification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NotificationUtils.NOTIFICATION_ID, initialNotification)
        }

        // Observe DB updates
        serviceScope.launch {
            notificationDBRepo.getAllNotifications()
                .map { list -> list.filter { it.text.isNotBlank() } }
                .distinctUntilChanged()
                .collect { messages ->
                    val notification = NotificationUtils.buildForegroundNotificationWithIconsAndCount(
                        this@NotificationForegroundService,
                        messages
                    )
                    startForeground(NotificationUtils.NOTIFICATION_ID, notification)
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }


}

