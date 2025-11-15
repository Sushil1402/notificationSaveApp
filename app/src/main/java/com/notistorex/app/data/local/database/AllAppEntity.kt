package com.notistorex.app.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "all_apps")
data class AllAppEntity(
    @PrimaryKey
    val packageName: String,           // App package name (e.g., com.whatsapp)
    
    val appName: String,               // App display name (e.g., WhatsApp)
    val isEnabled: Boolean = false,    // Whether notifications from this app should be saved
    val isSystemApp: Boolean = false,  // Whether this is a system app
    val isUserApp: Boolean = true,     // Whether this is a user-installed app
    
    // App metadata
    val versionName: String?,          // App version name
    val versionCode: Long?,            // App version code
    val targetSdkVersion: Int?,        // Target SDK version
    val minSdkVersion: Int?,           // Minimum SDK version
    
    // App categorization
    val category: String?,             // App category (social, productivity, etc.)
    val groupType: String = "default", // Custom group type
    
    // Timestamps
    val firstInstalledAt: Long?,      // When app was first installed
    val lastUpdatedAt: Long?,          // When app was last updated
    val addedToDatabaseAt: Long = System.currentTimeMillis(), // When added to our database
    
    // App permissions and capabilities
    val hasNotificationPermission: Boolean = false, // Whether app can send notifications
    val canSendNotifications: Boolean = true,      // Whether app is capable of sending notifications
    
    // User preferences
    val userNotes: String?,            // User notes about this app
    val userTags: String?,             // User-defined tags
    val priority: Int = 0,            // User-defined priority (0 = normal, 1 = high, -1 = low)
    
    // Analytics
    val notificationCount: Int = 0,   // Total notifications received from this app
    val lastNotificationAt: Long?,     // When last notification was received
    val isFrequentlyUsed: Boolean = false, // Whether this app sends frequent notifications
    
    // App icon and visual info
    val iconResourceId: Int?,          // App icon resource ID
    val iconPath: String?,             // Path to app icon (if stored locally)
    val appColor: Int?,                // App's primary color
    
    // Additional metadata
    val installSource: String?,        // Where app was installed from (Play Store, etc.)
    val appSize: Long?,                // App size in bytes
    val dataUsage: Long?,              // Data usage by this app
    
    // Sync and backup
    val isSynced: Boolean = false,     // Has been synced to backup
    val syncTime: Long?,               // When it was last synced
    val backupId: String?              // ID in backup system
)
