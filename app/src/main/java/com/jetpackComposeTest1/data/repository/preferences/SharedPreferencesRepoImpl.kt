package com.jetpackComposeTest1.data.repository.preferences

import com.jetpackComposeTest1.data.local.preferences.AppPreferences
import javax.inject.Inject

class SharedPreferencesRepoImpl @Inject constructor(private val appPref: AppPreferences):SharedPreferencesRepository {

    override fun setAppSelectionCompleted(completed: Boolean) {
        appPref.setAppSelectionCompleted(completed)
    }

    override fun isAppSelectionCompleted(): Boolean = appPref.isAppSelectionCompleted()
}