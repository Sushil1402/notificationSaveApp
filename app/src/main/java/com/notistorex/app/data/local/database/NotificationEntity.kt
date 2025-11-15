package com.notistorex.app.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Basic notification info
    val packageName: String,           // App package name (e.g., com.whatsapp)
    val appName: String,               // App display name (e.g., WhatsApp)
    val notificationId: Int,           // Original notification ID from system
    val tag: String?,                  // Notification tag if present
    
    // Notification content
    val title: String,                 // Notification title
    val text: String,                  // Notification text/content
    val subText: String?,              // Sub-text if present
    val summaryText: String?,          // Summary text for grouped notifications
    val bigText: String?,              // Big text content
    
    // Notification metadata
    val timestamp: Long = System.currentTimeMillis(), // When notification was received
    val postTime: Long,                // When notification was posted by app
    val isGroup: Boolean = false,      // Is this a group notification
    val groupKey: String?,             // Group key for grouped notifications
    val sortKey: String?,              // Sort key for ordering
    
    // Status fields
    val isRead: Boolean = false,       // Has user read this notification
    val isArchived: Boolean = false,   // Is notification archived
    val isPinned: Boolean = false,     // Is notification pinned
    val isMuted: Boolean = false,      // Is notification muted
    
    // Group/Category management
    val groupType: String = "default", // Custom group type (work, social, etc.)
    val category: String?,             // Notification category
    val channelId: String?,            // Notification channel ID
    val channelName: String?,          // Notification channel name
    
    // Priority and importance
    val priority: Int = 0,             // Notification priority
    val importance: Int = 0,           // Notification importance level
    
    // Media and actions
    val hasMedia: Boolean = false,     // Contains media content
    val mediaType: String?,            // Type of media (image, video, audio)
    val hasActions: Boolean = false,   // Has action buttons
    val actions: String?,              // Serialized action buttons
    
    // User interaction
    val isDismissed: Boolean = false,  // Was notification dismissed by user
    val isCleared: Boolean = false,    // Was notification cleared
    val clickTime: Long?,              // When user clicked on notification
    val dismissTime: Long?,            // When notification was dismissed
    
    // Search and filtering
    val searchableText: String,        // Combined searchable text
    val keywords: String?,             // Extracted keywords
    
    // Data retention
    val retentionDays: Int = 30,       // How many days to keep this notification
    val expiresAt: Long?,              // When this notification should be deleted
    
    // Additional metadata
    val extras: String?,               // Serialized notification extras
    val flags: Int = 0,                // Notification flags
    val color: Int?,                   // Notification color
    val smallIcon: String?,            // Small icon resource
    val largeIcon: String?,            // Large icon resource
    
    // User preferences
    val userGroup: String?,            // User-defined group
    val userTags: String?,             // User-defined tags
    val notes: String?,                // User notes about this notification
    
    // Analytics
    val viewCount: Int = 0,            // How many times user viewed this
    val lastViewedAt: Long?,           // Last time user viewed this notification
    
    // Sync and backup
    val isSynced: Boolean = false,     // Has been synced to backup
    val syncTime: Long?,               // When it was last synced
    val backupId: String?              // ID in backup system
)