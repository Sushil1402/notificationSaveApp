package com.notistorex.app.data.local.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "app_group_memberships",
    foreignKeys = [
        ForeignKey(
            entity = AllAppEntity::class,
            parentColumns = ["packageName"],
            childColumns = ["packageName"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = NotificationGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["packageName"]),
        Index(value = ["groupId"]),
        Index(value = ["packageName", "groupId"], unique = true) // Prevent duplicate memberships
    ]
)
data class AppGroupMembershipEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val packageName: String,           // Foreign key to AllAppEntity
    val groupId: String,               // Foreign key to NotificationGroupEntity
    
    // Membership metadata
    val addedAt: Long = System.currentTimeMillis(), // When app was added to group
    val isActive: Boolean = true,      // Whether membership is active
    val addedBy: String = "user",      // Who added this app (user, auto, etc.)
    
    // User preferences for this specific membership
    val userNotes: String? = null,            // User notes about this app in this group
    val priority: Int = 0,             // Priority within this group
    val isMuted: Boolean = false,       // Whether this app is muted in this group
    
    // Analytics for this specific membership
    val notificationCount: Int = 0,   // Notifications from this app in this group
    val lastNotificationAt: Long? = null,     // Last notification from this app in this group
    
    // Sync and backup
    val isSynced: Boolean = false,     // Has been synced to backup
    val syncTime: Long? = null,               // When it was last synced
    val backupId: String? = null              // ID in backup system
)
