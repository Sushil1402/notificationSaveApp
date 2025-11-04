package com.jetpackComposeTest1.utils

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.jetpackComposeTest1.workers.CleanupWorker
import java.util.concurrent.TimeUnit

class CleanupManager(
    private val context: Context
) {
    companion object {
        private const val WORK_NAME = "auto_cleanup_work"
    }
    
    private val workManager = WorkManager.getInstance(context)

    /**
     * Schedule periodic cleanup work (runs every 24 hours)
     */
    fun scheduleCleanup() {
        val periodicWorkRequest = PeriodicWorkRequestBuilder<CleanupWorker>(
            24, TimeUnit.HOURS,
            15, TimeUnit.MINUTES // Flex interval (runs within 15 min window after 24 hours)
        )
            .addTag(WORK_NAME)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE, // Update existing work if already scheduled
            periodicWorkRequest
        )
    }

    /**
     * Cancel the scheduled cleanup work
     */
    fun cancelCleanup() {
        workManager.cancelUniqueWork(WORK_NAME)
    }

    /**
     * Check if cleanup work is scheduled
     */
    fun isCleanupScheduled(): Boolean {
        val workInfos = workManager.getWorkInfosForUniqueWork(WORK_NAME).get()
        return workInfos.isNotEmpty() && 
               workInfos.any { it.state == androidx.work.WorkInfo.State.ENQUEUED || 
                             it.state == androidx.work.WorkInfo.State.RUNNING }
    }
}
