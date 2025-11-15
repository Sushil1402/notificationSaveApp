package com.notistorex.app.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.notistorex.app.data.local.preferences.AppPreferences
import com.notistorex.app.db.NotificationDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar

@HiltWorker
class CleanupWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val notificationDao: NotificationDao,
    private val appPreferences: AppPreferences
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "CleanupWorker"
        const val WORK_NAME = "auto_cleanup_work"
    }

    override suspend fun doWork(): Result {
        return try {
            // Check if auto cleanup is enabled
            if (!appPreferences.isAutoCleanupEnabled()) {
                Log.d(TAG, "Auto cleanup is disabled, skipping cleanup")
                return Result.success()
            }

            // Get retention days
            val retentionDays = appPreferences.getRetentionDays()
            
            // Calculate cutoff timestamp (current time minus retention days)
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -retentionDays)
            val cutoffTimestamp = calendar.timeInMillis

            Log.d(TAG, "Starting cleanup: deleting notifications older than $retentionDays days (before ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(cutoffTimestamp))})")

            // Delete old notifications
            notificationDao.deleteOldNotifications(cutoffTimestamp)
            
            // Store timestamp of when cleanup was last run
            val currentTime = System.currentTimeMillis()
            appPreferences.setLastCleanupTimestamp(currentTime)

            Log.d(TAG, "Cleanup completed successfully. Deleted notifications older than ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(cutoffTimestamp))}. Last cleanup: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(currentTime))}")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
            Result.retry() // Retry if there's an error
        }
    }
}
