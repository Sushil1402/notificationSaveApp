package com.jetpackComposeTest1.data.local.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationGroupDao {
    
    // Insert operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: NotificationGroupEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroups(groups: List<NotificationGroupEntity>)
    
    // Update operations
    @Update
    suspend fun updateGroup(group: NotificationGroupEntity)
    
    @Query("UPDATE notification_groups SET name = :name, updatedAt = :timestamp WHERE id = :groupId")
    suspend fun updateGroupName(groupId: String, name: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE notification_groups SET isMuted = :isMuted, updatedAt = :timestamp WHERE id = :groupId")
    suspend fun updateGroupMuteStatus(groupId: String, isMuted: Boolean, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE notification_groups SET isActive = :isActive, updatedAt = :timestamp WHERE id = :groupId")
    suspend fun updateGroupActiveStatus(groupId: String, isActive: Boolean, timestamp: Long = System.currentTimeMillis())
    
    // Query operations
    @Query("SELECT * FROM notification_groups WHERE isActive = 1 ORDER BY priority DESC, createdAt ASC")
    fun getAllActiveGroups(): Flow<List<NotificationGroupEntity>>
    
    @Query("SELECT * FROM notification_groups WHERE groupType = :groupType ORDER BY priority DESC, createdAt ASC")
    fun getGroupsByType(groupType: String): Flow<List<NotificationGroupEntity>>
    
    @Query("SELECT * FROM notification_groups WHERE id = :groupId")
    suspend fun getGroupById(groupId: String): NotificationGroupEntity?
    
    @Query("SELECT * FROM notification_groups WHERE name LIKE '%' || :query || '%' ORDER BY priority DESC, createdAt ASC")
    fun searchGroups(query: String): Flow<List<NotificationGroupEntity>>
    
    // Statistics queries
    @Query("SELECT COUNT(*) FROM notification_groups WHERE isActive = 1")
    fun getActiveGroupCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM notification_groups WHERE groupType = :groupType")
    fun getGroupCountByType(groupType: String): Flow<Int>
    
    // Delete operations
    @Delete
    suspend fun deleteGroup(group: NotificationGroupEntity)
    
    @Query("DELETE FROM notification_groups WHERE id = :groupId")
    suspend fun deleteGroupById(groupId: String)
    
    @Query("DELETE FROM notification_groups WHERE groupType = :groupType")
    suspend fun deleteGroupsByType(groupType: String)
    
    // Bulk operations
    @Query("UPDATE notification_groups SET isActive = :isActive WHERE id IN (:groupIds)")
    suspend fun bulkUpdateActiveStatus(groupIds: List<String>, isActive: Boolean)
    
    @Query("DELETE FROM notification_groups WHERE id IN (:groupIds)")
    suspend fun bulkDeleteGroups(groupIds: List<String>)
}
