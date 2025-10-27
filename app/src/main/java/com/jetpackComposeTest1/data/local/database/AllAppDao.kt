package com.jetpackComposeTest1.data.local.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AllAppDao {

    // Insert operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(appEntity: AllAppEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<AllAppEntity>)

    // Update operations
    @Update
    suspend fun updateApp(appEntity: AllAppEntity)

    @Query("UPDATE all_apps SET isEnabled = :isEnabled WHERE packageName = :packageName")
    suspend fun updateAppEnabledStatus(packageName: String, isEnabled: Boolean)

    @Query("UPDATE all_apps SET notificationCount = notificationCount + 1, lastNotificationAt = :timestamp WHERE packageName = :packageName")
    suspend fun incrementNotificationCount(packageName: String, timestamp: Long)

    @Query("UPDATE all_apps SET userNotes = :notes WHERE packageName = :packageName")
    suspend fun updateUserNotes(packageName: String, notes: String)

    @Query("UPDATE all_apps SET userTags = :tags WHERE packageName = :packageName")
    suspend fun updateUserTags(packageName: String, tags: String)

    @Query("UPDATE all_apps SET priority = :priority WHERE packageName = :packageName")
    suspend fun updatePriority(packageName: String, priority: Int)

    @Query("UPDATE all_apps SET groupType = :groupType WHERE packageName = :packageName")
    suspend fun updateGroupType(packageName: String, groupType: String)

    // Bulk update operations
    @Query("UPDATE all_apps SET isEnabled = :isEnabled")
    suspend fun updateAllAppsEnabledStatus(isEnabled: Boolean)

    @Query("UPDATE all_apps SET isEnabled = :isEnabled WHERE packageName IN (:packageNames)")
    suspend fun bulkUpdateEnabledStatus(packageNames: List<String>, isEnabled: Boolean)

    // Query operations
    @Query("SELECT * FROM all_apps ORDER BY appName ASC")
    fun getAllApps(): Flow<List<AllAppEntity>>

    @Query("SELECT * FROM all_apps WHERE isEnabled = :isEnabled ORDER BY appName ASC")
    fun getAppsByEnabledStatus(isEnabled: Boolean): Flow<List<AllAppEntity>>

    @Query("SELECT * FROM all_apps WHERE isSystemApp = :isSystemApp ORDER BY appName ASC")
    fun getAppsBySystemStatus(isSystemApp: Boolean): Flow<List<AllAppEntity>>

    @Query("SELECT * FROM all_apps WHERE isUserApp = :isUserApp ORDER BY appName ASC")
    fun getAppsByUserStatus(isUserApp: Boolean): Flow<List<AllAppEntity>>

    @Query("SELECT * FROM all_apps WHERE groupType = :groupType ORDER BY appName ASC")
    fun getAppsByGroupType(groupType: String): Flow<List<AllAppEntity>>

    @Query("SELECT * FROM all_apps WHERE category = :category ORDER BY appName ASC")
    fun getAppsByCategory(category: String): Flow<List<AllAppEntity>>

    @Query("SELECT * FROM all_apps WHERE appName LIKE '%' || :query || '%' OR packageName LIKE '%' || :query || '%' ORDER BY appName ASC")
    fun searchApps(query: String): Flow<List<AllAppEntity>>

    @Query("SELECT * FROM all_apps WHERE packageName = :packageName")
    suspend fun getAppByPackageName(packageName: String): AllAppEntity?

    @Query("SELECT * FROM all_apps WHERE packageName = :packageName")
    fun getAppByPackageNameFlow(packageName: String): Flow<AllAppEntity?>

    @Query("SELECT * FROM all_apps WHERE isEnabled = :isEnabled AND isFrequentlyUsed = :isFrequentlyUsed ORDER BY notificationCount DESC")
    fun getFrequentlyUsedApps(isEnabled: Boolean, isFrequentlyUsed: Boolean): Flow<List<AllAppEntity>>

    @Query("SELECT * FROM all_apps WHERE priority = :priority ORDER BY appName ASC")
    fun getAppsByPriority(priority: Int): Flow<List<AllAppEntity>>

    // Statistics queries
    @Query("SELECT COUNT(*) FROM all_apps")
    fun getTotalAppCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM all_apps WHERE isEnabled = :isEnabled")
    fun getEnabledAppCount(isEnabled: Boolean): Flow<Int>

    @Query("SELECT COUNT(*) FROM all_apps WHERE isSystemApp = :isSystemApp")
    fun getSystemAppCount(isSystemApp: Boolean): Flow<Int>

    @Query("SELECT COUNT(*) FROM all_apps WHERE isUserApp = :isUserApp")
    fun getUserAppCount(isUserApp: Boolean): Flow<Int>

    @Query("SELECT COUNT(*) FROM all_apps WHERE groupType = :groupType")
    fun getAppCountByGroupType(groupType: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM all_apps WHERE category = :category")
    fun getAppCountByCategory(category: String): Flow<Int>

    // Analytics queries
    @Query("SELECT packageName, notificationCount FROM all_apps WHERE notificationCount > 0 ORDER BY notificationCount DESC LIMIT :limit")
    fun getTopAppsByNotificationCount(limit: Int): Flow<List<AppNotificationStats>>

    @Query("SELECT groupType, COUNT(*) as count FROM all_apps GROUP BY groupType ORDER BY count DESC")
    fun getAppCountByGroupType(): Flow<List<GroupTypeAppCount>>

    @Query("SELECT category, COUNT(*) as count FROM all_apps WHERE category IS NOT NULL GROUP BY category ORDER BY count DESC")
    fun getAppCountByCategory(): Flow<List<CategoryAppCount>>

    // Delete operations
    @Delete
    suspend fun deleteApp(appEntity: AllAppEntity)

    @Query("DELETE FROM all_apps WHERE packageName = :packageName")
    suspend fun deleteAppByPackageName(packageName: String)

    @Query("DELETE FROM all_apps WHERE packageName IN (:packageNames)")
    suspend fun bulkDeleteApps(packageNames: List<String>)

    @Query("DELETE FROM all_apps")
    suspend fun clearAllApps()

    // Check if app exists
    @Query("SELECT EXISTS(SELECT 1 FROM all_apps WHERE packageName = :packageName)")
    suspend fun appExists(packageName: String): Boolean

    // Get enabled package names for notification filtering
    @Query("SELECT packageName FROM all_apps WHERE isEnabled = 1")
    fun getEnabledPackageNames(): Flow<List<String>>

    @Query("SELECT packageName FROM all_apps WHERE isEnabled = 1")
    suspend fun getEnabledPackageNamesList(): List<String>
}

// Data classes for analytics queries
data class AppNotificationStats(
    val packageName: String,
    val notificationCount: Int
)

data class GroupTypeAppCount(
    val groupType: String,
    val count: Int
)

data class CategoryAppCount(
    val category: String,
    val count: Int
)
