package com.jetpackComposeTest1.data.repository.database

import com.jetpackComposeTest1.db.NotificationEntity
import kotlinx.coroutines.flow.Flow

interface NotificationDBRepository {
    suspend fun insertNotification(notificationEntity: NotificationEntity)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)
    fun getAllNotifications(): Flow<List<NotificationEntity>>
    fun getNotificationsByReadStatus(isRead: Boolean): Flow<List<NotificationEntity>>
    
    // New methods for notification detail screen
    fun getNotificationsByPackageName(packageName: String): Flow<List<NotificationEntity>>
    suspend fun markAsRead(id: Long)
    suspend fun deleteNotification(id: Long)
    suspend fun clearAllNotifications()
}