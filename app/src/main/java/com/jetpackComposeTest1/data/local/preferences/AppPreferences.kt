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
        private const val KEY_AUTO_CLEANUP_ENABLED = "auto_cleanup_enabled"
        private const val KEY_RETENTION_DAYS = "retention_days"
        private const val KEY_LAST_CLEANUP_TIMESTAMP = "last_cleanup_timestamp"
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

    // Auto Cleanup Settings
    fun isAutoCleanupEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTO_CLEANUP_ENABLED, false)
    }

    fun setAutoCleanupEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_CLEANUP_ENABLED, enabled).apply()
    }

    fun getRetentionDays(): Int {
        return prefs.getInt(KEY_RETENTION_DAYS, 30)
    }

    fun setRetentionDays(days: Int) {
        prefs.edit().putInt(KEY_RETENTION_DAYS, days).apply()
    }

    // Last Cleanup Timestamp (for tracking when cleanup last ran)
    fun getLastCleanupTimestamp(): Long {
        return prefs.getLong(KEY_LAST_CLEANUP_TIMESTAMP, 0L)
    }

    fun setLastCleanupTimestamp(timestamp: Long) {
        prefs.edit().putLong(KEY_LAST_CLEANUP_TIMESTAMP, timestamp).apply()
    }
}
