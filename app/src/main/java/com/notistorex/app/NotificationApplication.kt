package com.notistorex.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.notistorex.app.ui.utils.NotificationUtils.createNotificationAliveServiceChannel
import com.notistorex.app.ui.utils.NotificationUtils.createNotificationChannel
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class NotificationApplication: Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase (though it auto-initializes with google-services plugin,
        // this ensures it's initialized early for Crashlytics)
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }
        
        // Initialize Firebase Crashlytics
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        
        MobileAds.initialize(this) {}
        createNotificationChannel(this)
        createNotificationAliveServiceChannel(this)
    }


    override val workManagerConfiguration: Configuration
        get() =  Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}