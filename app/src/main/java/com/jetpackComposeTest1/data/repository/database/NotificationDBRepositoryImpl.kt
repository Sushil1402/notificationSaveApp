package com.jetpackComposeTest1.data.repository.database

import com.jetpackComposeTest1.db.NotificationDao
import com.jetpackComposeTest1.db.NotificationEntity
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
}