package com.jetpackComposeTest1

import android.app.Application
import com.jetpackComposeTest1.utils.NotificationUtils.createNotificationAliveServiceChannel
import com.jetpackComposeTest1.utils.NotificationUtils.createNotificationChannel

import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NotificationApplication:Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(this)
        createNotificationAliveServiceChannel(this)

    }

}