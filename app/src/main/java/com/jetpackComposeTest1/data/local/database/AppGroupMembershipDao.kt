package com.jetpackComposeTest1.data.local.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppGroupMembershipDao {
    
    // Insert operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembership(membership: AppGroupMembershipEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemberships(memberships: List<AppGroupMembershipEntity>)
    
    // Update operations
    @Update
    suspend fun updateMembership(membership: AppGroupMembershipEntity)
    
    @Query("UPDATE app_group_memberships SET isActive = :isActive WHERE packageName = :packageName AND groupId = :groupId")
    suspend fun updateMembershipActiveStatus(packageName: String, groupId: String, isActive: Boolean)
    
    @Query("UPDATE app_group_memberships SET isMuted = :isMuted WHERE packageName = :packageName AND groupId = :groupId")
    suspend fun updateMembershipMuteStatus(packageName: String, groupId: String, isMuted: Boolean)
    
    @Query("UPDATE app_group_memberships SET priority = :priority WHERE packageName = :packageName AND groupId = :groupId")
    suspend fun updateMembershipPriority(packageName: String, groupId: String, priority: Int)
    
    // Query operations
    @Query("""
        SELECT agm.*, aa.appName, aa.isSystemApp 
        FROM app_group_memberships agm
        JOIN all_apps aa ON agm.packageName = aa.packageName
        WHERE agm.groupId = :groupId AND agm.isActive = 1
        ORDER BY agm.priority DESC, aa.appName ASC
    """)
    fun getActiveAppsInGroup(groupId: String): Flow<List<AppGroupMembershipWithApp>>
    
    @Query("""
        SELECT agm.*, ng.name as groupName, ng.colorHex, ng.iconName
        FROM app_group_memberships agm
        JOIN notification_groups ng ON agm.groupId = ng.id
        WHERE agm.packageName = :packageName AND agm.isActive = 1
        ORDER BY ng.priority DESC, ng.name ASC
    """)
    fun getActiveGroupsForApp(packageName: String): Flow<List<AppGroupMembershipWithGroup>>
    
    @Query("SELECT * FROM app_group_memberships WHERE packageName = :packageName AND groupId = :groupId")
    suspend fun getMembership(packageName: String, groupId: String): AppGroupMembershipEntity?
    
    @Query("SELECT COUNT(*) FROM app_group_memberships WHERE groupId = :groupId AND isActive = 1")
    fun getActiveAppCountInGroup(groupId: String): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM app_group_memberships WHERE packageName = :packageName AND isActive = 1")
    fun getActiveGroupCountForApp(packageName: String): Flow<Int>
    
    // Statistics queries
    @Query("SELECT packageName, COUNT(*) as groupCount FROM app_group_memberships WHERE isActive = 1 GROUP BY packageName ORDER BY groupCount DESC")
    fun getAppGroupCounts(): Flow<List<AppGroupCount>>
    
    @Query("SELECT groupId, COUNT(*) as appCount FROM app_group_memberships WHERE isActive = 1 GROUP BY groupId ORDER BY appCount DESC")
    fun getGroupAppCounts(): Flow<List<GroupAppCount>>
    
    // Delete operations
    @Delete
    suspend fun deleteMembership(membership: AppGroupMembershipEntity)
    
    @Query("DELETE FROM app_group_memberships WHERE packageName = :packageName AND groupId = :groupId")
    suspend fun deleteMembership(packageName: String, groupId: String)
    
    @Query("DELETE FROM app_group_memberships WHERE groupId = :groupId")
    suspend fun deleteAllMembershipsForGroup(groupId: String)
    
    @Query("DELETE FROM app_group_memberships WHERE packageName = :packageName")
    suspend fun deleteAllMembershipsForApp(packageName: String)
    
    // Bulk operations
    @Query("UPDATE app_group_memberships SET isActive = :isActive WHERE packageName = :packageName AND groupId IN (:groupIds)")
    suspend fun bulkUpdateMembershipActiveStatus(packageName: String, groupIds: List<String>, isActive: Boolean)
    
    @Query("DELETE FROM app_group_memberships WHERE packageName IN (:packageNames) AND groupId = :groupId")
    suspend fun bulkDeleteMembershipsForGroup(packageNames: List<String>, groupId: String)
    
    @Query("DELETE FROM app_group_memberships WHERE packageName = :packageName AND groupId IN (:groupIds)")
    suspend fun bulkDeleteMembershipsForApp(packageName: String, groupIds: List<String>)
}

// Data classes for joined queries
data class AppGroupMembershipWithApp(
    val id: Long,
    val packageName: String,
    val groupId: String,
    val addedAt: Long,
    val isActive: Boolean,
    val addedBy: String,
    val userNotes: String?,
    val priority: Int,
    val isMuted: Boolean,
    val notificationCount: Int,
    val lastNotificationAt: Long?,
    val isSynced: Boolean,
    val syncTime: Long?,
    val backupId: String?,
    val appName: String,
    val isSystemApp: Boolean
)

data class AppGroupMembershipWithGroup(
    val id: Long,
    val packageName: String,
    val groupId: String,
    val addedAt: Long,
    val isActive: Boolean,
    val addedBy: String,
    val userNotes: String?,
    val priority: Int,
    val isMuted: Boolean,
    val notificationCount: Int,
    val lastNotificationAt: Long?,
    val isSynced: Boolean,
    val syncTime: Long?,
    val backupId: String?,
    val groupName: String,
    val colorHex: String,
    val iconName: String
)

data class AppGroupCount(
    val packageName: String,
    val groupCount: Int
)

data class GroupAppCount(
    val groupId: String,
    val appCount: Int
)
