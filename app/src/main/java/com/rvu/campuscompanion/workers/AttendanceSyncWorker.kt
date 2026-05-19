package com.rvu.campuscompanion.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rvu.campuscompanion.RVUApplication
import com.rvu.campuscompanion.data.remote.FirebaseSource
import com.rvu.campuscompanion.utils.Constants
import kotlinx.coroutines.tasks.await

class AttendanceSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val app = applicationContext as RVUApplication
            val uid = FirebaseSource.currentUserId ?: return Result.success()
            val summary = app.attendanceRepository.summarySync()
            val data = summary.associate { it.subject to mapOf(
                "present" to it.present, "total" to it.total, "percentage" to it.percentage
            ) }
            FirebaseSource.firestore.collection(Constants.COLL_USERS).document(uid)
                .collection("attendance_snapshot").document("latest")
                .set(mapOf("subjects" to data, "syncedAt" to System.currentTimeMillis()))
                .await()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
