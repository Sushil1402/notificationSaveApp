package com.notistorex.app.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.notistorex.app.data.local.database.AllAppDao
import com.notistorex.app.data.local.database.AllAppEntity
import com.notistorex.app.data.local.database.NotificationGroupDao
import com.notistorex.app.data.local.database.NotificationGroupEntity
import com.notistorex.app.data.local.database.AppGroupMembershipDao
import com.notistorex.app.data.local.database.AppGroupMembershipEntity

@Database(
    entities = [
        NotificationEntity::class, 
        AllAppEntity::class,
        NotificationGroupEntity::class,
        AppGroupMembershipEntity::class
    ], 
    version = 2, // Increment version for new entities
    exportSchema = false
)
abstract class NotificationDataBase : RoomDatabase() {
    abstract val notificationDao: NotificationDao
    abstract val allAppDao: AllAppDao
    abstract val notificationGroupDao: NotificationGroupDao
    abstract val appGroupMembershipDao: AppGroupMembershipDao
}