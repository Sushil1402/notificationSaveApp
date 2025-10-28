package com.jetpackComposeTest1.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jetpackComposeTest1.data.local.database.AllAppDao
import com.jetpackComposeTest1.data.local.database.AllAppEntity
import com.jetpackComposeTest1.data.local.database.NotificationGroupDao
import com.jetpackComposeTest1.data.local.database.NotificationGroupEntity
import com.jetpackComposeTest1.data.local.database.AppGroupMembershipDao
import com.jetpackComposeTest1.data.local.database.AppGroupMembershipEntity

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