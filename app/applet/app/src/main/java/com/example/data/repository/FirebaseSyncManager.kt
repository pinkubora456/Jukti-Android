package com.example.data.repository

import android.util.Log
import com.example.data.local.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.firstOrNull

class FirebaseSyncManager(
    private val database: JuktiDatabase
) {
    private val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Firestore not available", e)
            null
        }

    private val examDao = database.examDao()
    private val syncQueueDao = database.syncQueueDao()

    suspend fun syncPendingQueue() = withContext(Dispatchers.IO) {
        val pendingSyncs = syncQueueDao.getPendingSyncs().firstOrNull() ?: return@withContext
        for (sync in pendingSyncs) {
            // Process sync based on dataType and operation
            // Currently placeholder
        }
    }

    // --- Admin Operations (Firebase as Source of Truth) --- //

    suspend fun addExam(exam: ExamEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val db = firestore
            if (db != null) {
                val docRef = db.collection("exams").document()
                val pendingExam = exam.copy(firebaseId = docRef.id, updatedAt = System.currentTimeMillis(), syncStatus = "PENDING")
                examDao.insertExam(pendingExam)

                val examMap = mapOf(
                    "id" to pendingExam.id,
                    "firebaseId" to pendingExam.firebaseId,
                    "title" to pendingExam.title,
                    "subtitle" to pendingExam.subtitle,
                    "status" to pendingExam.status,
                    "updatedAt" to pendingExam.updatedAt,
                    "version" to pendingExam.version
                )
                docRef.set(examMap).await()

                val syncedExam = pendingExam.copy(syncStatus = "SYNCED")
                examDao.updateExam(syncedExam)
            } else {
                examDao.insertExam(exam)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error adding exam", e)
            examDao.insertExam(exam)
            Result.failure(e)
        }
    }

    suspend fun updateExam(exam: ExamEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val db = firestore
            if (db != null) {
                val docId = if (exam.firebaseId.isNotEmpty()) exam.firebaseId else db.collection("exams").document().id
                val pendingExam = exam.copy(firebaseId = docId, updatedAt = System.currentTimeMillis(), version = exam.version + 1, syncStatus = "PENDING")
                examDao.updateExam(pendingExam)
                
                val docRef = db.collection("exams").document(docId)
                val examMap = mapOf(
                    "id" to pendingExam.id,
                    "firebaseId" to pendingExam.firebaseId,
                    "title" to pendingExam.title,
                    "subtitle" to pendingExam.subtitle,
                    "status" to pendingExam.status,
                    "updatedAt" to pendingExam.updatedAt,
                    "version" to pendingExam.version
                )
                docRef.set(examMap, SetOptions.merge()).await()

                val syncedExam = pendingExam.copy(syncStatus = "SYNCED")
                examDao.updateExam(syncedExam)
            } else {
                examDao.updateExam(exam)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error updating exam", e)
            examDao.updateExam(exam)
            Result.failure(e)
        }
    }

    suspend fun deleteExam(exam: ExamEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (exam.firebaseId.isNotEmpty()) {
                firestore?.collection("exams")?.document(exam.firebaseId)?.delete()?.await()
            }
            examDao.deleteExam(exam)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error deleting exam", e)
            examDao.deleteExam(exam)
            Result.failure(e)
        }
    }

    suspend fun fetchAllExams() = withContext(Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            val snapshot = db.collection("exams").get().await()
            val exams = snapshot.documents.mapNotNull { doc ->
                ExamEntity(
                    id = doc.getLong("id") ?: 0L,
                    firebaseId = doc.id,
                    title = doc.getString("title") ?: "",
                    subtitle = doc.getString("subtitle") ?: "",
                    status = doc.getString("status") ?: "Active",
                    updatedAt = doc.getLong("updatedAt") ?: 0L,
                    version = doc.getLong("version")?.toInt() ?: 1,
                    syncStatus = "SYNCED"
                )
            }
            
            examDao.getAllExams().firstOrNull()?.forEach { 
                if (it.syncStatus == "SYNCED") {
                    examDao.deleteExam(it) 
                }
            }
            examDao.insertAll(exams)
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error fetching exams", e)
        }
    }
}
