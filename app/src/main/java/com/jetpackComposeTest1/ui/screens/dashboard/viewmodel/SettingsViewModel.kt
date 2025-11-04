package com.jetpackComposeTest1.ui.screens.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetpackComposeTest1.data.local.preferences.AppPreferences
import com.jetpackComposeTest1.model.setting.SettingsData
import com.jetpackComposeTest1.utils.CleanupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val cleanupManager: CleanupManager
) : ViewModel() {

    private val _settings = MutableStateFlow(
        SettingsData(
        hasNotificationAccess = false,
        autoCleanup = false,
        retentionDays = 30,
        storageUsed = 45.6f,
        storagePercentage = 65.2f,
        darkMode = false,
        notificationSound = true
    )
    )
    val settings: StateFlow<SettingsData> = _settings.asStateFlow()

    init {
        // Load initial settings from preferences
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _settings.value = _settings.value.copy(
                autoCleanup = appPreferences.isAutoCleanupEnabled(),
                retentionDays = appPreferences.getRetentionDays()
            )
        }
    }

    fun requestNotificationAccess() {
        // Implement notification access request
        viewModelScope.launch {
            _settings.value = _settings.value.copy(hasNotificationAccess = true)
        }
    }

    fun setAutoCleanupEnabled(enabled: Boolean) {
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

    fun toggleDarkMode() {
        viewModelScope.launch {
            _settings.value = _settings.value.copy(
                darkMode = !_settings.value.darkMode
            )
        }
    }

    fun toggleNotificationSound() {
        viewModelScope.launch {
            _settings.value = _settings.value.copy(
                notificationSound = !_settings.value.notificationSound
            )
        }
    }

    fun exportAllData() {
        // Implement export functionality
    }

    fun importData() {
        // Implement import functionality
    }

    fun showClearDataDialog() {
        // Implement clear data dialog
    }
}


