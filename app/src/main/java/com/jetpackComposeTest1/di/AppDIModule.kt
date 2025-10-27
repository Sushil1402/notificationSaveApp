package com.jetpackComposeTest1.di

import android.content.Context
import androidx.room.Room
import com.jetpackComposeTest1.data.repository.database.AllAppRepository
import com.jetpackComposeTest1.data.repository.database.NotificationDBRepository
import com.jetpackComposeTest1.data.repository.database.NotificationDBRepositoryImpl
import com.jetpackComposeTest1.data.local.database.AllAppDao
import com.jetpackComposeTest1.db.NotificationDao
import com.jetpackComposeTest1.db.NotificationDataBase
import com.jetpackComposeTest1.data.local.preferences.AppPreferences
import com.jetpackComposeTest1.data.repository.preferences.SharedPreferencesRepoImpl
import com.jetpackComposeTest1.data.repository.preferences.SharedPreferencesRepository
import com.jetpackComposeTest1.ui.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AppDIModule {

    @Provides
    @Singleton
    fun provideRoomDb(@ApplicationContext context: Context): NotificationDataBase {
        return Room.databaseBuilder(
            context,
            NotificationDataBase::class.java,
            Constants.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideNotificationDao(db: NotificationDataBase): NotificationDao = db.notificationDao

    @Provides
    @Singleton
    fun provideAllAppDao(db: NotificationDataBase): AllAppDao = db.allAppDao

    @Provides
    @Singleton
    fun provideNotificationRepository(notificationDao: NotificationDao): NotificationDBRepository {
        return NotificationDBRepositoryImpl(notificationDao)
    }

    @Provides
    @Singleton
    fun provideAllAppRepository(allAppDao: AllAppDao): AllAppRepository {
        return AllAppRepository(allAppDao)
    }

    @Provides
    @Singleton
    fun provideAppPreferences(@ApplicationContext context: Context): AppPreferences {
        return AppPreferences(context)
    }


    @Provides
    @Singleton
    fun provideAppPreferencesRepo(appPreferences: AppPreferences):SharedPreferencesRepository{
        return SharedPreferencesRepoImpl(appPreferences)
    }

}