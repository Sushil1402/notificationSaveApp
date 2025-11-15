package com.notistorex.app.data.repository.database

import com.notistorex.app.db.NotificationDao
import com.notistorex.app.db.NotificationEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NotificationDBRepositoryImpl @Inject constructor(private val notificationDao:NotificationDao) :NotificationDBRepository {

    override suspend fun insertNotification(notificationEntity: NotificationEntity) {
        notificationDao.insertNotification(notificationEntity)
    }

    override suspend fun insertNotifications(notifications: List<NotificationEntity>) {
        notificationDao.insertNotifications(notifications)
    }

    override fun getAllNotifications(): Flow<List<NotificationEntity>> {
        return notificationDao.getAllNotifications()
    }

    override fun getNotificationsByReadStatus(isRead: Boolean): Flow<List<NotificationEntity>> {
        return notificationDao.getNotificationsByReadStatus(isRead = isRead)
    }

    override fun getNotificationsByPackageName(packageName: String): Flow<List<NotificationEntity>> {
        return notificationDao.getNotificationsByPackage(packageName)
    }

    override suspend fun getNotificationById(id: Long): NotificationEntity? {
        return notificationDao.getNotificationById(id)
    }

    override suspend fun markAsRead(id: Long) {
        notificationDao.updateReadStatus(id, true)
    }

    override suspend fun updateReadStatus(id: Long, isRead: Boolean) {
        notificationDao.updateReadStatus(id, isRead)
    }

    override suspend fun deleteNotification(id: Long) {
        notificationDao.deleteNotificationById(id)
    }

    override suspend fun clearAllNotifications() {
        notificationDao.clearAll()
    }
}