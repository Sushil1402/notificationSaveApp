package com.notistorex.app.data.repository.database

import com.notistorex.app.data.local.database.*
import com.notistorex.app.ui.utils.GroupIconUtils
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationGroupRepository @Inject constructor(
    private val groupDao: NotificationGroupDao,
    private val membershipDao: AppGroupMembershipDao,
) {
    
    // Group operations
    suspend fun createGroup(
        name: String,
        description: String? = null,
        iconName: String? = null, // Will be generated from name if null
        colorHex: String? = null, // Will be generated from name if null
        groupType: String = "custom",
        selectedApps: List<String> = emptyList()
    ): String {
        val groupId = "custom_${System.currentTimeMillis()}"
        
        // Generate color based on group name if not provided
        val finalColorHex = colorHex ?: generateColorFromName(name)
        
        // Generate initials for icon name if not provided
        val finalIconName = iconName ?: GroupIconUtils.generateInitials(name)
        
        // Create the group
        val group = NotificationGroupEntity(
            id = groupId,
            name = name,
            description = description,
            iconName = finalIconName,
            colorHex = finalColorHex,
            groupType = groupType,
            appCount = selectedApps.size
        )
        groupDao.insertGroup(group)
        
        // Add apps to the group
        if (selectedApps.isNotEmpty()) {
            val memberships = selectedApps.map { packageName ->
                AppGroupMembershipEntity(
                    packageName = packageName,
                    groupId = groupId
                )
            }
            membershipDao.insertMemberships(memberships)
        }
        
        return groupId
    }
    
    /**
     * Generates a consistent color hex string based on group name
     * This ensures the same group name always gets the same color
     */
    private fun generateColorFromName(name: String): String {
        val colors = listOf(
            "#2196F3", // Blue
            "#4CAF50", // Green
            "#FF9800", // Orange
            "#9C27B0", // Purple
            "#F44336", // Red
            "#00BCD4", // Cyan
            "#8BC34A", // Light Green
            "#FFC107", // Amber
            "#E91E63", // Pink
            "#607D8B"  // Blue Grey
        )
        
        val hash = name.hashCode()
        val colorIndex = kotlin.math.abs(hash) % colors.size
        return colors[colorIndex]
    }
    
    suspend fun updateGroupName(groupId: String, name: String) {
        groupDao.updateGroupName(groupId, name)
    }
    
    suspend fun deleteGroup(groupId: String) {
        // Delete all memberships first (due to foreign key constraints)
        membershipDao.deleteAllMembershipsForGroup(groupId)
        // Then delete the group
        groupDao.deleteGroupById(groupId)
    }
    
    // Membership operations
    suspend fun addAppToGroup(packageName: String, groupId: String) {
        val membership = AppGroupMembershipEntity(
            packageName = packageName,
            groupId = groupId
        )
        membershipDao.insertMembership(membership)
        
        // Update group app count
        updateGroupAppCount(groupId)
    }
    
    suspend fun removeAppFromGroup(packageName: String, groupId: String) {
        membershipDao.deleteMembership(packageName, groupId)
        
        // Update group app count
        updateGroupAppCount(groupId)
    }
    
    suspend fun addAppsToGroup(packageNames: List<String>, groupId: String) {
        val memberships = packageNames.map { packageName ->
            AppGroupMembershipEntity(
                packageName = packageName,
                groupId = groupId
            )
        }
        membershipDao.insertMemberships(memberships)
        
        // Update group app count
        updateGroupAppCount(groupId)
    }
    
    suspend fun removeAppsFromGroup(packageNames: List<String>, groupId: String) {
        membershipDao.bulkDeleteMembershipsForGroup(packageNames, groupId)
        
        // Update group app count
        updateGroupAppCount(groupId)
    }
    
    // Query operations
    fun getAllGroups(): Flow<List<NotificationGroupEntity>> {
        return groupDao.getAllActiveGroups()
    }
    
    fun getGroupsByType(groupType: String): Flow<List<NotificationGroupEntity>> {
        return groupDao.getGroupsByType(groupType)
    }
    
    fun getAppsInGroup(groupId: String): Flow<List<AppGroupMembershipWithApp>> {
        return membershipDao.getActiveAppsInGroup(groupId)
    }
    
    fun getGroupsForApp(packageName: String): Flow<List<AppGroupMembershipWithGroup>> {
        return membershipDao.getActiveGroupsForApp(packageName)
    }
    
    suspend fun getGroupById(groupId: String): NotificationGroupEntity? {
        return groupDao.getGroupById(groupId)
    }
    
    suspend fun isAppInGroup(packageName: String, groupId: String): Boolean {
        return membershipDao.getMembership(packageName, groupId) != null
    }
    
    // Statistics operations
    suspend fun updateGroupStatistics(groupId: String) {
        val group = groupDao.getGroupById(groupId) ?: return
        
        // Get all apps in this group
        val appsInGroup = membershipDao.getActiveAppsInGroup(groupId)
        
        // Calculate statistics from notifications
        // This would need to be implemented based on your notification filtering logic
        // For now, we'll just update the app count
        updateGroupAppCount(groupId)
    }
    
    private suspend fun updateGroupAppCount(groupId: String) {
        val appCount = membershipDao.getActiveAppCountInGroup(groupId)
        // Note: This is a Flow, so you'd need to collect it in a coroutine
        // For now, we'll handle this differently in the ViewModel
    }
    
    // Search operations
    fun searchGroups(query: String): Flow<List<NotificationGroupEntity>> {
        return groupDao.searchGroups(query)
    }
    
    // Bulk operations
    suspend fun bulkDeleteGroups(groupIds: List<String>) {
        groupIds.forEach { groupId ->
            membershipDao.deleteAllMembershipsForGroup(groupId)
        }
        groupDao.bulkDeleteGroups(groupIds)
    }
    
    suspend fun bulkUpdateGroupActiveStatus(groupIds: List<String>, isActive: Boolean) {
        groupDao.bulkUpdateActiveStatus(groupIds, isActive)
    }
}
