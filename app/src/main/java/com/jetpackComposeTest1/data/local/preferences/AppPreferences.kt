package com.jetpackComposeTest1.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "app_preferences", 
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_APP_SELECTION_COMPLETED = "app_selection_completed"
        private const val KEY_FIRST_LAUNCH = "first_launch"
    }

    fun isAppSelectionCompleted(): Boolean {
        return prefs.getBoolean(KEY_APP_SELECTION_COMPLETED, false)
    }

    fun setAppSelectionCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_APP_SELECTION_COMPLETED, completed).apply()
    }

    fun isFirstLaunch(): Boolean {
        return prefs.getBoolean(KEY_FIRST_LAUNCH, true)
    }

    fun setFirstLaunchCompleted() {
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
    }
}
