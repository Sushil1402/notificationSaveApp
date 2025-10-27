package com.jetpackComposeTest1.services

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.jetpackComposeTest1.data.repository.database.AllAppRepository
import com.jetpackComposeTest1.data.repository.database.NotificationDBRepository
import com.jetpackComposeTest1.db.NotificationDao
import com.jetpackComposeTest1.db.NotificationEntity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyNotificationListener :
    NotificationListenerService() {

    @Inject
    lateinit var notificationDBRepo: NotificationDBRepository
    
    @Inject
    lateinit var allAppRepository: AllAppRepository



    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        val notificationsFlow = MutableSharedFlow<String>(replay = 0)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        Log.d("find_error", "onNotificationPosted: ${sbn}")
        val packageName = sbn.packageName
        val title = sbn.notification.extras.getString("android.title")
        val text = sbn.notification.extras.getCharSequence("android.text")?.toString()
        val pm = applicationContext.packageManager
        val appIcon = try {
            pm.getApplicationIcon(packageName)
        } catch (e: Exception) {
            null
        }

        // Optional: Get app name
        val appName = try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }

        scope.launch {
            if (shouldSaveNotification(sbn, packageName, title, text)) {
                // Check if app is enabled for notification saving
                val appEntity = allAppRepository.getAppByPackageName(packageName)
                if (appEntity?.isEnabled != true) {
                    return@launch
                }
                
                val searchableText = "${title ?: ""} ${text ?: ""} ${appName}".trim()
                
                notificationDBRepo.insertNotification(
                    NotificationEntity(
                        // Basic notification info
                        packageName = packageName,
                        appName = appName,
                        notificationId = sbn.id,
                        tag = sbn.tag,
                        
                        // Notification content
                        title = title ?: "",
                        text = text ?: "",
                        subText = sbn.notification.extras.getString("android.subText"),
                        summaryText = sbn.notification.extras.getString("android.summaryText"),
                        bigText = sbn.notification.extras.getString("android.bigText"),
                        
                        // Notification metadata
                        timestamp = System.currentTimeMillis(),
                        postTime = sbn.postTime,
                        isGroup = sbn.notification.group != null,
                        groupKey = sbn.notification.group,
                        sortKey = sbn.notification.sortKey,
                        
                        // Status fields
                        isRead = false,
                        isArchived = false,
                        isPinned = false,
                        isMuted = false,
                        
                        // Group/Category management
                        groupType = getGroupTypeFromPackage(packageName),
                        category = sbn.notification.category,
                        channelId = sbn.notification.channelId,
                        channelName = null, // Will be filled later if needed
                        
                        // Priority and importance
                        priority = sbn.notification.priority,
                        importance = 0, // Will be calculated based on priority
                        
                        // Media and actions
                        hasMedia = sbn.notification.extras.containsKey("android.picture") || 
                                  sbn.notification.extras.containsKey("android.bigPicture"),
                        mediaType = getMediaType(sbn),
                        hasActions = sbn.notification.actions?.isNotEmpty() == true,
                        actions = serializeActions(sbn.notification.actions),
                        
                        // Search and filtering
                        searchableText = searchableText,
                        keywords = extractKeywords(searchableText),
                        
                        // Data retention
                        retentionDays = 30,
                        expiresAt = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000L), // 30 days
                        
                        // Additional metadata
                        extras = serializeExtras(sbn.notification.extras),
                        flags = sbn.notification.flags,
                        color = sbn.notification.color,
                        smallIcon = appIcon?.toString(),
                        largeIcon = null,
                        
                        // User preferences
                        userGroup = null,
                        userTags = null,
                        notes = null,
                        
                        // Analytics
                        viewCount = 0,
                        lastViewedAt = null,
                        
                        // Sync and backup
                        isSynced = false,
                        syncTime = null,
                        backupId = null,
                        
                        // User interaction
                        clickTime = null,
                        dismissTime = null
                    )
                )
                
                // Update notification count for the app
                allAppRepository.incrementNotificationCount(packageName, System.currentTimeMillis())
            }
        }


        // Emit notification to Flow
        notificationsFlow.tryEmit("Message")
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.d("Notification", "Removed: ${sbn.packageName}")
    }


    /**
     * Comprehensive filtering function to determine if a notification should be saved
     */
    private fun shouldSaveNotification(
        sbn: StatusBarNotification, 
        packageName: String, 
        title: String?, 
        text: String?
    ): Boolean {
        
        // 1. Filter out your own app notifications
        if (packageName == applicationContext.packageName) {
            Log.d("NotificationFilter", "Filtered out own app notification: $packageName")
            return false
        }
        
        // 2. Filter out system notifications
        if (isSystemNotification(packageName)) {
            Log.d("NotificationFilter", "Filtered out system notification: $packageName")
            return false
        }
        
        // 3. Filter out specific apps (like Notisave)
        if (isBlockedApp(packageName)) {
            Log.d("NotificationFilter", "Filtered out blocked app: $packageName")
            return false
        }
        
        // 4. Filter out notifications with no meaningful content
        if (!hasMeaningfulContent(title, text)) {
            Log.d("NotificationFilter", "Filtered out notification with no content: title=$title, text=$text")
            return false
        }
        
        // 4.1. Additional check for meaningful content in other fields
        if (!hasAdditionalMeaningfulContent(sbn)) {
            Log.d("NotificationFilter", "Filtered out notification with no additional meaningful content")
            return false
        }
        
        // 5. Filter out notifications from specific channels
        if (isBlockedChannel(sbn.notification.channelId)) {
            Log.d("NotificationFilter", "Filtered out notification from blocked channel: ${sbn.notification.channelId}")
            return false
        }
        
        // 6. Filter out notifications with specific flags
        if (shouldFilterByFlags(sbn.notification.flags)) {
            Log.d("NotificationFilter", "Filtered out notification by flags: ${sbn.notification.flags}")
            return false
        }
        
        // 7. Filter out notifications with specific categories
        if (isBlockedCategory(sbn.notification.category)) {
            Log.d("NotificationFilter", "Filtered out notification by category: ${sbn.notification.category}")
            return false
        }
        
        // 8. Filter out notifications that are too short or too long
        if (!isAppropriateLength(title, text)) {
            Log.d("NotificationFilter", "Filtered out notification by length: title=$title, text=$text")
            return false
        }
        
        Log.d("NotificationFilter", "Notification passed all filters: $packageName - $title")
        return true
    }
    
    private fun isSystemNotification(packageName: String): Boolean {
        val systemPackages = setOf(
            "com.android.systemui",
            "android",
            "com.android.systemui.statusbar",
            "com.android.systemui.notification"
        )
        return systemPackages.contains(packageName)
    }
    
    private fun isBlockedApp(packageName: String): Boolean {
        val blockedApps = setOf(
            "com.tenqube.notisave", // Notisave app
            "com.jetpackComposeTest1", // Your own app
            "com.android.systemui",
            "com.google.android.systemui",
            "com.samsung.android.systemui",
            "com.miui.systemui",
            "com.oneplus.systemui"
        )
        
        // Also check for apps with similar names (like other notification saver apps)
        val blockedAppPatterns = setOf(
            "notisave", "notification.save", "notification.saver", 
            "notification.manager", "notification.history"
        )
        
        return blockedApps.contains(packageName) || 
               blockedAppPatterns.any { pattern -> packageName.contains(pattern, ignoreCase = true) }
    }
    
    private fun hasMeaningfulContent(title: String?, text: String?): Boolean {
        // Check if notification has meaningful content
        val hasTitle = !title.isNullOrBlank()
        val hasText = !text.isNullOrBlank()
        
        // Both title and text must have content for meaningful notifications
        if (!hasTitle || !hasText) {
            Log.d("NotificationFilter", "Filtered out notification missing title or text: title=$title, text=$text")
            return false
        }
        
        // Filter out notifications with very generic content
        val genericTitles = setOf(
            "Notification", "Alert", "Message", "Update", "Info", "System",
            "New Message", "New Notification", "Alert", "Reminder", "Notice"
        )
        
        val genericTexts = setOf(
            "Tap to view", "New notification", "You have a new message", 
            "Notification received", "System notification", "Tap to open",
            "New message", "Message received", "Click to view", "View details",
            "Open app", "See more", "Show details", "View notification"
        )
        
        // Filter out if both title and text are generic
        if (title in genericTitles && text in genericTexts) {
            Log.d("NotificationFilter", "Filtered out generic notification: title=$title, text=$text")
            return false
        }
        
        // Filter out notifications with very short content (less than 5 characters)
        if ((title?.length ?: 0) < 5 || (text?.length ?: 0) < 5) {
            Log.d("NotificationFilter", "Filtered out notification with short content: title=$title, text=$text")
            return false
        }
        
        // Filter out notifications that are just numbers or symbols
        val titleIsOnlyNumbers = title?.matches(Regex("^[\\d\\s\\-\\+\\(\\)]+$")) == true
        val textIsOnlyNumbers = text?.matches(Regex("^[\\d\\s\\-\\+\\(\\)]+$")) == true
        
        if (titleIsOnlyNumbers || textIsOnlyNumbers) {
            Log.d("NotificationFilter", "Filtered out notification with only numbers/symbols: title=$title, text=$text")
            return false
        }
        
        // Filter out notifications with repeated characters (like "aaaa" or "....")
        val titleHasRepeatedChars = title?.let { it.length > 3 && it.groupBy { char -> char }.any { (_, list) -> list.size > it.length / 2 } } == true
        val textHasRepeatedChars = text?.let { it.length > 3 && it.groupBy { char -> char }.any { (_, list) -> list.size > it.length / 2 } } == true
        
        if (titleHasRepeatedChars || textHasRepeatedChars) {
            Log.d("NotificationFilter", "Filtered out notification with repeated characters: title=$title, text=$text")
            return false
        }
        
        Log.d("NotificationFilter", "Notification has meaningful content: title=$title, text=$text")
        return true
    }
    
    private fun hasAdditionalMeaningfulContent(sbn: StatusBarNotification): Boolean {
        val extras = sbn.notification.extras
        
        // Check for additional meaningful content in other fields
        val subText = extras.getString("android.subText")
        val summaryText = extras.getString("android.summaryText")
        val bigText = extras.getString("android.bigText")
        
        // Check if any of these additional fields have meaningful content
        val hasSubText = !subText.isNullOrBlank() && subText.length >= 5
        val hasSummaryText = !summaryText.isNullOrBlank() && summaryText.length >= 5
        val hasBigText = !bigText.isNullOrBlank() && bigText.length >= 5
        
        // Check for media content
        val hasPicture = extras.containsKey("android.picture") || extras.containsKey("android.bigPicture")
        val hasMediaSession = extras.containsKey("android.mediaSession")
        
        // Check for actions
        val hasActions = sbn.notification.actions?.isNotEmpty() == true
        
        // If the main title/text passed the meaningful content check, 
        // we should allow it even if additional fields are empty
        // This function is mainly to catch edge cases where main content is generic
        // but additional fields might provide context
        
        return true // Allow by default since main content check is already strict
    }
    
    private fun isBlockedChannel(channelId: String?): Boolean {
        if (channelId == null) return false
        
        val blockedChannels = setOf(
            "ranker_group", // From your logs
            "system_channel",
            "low_priority",
            "background_sync"
        )
        
        return blockedChannels.contains(channelId)
    }
    
    private fun shouldFilterByFlags(flags: Int): Boolean {
        // Filter out notifications with specific flags
        // FLAG_ONGOING_EVENT = 2, FLAG_NO_CLEAR = 32, FLAG_FOREGROUND_SERVICE = 64
        val filteredFlags = android.app.Notification.FLAG_ONGOING_EVENT or 
                           android.app.Notification.FLAG_NO_CLEAR or 
                           android.app.Notification.FLAG_FOREGROUND_SERVICE
        
        return (flags and filteredFlags) != 0
    }
    
    private fun isBlockedCategory(category: String?): Boolean {
        if (category == null) return false
        
        val blockedCategories = setOf(
            android.app.Notification.CATEGORY_SERVICE,
            android.app.Notification.CATEGORY_SYSTEM,
            android.app.Notification.CATEGORY_ERROR,
            android.app.Notification.CATEGORY_PROGRESS
        )
        
        return blockedCategories.contains(category)
    }
    
    private fun isAppropriateLength(title: String?, text: String?): Boolean {
        // Filter out notifications that are too short (less than 5 characters) or too long (more than 500 characters)
        val titleLength = title?.length ?: 0
        val textLength = text?.length ?: 0
        
        // Both title and text must have meaningful length
        val hasValidTitle = titleLength in 5..500
        val hasValidText = textLength in 5..500
        
        if (!hasValidTitle || !hasValidText) {
            Log.d("NotificationFilter", "Filtered out notification by length: titleLength=$titleLength, textLength=$textLength")
            return false
        }
        
        return true
    }
    
    private fun getGroupTypeFromPackage(packageName: String): String {
        return when {
            packageName.contains("whatsapp") || packageName.contains("telegram") || 
            packageName.contains("messenger") || packageName.contains("discord") -> "social"
            packageName.contains("gmail") || packageName.contains("outlook") || 
            packageName.contains("mail") -> "email"
            packageName.contains("slack") || packageName.contains("teams") || 
            packageName.contains("zoom") -> "work"
            packageName.contains("bank") || packageName.contains("pay") || 
            packageName.contains("finance") -> "finance"
            packageName.contains("news") || packageName.contains("bbc") || 
            packageName.contains("cnn") -> "news"
            else -> "default"
        }
    }
    
    private fun getMediaType(sbn: StatusBarNotification): String? {
        val extras = sbn.notification.extras
        return when {
            extras.containsKey("android.picture") || extras.containsKey("android.bigPicture") -> "image"
            extras.containsKey("android.mediaSession") -> "audio"
            extras.containsKey("android.mediaSession") && extras.containsKey("android.mediaSession") -> "video"
            else -> null
        }
    }
    
    private fun serializeActions(actions: Array<android.app.Notification.Action>?): String? {
        return if (actions != null && actions.isNotEmpty()) {
            actions.joinToString("|") { action ->
                action.title ?: ""
            }
        } else null
    }
    
    private fun extractKeywords(text: String): String? {
        // Simple keyword extraction - can be enhanced with NLP
        val words = text.lowercase()
            .replace(Regex("[^a-zA-Z0-9\\s]"), " ")
            .split("\\s+".toRegex())
            .filter { it.length > 3 }
            .distinct()
            .take(10)
        return if (words.isNotEmpty()) words.joinToString(",") else null
    }
    
    private fun serializeExtras(extras: android.os.Bundle): String? {
        // Simple serialization - in production, use proper JSON serialization
        return try {
            val keys = extras.keySet()
            if (keys.isNotEmpty()) {
                keys.joinToString("|") { key ->
                    "$key:${extras.get(key)}"
                }
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
