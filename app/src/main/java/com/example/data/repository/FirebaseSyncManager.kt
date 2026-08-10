package com.example.data.repository

import android.util.Log
import com.example.data.local.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

sealed class SyncState {
    object Synced : SyncState()
    data class Pending(val count: Int) : SyncState()
    data class Uploading(val count: Int) : SyncState()
    data class Error(val failedCount: Int, val pendingCount: Int, val lastErrorMessage: String?) : SyncState()
}

class FirebaseSyncManager(
    private val database: JuktiDatabase
) {
    private val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Throwable) {
            Log.e("FirebaseSyncManager", "Firestore instance unavailable", e)
            null
        }

    private val syncQueueDao: SyncQueueDao = database.syncQueueDao()
    private val examDao: ExamDao = database.examDao()

    val allSyncQueueFlow: Flow<List<SyncQueueEntity>> = syncQueueDao.getAllSyncQueueFlow()
    val pendingSyncsFlow: Flow<List<SyncQueueEntity>> = syncQueueDao.getPendingSyncs()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    // --- JSON Conversion Helpers --- //

    fun mapToJson(map: Map<String, Any?>): String {
        val json = JSONObject()
        map.forEach { (key, value) ->
            when (value) {
                null -> json.put(key, JSONObject.NULL)
                is List<*> -> json.put(key, JSONArray(value))
                else -> json.put(key, value)
            }
        }
        return json.toString()
    }

    fun jsonToMap(jsonStr: String): Map<String, Any?> {
        if (jsonStr.isBlank()) return emptyMap()
        return try {
            val json = JSONObject(jsonStr)
            val map = mutableMapOf<String, Any?>()
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = json.get(key)
                when {
                    value == JSONObject.NULL -> map[key] = null
                    value is JSONArray -> {
                        val list = mutableListOf<Any?>()
                        for (i in 0 until value.length()) {
                            list.add(value.get(i))
                        }
                        map[key] = list
                    }
                    else -> map[key] = value
                }
            }
            map
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Error parsing json payload", e)
            emptyMap()
        }
    }

    // --- Entity Mappers --- //

    fun questionToMap(q: QuestionEntity): Map<String, Any?> = mapOf(
        "id" to q.id,
        "subject" to q.subject,
        "topic" to q.topic,
        "difficulty" to q.difficulty,
        "questionEn" to q.questionEn,
        "questionAs" to q.questionAs,
        "optionAEn" to q.optionAEn,
        "optionBEn" to q.optionBEn,
        "optionCEn" to q.optionCEn,
        "optionDEn" to q.optionDEn,
        "optionAAs" to q.optionAAs,
        "optionBAs" to q.optionBAs,
        "optionCAs" to q.optionCAs,
        "optionDAs" to q.optionDAs,
        "correctOptionIndex" to q.correctOptionIndex,
        "explanationEn" to q.explanationEn,
        "explanationAs" to q.explanationAs,
        "isBookmarked" to q.isBookmarked,
        "isLiked" to q.isLiked,
        "isHidden" to q.isHidden,
        "examCategory" to q.examCategory,
        "isPremium" to q.isPremium,
        "questionType" to q.questionType,
        "isReported" to q.isReported,
        "cachedAt" to q.cachedAt,
        "lastAccessedAt" to q.lastAccessedAt,
        "version" to q.version,
        "updatedAt" to System.currentTimeMillis(),
        "firebaseId" to q.firebaseId
    )

    fun mockTestToMap(m: MockTestEntity): Map<String, Any?> = mapOf(
        "id" to m.id,
        "titleEn" to m.titleEn,
        "titleAs" to m.titleAs,
        "category" to m.category,
        "durationMinutes" to m.durationMinutes,
        "totalQuestions" to m.totalQuestions,
        "totalMarks" to m.totalMarks,
        "isScheduled" to m.isScheduled,
        "scheduledDate" to m.scheduledDate,
        "isCompleted" to m.isCompleted,
        "userScore" to m.userScore,
        "userAccuracy" to m.userAccuracy,
        "userRank" to m.userRank,
        "userPercentile" to m.userPercentile,
        "isPublished" to m.isPublished,
        "testType" to m.testType,
        "subjectOrChapter" to m.subjectOrChapter,
        "negativeMarking" to m.negativeMarking,
        "difficulty" to m.difficulty,
        "isPremium" to m.isPremium,
        "inProgress" to m.inProgress,
        "questionsAnswered" to m.questionsAnswered,
        "timeRemainingSeconds" to m.timeRemainingSeconds,
        "questionIds" to m.questionIds,
        "markPerQuestion" to m.markPerQuestion
    )

    fun studyNoteToMap(n: StudyNoteEntity): Map<String, Any?> = mapOf(
        "id" to n.id,
        "subject" to n.subject,
        "topic" to n.topic,
        "titleEn" to n.titleEn,
        "titleAs" to n.titleAs,
        "contentEn" to n.contentEn,
        "contentAs" to n.contentAs,
        "isBookmarked" to n.isBookmarked,
        "isDownloaded" to n.isDownloaded,
        "readTimeMinutes" to n.readTimeMinutes,
        "isPremium" to n.isPremium
    )

    fun examUpdateToMap(u: ExamUpdateEntity): Map<String, Any?> = mapOf(
        "id" to u.id,
        "examName" to u.examName,
        "category" to u.category,
        "titleEn" to u.titleEn,
        "titleAs" to u.titleAs,
        "updateDate" to u.updateDate,
        "detailEn" to u.detailEn,
        "detailAs" to u.detailAs,
        "officialLink" to u.officialLink,
        "isImportantNotice" to u.isImportantNotice
    )

    fun bannerToMap(b: BannerEntity): Map<String, Any?> = mapOf(
        "id" to b.id,
        "titleEn" to b.titleEn,
        "titleAs" to b.titleAs,
        "subtitleEn" to b.subtitleEn,
        "subtitleAs" to b.subtitleAs,
        "type" to b.type,
        "badgeText" to b.badgeText,
        "actionUrl" to b.actionUrl,
        "isActive" to b.isActive,
        "imageUrl" to b.imageUrl,
        "actionType" to b.actionType,
        "offerValidity" to b.offerValidity,
        "planPrice" to b.planPrice,
        "discount" to b.discount,
        "finalPrice" to b.finalPrice
    )

    fun planToMap(p: PlanEntity): Map<String, Any?> = mapOf(
        "id" to p.id,
        "planName" to p.planName,
        "planPrice" to p.planPrice,
        "discount" to p.discount,
        "finalPrice" to p.finalPrice,
        "offerValidity" to p.offerValidity,
        "planValidity" to p.planValidity,
        "contents" to p.contents,
        "features" to p.features,
        "isActive" to p.isActive
    )

    fun faqToMap(f: FaqEntity): Map<String, Any?> = mapOf(
        "id" to f.id,
        "questionEn" to f.questionEn,
        "questionAs" to f.questionAs,
        "answerEn" to f.answerEn,
        "answerAs" to f.answerAs
    )

    fun subjectChapterToMap(sc: SubjectChapterEntity): Map<String, Any?> = mapOf(
        "id" to sc.id,
        "subject" to sc.subject,
        "chapter" to sc.chapter
    )

    fun examToMap(e: ExamEntity): Map<String, Any?> = mapOf(
        "id" to e.id,
        "firebaseId" to e.firebaseId,
        "title" to e.title,
        "subtitle" to e.subtitle,
        "status" to e.status,
        "updatedAt" to e.updatedAt,
        "version" to e.version
    )

    fun notificationToMap(n: NotificationEntity): Map<String, Any?> = mapOf(
        "id" to n.id,
        "title" to n.title,
        "body" to n.body,
        "timestamp" to n.timestamp,
        "category" to n.category,
        "isRead" to n.isRead
    )

    private fun getCollectionName(dataType: String): String {
        return when (dataType.uppercase()) {
            "QUESTION" -> "questions"
            "MOCK_TEST" -> "mock_tests"
            "STUDY_NOTE" -> "study_notes"
            "EXAM_UPDATE" -> "exam_updates"
            "BANNER" -> "banners"
            "PLAN" -> "plans"
            "FAQ" -> "faqs"
            "SUBJECT_CHAPTER" -> "subjects_chapters"
            "EXAM" -> "exams"
            "NOTIFICATION" -> "notifications"
            else -> dataType.lowercase() + "s"
        }
    }

    // --- Core Enqueue & Auto-Sync Engine --- //

    suspend fun enqueueAndSync(
        dataType: String,
        entityId: String,
        operation: String, // "CREATE", "UPDATE", "DELETE"
        payloadMap: Map<String, Any?> = emptyMap()
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val existing = syncQueueDao.getSyncByEntity(dataType, entityId)
        val payloadJson = mapToJson(payloadMap)
        val now = System.currentTimeMillis()

        if (operation == "DELETE" && existing != null && existing.operation == "CREATE") {
            // Created locally but never uploaded, then deleted. Simply purge from queue.
            syncQueueDao.deleteSync(existing)
            return@withContext Pair(true, "✅ Item removed locally.")
        }

        val itemToEnqueue = if (existing != null) {
            val op = if (existing.operation == "CREATE" && operation == "UPDATE") "CREATE" else operation
            existing.copy(
                operation = op,
                payloadJson = if (operation == "DELETE") "" else payloadJson,
                updatedAt = now,
                syncStatus = "PENDING",
                lastError = null
            )
        } else {
            SyncQueueEntity(
                entityId = entityId,
                dataType = dataType,
                operation = operation,
                payloadJson = payloadJson,
                createdAt = now,
                updatedAt = now,
                syncStatus = "PENDING"
            )
        }

        val insertedId = syncQueueDao.insertSync(itemToEnqueue)
        val currentItem = itemToEnqueue.copy(syncId = if (itemToEnqueue.syncId != 0L) itemToEnqueue.syncId else insertedId)

        // Attempt immediate sync
        val (success, errorMsg) = executeSingleSync(currentItem)
        if (success) {
            Pair(true, "✅ Uploaded successfully to Firebase.")
        } else {
            Log.w("FirebaseSyncManager", "Immediate sync failed for $dataType #$entityId: $errorMsg")
            Pair(false, "⚠️ Saved locally. Firebase upload failed. We will retry automatically.")
        }
    }

    suspend fun executeSingleSync(item: SyncQueueEntity): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val db = firestore
        if (db == null) {
            val error = "Firestore instance is null or uninitialized."
            updateItemFailure(item, error)
            return@withContext Pair(false, error)
        }

        try {
            val collectionName = getCollectionName(item.dataType)
            val docRef = db.collection(collectionName).document(item.entityId)

            if (item.operation == "DELETE") {
                docRef.delete().await()
                // Secondary check for id query deletion if string matching differs
                val queryId = item.entityId.toLongOrNull()
                if (queryId != null) {
                    val querySnap = db.collection(collectionName).whereEqualTo("id", queryId).get().await()
                    querySnap.documents.forEach { doc -> doc.reference.delete().await() }
                }
            } else {
                val payloadMap = jsonToMap(item.payloadJson)
                if (payloadMap.isNotEmpty()) {
                    docRef.set(payloadMap, SetOptions.merge()).await()
                }
            }

            // Sync successful: remove from pending queue
            syncQueueDao.deleteSync(item)
            Pair(true, "Synced")
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Throwable) {
            val errorMsg = e.localizedMessage ?: "Network or Firestore write failure"
            Log.e("FirebaseSyncManager", "Failed syncing item #${item.syncId} (${item.dataType}/${item.entityId})", e)
            updateItemFailure(item, errorMsg)
            Pair(false, errorMsg)
        }
    }

    private suspend fun updateItemFailure(item: SyncQueueEntity, errorMsg: String) {
        val updated = item.copy(
            syncStatus = "FAILED",
            retryCount = item.retryCount + 1,
            lastAttemptAt = System.currentTimeMillis(),
            lastError = errorMsg
        )
        syncQueueDao.updateSync(updated)
    }

    // --- Bulk Workspace Upload ("Sync Now") --- //

    suspend fun uploadAllWorkspaceChangesToFirebase(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        _isUploading.value = true
        try {
            val pendingList = syncQueueDao.getPendingSyncsList()
            if (pendingList.isEmpty()) {
                _isUploading.value = false
                return@withContext Pair(true, "☁️ All changes synced! No pending updates.")
            }

            var successCount = 0
            var failCount = 0

            for (item in pendingList) {
                val (ok, _) = executeSingleSync(item)
                if (ok) {
                    successCount++
                } else {
                    failCount++
                }
            }

            _isUploading.value = false

            val message = when {
                failCount == 0 -> "✅ Firebase Updated Successfully\nAll $successCount changes have been uploaded to Firebase."
                successCount > 0 -> "⚠️ Firebase Update Partially Completed\n$successCount changes uploaded successfully.\n$failCount changes are still pending and will retry automatically."
                else -> "❌ Firebase Update Failed\nYour changes are safely saved locally. Firebase upload will be retried automatically."
            }

            Pair(failCount == 0, message)
        } catch (e: Exception) {
            _isUploading.value = false
            Pair(false, "❌ Firebase Update Failed: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    suspend fun syncPendingQueue(): Pair<Int, Int> = withContext(Dispatchers.IO) {
        val pendingList = syncQueueDao.getPendingSyncsList()
        var successCount = 0
        var failCount = 0
        for (item in pendingList) {
            val (ok, _) = executeSingleSync(item)
            if (ok) successCount++ else failCount++
        }
        Pair(successCount, failCount)
    }

    // --- Admin Operations (Exams) --- //

    suspend fun addExam(exam: ExamEntity): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val examId = if (exam.id == 0L) System.currentTimeMillis() else exam.id
        val db = firestore
        val firebaseId = exam.firebaseId.ifEmpty { db?.collection("exams")?.document()?.id ?: examId.toString() }
        val updatedExam = exam.copy(id = examId, firebaseId = firebaseId, updatedAt = System.currentTimeMillis(), syncStatus = "PENDING")
        examDao.insertExam(updatedExam)
        enqueueAndSync("EXAM", firebaseId, "CREATE", examToMap(updatedExam))
    }

    suspend fun updateExam(exam: ExamEntity): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val firebaseId = exam.firebaseId.ifEmpty { exam.id.toString() }
        val updatedExam = exam.copy(firebaseId = firebaseId, updatedAt = System.currentTimeMillis(), version = exam.version + 1, syncStatus = "PENDING")
        examDao.updateExam(updatedExam)
        enqueueAndSync("EXAM", firebaseId, "UPDATE", examToMap(updatedExam))
    }

    suspend fun deleteExam(exam: ExamEntity): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        examDao.deleteExam(exam)
        val firebaseId = exam.firebaseId.ifEmpty { exam.id.toString() }
        enqueueAndSync("EXAM", firebaseId, "DELETE")
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
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Throwable) {
            Log.e("FirebaseSyncManager", "Error fetching exams", e)
        }
    }
}
