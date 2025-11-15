package com.notistorex.app.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_groups")
data class NotificationGroupEntity(
    @PrimaryKey
    val id: String,                    // Unique group ID (e.g., "custom_1234567890")
    
    val name: String,                  // Group name (e.g., "Work Apps")
    val description: String? = null,         // Group description
    val iconName: String = "face",     // Icon identifier (for UI)
    val colorHex: String = "#2196F3",  // Group color in hex format
    val groupType: String = "custom", // Type: "unread", "read", "muted", "custom"
    val isMuted: Boolean = false,      // Whether group is muted
    val isActive: Boolean = true,      // Whether group is active/enabled
    
    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    
    // User preferences
    val userNotes: String? = null,            // User notes about this group
    val priority: Int = 0,             // Group priority (0 = normal, 1 = high, -1 = low)
    
    // Analytics
    val totalNotifications: Int = 0,  // Total notifications in this group
    val unreadNotifications: Int = 0,  // Unread notifications in this group
    val todayNotifications: Int = 0,   // Notifications received today
    val appCount: Int = 0,             // Number of apps in this group
    
    // Sync and backup
    val isSynced: Boolean = false,     // Has been synced to backup
    val syncTime: Long? = null,               // When it was last synced
    val backupId: String? = null              // ID in backup system
)
