package com.jetpackComposeTest1.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [NotificationEntity::class], version = 1)
abstract class NotificationDataBase : RoomDatabase() {
    abstract val notificationDao: NotificationDao
}