package com.notistorex.app.data.repository.preferences

import com.notistorex.app.data.local.preferences.AppPreferences
import javax.inject.Inject

class SharedPreferencesRepoImpl @Inject constructor(private val appPref: AppPreferences):SharedPreferencesRepository {

    override fun setAppSelectionCompleted(completed: Boolean) {
        appPref.setAppSelectionCompleted(completed)
    }

    override fun isAppSelectionCompleted(): Boolean = appPref.isAppSelectionCompleted()
}