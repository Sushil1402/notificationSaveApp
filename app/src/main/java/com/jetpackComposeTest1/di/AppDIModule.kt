package com.jetpackComposeTest1.di

import android.content.Context
import androidx.room.Room
import com.jetpackComposeTest1.db.NotificationDao
import com.jetpackComposeTest1.db.NotificationDataBase
import com.jetpackComposeTest1.utils.Constants
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
    fun provideRowSensorDao(db: NotificationDataBase):NotificationDao  = db.notificationDao

}