package com.jetpackComposeTest1.data.repository.database

import com.jetpackComposeTest1.data.local.database.AllAppDao
import com.jetpackComposeTest1.data.local.database.AllAppEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AllAppRepository @Inject constructor(
    private val allAppDao: AllAppDao
) {
    
    // Insert operations
    suspend fun insertApp(appEntity: AllAppEntity) = allAppDao.insertApp(appEntity)
    
    suspend fun insertApps(apps: List<AllAppEntity>) = allAppDao.insertApps(apps)
    
    // Update operations
    suspend fun updateApp(appEntity: AllAppEntity) = allAppDao.updateApp(appEntity)
    
    suspend fun updateAppEnabledStatus(packageName: String, isEnabled: Boolean) = 
        allAppDao.updateAppEnabledStatus(packageName, isEnabled)
    
    suspend fun updateAllAppsEnabledStatus(isEnabled: Boolean) = 
        allAppDao.updateAllAppsEnabledStatus(isEnabled)
    
    suspend fun bulkUpdateEnabledStatus(packageNames: List<String>, isEnabled: Boolean) = 
        allAppDao.bulkUpdateEnabledStatus(packageNames, isEnabled)
    
    suspend fun incrementNotificationCount(packageName: String, timestamp: Long) = 
        allAppDao.incrementNotificationCount(packageName, timestamp)
    
    suspend fun updateUserNotes(packageName: String, notes: String) = 
        allAppDao.updateUserNotes(packageName, notes)
    
    suspend fun updateUserTags(packageName: String, tags: String) = 
        allAppDao.updateUserTags(packageName, tags)
    
    suspend fun updatePriority(packageName: String, priority: Int) = 
        allAppDao.updatePriority(packageName, priority)
    
    suspend fun updateGroupType(packageName: String, groupType: String) = 
        allAppDao.updateGroupType(packageName, groupType)
    
    // Query operations
    fun getAllApps(): Flow<List<AllAppEntity>> = allAppDao.getAllApps()
    
    fun getAppsByEnabledStatus(isEnabled: Boolean): Flow<List<AllAppEntity>> = 
        allAppDao.getAppsByEnabledStatus(isEnabled)
    
    fun getAppsBySystemStatus(isSystemApp: Boolean): Flow<List<AllAppEntity>> = 
        allAppDao.getAppsBySystemStatus(isSystemApp)
    
    fun getAppsByUserStatus(isUserApp: Boolean): Flow<List<AllAppEntity>> = 
        allAppDao.getAppsByUserStatus(isUserApp)
    
    fun getAppsByGroupType(groupType: String): Flow<List<AllAppEntity>> = 
        allAppDao.getAppsByGroupType(groupType)
    
    fun getAppsByCategory(category: String): Flow<List<AllAppEntity>> = 
        allAppDao.getAppsByCategory(category)
    
    fun searchApps(query: String): Flow<List<AllAppEntity>> = allAppDao.searchApps(query)
    
    suspend fun getAppByPackageName(packageName: String): AllAppEntity? = 
        allAppDao.getAppByPackageName(packageName)
    
    fun getAppByPackageNameFlow(packageName: String): Flow<AllAppEntity?> = 
        allAppDao.getAppByPackageNameFlow(packageName)
    
    fun getFrequentlyUsedApps(isEnabled: Boolean, isFrequentlyUsed: Boolean): Flow<List<AllAppEntity>> = 
        allAppDao.getFrequentlyUsedApps(isEnabled, isFrequentlyUsed)
    
    fun getAppsByPriority(priority: Int): Flow<List<AllAppEntity>> = 
        allAppDao.getAppsByPriority(priority)
    
    // Statistics
    fun getTotalAppCount(): Flow<Int> = allAppDao.getTotalAppCount()
    
    fun getEnabledAppCount(isEnabled: Boolean): Flow<Int> = 
        allAppDao.getEnabledAppCount(isEnabled)
    
    fun getSystemAppCount(isSystemApp: Boolean): Flow<Int> = 
        allAppDao.getSystemAppCount(isSystemApp)
    
    fun getUserAppCount(isUserApp: Boolean): Flow<Int> = 
        allAppDao.getUserAppCount(isUserApp)
    
    fun getAppCountByGroupType(groupType: String): Flow<Int> = 
        allAppDao.getAppCountByGroupType(groupType)
    
    fun getAppCountByCategory(category: String): Flow<Int> = 
        allAppDao.getAppCountByCategory(category)
    
    // Analytics
    fun getTopAppsByNotificationCount(limit: Int) = 
        allAppDao.getTopAppsByNotificationCount(limit)
    
    fun getAppCountByGroupType() = allAppDao.getAppCountByGroupType()
    
    fun getAppCountByCategory() = allAppDao.getAppCountByCategory()
    
    // Delete operations
    suspend fun deleteApp(appEntity: AllAppEntity) = allAppDao.deleteApp(appEntity)
    
    suspend fun deleteAppByPackageName(packageName: String) = 
        allAppDao.deleteAppByPackageName(packageName)
    
    suspend fun bulkDeleteApps(packageNames: List<String>) = 
        allAppDao.bulkDeleteApps(packageNames)
    
    suspend fun clearAllApps() = allAppDao.clearAllApps()
    
    // Utility operations
    suspend fun appExists(packageName: String): Boolean = 
        allAppDao.appExists(packageName)
    
    fun getEnabledPackageNames(): Flow<List<String>> = 
        allAppDao.getEnabledPackageNames()
    
    suspend fun getEnabledPackageNamesList(): List<String> = 
        allAppDao.getEnabledPackageNamesList()
}
