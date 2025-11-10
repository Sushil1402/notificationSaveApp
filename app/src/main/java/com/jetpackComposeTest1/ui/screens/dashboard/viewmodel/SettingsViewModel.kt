package com.jetpackComposeTest1.ui.screens.dashboard.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetpackComposeTest1.data.local.preferences.AppPreferences
import com.jetpackComposeTest1.data.repository.database.NotificationDBRepository
import com.jetpackComposeTest1.model.setting.SettingsData
import com.jetpackComposeTest1.model.setting.ThemeMode
import com.jetpackComposeTest1.utils.CleanupManager
import com.jetpackComposeTest1.utils.NotificationExportManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val cleanupManager: CleanupManager,
    private val notificationDBRepo: NotificationDBRepository
) : ViewModel() {

    private val _settings = MutableStateFlow(
        SettingsData(
            hasNotificationAccess = false,
            autoCleanup = false,
            retentionDays = 30,
            storageUsed = 45.6f,
            storagePercentage = 65.2f,
            themeMode = ThemeMode.SYSTEM,
            notificationSound = true,
            isPremiumUser = false
        )
    )
    val settings: StateFlow<SettingsData> = _settings.asStateFlow()

    // Export state
    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    init {
        // Load initial settings from preferences
        loadSettings()
        observePremiumStatus()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val isPremium = appPreferences.isPremiumUnlocked()
            if (!isPremium && appPreferences.isAutoCleanupEnabled()) {
                appPreferences.setAutoCleanupEnabled(false)
            }
            _settings.value = _settings.value.copy(
                autoCleanup = if (isPremium) appPreferences.isAutoCleanupEnabled() else false,
                retentionDays = if (isPremium) appPreferences.getRetentionDays() else 30,
                themeMode = appPreferences.getThemeMode(),
                isPremiumUser = isPremium
            )
        }
    }

    private fun observePremiumStatus() {
        viewModelScope.launch {
            appPreferences.premiumStatusFlow().collect { isPremium ->
                if (!isPremium && appPreferences.isAutoCleanupEnabled()) {
                    appPreferences.setAutoCleanupEnabled(false)
                }
                _settings.value = _settings.value.copy(
                    isPremiumUser = isPremium,
                    autoCleanup = if (isPremium) appPreferences.isAutoCleanupEnabled() else false,
                    retentionDays = if (isPremium) appPreferences.getRetentionDays() else 30
                )
            }
        }
    }

    fun requestNotificationAccess() {
        // Implement notification access request
        viewModelScope.launch {
            _settings.value = _settings.value.copy(hasNotificationAccess = true)
        }
    }

    fun setAutoCleanupEnabled(enabled: Boolean) {
        if (!_settings.value.isPremiumUser) {
            return
        }
        viewModelScope.launch {
            // Save to preferences
            appPreferences.setAutoCleanupEnabled(enabled)

            // Update state
            _settings.value = _settings.value.copy(autoCleanup = enabled)

            // Manage worker
            if (enabled) {
                cleanupManager.scheduleCleanup()
            } else {
                cleanupManager.cancelCleanup()
            }
        }
    }

    fun setRetentionDays(days: Int) {
        if (!_settings.value.isPremiumUser) {
            return
        }
        viewModelScope.launch {
            // Save to preferences
            appPreferences.setRetentionDays(days)

            // Update state
            _settings.value = _settings.value.copy(retentionDays = days)

            // If auto cleanup is enabled, reschedule the worker to use new retention days
            if (_settings.value.autoCleanup) {
                cleanupManager.scheduleCleanup()
            }
        }
    }

    fun showRetentionDialog() {
        // Implement retention period dialog
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            appPreferences.setThemeMode(mode)
            _settings.value = _settings.value.copy(themeMode = mode)
        }
    }

    fun toggleNotificationSound() {
        viewModelScope.launch {
            _settings.value = _settings.value.copy(
                notificationSound = !_settings.value.notificationSound
            )
        }
    }

    fun exportAllData(context: Context) {
        if (!_settings.value.isPremiumUser) {
            return
        }
        viewModelScope.launch {
            _exportState.value = ExportState.Exporting
            try {
                // Get all notifications from database
                val allNotifications = notificationDBRepo.getAllNotifications().first()

                if (allNotifications.isEmpty()) {
                    _exportState.value = ExportState.Error("No notifications to export")
                    return@launch
                }

                // Create export manager and export to Excel
                val exportManager = NotificationExportManager(context)
                val result = exportManager.exportToExcel(allNotifications)

                result.fold(
                    onSuccess = { uri ->
                        _exportState.value = ExportState.Success(uri)
                    },
                    onFailure = { exception ->
                        _exportState.value = ExportState.Error(exception.message ?: "Export failed")
                    }
                )
            } catch (e: Exception) {
                _exportState.value = ExportState.Error(e.message ?: "Export failed")
            }
        }
    }

    fun resetExportState() {
        _exportState.value = ExportState.Idle
    }

    fun importData() {
        // Implement import functionality
    }

    fun clearAllData() {
        viewModelScope.launch {
            try {
//                notificationDBRepo.clearAllNotifications()
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }

    sealed class ExportState {
        object Idle : ExportState()
        object Exporting : ExportState()
        data class Success(val fileUri: Uri) : ExportState()
        data class Error(val message: String) : ExportState()
    }
}


