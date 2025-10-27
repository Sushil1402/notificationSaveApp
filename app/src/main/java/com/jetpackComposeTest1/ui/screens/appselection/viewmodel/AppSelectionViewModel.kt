package com.jetpackComposeTest1.ui.screens.appselection.viewmodel

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetpackComposeTest1.data.local.database.AllAppEntity
import com.jetpackComposeTest1.data.repository.database.AllAppRepository
import com.jetpackComposeTest1.data.repository.preferences.SharedPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AppSelectionViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val allAppRepository: AllAppRepository,
    private val sharedPreferencesRepository: SharedPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppSelectionUiState())
    val uiState: StateFlow<AppSelectionUiState> = _uiState.asStateFlow()

    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isAllAppsEnabled = MutableStateFlow(false)
    val isAllAppsEnabled: StateFlow<Boolean> = _isAllAppsEnabled.asStateFlow()

    init {
        loadInstalledApps()
        loadAppSelectionState()
        setAppSelectionCompleted()
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // Run the heavy app loading operation in background thread
                val appInfoList = withContext(Dispatchers.IO) {
                    val packageManager = context.packageManager
                    val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                        addCategory(Intent.CATEGORY_LAUNCHER)
                    }
                    
                    // Get list of all apps with launcher intent (excludes system apps)
                    val resolveInfos: List<ResolveInfo> = packageManager.queryIntentActivities(mainIntent, 0)
                    
                    resolveInfos.mapNotNull { ri ->
                        try {
                            val activityInfo = ri.activityInfo ?: return@mapNotNull null
                            val appInfo = activityInfo.applicationInfo
                            
                            val appName = try {
                                val res = packageManager.getResourcesForApplication(appInfo)
                                if (res != null && activityInfo.labelRes != 0) {
                                    res.getString(activityInfo.labelRes)
                                } else {
                                    appInfo.loadLabel(packageManager).toString()
                                }
                            } catch (e: PackageManager.NameNotFoundException) {
                                appInfo.loadLabel(packageManager).toString()
                            }
                            
                            val appIcon = appInfo.loadIcon(packageManager)
                            val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                            
                            // Get additional app information
                            val packageInfo = try {
                                packageManager.getPackageInfo(appInfo.packageName, 0)
                            } catch (e: Exception) {
                                null
                            }
                            
                            AppInfo(
                                packageName = appInfo.packageName,
                                appName = appName,
                                appIcon = appIcon,
                                isSystemApp = isSystemApp,
                                isEnabled = false, // Will be updated from database
                                versionName = packageInfo?.versionName,
                                versionCode = packageInfo?.longVersionCode,
                                targetSdkVersion = packageInfo?.applicationInfo?.targetSdkVersion,
                                minSdkVersion = packageInfo?.applicationInfo?.minSdkVersion,
                                firstInstalledAt = packageInfo?.firstInstallTime,
                                lastUpdatedAt = packageInfo?.lastUpdateTime,
                                installSource = getInstallSource(packageInfo),
                                appSize = getAppSize(appInfo)
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }.sortedBy { it.appName }
                }
                
                _apps.value = appInfoList
                
                // Save all apps to database
                saveAppsToDatabase(appInfoList)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load apps"
                )
            }
        }
    }

    private suspend fun saveAppsToDatabase(appInfoList: List<AppInfo>) {
        val appEntities = appInfoList.map { appInfo ->
            AllAppEntity(
                packageName = appInfo.packageName,
                appName = appInfo.appName,
                isEnabled = false,
                isSystemApp = appInfo.isSystemApp,
                isUserApp = !appInfo.isSystemApp,
                versionName = appInfo.versionName,
                versionCode = appInfo.versionCode,
                targetSdkVersion = appInfo.targetSdkVersion,
                minSdkVersion = appInfo.minSdkVersion,
                category = getAppCategory(appInfo.packageName),
                groupType = getAppGroupType(appInfo.packageName),
                firstInstalledAt = appInfo.firstInstalledAt,
                lastUpdatedAt = appInfo.lastUpdatedAt,
                addedToDatabaseAt = System.currentTimeMillis(),
                canSendNotifications = true,
                userNotes = null,
                userTags = null,
                lastNotificationAt = null,
                iconResourceId = null,
                iconPath = null,
                appColor = null,
                installSource = appInfo.installSource,
                appSize = appInfo.appSize,
                dataUsage = null,
                isSynced = false,
                syncTime = null,
                backupId = null
            )
        }
        
        allAppRepository.insertApps(appEntities)
    }

    private fun loadAppSelectionState() {
        viewModelScope.launch {
            allAppRepository.getAllApps().collect { appEntities ->
                val appMap = appEntities.associateBy { it.packageName }
                val updatedApps = _apps.value.map { appInfo ->
                    val entity = appMap[appInfo.packageName]
                    appInfo.copy(isEnabled = entity?.isEnabled ?: false)
                }
                _apps.value = updatedApps
                
                // Update all apps toggle state
                val enabledCount = updatedApps.count { it.isEnabled }
                _isAllAppsEnabled.value = enabledCount == updatedApps.size
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onAppToggleChanged(packageName: String, isEnabled: Boolean) {
        viewModelScope.launch {
            try {
                allAppRepository.updateAppEnabledStatus(packageName, isEnabled)
                
                // Update local state
                val updatedApps = _apps.value.map { appInfo ->
                    if (appInfo.packageName == packageName) {
                        appInfo.copy(isEnabled = isEnabled)
                    } else {
                        appInfo
                    }
                }
                _apps.value = updatedApps
                
                // Update all apps toggle state
                val enabledCount = updatedApps.count { it.isEnabled }
                _isAllAppsEnabled.value = enabledCount == updatedApps.size
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update app selection"
                )
            }
        }
    }

    fun onAllAppsToggleChanged(isEnabled: Boolean) {
        viewModelScope.launch {
            try {
                val packageNames = _apps.value.map { it.packageName }
                allAppRepository.bulkUpdateEnabledStatus(packageNames, isEnabled)
                
                // Update local state
                val updatedApps = _apps.value.map { appInfo ->
                    appInfo.copy(isEnabled = isEnabled)
                }
                _apps.value = updatedApps
                _isAllAppsEnabled.value = isEnabled
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update all apps"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun refreshApps() {
        loadInstalledApps()
    }

    fun getFilteredApps(): List<AppInfo> {
        val query = _searchQuery.value.lowercase()
        return if (query.isBlank()) {
            _apps.value
        } else {
            _apps.value.filter { 
                it.appName.lowercase().contains(query) || 
                it.packageName.lowercase().contains(query)
            }
        }
    }

    private fun getAppCategory(packageName: String): String? {
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
            packageName.contains("game") || packageName.contains("play") -> "games"
            packageName.contains("music") || packageName.contains("spotify") -> "music"
            packageName.contains("photo") || packageName.contains("camera") -> "media"
            else -> null
        }
    }

    private fun getAppGroupType(packageName: String): String {
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
    
    private fun getInstallSource(packageInfo: PackageInfo?): String? {
        return try {
            val installerPackageName = context.packageManager.getInstallerPackageName(packageInfo?.packageName ?: "")
            when (installerPackageName) {
                "com.android.vending" -> "Google Play Store"
                "com.amazon.venezia" -> "Amazon Appstore"
                "com.samsung.android.galaxy" -> "Samsung Galaxy Store"
                "com.huawei.appmarket" -> "Huawei AppGallery"
                "com.mi.globalappstore" -> "Xiaomi GetApps"
                "com.oneplus.appstore" -> "OnePlus Store"
                null -> "Unknown"
                else -> installerPackageName
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    private fun getAppSize(appInfo: ApplicationInfo): Long? {
        return try {
            appInfo.sourceDir?.let { sourceDir ->
                java.io.File(sourceDir).length()
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun setAppSelectionCompleted(){
        sharedPreferencesRepository.setAppSelectionCompleted(true)
    }
}

data class AppInfo(
    val packageName: String,
    val appName: String,
    val appIcon: Drawable,
    val isSystemApp: Boolean,
    val isEnabled: Boolean,
    val versionName: String? = null,
    val versionCode: Long? = null,
    val targetSdkVersion: Int? = null,
    val minSdkVersion: Int? = null,
    val firstInstalledAt: Long? = null,
    val lastUpdatedAt: Long? = null,
    val installSource: String? = null,
    val appSize: Long? = null
)

data class AppSelectionUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)
