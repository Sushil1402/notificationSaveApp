package com.jetpackComposeTest1.di

import android.content.Context
import androidx.room.Room
import com.jetpackComposeTest1.data.repository.database.AllAppRepository
import com.jetpackComposeTest1.data.repository.database.NotificationDBRepository
import com.jetpackComposeTest1.data.repository.database.NotificationDBRepositoryImpl
import com.jetpackComposeTest1.data.repository.database.NotificationGroupRepository
import com.jetpackComposeTest1.data.local.database.AllAppDao
import com.jetpackComposeTest1.data.local.database.NotificationGroupDao
import com.jetpackComposeTest1.data.local.database.AppGroupMembershipDao
import com.jetpackComposeTest1.data.local.database.DatabaseMigrations
import com.jetpackComposeTest1.db.NotificationDao
import com.jetpackComposeTest1.db.NotificationDataBase
import com.jetpackComposeTest1.data.local.preferences.AppPreferences
import com.jetpackComposeTest1.data.repository.preferences.SharedPreferencesRepoImpl
import com.jetpackComposeTest1.data.repository.preferences.SharedPreferencesRepository
import com.jetpackComposeTest1.ui.utils.Constants
import com.jetpackComposeTest1.utils.CleanupManager
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