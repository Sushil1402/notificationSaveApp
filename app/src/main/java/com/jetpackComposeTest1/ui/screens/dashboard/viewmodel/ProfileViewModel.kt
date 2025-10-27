package com.jetpackComposeTest1.ui.screens.dashboard.viewmodel

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetpackComposeTest1.db.NotificationDao
import com.jetpackComposeTest1.model.AppInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ProfileViewModel @Inject constructor(private val dto:NotificationDao) : ViewModel() {

    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps

    fun getAllApps(context: Context) {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        // Get list of all apps with launcher intent
        val resolveInfos: List<ResolveInfo> = pm.queryIntentActivities(mainIntent, 0)

        val appsList = resolveInfos.mapNotNull { ri ->
            val activityInfo = ri.activityInfo ?: return@mapNotNull null

            val appName = try {
                val res = pm.getResourcesForApplication(activityInfo.applicationInfo)
                if (res != null && activityInfo.labelRes != 0) {
                    res.getString(activityInfo.labelRes)
                } else {
                    activityInfo.applicationInfo.loadLabel(pm).toString()
                }
            } catch (e: PackageManager.NameNotFoundException) {
                activityInfo.applicationInfo.loadLabel(pm).toString()
            }

            val packageName = activityInfo.applicationInfo.packageName
            val icon = activityInfo.applicationInfo.loadIcon(pm)

            AppInfo(name = appName, packageName = packageName, icon = icon)
        }

        // Update your StateFlow
        _apps.value = appsList


    }


    fun clearAllNotification(){
        viewModelScope.launch {
            dto.clearAll()
        }
    }
}