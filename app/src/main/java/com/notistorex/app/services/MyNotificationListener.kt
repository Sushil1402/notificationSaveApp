package com.notistorex.app.services

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Base64
import android.util.Log
import com.notistorex.app.data.repository.database.AllAppRepository
import com.notistorex.app.data.repository.database.NotificationDBRepository
import com.notistorex.app.db.NotificationDao
import com.notistorex.app.db.NotificationEntity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/**
 * Data class to hold all InboxStyle notification details
 */
data class InboxNotificationDetails(
    val isInboxStyle: Boolean = false,
    val title: String? = null,
    val summaryText: String? = null,
    val lines: List<String> = emptyList(),
    val lineCount: Int = 0,
    val bigContentTitle: String? = null,
    val contentText: String? = null
)

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
        val packageName = sbn.packageName
        val extras = sbn.notification.extras
        
        // Extract InboxStyle notification details if it's an inbox notification
        val inboxDetails = extractInboxNotificationDetails(sbn)
        
        // Use inbox details if available, otherwise use standard notification fields
        val title = inboxDetails.title ?: extras.getString("android.title")
        val text = if (inboxDetails.isInboxStyle && inboxDetails.lines.isNotEmpty()) {
            // For inbox notifications, combine all lines into text field
            inboxDetails.lines.joinToString("\n")
        } else {
            extras.getCharSequence("android.text")?.toString()
        }
        
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

        // Log InboxStyle notification details if it's an inbox notification
        if (inboxDetails.isInboxStyle) {
            Log.d("InboxNotification", "InboxStyle Notification Detected from: $packageName")
            Log.d("InboxNotification", "Title: ${inboxDetails.title}")
            Log.d("InboxNotification", "Summary Text: ${inboxDetails.summaryText}")
            Log.d("InboxNotification", "Big Content Title: ${inboxDetails.bigContentTitle}")
            Log.d("InboxNotification", "Content Text: ${inboxDetails.contentText}")
            Log.d("InboxNotification", "Line Count: ${inboxDetails.lineCount}")
            inboxDetails.lines.forEachIndexed { index, line ->
                Log.d("InboxNotification", "  Line ${index + 1}: $line")
            }
        }

        scope.launch {
            // Pass inbox details to filtering function
            if (shouldSaveNotification(sbn, packageName, title, text, inboxDetails)) {
                // Check if app is enabled for notification saving
                val appEntity = allAppRepository.getAppByPackageName(packageName)
                if (appEntity?.isEnabled != true) {
                    return@launch
                }

                // Extract big picture image and convert to base64
                val bigPictureBase64 = extractBigPictureImage(sbn)
                if (bigPictureBase64 != null) {
                    Log.d("BigPictureExtraction", "Big picture extracted and stored for notification: $packageName")
                }
                
                // Build searchable text with inbox details if available
                val searchableText = buildString {
                    append(title ?: "")
                    append(" ")
                    if (inboxDetails.isInboxStyle) {
                        inboxDetails.lines.forEach { append(it).append(" ") }
                        inboxDetails.summaryText?.let { append(it).append(" ") }
                    } else {
                        append(text ?: "")
                    }
                    append(appName)
                }.trim()
                
                notificationDBRepo.insertNotification(
                    NotificationEntity(
                        // Basic notification info
                        packageName = packageName,
                        appName = appName,
                        notificationId = sbn.id,
                        tag = sbn.tag,
                        
                        // Notification content - use inbox details if available
                        title = title ?: "",
                        text = text ?: "",
                        subText = sbn.notification.extras.getString("android.subText"),
                        summaryText = inboxDetails.summaryText ?: sbn.notification.extras.getString("android.summaryText"),
                        bigText = if (inboxDetails.isInboxStyle) {
                            // Store all inbox lines in bigText field
                            inboxDetails.lines.joinToString("\n")
                        } else {
                            extras.getCharSequence("android.bigText")?.toString()
                                ?: extras.getString("android.bigText")
                        },
                        
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
                        groupType = if (inboxDetails.isInboxStyle) "inbox" else getGroupTypeFromPackage(packageName),
                        category = sbn.notification.category,
                        channelId = sbn.notification.channelId,
                        channelName = null, // Will be filled later if needed
                        
                        // Priority and importance
                        priority = sbn.notification.priority,
                        importance = 0, // Will be calculated based on priority
                        
                        // Media and actions
                        hasMedia = sbn.notification.extras.containsKey("android.picture") || 
                                  sbn.notification.extras.containsKey("android.bigPicture") ||
                                  bigPictureBase64 != null,
                        mediaType = getMediaType(sbn),
                        hasActions = sbn.notification.actions?.isNotEmpty() == true,
                        actions = serializeActions(sbn.notification.actions),
                        
                        // Search and filtering
                        searchableText = searchableText,
                        keywords = extractKeywords(searchableText),
                        
                        // Data retention
                        retentionDays = 30,
                        expiresAt = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000L), // 30 days
                        
                        // Additional metadata - include inbox details in extras
                        extras = serializeInboxExtras(inboxDetails) ?: serializeExtras(sbn.notification.extras),
                        flags = sbn.notification.flags,
                        color = sbn.notification.color,
                        smallIcon = appIcon?.toString(),
                        largeIcon = bigPictureBase64, // Store big picture as base64 string
                        
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
        text: String?,
        inboxDetails: InboxNotificationDetails = InboxNotificationDetails()
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
        // For inbox notifications, check if lines have content
        if (!hasMeaningfulContent(title, text, inboxDetails)) {
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
            "com.notistorex.app", // Your own app
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
    
    private fun hasMeaningfulContent(title: String?, text: String?, inboxDetails: InboxNotificationDetails = InboxNotificationDetails()): Boolean {
        // Check if notification has meaningful content
        val hasTitle = !title.isNullOrBlank()
        val hasText = !text.isNullOrBlank()
        val hasInboxLines = inboxDetails.isInboxStyle && inboxDetails.lines.isNotEmpty()
        
        // For inbox notifications, text might be empty but textLines contain content
        if (!hasTitle) {
            Log.d("NotificationFilter", "Filtered out notification missing title: title=$title")
            return false
        }
        
        // If it's an inbox notification with lines, that's valid content
        if (hasInboxLines) {
            val hasMeaningfulLines = inboxDetails.lines.any { line ->
                !line.isBlank() && line.length >= 5
            }
            if (hasMeaningfulLines) {
                return true
            }
        }
        
        // For non-inbox notifications, text should have content
        if (!hasText && !hasInboxLines) {
            Log.d("NotificationFilter", "Filtered out notification missing text: title=$title, text=$text")
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
        // For inbox notifications, text might be empty, so only check title here
        if ((title?.length ?: 0) < 5) {
            Log.d("NotificationFilter", "Filtered out notification with short title: title=$title")
            return false
        }
        
        // If text exists, check its length
        if (hasText && (text?.length ?: 0) < 5) {
            Log.d("NotificationFilter", "Notification with short text (may be inbox): title=$title, text=$text")
            // Don't filter out, might be inbox style with textLines
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
        
        // Check for inbox style notifications with textLines
        val hasTextLines = extras.containsKey("android.textLines")
        if (hasTextLines) {
            try {
                val textLines = extras.getCharSequenceArray("android.textLines")
                if (textLines != null && textLines.isNotEmpty()) {
                    // Check if any line has meaningful content
                    val hasMeaningfulLines = textLines.any { line ->
                        !line.isNullOrBlank() && line.toString().length >= 5
                    }
                    if (hasMeaningfulLines) {
                        return true
                    }
                }
            } catch (e: Exception) {
                // Continue with other checks
            }
        }
        
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
        
        // Title must have meaningful length
        val hasValidTitle = titleLength in 5..500
        
        if (!hasValidTitle) {
            Log.d("NotificationFilter", "Filtered out notification by title length: titleLength=$titleLength")
            return false
        }
        
        // For text, if it's empty it might be an inbox notification (will be checked elsewhere)
        // Only filter out if text exists and is invalid
        if (text != null && text.isNotBlank()) {
            val hasValidText = textLength in 5..5000 // Allow longer text for inbox notifications
            if (!hasValidText) {
                Log.d("NotificationFilter", "Notification text length issue (may be inbox): titleLength=$titleLength, textLength=$textLength")
                // Don't filter out, might be inbox style
            }
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
    
    /**
     * Comprehensive function to extract all InboxStyle notification details
     * This function extracts metadata from InboxStyle notifications including:
     * - Title and summary text
     * - All individual lines (up to 6 lines)
     * - Big content title and content text
     */
    private fun extractInboxNotificationDetails(sbn: StatusBarNotification): InboxNotificationDetails {
        val extras = sbn.notification.extras
        
        // Check if this is an InboxStyle notification
        // InboxStyle notifications can be identified by:
        // 1. Presence of android.textLines array in extras
        // 2. Notification style class name (via reflection)
        // 3. Template class name containing "InboxStyle"
        // 4. Multiple text entries in extras
        
        val hasTextLines = extras.containsKey("android.textLines")
        
        // Try to detect inbox style using reflection
        var isInboxStyleClass = false
        try {
            val styleField = android.app.Notification::class.java.getDeclaredField("extras")
            // Alternative: check the style via template key
            val template = extras.getString("android.template") ?: ""
            isInboxStyleClass = template.contains("InboxStyle", ignoreCase = true) ||
                              template.contains("Inbox", ignoreCase = true)
            
            // Also check the notification's style object directly
            val notification = sbn.notification
            val style = try {
                val styleField2 = android.app.Notification::class.java.getDeclaredField("mStyle")
                styleField2.isAccessible = true
                styleField2.get(notification)
            } catch (e: Exception) {
                null
            }
            
            if (style != null) {
                val styleClassName = style.javaClass.simpleName
                isInboxStyleClass = isInboxStyleClass || 
                                  styleClassName.contains("InboxStyle", ignoreCase = true) ||
                                  styleClassName.contains("Inbox", ignoreCase = true)
            }
        } catch (e: Exception) {
            // Reflection failed, continue with other methods
        }
        
        // Try to get text lines array
        val textLines = try {
            val linesArray = extras.getCharSequenceArray("android.textLines")
            linesArray?.mapNotNull { it?.toString() } ?: emptyList()
        } catch (e: Exception) {
            try {
                // Alternative: try as String array
                val stringArray = extras.getStringArray("android.textLines")
                stringArray?.toList() ?: emptyList()
            } catch (e2: Exception) {
                emptyList()
            }
        }
        
        // If we have text lines or it's identified as inbox style, extract details
        val isInboxStyle = hasTextLines || isInboxStyleClass || textLines.isNotEmpty()
        
        if (!isInboxStyle) {
            return InboxNotificationDetails()
        }
        
        // Extract title
        val title = extras.getString("android.title") ?:
                   extras.getCharSequence("android.title")?.toString()
        
        // Extract summary text
        val summaryText = extras.getString("android.summaryText") ?:
                         extras.getCharSequence("android.summaryText")?.toString()
        
        // Extract big content title (used in expanded inbox style)
        val bigContentTitle = extras.getString("android.bigContentTitle") ?:
                            extras.getCharSequence("android.bigContentTitle")?.toString() ?:
                            title
        
        // Extract content text
        val contentText = extras.getString("android.contentText") ?:
                         extras.getCharSequence("android.contentText")?.toString()
        
        // If we don't have text lines but it's identified as inbox, try alternative methods
        val finalLines = if (textLines.isEmpty() && isInboxStyle) {
            // Try to extract from other fields
            val alternativeLines = mutableListOf<String>()
            
            // Check for multiple text entries (some apps use text1, text2, etc.)
            for (i in 0..10) {
                val key = if (i == 0) "android.text" else "android.text$i"
                try {
                    extras.getCharSequence(key)?.toString()?.takeIf { it.isNotBlank() }?.let {
                        alternativeLines.add(it)
                    }
                } catch (e: Exception) {
                    // Key doesn't exist, continue
                }
            }
            
            // Try to get from bigText if available
            extras.getCharSequence("android.bigText")?.toString()?.let { bigText ->
                // Split bigText by newlines if it contains multiple lines
                if (bigText.contains("\n")) {
                    val splitLines = bigText.split("\n").filter { it.isNotBlank() }
                    if (splitLines.isNotEmpty()) {
                        alternativeLines.clear()
                        alternativeLines.addAll(splitLines)
                    }
                } else if (alternativeLines.isEmpty()) {
                    alternativeLines.add(bigText)
                }
            }
            
            // Also check all keys in extras for potential inbox data
            if (alternativeLines.isEmpty()) {
                try {
                    for (key in extras.keySet()) {
                        if (key.contains("text", ignoreCase = true) || 
                            key.contains("line", ignoreCase = true) ||
                            key.contains("message", ignoreCase = true)) {
                            try {
                                val value = extras.get(key)
                                when (value) {
                                    is CharSequence -> value.toString().takeIf { it.isNotBlank() }?.let {
                                        if (!alternativeLines.contains(it)) alternativeLines.add(it)
                                    }
                                    is String -> value.takeIf { it.isNotBlank() }?.let {
                                        if (!alternativeLines.contains(it)) alternativeLines.add(it)
                                    }
                                    is Array<*> -> {
                                        value.forEach { item ->
                                            when (item) {
                                                is CharSequence -> item.toString().takeIf { it.isNotBlank() }?.let {
                                                    if (!alternativeLines.contains(it)) alternativeLines.add(it)
                                                }
                                                is String -> item.takeIf { it.isNotBlank() }?.let {
                                                    if (!alternativeLines.contains(it)) alternativeLines.add(it)
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                // Skip this key
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("InboxNotification", "Error extracting alternative lines", e)
                }
            }
            
            alternativeLines
        } else {
            textLines
        }
        
        return InboxNotificationDetails(
            isInboxStyle = true,
            title = title,
            summaryText = summaryText,
            lines = finalLines,
            lineCount = finalLines.size,
            bigContentTitle = bigContentTitle,
            contentText = contentText
        )
    }
    
    /**
     * Serialize inbox notification details to string for storage in extras field
     */
    private fun serializeInboxExtras(inboxDetails: InboxNotificationDetails): String? {
        if (!inboxDetails.isInboxStyle) return null
        
        return buildString {
            append("INBOX_NOTIFICATION|")
            append("lineCount:${inboxDetails.lineCount}|")
            inboxDetails.title?.let { append("title:$it|") }
            inboxDetails.summaryText?.let { append("summaryText:$it|") }
            inboxDetails.bigContentTitle?.let { append("bigContentTitle:$it|") }
            inboxDetails.contentText?.let { append("contentText:$it|") }
            if (inboxDetails.lines.isNotEmpty()) {
                append("lines:")
                inboxDetails.lines.forEachIndexed { index, line ->
                    if (index > 0) append("\\n")
                    append(line.replace("|", "\\|").replace("\n", "\\n"))
                }
                append("|")
            }
        }.trimEnd('|')
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
    
    /**
     * Extract big picture image from notification and convert to base64 string
     * This function tries multiple sources:
     * 1. android.bigPicture from extras (BigPictureStyle notifications)
     * 2. android.picture from extras
     * 3. android.media.metadata.ART for media notifications
     * 4. android.media.metadata.ALBUM_ART for media notifications
     * 5. Large icon from notification (only if it's significantly larger than small icon)
     */
    private fun extractBigPictureImage(sbn: StatusBarNotification): String? {
        return try {
            val extras = sbn.notification.extras
            var bitmap: Bitmap? = null
            
            // Try to get big picture from extras (BigPictureStyle)
            bitmap = try {
                @Suppress("DEPRECATION")
                val bigPicture = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    extras.getParcelable("android.bigPicture", Bitmap::class.java)
                } else {
                    extras.getParcelable<Bitmap>("android.bigPicture")
                }
                if (bigPicture != null) {
                    Log.d("BigPictureExtraction", "Found android.bigPicture: ${bigPicture.width}x${bigPicture.height}")
                }
                bigPicture
            } catch (e: Exception) {
                Log.d("BigPictureExtraction", "Error getting android.bigPicture: ${e.message}")
                null
            }
            
            // If not found, try regular picture
            if (bitmap == null) {
                bitmap = try {
                    @Suppress("DEPRECATION")
                    val picture = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        extras.getParcelable("android.picture", Bitmap::class.java)
                    } else {
                        extras.getParcelable<Bitmap>("android.picture")
                    }
                    if (picture != null) {
                        Log.d("BigPictureExtraction", "Found android.picture: ${picture.width}x${picture.height}")
                    }
                    picture
                } catch (e: Exception) {
                    Log.d("BigPictureExtraction", "Error getting android.picture: ${e.message}")
                    null
                }
            }
            
            // If still not found, try media metadata ART (for media notifications)
            if (bitmap == null) {
                bitmap = try {
                    @Suppress("DEPRECATION")
                    val mediaSession = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        extras.getParcelable("android.mediaSession", android.os.Parcelable::class.java)
                    } else {
                        extras.getParcelable<android.os.Parcelable>("android.mediaSession")
                    }
                    if (mediaSession != null) {
                        // Try to get artwork from MediaMetadata
                        @Suppress("DEPRECATION")
                        val art = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            extras.getParcelable("android.media.metadata.ART", Bitmap::class.java)
                        } else {
                            extras.getParcelable<Bitmap>("android.media.metadata.ART")
                        }
                        if (art != null) {
                            Log.d("BigPictureExtraction", "Found media metadata ART: ${art.width}x${art.height}")
                        }
                        art
                    } else null
                } catch (e: Exception) {
                    Log.d("BigPictureExtraction", "Error getting media metadata: ${e.message}")
                    null
                }
            }
            
            // Try album art from media notifications
            if (bitmap == null) {
                bitmap = try {
                    @Suppress("DEPRECATION")
                    val albumArt = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        extras.getParcelable("android.media.metadata.ALBUM_ART", Bitmap::class.java)
                    } else {
                        extras.getParcelable<Bitmap>("android.media.metadata.ALBUM_ART")
                    }
                    if (albumArt != null) {
                        Log.d("BigPictureExtraction", "Found album art: ${albumArt.width}x${albumArt.height}")
                    }
                    albumArt
                } catch (e: Exception) {
                    Log.d("BigPictureExtraction", "Error getting album art: ${e.message}")
                    null
                }
            }
            
            // Last resort: Try to get from large icon (but only if it's actually large)
            // We skip this for BigPictureStyle notifications as large icon is usually the app icon
            if (bitmap == null) {
                try {
                    val largeIcon = sbn.notification.getLargeIcon()
                    if (largeIcon != null) {
                        val drawable = largeIcon.loadDrawable(applicationContext)
                        val iconBitmap = when (drawable) {
                            is BitmapDrawable -> drawable.bitmap
                            else -> {
                                // Convert drawable to bitmap
                                val width = drawable?.intrinsicWidth?.takeIf { it > 0 } ?: 512
                                val height = drawable?.intrinsicHeight?.takeIf { it > 0 } ?: 512
                                // Only use large icon if it's significantly larger (likely an image, not just app icon)
                                if (width >= 256 && height >= 256) {
                                    val createdBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                                    val canvas = android.graphics.Canvas(createdBitmap)
                                    drawable?.setBounds(0, 0, canvas.width, canvas.height)
                                    drawable?.draw(canvas)
                                    createdBitmap
                                } else {
                                    Log.d("BigPictureExtraction", "Skipping small large icon: ${width}x${height}")
                                    null
                                }
                            }
                        }
                        if (iconBitmap != null) {
                            Log.d("BigPictureExtraction", "Using large icon as big picture: ${iconBitmap.width}x${iconBitmap.height}")
                            bitmap = iconBitmap
                        }
                    }
                } catch (e: Exception) {
                    Log.e("BigPictureExtraction", "Error extracting from large icon", e)
                }
            }
            
            // Convert bitmap to base64 string if found
            bitmap?.let { bmp ->
                val base64 = convertBitmapToBase64(bmp)
                if (base64 != null) {
                    Log.d("BigPictureExtraction", "Successfully extracted big picture: ${bmp.width}x${bmp.height}, Base64 length: ${base64.length}")
                }
                base64
            } ?: run {
                Log.d("BigPictureExtraction", "No big picture found in notification from ${sbn.packageName}")
                null
            }
        } catch (e: Exception) {
            Log.e("BigPictureExtraction", "Error extracting big picture", e)
            null
        }
    }
    
    /**
     * Convert Bitmap to Base64 string for database storage
     * Uses JPEG compression with quality 85 to reduce size while maintaining quality
     * Allows larger images (up to 2048x2048) for better big picture quality
     */
    private fun convertBitmapToBase64(bitmap: Bitmap): String? {
        var scaledBitmap: Bitmap? = null
        return try {
            // Limit image size to prevent database bloat (max 2048x2048 for big pictures)
            // This allows better quality for notifications with large images
            val maxDimension = 2048
            val maxFileSize = 2 * 1024 * 1024 // 2MB max file size
            
            scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                val scale = minOf(
                    maxDimension.toFloat() / bitmap.width,
                    maxDimension.toFloat() / bitmap.height
                )
                val newWidth = (bitmap.width * scale).toInt()
                val newHeight = (bitmap.height * scale).toInt()
                Log.d("BigPictureExtraction", "Scaling image from ${bitmap.width}x${bitmap.height} to ${newWidth}x${newHeight}")
                Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            } else {
                null // Use original bitmap
            }
            
            val bitmapToCompress = scaledBitmap ?: bitmap
            
            // Convert to JPEG and compress with adaptive quality
            var quality = 85
            val outputStream = ByteArrayOutputStream()
            bitmapToCompress.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            var byteArray = outputStream.toByteArray()
            
            // If file is too large, reduce quality
            while (byteArray.size > maxFileSize && quality > 50) {
                quality -= 10
                outputStream.reset()
                bitmapToCompress.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                byteArray = outputStream.toByteArray()
                Log.d("BigPictureExtraction", "Reduced quality to $quality%, new size: ${byteArray.size} bytes")
            }
            
            // Convert to base64
            val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            
            // Log success
            Log.d("BigPictureExtraction", "Successfully extracted and converted image. Original: ${bitmap.width}x${bitmap.height}, Final: ${bitmapToCompress.width}x${bitmapToCompress.height}, Quality: $quality%, Size: ${byteArray.size} bytes, Base64 length: ${base64String.length}")
            
            base64String
            
        } catch (e: Exception) {
            Log.e("BigPictureExtraction", "Error converting bitmap to base64", e)
            null
        } finally {
            // Clean up if we created a scaled bitmap
            scaledBitmap?.let {
                try {
                    it.recycle()
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }
}
