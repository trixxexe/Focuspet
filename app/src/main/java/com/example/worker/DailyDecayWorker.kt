package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.db.AppDatabase
import com.example.data.db.FocusPetRepository
import com.example.data.db.VitalityManager

class DailyDecayWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val dao = AppDatabase.getInstance(applicationContext).focusPetDao()
            val repository = FocusPetRepository(dao)
            val vitalityManager = VitalityManager(repository)
            
            // Run daily decay logic
            vitalityManager.applyDailyDecay()
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
