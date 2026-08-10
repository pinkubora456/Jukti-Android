package com.example.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.JuktiDatabase
import com.example.data.repository.FirebaseSyncManager

class FirestoreSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Log.d("FirestoreSyncWorker", "Starting background sync of pending queue...")
            val database = JuktiDatabase.getDatabase(applicationContext)
            val syncManager = FirebaseSyncManager(database)
            val (successCount, failCount) = syncManager.syncPendingQueue()
            
            Log.d("FirestoreSyncWorker", "Background sync finished: $successCount succeeded, $failCount failed")
            if (failCount > 0) {
                Result.retry()
            } else {
                Result.success()
            }
        } catch (e: Exception) {
            Log.e("FirestoreSyncWorker", "Error executing background sync worker", e)
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "FirestoreSyncWorker"

        fun scheduleSync(context: Context) {
            try {
                val constraints = androidx.work.Constraints.Builder()
                    .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                    .build()

                val syncRequest = androidx.work.OneTimeWorkRequestBuilder<FirestoreSyncWorker>()
                    .setConstraints(constraints)
                    .build()

                androidx.work.WorkManager.getInstance(context).enqueueUniqueWork(
                    WORK_NAME,
                    androidx.work.ExistingWorkPolicy.REPLACE,
                    syncRequest
                )
            } catch (e: Exception) {
                Log.e("FirestoreSyncWorker", "Failed to schedule WorkManager task", e)
            }
        }
    }
}
