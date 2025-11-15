package com.notistorex.app.ui.screens.dashboard.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notistorex.app.data.repository.database.NotificationDBRepository
import com.notistorex.app.db.NotificationEntity
import com.notistorex.app.ui.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationDetailViewViewModel @Inject constructor(
    private val notificationDBRepo: NotificationDBRepository
) : ViewModel() {

    private val _notification = MutableStateFlow<NotificationEntity?>(null)
    val notification: StateFlow<NotificationEntity?> = _notification.asStateFlow()

    private val _appIcon = MutableStateFlow<android.graphics.drawable.Drawable?>(null)
    val appIcon: StateFlow<android.graphics.drawable.Drawable?> = _appIcon.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadNotification(context: Context, notificationId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val id = notificationId.toLong()
                val entity = notificationDBRepo.getNotificationById(id)
                
                if (entity != null) {
                    _notification.value = entity
                    
                    // Get app icon
                    _appIcon.value = Utils.getAppIcon(context, entity.packageName)
                    
                    // Mark as read if not already read
                    if (!entity.isRead) {
                        notificationDBRepo.markAsRead(id)
                        _notification.value = entity.copy(isRead = true)
                    }
                } else {
                    _error.value = "Notification not found"
                }
            } catch (e: Exception) {
                _error.value = "Failed to load notification: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleReadStatus(notificationId: String) {
        viewModelScope.launch {
            try {
                val id = notificationId.toLong()
                _notification.value?.let { entity ->
                    val newReadStatus = !entity.isRead
                    notificationDBRepo.updateReadStatus(id, newReadStatus)
                    _notification.value = entity.copy(isRead = newReadStatus)
                }
            } catch (e: Exception) {
                _error.value = "Failed to toggle read status: ${e.message}"
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                val id = notificationId.toLong()
                notificationDBRepo.deleteNotification(id)
                // Set notification to null after successful deletion to trigger navigation back
                _notification.value = null
            } catch (e: Exception) {
                _error.value = "Failed to delete notification: ${e.message}"
            }
        }
    }

    fun formatTimestamp(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("EEEE, d MMMM yyyy 'at' hh:mm a", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

    fun formatTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000}m ago"
            diff < 86400000 -> "${diff / 3600000}h ago"
            diff < 604800000 -> "${diff / 86400000}d ago"
            else -> "${diff / 604800000}w ago"
        }
    }
}

