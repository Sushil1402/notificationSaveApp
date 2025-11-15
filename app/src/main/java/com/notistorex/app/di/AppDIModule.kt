package com.notistorex.app.di

import android.content.Context
import androidx.room.Room
import com.notistorex.app.data.repository.database.AllAppRepository
import com.notistorex.app.data.repository.database.NotificationDBRepository
import com.notistorex.app.data.repository.database.NotificationDBRepositoryImpl
import com.notistorex.app.data.repository.database.NotificationGroupRepository
import com.notistorex.app.data.local.database.AllAppDao
import com.notistorex.app.data.local.database.NotificationGroupDao
import com.notistorex.app.data.local.database.AppGroupMembershipDao
import com.notistorex.app.data.local.database.DatabaseMigrations
import com.notistorex.app.db.NotificationDao
import com.notistorex.app.db.NotificationDataBase
import com.notistorex.app.data.local.preferences.AppPreferences
import com.notistorex.app.data.repository.preferences.SharedPreferencesRepoImpl
import com.notistorex.app.data.repository.preferences.SharedPreferencesRepository
import com.notistorex.app.ui.utils.Constants
import com.notistorex.app.utils.CleanupManager
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
        )
        .addMigrations(DatabaseMigrations.MIGRATION_1_2)
        .build()
    }

    @Provides
    @Singleton
    fun provideNotificationDao(db: NotificationDataBase): NotificationDao = db.notificationDao

    @Provides
    @Singleton
    fun provideAllAppDao(db: NotificationDataBase): AllAppDao = db.allAppDao

    @Provides
    @Singleton
    fun provideNotificationGroupDao(db: NotificationDataBase): NotificationGroupDao = db.notificationGroupDao

    @Provides
    @Singleton
    fun provideAppGroupMembershipDao(db: NotificationDataBase): AppGroupMembershipDao = db.appGroupMembershipDao

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
    fun provideNotificationGroupRepository(
        groupDao: NotificationGroupDao,
        membershipDao: AppGroupMembershipDao
    ): NotificationGroupRepository {
        return NotificationGroupRepository(groupDao, membershipDao)
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

    @Provides
    @Singleton
    fun provideCleanupManager(@ApplicationContext context: Context): CleanupManager {
        return CleanupManager(context)
    }

}