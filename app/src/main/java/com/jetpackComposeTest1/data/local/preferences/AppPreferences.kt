package com.jetpackComposeTest1.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import com.jetpackComposeTest1.model.setting.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

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
        private const val KEY_PASSCODE_ENABLED = "passcode_enabled"
        private const val KEY_PASSCODE = "passcode"
        private const val KEY_PASSCODE_DISABLE_INTENT = "passcode_disable_intent"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_PREMIUM_UNLOCKED = "premium_unlocked"
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

    // Passcode Settings
    fun isPasscodeEnabled(): Boolean {
        return prefs.getBoolean(KEY_PASSCODE_ENABLED, false)
    }

    fun setPasscodeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PASSCODE_ENABLED, enabled).apply()
    }

    fun getPasscode(): String? {
        val passcode = prefs.getString(KEY_PASSCODE, null)
        return if (passcode.isNullOrEmpty()) null else passcode
    }

    fun setPasscode(passcode: String) {
        prefs.edit().putString(KEY_PASSCODE, passcode).apply()
    }

    fun clearPasscode() {
        prefs.edit().remove(KEY_PASSCODE).apply()
    }

    fun setPasscodeDisableIntent(intent: Boolean) {
        prefs.edit().putBoolean(KEY_PASSCODE_DISABLE_INTENT, intent).apply()
    }

    fun getPasscodeDisableIntent(): Boolean {
        return prefs.getBoolean(KEY_PASSCODE_DISABLE_INTENT, false)
    }

    fun getThemeMode(): ThemeMode {
        val stored = prefs.getString(KEY_THEME_MODE, null) ?: return ThemeMode.SYSTEM
        return runCatching { ThemeMode.valueOf(stored) }.getOrDefault(ThemeMode.SYSTEM)
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    fun themeModeFlow(): Flow<ThemeMode> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_THEME_MODE) {
                trySend(getThemeMode()).isSuccess
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(getThemeMode())
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    // Premium status
    fun isPremiumUnlocked(): Boolean {
        return prefs.getBoolean(KEY_PREMIUM_UNLOCKED, false)
    }

    fun setPremiumUnlocked(unlocked: Boolean) {
        prefs.edit().putBoolean(KEY_PREMIUM_UNLOCKED, unlocked).apply()
    }

    fun premiumStatusFlow(): Flow<Boolean> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_PREMIUM_UNLOCKED) {
                trySend(isPremiumUnlocked()).isSuccess
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(isPremiumUnlocked())
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
}
