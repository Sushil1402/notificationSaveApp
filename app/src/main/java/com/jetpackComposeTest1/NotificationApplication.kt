package com.jetpackComposeTest1

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.jetpackComposeTest1.ui.utils.NotificationUtils.createNotificationAliveServiceChannel
import com.jetpackComposeTest1.ui.utils.NotificationUtils.createNotificationChannel
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class NotificationApplication: Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(this)
        createNotificationAliveServiceChannel(this)
    }


    override val workManagerConfiguration: Configuration
        get() =  Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}