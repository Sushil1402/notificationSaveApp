package com.jetpackComposeTest1.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object GroupIconUtils {
    
    /**
     * Generates initials from group name
     * Examples:
     * "Work Apps" -> "WA"
     * "Social Media" -> "SM" 
     * "Games" -> "G"
     * "My Custom Group" -> "MC"
     */
    fun generateInitials(groupName: String): String {
        if (groupName.isBlank()) return "?"
        
        val words = groupName.trim().split("\\s+".toRegex())
            .filter { it.isNotBlank() }
            .map { it.firstOrNull()?.uppercaseChar() ?: "" }
        
        return when {
            words.isEmpty() -> "?"
            words.size == 1 -> words.first().toString().take(2) // Take first 2 chars if single word
            else -> words.take(2).joinToString("") // Take first letter of first 2 words
        }
    }
    
    /**
     * Gets the appropriate icon for a group based on its type and name
     * System groups (unread, read, muted) use predefined icons
     * Custom groups use initials
     */
    fun getGroupIcon(groupType: String, groupName: String): ImageVector {
        return when (groupType.lowercase()) {
            "unread" -> Icons.Default.Email
            "read" -> Icons.Default.Check
            "muted" -> Icons.Default.CheckCircle
            else -> Icons.Default.Face // Custom groups will show initials as text
        }
    }
    
    /**
     * Generates a color hash for consistent coloring based on group name
     * This ensures the same group always gets the same color
     */
    fun getColorHash(groupName: String): Int {
        return groupName.hashCode()
    }
}
