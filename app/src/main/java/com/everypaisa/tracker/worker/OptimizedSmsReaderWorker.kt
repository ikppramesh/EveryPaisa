package com.everypaisa.tracker.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.everypaisa.tracker.data.sms.SmsTransactionProcessor
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class OptimizedSmsReaderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val smsProcessor: SmsTransactionProcessor
) : CoroutineWorker(appContext, workerParams) {
    
    companion object {
        private const val TAG = "SmsReaderWorker"
    }
    
    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "🚀 Worker started")
            val count = smsProcessor.processAllSms()
            Log.d(TAG, "✅ Worker completed. Parsed $count transactions")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Worker failed: ${e.message}", e)
            Result.retry()
        }
    }
}
