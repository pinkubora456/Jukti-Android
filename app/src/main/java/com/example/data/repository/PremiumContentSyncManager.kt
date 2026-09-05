package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.*
import com.example.data.util.EffectiveUserEntitlement
import com.example.data.util.PlanValidityEngine
import com.example.util.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

enum class PremiumSyncStatus {
    IDLE,
    SYNCING,
    SUCCESS,
    FAILED,
    RETRY_PENDING
}

data class PremiumSyncState(
    val status: PremiumSyncStatus = PremiumSyncStatus.IDLE,
    val lastSyncTime: Long = 0L,
    val localContentVersion: Int = 0,
    val serverContentVersion: Int = 0,
    val activePlan: String = "Free Plan",
    val activePlanName: String = "Free Plan",
    val syncError: String? = null,
    val isRetrying: Boolean = false,
    val syncedItemsCount: Int = 0
) {
    val syncStatus: PremiumSyncStatus get() = status
    val isSyncing: Boolean get() = status == PremiumSyncStatus.SYNCING
}

class PremiumContentSyncManager(
    private val context: Context,
    private val database: JuktiDatabase,
    private val firebaseRepository: FirebaseRepository,
    private val networkMonitor: NetworkMonitor
) {
    private val prefs = context.getSharedPreferences("jukti_premium_sync_prefs", Context.MODE_PRIVATE)
    private val cacheFile = File(context.filesDir, "jukti_cached_premium_content.json")
    private val tempCacheFile = File(context.filesDir, "jukti_cached_premium_content.json.tmp")

    private val _syncState = MutableStateFlow(
        PremiumSyncState(
            lastSyncTime = prefs.getLong("last_sync_time", 0L),
            localContentVersion = prefs.getInt("local_content_version", 0),
            activePlan = prefs.getString("last_synced_plan_name", "Free Plan") ?: "Free Plan",
            activePlanName = prefs.getString("last_synced_plan_name", "Free Plan") ?: "Free Plan"
        )
    )
    val syncState: StateFlow<PremiumSyncState> = _syncState.asStateFlow()

    @Volatile
    private var activeSyncJob: Job? = null

    /**
     * Initializes the local offline cache immediately on app start.
     * Ensures paying users have instant access to their previously downloaded premium content
     * even before any network request completes or when offline.
     */
    fun initializeLocalCache(
        repository: JuktiRepository,
        effectiveEntitlement: EffectiveUserEntitlement? = null,
        isAdminOrOwner: Boolean = false
    ) {
        try {
            val cached = loadLocalCachedData() ?: return
            if (effectiveEntitlement == null || effectiveEntitlement.isPremium || isAdminOrOwner) {
                val eff = effectiveEntitlement
                val eligibleQs = if (eff != null) {
                    cached.questions.filter { PlanValidityEngine.isQuestionAccessible(it, eff, isAdminOrOwner) }
                } else cached.questions

                val eligibleMocks = if (eff != null) {
                    cached.mockTests.filter { PlanValidityEngine.isMockTestAccessible(it, eff, isAdminOrOwner) }
                } else cached.mockTests

                val eligibleNotes = if (eff != null) {
                    cached.studyNotes.filter { PlanValidityEngine.isStudyNoteAccessible(it, eff, isAdminOrOwner) }
                } else cached.studyNotes

                repository.updatePremiumContent(eligibleQs, eligibleMocks, eligibleNotes)

                _syncState.value = _syncState.value.copy(
                    localContentVersion = cached.version,
                    syncedItemsCount = eligibleQs.size + eligibleMocks.size + eligibleNotes.size
                )
                Log.d("PremiumSyncManager", "Local premium cache restored: ${eligibleQs.size} questions, ${eligibleMocks.size} mock tests, ${eligibleNotes.size} notes")
            }
        } catch (e: Exception) {
            Log.e("PremiumSyncManager", "Failed to initialize local cache", e)
        }
    }

    /**
     * Triggers asynchronous background synchronization of premium content strictly based on
     * the user's active subscription plan benefits and version-based incremental check.
     */
    fun triggerBackgroundSync(
        coroutineScope: CoroutineScope,
        repository: JuktiRepository,
        userProfile: UserProfileEntity?,
        userEntitlements: List<EntitlementEntity>,
        allPlans: List<PlanEntity>,
        isAdminOrOwner: Boolean,
        isManualRetry: Boolean = false
    ) {
        synchronized(this) {
            if (activeSyncJob?.isActive == true) {
                Log.d("PremiumSyncManager", "Sync job already running. Skipping duplicate trigger.")
                return
            }

            activeSyncJob = coroutineScope.launch(Dispatchers.IO) {
                try {
                    val currentTime = System.currentTimeMillis()
                    val effectiveEntitlement = PlanValidityEngine.resolveEffectiveEntitlement(
                        userEntitlements,
                        allPlans,
                        currentTime,
                        isAdminOrOwner
                    )

                    val activePlanName = effectiveEntitlement.effectivePlanName
                    _syncState.value = _syncState.value.copy(
                        activePlan = activePlanName,
                        activePlanName = activePlanName
                    )

                    // 1. FREE PLAN / EXPIRED SUBSCRIPTION CHECK
                    if (!effectiveEntitlement.isPremium) {
                        Log.d("PremiumSyncManager", "User is on Free Plan or Entitlement Expired. Restricting premium access.")
                        repository.clearPremiumCache()
                        _syncState.value = _syncState.value.copy(
                            status = PremiumSyncStatus.SUCCESS,
                            syncError = null,
                            isRetrying = false,
                            activePlan = "Free Plan",
                            activePlanName = "Free Plan"
                        )
                        return@launch
                    }

                    // 2. RESTORE EXISTING OFFLINE CACHE (Never block offline or on start)
                    val cachedData = loadLocalCachedData()
                    if (cachedData != null) {
                        val eligibleCachedQs = cachedData.questions.filter {
                            PlanValidityEngine.isQuestionAccessible(it, effectiveEntitlement, isAdminOrOwner)
                        }
                        val eligibleCachedMocks = cachedData.mockTests.filter {
                            PlanValidityEngine.isMockTestAccessible(it, effectiveEntitlement, isAdminOrOwner)
                        }
                        val eligibleCachedNotes = cachedData.studyNotes.filter {
                            PlanValidityEngine.isStudyNoteAccessible(it, effectiveEntitlement, isAdminOrOwner)
                        }
                        repository.updatePremiumContent(eligibleCachedQs, eligibleCachedMocks, eligibleCachedNotes)
                    }

                    // 3. NETWORK CHECK
                    val isConnected = networkMonitor.isConnected.value
                    if (!isConnected) {
                        if (isManualRetry) {
                            _syncState.value = _syncState.value.copy(
                                status = PremiumSyncStatus.FAILED,
                                syncError = "Premium content couldn't be updated. Please check your internet connection and try again.",
                                isRetrying = false
                            )
                        } else {
                            // Non-blocking retry pending for automatic startup
                            _syncState.value = _syncState.value.copy(
                                status = PremiumSyncStatus.RETRY_PENDING,
                                syncError = null,
                                isRetrying = false
                            )
                        }
                        return@launch
                    }

                    // 4. INCREMENTAL CONTENT VERSION & PLAN CHANGE CHECK
                    _syncState.value = _syncState.value.copy(
                        status = PremiumSyncStatus.SYNCING,
                        syncError = null,
                        isRetrying = isManualRetry
                    )

                    val localVersion = prefs.getInt("local_content_version", 0)
                    val lastPlanKey = prefs.getString("last_synced_plan_key", "")
                    val currentPlanKey = "${effectiveEntitlement.effectivePlanName}_${effectiveEntitlement.combinedTargetExams.sorted().joinToString()}"

                    val serverVersion = try {
                        firebaseRepository.fetchServerContentVersion()
                    } catch (_: Exception) {
                        localVersion
                    }

                    _syncState.value = _syncState.value.copy(
                        serverContentVersion = serverVersion,
                        localContentVersion = localVersion
                    )

                    // If local version matches server version, plan hasn't changed, and valid cache exists:
                    if (localVersion > 0 && localVersion == serverVersion && lastPlanKey == currentPlanKey && cacheFile.exists()) {
                        Log.d("PremiumSyncManager", "Local version ($localVersion) matches server version. No download required.")
                        _syncState.value = _syncState.value.copy(
                            status = PremiumSyncStatus.SUCCESS,
                            syncError = null,
                            isRetrying = false,
                            lastSyncTime = System.currentTimeMillis()
                        )
                        prefs.edit().putLong("last_sync_time", System.currentTimeMillis()).apply()
                        return@launch
                    }

                    // 5. DOWNLOAD & FILTER ONLY ELIGIBLE PREMIUM CONTENT
                    var success = false
                    var attempt = 0
                    val maxAttempts = 3

                    while (attempt < maxAttempts && !success && isActive) {
                        attempt++
                        try {
                            withTimeout(20000L) { // 20s timeout per attempt
                                val fetchedQs = firebaseRepository.fetchPremiumQuestions()
                                val fetchedMocks = firebaseRepository.fetchPremiumMockTests()
                                val fetchedNotes = firebaseRepository.fetchPremiumStudyNotes()

                                // Strict plan-based filtering
                                val eligibleQs = fetchedQs.filter { q ->
                                    PlanValidityEngine.isQuestionAccessible(q, effectiveEntitlement, isAdminOrOwner)
                                }
                                val eligibleMocks = fetchedMocks.filter { m ->
                                    PlanValidityEngine.isMockTestAccessible(m, effectiveEntitlement, isAdminOrOwner)
                                }
                                val eligibleNotes = fetchedNotes.filter { n ->
                                    PlanValidityEngine.isStudyNoteAccessible(n, effectiveEntitlement, isAdminOrOwner)
                                }

                                // Atomic cache persistence: only replace local content after validation
                                saveToLocalCache(
                                    version = serverVersion,
                                    planKey = currentPlanKey,
                                    questions = eligibleQs,
                                    mockTests = eligibleMocks,
                                    studyNotes = eligibleNotes
                                )

                                // Update runtime repository cache
                                repository.updatePremiumContent(eligibleQs, eligibleMocks, eligibleNotes)

                                prefs.edit()
                                    .putLong("last_sync_time", System.currentTimeMillis())
                                    .putInt("local_content_version", serverVersion)
                                    .putString("last_synced_plan_key", currentPlanKey)
                                    .putString("last_synced_plan_name", activePlanName)
                                    .apply()

                                val syncedCount = eligibleQs.size + eligibleMocks.size + eligibleNotes.size
                                _syncState.value = _syncState.value.copy(
                                    status = PremiumSyncStatus.SUCCESS,
                                    lastSyncTime = System.currentTimeMillis(),
                                    localContentVersion = serverVersion,
                                    serverContentVersion = serverVersion,
                                    syncError = null,
                                    isRetrying = false,
                                    syncedItemsCount = syncedCount
                                )
                                success = true
                                Log.d("PremiumSyncManager", "Premium sync successful: $syncedCount items synced (v$serverVersion).")
                            }
                        } catch (e: Exception) {
                            Log.e("PremiumSyncManager", "Sync attempt $attempt failed: ${e.message}")
                            if (attempt < maxAttempts) {
                                delay(2000L * attempt)
                            }
                        }
                    }

                    // 6. FAILURE HANDLING (DO NOT DELETE VALID LOCAL CACHE)
                    if (!success) {
                        Log.e("PremiumSyncManager", "All premium sync attempts failed.")
                        if (!networkMonitor.isConnected.value && !isManualRetry) {
                            _syncState.value = _syncState.value.copy(
                                status = PremiumSyncStatus.RETRY_PENDING,
                                syncError = null,
                                isRetrying = false
                            )
                        } else {
                            _syncState.value = _syncState.value.copy(
                                status = PremiumSyncStatus.FAILED,
                                syncError = "Premium content couldn't be updated. Please check your internet connection and try again.",
                                isRetrying = false
                            )
                        }
                    }

                } catch (e: Exception) {
                    Log.e("PremiumSyncManager", "Fatal error in background sync job", e)
                    _syncState.value = _syncState.value.copy(
                        status = PremiumSyncStatus.FAILED,
                        syncError = "Premium content couldn't be updated. Please check your internet connection and try again.",
                        isRetrying = false
                    )
                }
            }
        }
    }

    private data class LocalCachedData(
        val version: Int,
        val planKey: String,
        val questions: List<QuestionEntity>,
        val mockTests: List<MockTestEntity>,
        val studyNotes: List<StudyNoteEntity>
    )

    private fun loadLocalCachedData(): LocalCachedData? {
        if (!cacheFile.exists()) return null
        return try {
            val jsonStr = cacheFile.readText()
            if (jsonStr.isBlank()) return null
            val root = JSONObject(jsonStr)
            val version = root.optInt("version", 1)
            val planKey = root.optString("planKey", "")

            val qArray = root.optJSONArray("questions") ?: JSONArray()
            val questions = mutableListOf<QuestionEntity>()
            for (i in 0 until qArray.length()) {
                val obj = qArray.optJSONObject(i) ?: continue
                questions.add(jsonToQuestion(obj))
            }

            val mArray = root.optJSONArray("mockTests") ?: JSONArray()
            val mocks = mutableListOf<MockTestEntity>()
            for (i in 0 until mArray.length()) {
                val obj = mArray.optJSONObject(i) ?: continue
                mocks.add(jsonToMockTest(obj))
            }

            val nArray = root.optJSONArray("studyNotes") ?: JSONArray()
            val notes = mutableListOf<StudyNoteEntity>()
            for (i in 0 until nArray.length()) {
                val obj = nArray.optJSONObject(i) ?: continue
                notes.add(jsonToStudyNote(obj))
            }

            LocalCachedData(version, planKey, questions, mocks, notes)
        } catch (e: Exception) {
            Log.e("PremiumSyncManager", "Error reading local premium cache file", e)
            null
        }
    }

    private fun saveToLocalCache(
        version: Int,
        planKey: String,
        questions: List<QuestionEntity>,
        mockTests: List<MockTestEntity>,
        studyNotes: List<StudyNoteEntity>
    ): Boolean {
        return try {
            val root = JSONObject()
            root.put("version", version)
            root.put("planKey", planKey)
            root.put("savedAt", System.currentTimeMillis())

            val qArray = JSONArray()
            for (q in questions) {
                qArray.put(questionToJson(q))
            }
            root.put("questions", qArray)

            val mArray = JSONArray()
            for (m in mockTests) {
                mArray.put(mockTestToJson(m))
            }
            root.put("mockTests", mArray)

            val nArray = JSONArray()
            for (n in studyNotes) {
                nArray.put(studyNoteToJson(n))
            }
            root.put("studyNotes", nArray)

            tempCacheFile.writeText(root.toString())
            tempCacheFile.renameTo(cacheFile)
            true
        } catch (e: Exception) {
            Log.e("PremiumSyncManager", "Error saving local premium cache file", e)
            false
        }
    }

    private fun questionToJson(q: QuestionEntity): JSONObject {
        val obj = JSONObject()
        obj.put("id", q.id)
        obj.put("subject", q.subject)
        obj.put("topic", q.topic)
        obj.put("difficulty", q.difficulty)
        obj.put("questionEn", q.questionEn)
        obj.put("questionAs", q.questionAs)
        obj.put("optionAEn", q.optionAEn)
        obj.put("optionBEn", q.optionBEn)
        obj.put("optionCEn", q.optionCEn)
        obj.put("optionDEn", q.optionDEn)
        obj.put("optionAAs", q.optionAAs)
        obj.put("optionBAs", q.optionBAs)
        obj.put("optionCAs", q.optionCAs)
        obj.put("optionDAs", q.optionDAs)
        obj.put("correctOptionIndex", q.correctOptionIndex)
        obj.put("explanationEn", q.explanationEn)
        obj.put("explanationAs", q.explanationAs)
        obj.put("examCategory", q.examCategory)
        obj.put("isPremium", q.isPremium)
        obj.put("accessType", q.accessType)
        obj.put("questionType", q.questionType)
        obj.put("isReported", q.isReported)
        obj.put("status", q.status)
        obj.put("cachedAt", q.cachedAt)
        obj.put("version", q.version)
        obj.put("duplicateKey", q.duplicateKey)
        return obj
    }

    private fun jsonToQuestion(obj: JSONObject): QuestionEntity {
        return QuestionEntity(
            id = obj.optLong("id", 0L),
            subject = obj.optString("subject", ""),
            topic = obj.optString("topic", ""),
            difficulty = obj.optString("difficulty", "Medium"),
            questionEn = obj.optString("questionEn", ""),
            questionAs = obj.optString("questionAs", ""),
            optionAEn = obj.optString("optionAEn", ""),
            optionBEn = obj.optString("optionBEn", ""),
            optionCEn = obj.optString("optionCEn", ""),
            optionDEn = obj.optString("optionDEn", ""),
            optionAAs = obj.optString("optionAAs", ""),
            optionBAs = obj.optString("optionBAs", ""),
            optionCAs = obj.optString("optionCAs", ""),
            optionDAs = obj.optString("optionDAs", ""),
            correctOptionIndex = obj.optInt("correctOptionIndex", 0),
            explanationEn = obj.optString("explanationEn", ""),
            explanationAs = obj.optString("explanationAs", ""),
            examCategory = obj.optString("examCategory", ""),
            isPremium = obj.optBoolean("isPremium", true),
            accessType = obj.optString("accessType", "PREMIUM"),
            questionType = obj.optString("questionType", "Expected"),
            isReported = obj.optBoolean("isReported", false),
            status = obj.optString("status", "ACTIVE"),
            cachedAt = obj.optLong("cachedAt", System.currentTimeMillis()),
            version = obj.optInt("version", 1),
            duplicateKey = obj.optString("duplicateKey", "")
        )
    }

    private fun mockTestToJson(m: MockTestEntity): JSONObject {
        val obj = JSONObject()
        obj.put("id", m.id)
        obj.put("titleEn", m.titleEn)
        obj.put("titleAs", m.titleAs)
        obj.put("category", m.category)
        obj.put("durationMinutes", m.durationMinutes)
        obj.put("totalQuestions", m.totalQuestions)
        obj.put("totalMarks", m.totalMarks)
        obj.put("isScheduled", m.isScheduled)
        obj.put("scheduledDate", m.scheduledDate)
        obj.put("isCompleted", m.isCompleted)
        obj.put("isPublished", m.isPublished)
        obj.put("testType", m.testType)
        obj.put("subjectOrChapter", m.subjectOrChapter)
        obj.put("negativeMarking", m.negativeMarking)
        obj.put("difficulty", m.difficulty)
        obj.put("isPremium", m.isPremium)
        obj.put("accessType", m.accessType)
        obj.put("questionIds", m.questionIds)
        obj.put("markPerQuestion", m.markPerQuestion)
        obj.put("questionMarksJson", m.questionMarksJson)
        obj.put("subjectMarksJson", m.subjectMarksJson)
        return obj
    }

    private fun jsonToMockTest(obj: JSONObject): MockTestEntity {
        return MockTestEntity(
            id = obj.optLong("id", 0L),
            titleEn = obj.optString("titleEn", ""),
            titleAs = obj.optString("titleAs", ""),
            category = obj.optString("category", ""),
            durationMinutes = obj.optInt("durationMinutes", 0),
            totalQuestions = obj.optInt("totalQuestions", 0),
            totalMarks = obj.optDouble("totalMarks", 0.0).toFloat(),
            isScheduled = obj.optBoolean("isScheduled", false),
            scheduledDate = obj.optString("scheduledDate", ""),
            isCompleted = obj.optBoolean("isCompleted", false),
            isPublished = obj.optBoolean("isPublished", true),
            testType = obj.optString("testType", "Full-Length"),
            subjectOrChapter = obj.optString("subjectOrChapter", "General Studies & Assam GK"),
            negativeMarking = obj.optString("negativeMarking", "0.25 Marks"),
            difficulty = obj.optString("difficulty", "Medium"),
            isPremium = obj.optBoolean("isPremium", true),
            accessType = obj.optString("accessType", "PREMIUM"),
            questionIds = obj.optString("questionIds", ""),
            markPerQuestion = obj.optDouble("markPerQuestion", 1.0).toFloat(),
            questionMarksJson = obj.optString("questionMarksJson", "{}"),
            subjectMarksJson = obj.optString("subjectMarksJson", "{}")
        )
    }

    private fun studyNoteToJson(n: StudyNoteEntity): JSONObject {
        val obj = JSONObject()
        obj.put("id", n.id)
        obj.put("subject", n.subject)
        obj.put("topic", n.topic)
        obj.put("titleEn", n.titleEn)
        obj.put("titleAs", n.titleAs)
        obj.put("contentEn", n.contentEn)
        obj.put("contentAs", n.contentAs)
        obj.put("isBookmarked", n.isBookmarked)
        obj.put("isDownloaded", n.isDownloaded)
        obj.put("readTimeMinutes", n.readTimeMinutes)
        obj.put("isPremium", n.isPremium)
        obj.put("accessType", n.accessType)
        return obj
    }

    private fun jsonToStudyNote(obj: JSONObject): StudyNoteEntity {
        return StudyNoteEntity(
            id = obj.optLong("id", 0L),
            subject = obj.optString("subject", ""),
            topic = obj.optString("topic", ""),
            titleEn = obj.optString("titleEn", ""),
            titleAs = obj.optString("titleAs", ""),
            contentEn = obj.optString("contentEn", ""),
            contentAs = obj.optString("contentAs", ""),
            isBookmarked = obj.optBoolean("isBookmarked", false),
            isDownloaded = obj.optBoolean("isDownloaded", false),
            readTimeMinutes = obj.optInt("readTimeMinutes", 5),
            isPremium = obj.optBoolean("isPremium", true),
            accessType = obj.optString("accessType", "PREMIUM")
        )
    }
}
