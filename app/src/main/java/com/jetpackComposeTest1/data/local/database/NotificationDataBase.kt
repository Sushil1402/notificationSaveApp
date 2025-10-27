package com.jetpackComposeTest1.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jetpackComposeTest1.data.local.database.AllAppDao
import com.jetpackComposeTest1.data.local.database.AllAppEntity

@Database(entities = [NotificationEntity::class, AllAppEntity::class], version = 1)
abstract class NotificationDataBase : RoomDatabase() {
    abstract val notificationDao: NotificationDao
    abstract val allAppDao: AllAppDao
}