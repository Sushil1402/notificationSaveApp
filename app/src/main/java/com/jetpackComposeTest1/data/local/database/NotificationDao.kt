package com.jetpackComposeTest1.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    // Insert operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notificationEntity: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    // Update operations
    @Update
    suspend fun updateNotification(notificationEntity: NotificationEntity)

    @Query("UPDATE notifications SET isRead = :isRead WHERE id = :id")
    suspend fun updateReadStatus(id: Long, isRead: Boolean)

    @Query("UPDATE notifications SET isArchived = :isArchived WHERE id = :id")
    suspend fun updateArchiveStatus(id: Long, isArchived: Boolean)

    @Query("UPDATE notifications SET isPinned = :isPinned WHERE id = :id")
    suspend fun updatePinStatus(id: Long, isPinned: Boolean)

    @Query("UPDATE notifications SET isMuted = :isMuted WHERE id = :id")
    suspend fun updateMuteStatus(id: Long, isMuted: Boolean)

    @Query("UPDATE notifications SET groupType = :groupType WHERE id = :id")
    suspend fun updateGroupType(id: Long, groupType: String)

    @Query("UPDATE notifications SET userGroup = :userGroup WHERE id = :id")
    suspend fun updateUserGroup(id: Long, userGroup: String)

    @Query("UPDATE notifications SET notes = :notes WHERE id = :id")
    suspend fun updateNotes(id: Long, notes: String)

    @Query("UPDATE notifications SET viewCount = viewCount + 1, lastViewedAt = :timestamp WHERE id = :id")
    suspend fun incrementViewCount(id: Long, timestamp: Long)

    // Query operations
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE isRead = :isRead ORDER BY timestamp DESC")
    fun getNotificationsByReadStatus(isRead: Boolean): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE isArchived = :isArchived ORDER BY timestamp DESC")
    fun getNotificationsByArchiveStatus(isArchived: Boolean): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE isPinned = :isPinned ORDER BY timestamp DESC")
    fun getNotificationsByPinStatus(isPinned: Boolean): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE isMuted = :isMuted ORDER BY timestamp DESC")
    fun getNotificationsByMuteStatus(isMuted: Boolean): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE groupType = :groupType ORDER BY timestamp DESC")
    fun getNotificationsByGroupType(groupType: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE userGroup = :userGroup ORDER BY timestamp DESC")
    fun getNotificationsByUserGroup(userGroup: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE packageName = :packageName ORDER BY timestamp DESC")
    fun getNotificationsByPackage(packageName: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE appName LIKE '%' || :appName || '%' ORDER BY timestamp DESC")
    fun searchNotificationsByAppName(appName: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE title LIKE '%' || :query || '%' OR text LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchNotifications(query: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getNotificationsByTimeRange(startTime: Long, endTime: Long): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE priority >= :minPriority ORDER BY timestamp DESC")
    fun getNotificationsByPriority(minPriority: Int): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE hasMedia = :hasMedia ORDER BY timestamp DESC")
    fun getNotificationsByMediaStatus(hasMedia: Boolean): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE isGroup = :isGroup ORDER BY timestamp DESC")
    fun getNotificationsByGroupStatus(isGroup: Boolean): Flow<List<NotificationEntity>>

    // Grouped notifications (latest from each app)
    @Query("""
        SELECT * FROM notifications 
        WHERE id IN (
            SELECT MAX(id) 
            FROM notifications 
            WHERE isArchived = 0
            GROUP BY packageName
        )
        ORDER BY timestamp DESC
    """)
    fun getGroupedNotifications(): Flow<List<NotificationEntity>>

    // Statistics queries
    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0 AND isArchived = 0")
    fun getUnreadCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notifications WHERE packageName = :packageName")
    fun getNotificationCountByPackage(packageName: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM notifications WHERE groupType = :groupType")
    fun getNotificationCountByGroupType(groupType: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM notifications WHERE timestamp >= :startTime")
    fun getNotificationCountSince(startTime: Long): Flow<Int>

    @Query("SELECT DISTINCT packageName FROM notifications ORDER BY packageName")
    fun getDistinctPackageNames(): Flow<List<String>>

    @Query("SELECT DISTINCT groupType FROM notifications ORDER BY groupType")
    fun getDistinctGroupTypes(): Flow<List<String>>

    @Query("SELECT DISTINCT userGroup FROM notifications WHERE userGroup IS NOT NULL ORDER BY userGroup")
    fun getDistinctUserGroups(): Flow<List<String>>

    // Delete operations
    @Delete
    suspend fun deleteNotification(notificationEntity: NotificationEntity)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotificationById(id: Long)

    @Query("DELETE FROM notifications WHERE packageName = :packageName")
    suspend fun deleteNotificationsByPackage(packageName: String)

    @Query("DELETE FROM notifications WHERE groupType = :groupType")
    suspend fun deleteNotificationsByGroupType(groupType: String)

    @Query("DELETE FROM notifications WHERE timestamp < :timestamp")
    suspend fun deleteOldNotifications(timestamp: Long)

    @Query("DELETE FROM notifications WHERE expiresAt IS NOT NULL AND expiresAt < :currentTime")
    suspend fun deleteExpiredNotifications(currentTime: Long)

    @Query("DELETE FROM notifications")
    suspend fun clearAll()

    // Bulk operations
    @Query("UPDATE notifications SET isRead = :isRead WHERE id IN (:ids)")
    suspend fun bulkUpdateReadStatus(ids: List<Long>, isRead: Boolean)

    @Query("UPDATE notifications SET isArchived = :isArchived WHERE id IN (:ids)")
    suspend fun bulkUpdateArchiveStatus(ids: List<Long>, isArchived: Boolean)

    @Query("DELETE FROM notifications WHERE id IN (:ids)")
    suspend fun bulkDeleteNotifications(ids: List<Long>)

    // Analytics queries - using data classes instead of Map
    @Query("SELECT packageName, COUNT(*) as count FROM notifications GROUP BY packageName ORDER BY count DESC")
    fun getTopAppsByNotificationCount(): Flow<List<AppNotificationCount>>

    @Query("SELECT DATE(timestamp/1000, 'unixepoch') as date, COUNT(*) as count FROM notifications GROUP BY date ORDER BY date DESC")
    fun getNotificationCountByDate(): Flow<List<DateNotificationCount>>

    @Query("SELECT groupType, COUNT(*) as count FROM notifications GROUP BY groupType ORDER BY count DESC")
    fun getNotificationCountByGroupType(): Flow<List<GroupTypeNotificationCount>>
}

// Data classes for analytics queries
data class AppNotificationCount(
    val packageName: String,
    val count: Int
)

data class DateNotificationCount(
    val date: String,
    val count: Int
)

data class GroupTypeNotificationCount(
    val groupType: String,
    val count: Int
)