package com.jetpackComposeTest1.ui.utils

import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PermissionManager @Inject constructor() : ViewModel() {
    
    private val _permissionState = MutableStateFlow(PermissionState())
    val permissionState: StateFlow<PermissionState> = _permissionState.asStateFlow()
    
    private val _showPermissionBottomSheet = MutableStateFlow(false)
    val showPermissionBottomSheet: StateFlow<Boolean> = _showPermissionBottomSheet.asStateFlow()
    
    fun checkPermissions(context: Context) {
        viewModelScope.launch {
            val isNotificationListenerGranted = PermissionChecker.isNotificationListenerPermissionGranted(context)
            val isNotificationGranted = PermissionChecker.isNotificationPermissionGranted(context)
            val allGranted = isNotificationListenerGranted && isNotificationGranted

            _permissionState.value = PermissionState(
                isNotificationListenerGranted = isNotificationListenerGranted,
                isNotificationGranted = isNotificationGranted,
                allGranted = allGranted
            )
            
            // Show bottom sheet if permissions are not granted
            if (!allGranted) {
                _showPermissionBottomSheet.value = true
            }
        }
    }
    
    fun recheckPermissions(context: Context) {
        checkPermissions(context)
    }
    
    fun showPermissionDialog() {
        _showPermissionBottomSheet.value = true
    }
    
    fun hidePermissionDialog() {
        _showPermissionBottomSheet.value = false
    }
    
    fun onPermissionGranted() {
        _showPermissionBottomSheet.value = false
        // You can add any additional logic here when permissions are granted
    }
}

data class PermissionState(
    val isNotificationListenerGranted: Boolean = false,
    val isNotificationGranted: Boolean = false,
    val allGranted: Boolean = false
)
