package com.jetpackComposeTest1.ui.screens.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetpackComposeTest1.model.setting.SettingsData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _settings = MutableStateFlow(
        SettingsData(
        hasNotificationAccess = false,
        autoCleanup = true,
        retentionDays = 30,
        storageUsed = 45.6f,
        storagePercentage = 65.2f,
        darkMode = false,
        notificationSound = true
    )
    )
    val settings: StateFlow<SettingsData> = _settings.asStateFlow()

    fun requestNotificationAccess() {
        // Implement notification access request
        viewModelScope.launch {
            _settings.value = _settings.value.copy(hasNotificationAccess = true)
        }
    }

    fun toggleAutoCleanup() {
        viewModelScope.launch {
            _settings.value = _settings.value.copy(
                autoCleanup = !_settings.value.autoCleanup
            )
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


