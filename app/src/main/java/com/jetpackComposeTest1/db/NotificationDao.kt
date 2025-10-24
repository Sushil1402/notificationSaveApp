package com.jetpackComposeTest1.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notificationEntity: NotificationEntity)

    @Query(
        """
    SELECT * FROM NotificationEntity 
    WHERE timestamp IN (
        SELECT MAX(timestamp) 
        FROM NotificationEntity 
        GROUP BY packageId
    )
    ORDER BY timestamp DESC
"""
    )
    fun getGroupedNotifications(): Flow<List<NotificationEntity>>

    @Query("""
    SELECT * FROM NotificationEntity
    ORDER BY timestamp DESC
    LIMIT CASE WHEN :limit > 0 THEN :limit ELSE -1 END
""")
    fun getAllNotifications(limit: Int = -1): Flow<List<NotificationEntity>>


    @Query("DELETE FROM NotificationEntity")
    suspend fun clearAll()

}