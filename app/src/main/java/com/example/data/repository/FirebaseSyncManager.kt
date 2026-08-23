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

data class SyncProgressState(
    val isUploading: Boolean = false,
    val stage: String = "Idle",
    val currentItem: Int = 0,
    val totalItems: Int = 0,
    val successCount: Int = 0,
    val failCount: Int = 0,
    val message: String = ""
)

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

    private val _syncProgressState = MutableStateFlow(SyncProgressState())
    val syncProgressState: StateFlow<SyncProgressState> = _syncProgressState.asStateFlow()

    fun updateSyncProgress(
        isUploading: Boolean,
        stage: String,
        currentItem: Int = 0,
        totalItems: Int = 0,
        successCount: Int = 0,
        failCount: Int = 0,
        message: String = ""
    ) {
        _isUploading.value = isUploading
        _syncProgressState.value = SyncProgressState(
            isUploading = isUploading,
            stage = stage,
            currentItem = currentItem,
            totalItems = totalItems,
            successCount = successCount,
            failCount = failCount,
            message = message
        )
    }

    // --- JSON Conversion Helpers --- //

    fun mapToJson(map: Map<String, Any?>): String {
        return try {
            mapToJsonObject(map).toString()
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Error in mapToJson", e)
            "{}"
        }
    }

    private fun mapToJsonObject(map: Map<String, Any?>): JSONObject {
        val json = JSONObject()
        map.forEach { (key, value) ->
            json.put(key, wrapJsonValue(value))
        }
        return json
    }

    private fun wrapJsonValue(value: Any?): Any {
        return when (value) {
            null -> JSONObject.NULL
            is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                mapToJsonObject(value as Map<String, Any?>)
            }
            is List<*> -> {
                val jsonArray = JSONArray()
                value.forEach { item ->
                    jsonArray.put(wrapJsonValue(item))
                }
                jsonArray
            }
            else -> value
        }
    }

    fun jsonToMap(jsonStr: String): Map<String, Any?> {
        if (jsonStr.isBlank()) return emptyMap()
        return try {
            val json = JSONObject(jsonStr)
            parseJsonObjectToMap(json)
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Error parsing json payload", e)
            emptyMap()
        }
    }

    private fun parseJsonObjectToMap(json: JSONObject): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = json.get(key)
            map[key] = unwrapJsonValue(value)
        }
        return map
    }

    private fun unwrapJsonValue(value: Any?): Any? {
        return when {
            value == null || value == JSONObject.NULL -> null
            value is JSONObject -> parseJsonObjectToMap(value)
            value is JSONArray -> {
                val list = mutableListOf<Any?>()
                for (i in 0 until value.length()) {
                    list.add(unwrapJsonValue(value.get(i)))
                }
                list
            }
            else -> value
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
        "examCategory" to q.examCategory,
        "isPremium" to q.isPremium,
        "questionType" to q.questionType,
        "cachedAt" to q.cachedAt,
        "lastAccessedAt" to q.lastAccessedAt,
        "version" to q.version,
        "updatedAt" to System.currentTimeMillis(),
        "firebaseId" to q.firebaseId
    )
    
    fun userQuestionStateToMap(s: UserQuestionStateEntity): Map<String, Any?> = mapOf(
        "userId" to s.userId,
        "questionId" to s.questionId,
        "isBookmarked" to s.isBookmarked,
        "isLiked" to s.isLiked,
        "isHidden" to s.isHidden,
        "isMastered" to s.isMastered,
        "everGotWrong" to s.everGotWrong,
        "firstAttemptCorrect" to s.firstAttemptCorrect,
        "lastUpdatedDateStr" to s.lastUpdatedDateStr
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
        "validityType" to p.validityType,
        "validityValue" to p.validityValue,
        "validityLabel" to p.validityLabel,
        "isLifetime" to p.isLifetime,
        "contents" to p.contents,
        "features" to p.features,
        "isActive" to p.isActive,
        "imageUrl" to p.imageUrl,
        "examTarget" to p.examTarget,
        "googlePlayProductId" to p.googlePlayProductId,
        "createdAt" to (if (p.createdAt > 0L) p.createdAt else System.currentTimeMillis()),
        "updatedAt" to System.currentTimeMillis()
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

    fun pendingRequestToMap(r: PendingRequestEntity): Map<String, Any?> = mapOf(
        "id" to r.id,
        "requestType" to r.requestType,
        "title" to r.title,
        "description" to r.description,
        "targetId" to r.targetId,
        "payloadJson" to r.payloadJson,
        "requestedBy" to r.requestedBy,
        "timestamp" to r.timestamp,
        "status" to r.status
    )

    fun activityLogToMap(a: ActivityLogEntity): Map<String, Any?> = mapOf(
        "id" to a.id,
        "role" to a.role,
        "action" to a.actionDetails,
        "userEmail" to a.userEmail,
        "timestamp" to a.timestamp,
        "details" to a.actionDetails
    )

    fun aboutConfigToMap(config: AboutConfigEntity): Map<String, Any?> = mapOf(
        "id" to config.id,
        "appTitle" to config.appTitle,
        "appSubtitleEn" to config.appSubtitleEn,
        "appSubtitleAs" to config.appSubtitleAs,
        "versionText" to config.versionText,
        "missionEn" to config.missionEn,
        "missionAs" to config.missionAs,
        "logoIconName" to config.logoIconName,
        "logoUrl" to config.logoUrl,
        "logoUpdatedAt" to config.logoUpdatedAt,
        "copyrightText" to config.copyrightText,
        "developerTagline" to config.developerTagline,
        "contactEmail" to config.contactEmail,
        "contactPhone" to config.contactPhone,
        "contactTelegram" to config.contactTelegram,
        "contactWhatsapp" to config.contactWhatsapp,
        "adminEmails" to config.adminEmails,
        "refundPolicyEn" to config.refundPolicyEn,
        "refundPolicyAs" to config.refundPolicyAs,
        "founderName" to config.founderName,
        "founderTitle" to config.founderTitle,
        "founderCredential" to config.founderCredential,
        "founderDescription" to config.founderDescription,
        "founderPhotoUrl" to config.founderPhotoUrl,
        "founderTagline" to config.founderTagline,
        "privacyPolicyContent" to config.privacyPolicyContent,
        "termsConditionsContent" to config.termsConditionsContent,
        "playStoreUrl" to config.playStoreUrl
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
            "PENDING_REQUEST" -> "pending_requests"
            "ACTIVITY_LOG" -> "activity_logs"
            "USER_QUESTION_STATE" -> "user_question_states"
            "ABOUT_CONFIG", "APP_CONFIG" -> "app_config"
            else -> dataType.lowercase() + "s"
        }
    }

    // --- Core Enqueue & Auto-Sync Engine --- //

    suspend fun enqueueBatch(items: List<SyncQueueEntity>) = withContext(Dispatchers.IO) {
        if (items.isEmpty()) return@withContext
        syncQueueDao.insertAllSyncs(items)
    }

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
            Pair(false, "⚠️ Saved locally. Firebase upload failed: $errorMsg\n\nWe will retry automatically.")
        }
    }

    suspend fun executeSingleSync(item: SyncQueueEntity): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val db = firestore
        if (db == null) {
            val error = "[UNAVAILABLE] Firestore instance is null or uninitialized."
            updateItemFailure(item, error)
            return@withContext Pair(false, error)
        }

        val auth = try { com.google.firebase.auth.FirebaseAuth.getInstance() } catch (e: Throwable) { null }
        val currentUser = auth?.currentUser
        val collectionName = getCollectionName(item.dataType)
        val path = "$collectionName/${item.entityId}"

        if (currentUser == null) {
            val errorMsg = "[UNAUTHENTICATED] Authentication required for Firestore upload. Please log in as Admin/Owner. | Path: $path"
            Log.w("FirebaseSyncManager", "Upload halted: $errorMsg")
            updateItemFailure(item, errorMsg)
            return@withContext Pair(false, errorMsg)
        }

        val docRef = db.collection(collectionName).document(item.entityId)
        val authUid = currentUser.uid

        try {
            if (item.operation == "DELETE") {
                docRef.delete().await()
                val queryId = item.entityId.toLongOrNull()
                if (queryId != null) {
                    val querySnap = db.collection(collectionName).whereEqualTo("id", queryId).get().await()
                    querySnap.documents.forEach { doc -> doc.reference.delete().await() }
                }
            } else {
                val payloadMap = jsonToMap(item.payloadJson)
                if (payloadMap.isNotEmpty()) {
                    docRef.set(payloadMap, SetOptions.merge()).await()
                } else {
                    throw IllegalStateException("Payload JSON is empty or invalid for entity $path")
                }
            }

            // Sync successful: remove from pending queue
            syncQueueDao.deleteSync(item)
            Log.i("FirebaseSyncManager", "Successfully synced $path to Firestore (AuthUID: $authUid)")
            Pair(true, "Synced")
        } catch (e: com.google.firebase.firestore.FirebaseFirestoreException) {
            val codeName = e.code.name // e.g., PERMISSION_DENIED, UNAUTHENTICATED, UNAVAILABLE
            var errorMsg = "[$codeName] ${e.message ?: "Firestore Exception"} | Path: $path | AuthUID: $authUid"
            if (codeName.contains("PERMISSION_DENIED")) {
                errorMsg = "Permission Denied: Please update your Firestore Security Rules in the Firebase Console."
            }
            Log.e("FirebaseSyncManager", "Firestore error on $path: $errorMsg", e)
            updateItemFailure(item, errorMsg)
            Pair(false, errorMsg)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Throwable) {
            val exceptionClass = e.javaClass.simpleName
            val errorMsg = "[$exceptionClass] ${e.localizedMessage ?: "Network or Firestore write failure"} | Path: $path | AuthUID: $authUid"
            Log.e("FirebaseSyncManager", "Failed syncing item #${item.syncId} ($path)", e)
            updateItemFailure(item, errorMsg)
            Pair(false, errorMsg)
        }
    }

    suspend fun retrySingleItem(item: SyncQueueEntity): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val updating = item.copy(syncStatus = "UPLOADING", updatedAt = System.currentTimeMillis())
        syncQueueDao.updateSync(updating)
        executeSingleSync(updating)
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

    suspend fun runMinimalDiagnosticTest(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val db = firestore
        if (db == null) return@withContext Pair(false, "Firestore instance is null")
        val auth = try { com.google.firebase.auth.FirebaseAuth.getInstance() } catch (e: Throwable) { null }
        val currentUser = auth?.currentUser
        if (currentUser == null) return@withContext Pair(false, "Authentication required. currentUser is null.")

        val path = "firestore_sync_test/${currentUser.uid}"
        val docRef = db.collection("firestore_sync_test").document(currentUser.uid)

        try {
            val payload = mapOf(
                "test" to true,
                "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            docRef.set(payload, SetOptions.merge()).await()
            Pair(true, "✅ Minimal write test SUCCESS to $path")
        } catch (e: Exception) {
            val codeName = if (e is com.google.firebase.firestore.FirebaseFirestoreException) e.code.name else e.javaClass.simpleName
            var errorMsg = "❌ Minimal write test FAILED on $path: [$codeName] ${e.message}"
            if (codeName.contains("PERMISSION_DENIED")) {
                errorMsg = "Permission Denied: Please update your Firestore Security Rules in the Firebase Console."
            }
            Log.e("FirebaseSyncManager", errorMsg, e)
            Pair(false, errorMsg)
        }
    }

    // --- Bulk Workspace Upload ("Sync Now") --- //

    suspend fun uploadAllWorkspaceChangesToFirebase(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        updateSyncProgress(
            isUploading = true,
            stage = "Preparing",
            currentItem = 0,
            totalItems = 0,
            message = "Preparing data for upload..."
        )
        
        var successCount = 0
        var failCount = 0
        var totalCount = 0
        var resultMessage = ""

        try {
            val pendingList = syncQueueDao.getPendingSyncsList()
            if (pendingList.isEmpty()) {
                resultMessage = "☁️ All changes synced! No pending updates."
                return@withContext Pair(true, resultMessage)
            }

            totalCount = pendingList.size
            updateSyncProgress(
                isUploading = true,
                stage = "Uploading",
                currentItem = 0,
                totalItems = totalCount,
                message = "Uploading 0 of $totalCount changes..."
            )

            var lastErrStr = ""
            for ((index, item) in pendingList.withIndex()) {
                val currentNum = index + 1
                updateSyncProgress(
                    isUploading = true,
                    stage = "Uploading",
                    currentItem = currentNum,
                    totalItems = totalCount,
                    successCount = successCount,
                    failCount = failCount,
                    message = "Uploading $currentNum of $totalCount changes..."
                )

                val (ok, err) = executeSingleSync(item)
                if (ok) {
                    successCount++
                } else {
                    failCount++
                    lastErrStr = err
                }
            }

            updateSyncProgress(
                isUploading = true,
                stage = "Verifying",
                currentItem = totalCount,
                totalItems = totalCount,
                successCount = successCount,
                failCount = failCount,
                message = "Verifying Firebase data..."
            )

            resultMessage = when {
                failCount == 0 -> "✅ Firebase Updated Successfully\nAll $successCount changes uploaded to Firebase."
                successCount > 0 -> "⚠️ Firebase Update Partially Completed\n$successCount changes uploaded successfully.\n$failCount changes are pending retry.\nLast error: $lastErrStr"
                else -> "❌ Firebase Update Failed\nReason: $lastErrStr\nData is saved locally. Firebase sync will retry automatically."
            }

            Pair(failCount == 0, resultMessage)
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Exception during bulk upload", e)
            resultMessage = "❌ Firebase Update Failed: ${e.localizedMessage ?: "Unknown error"}"
            Pair(false, resultMessage)
        } finally {
            updateSyncProgress(
                isUploading = false,
                stage = if (failCount == 0 && totalCount > 0) "Success" else if (successCount > 0) "Partial Failure" else "Failure",
                currentItem = totalCount,
                totalItems = totalCount,
                successCount = successCount,
                failCount = failCount,
                message = resultMessage
            )
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
