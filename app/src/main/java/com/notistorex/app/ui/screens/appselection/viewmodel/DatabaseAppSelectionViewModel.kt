package com.notistorex.app.ui.screens.appselection.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notistorex.app.data.repository.database.AllAppRepository
import com.notistorex.app.data.repository.database.NotificationGroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DatabaseAppSelectionViewModel @Inject constructor(
    private val allAppRepository: AllAppRepository,
    private val groupRepository: NotificationGroupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DatabaseAppSelectionUiState())
    val uiState: StateFlow<DatabaseAppSelectionUiState> = _uiState.asStateFlow()

    private val _apps = MutableStateFlow<List<DatabaseAppInfo>>(emptyList())
    val apps: StateFlow<List<DatabaseAppInfo>> = _apps.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isAllAppsEnabled = MutableStateFlow(false)
    val isAllAppsEnabled: StateFlow<Boolean> = _isAllAppsEnabled.asStateFlow()

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                allAppRepository.getAllApps().collect { appEntities ->
                    val databaseApps = appEntities.map { entity ->
                        DatabaseAppInfo(
                            packageName = entity.packageName,
                            appName = entity.appName,
                            isEnabled = false, // Start with all apps disabled
                            notificationCount = entity.notificationCount ?: 0
                        )
                    }
                    _apps.value = databaseApps
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onAppToggleChanged(packageName: String, isEnabled: Boolean) {
        val updatedApps = _apps.value.map { app ->
            if (app.packageName == packageName) {
                app.copy(isEnabled = isEnabled)
            } else {
                app
            }
        }
        _apps.value = updatedApps
        
        // Update "All Apps" toggle state
        val enabledCount = updatedApps.count { it.isEnabled }
        val totalCount = updatedApps.size
        _isAllAppsEnabled.value = enabledCount == totalCount && totalCount > 0
    }

    fun onAllAppsToggleChanged(isEnabled: Boolean) {
        _isAllAppsEnabled.value = isEnabled
        
        val updatedApps = _apps.value.map { app ->
            app.copy(isEnabled = isEnabled)
        }
        _apps.value = updatedApps
    }

    fun getFilteredApps(): List<DatabaseAppInfo> {
        val query = _searchQuery.value.lowercase()
        val allApps = _apps.value
        
        return if (query.isEmpty()) {
            allApps
        } else {
            allApps.filter { app ->
                app.appName.lowercase().contains(query) ||
                app.packageName.lowercase().contains(query)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun createCustomGroup(groupName: String, selectedApps: List<String>) {
        viewModelScope.launch {
            try {
                groupRepository.createGroup(
                    name = groupName,
                    description = "Custom group with ${selectedApps.size} apps",
                    selectedApps = selectedApps
                )
                // The groups list will automatically update due to the reactive flow
            } catch (e: Exception) {
                // Handle error - you might want to show a snackbar or dialog
                println("Error creating group: ${e.message}")
            }
        }
    }

    fun deleteGroup(groupId: String) {
        viewModelScope.launch {
            if (groupId !in listOf("unread", "read", "muted")) {
                // Only allow deletion of custom groups
                groupRepository.deleteGroup(groupId)
            }
        }
    }

    fun addAppsToGroup(groupId: String, packageNames: List<String>) {
        viewModelScope.launch {
            groupRepository.addAppsToGroup(packageNames, groupId)
        }
    }

    fun removeAppsFromGroup(groupId: String, packageNames: List<String>) {
        viewModelScope.launch {
            groupRepository.removeAppsFromGroup(packageNames, groupId)
        }
    }

}

data class DatabaseAppSelectionUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

data class DatabaseAppInfo(
    val packageName: String,
    val appName: String,
    val isEnabled: Boolean,
    val notificationCount: Int
)
