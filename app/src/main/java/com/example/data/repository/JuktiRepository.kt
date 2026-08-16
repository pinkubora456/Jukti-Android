package com.example.data.repository

import com.example.data.local.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class JuktiRepository(
    private val questionDao: QuestionDao,
    private val mockTestDao: MockTestDao,
    private val studyNoteDao: StudyNoteDao,
    private val examUpdateDao: ExamUpdateDao,
    private val bannerDao: BannerDao,
    private val notificationDao: NotificationDao,
    private val notificationCategoryDao: NotificationCategoryDao,
    private val userProfileDao: UserProfileDao,
    private val aboutConfigDao: AboutConfigDao,
    private val planDao: PlanDao,
    private val examDao: ExamDao,
    private val subjectChapterDao: SubjectChapterDao,
    private val pendingRequestDao: PendingRequestDao,
    private val faqDao: FaqDao,
    private val questionProgressDao: QuestionProgressDao,
    private val activityLogDao: ActivityLogDao,
    private val entitlementDao: EntitlementDao,
    val syncManager: FirebaseSyncManager
) {
    private val firebaseRepository = FirebaseRepository()

    val activityLogs: Flow<List<ActivityLogEntity>> = combine(
        firebaseRepository.observeActivityLogs(),
        activityLogDao.getAllLogs()
    ) { remote, local ->
        val map = HashMap<Long, ActivityLogEntity>()
        local.forEach { map[it.id] = it }
        remote.forEach { map[it.id] = it }
        map.values.sortedByDescending { it.timestamp }
    }

    val allQuestions: Flow<List<QuestionEntity>> = combine(
        firebaseRepository.observeQuestions(),
        questionDao.getAllQuestions()
    ) { remoteQuestions, localQuestions ->
        if (remoteQuestions.isEmpty()) {
            localQuestions
        } else {
            val remoteIds = remoteQuestions.map { it.id }.toSet()
            val mergedRemote = remoteQuestions.map { remote ->
                val local = localQuestions.find { it.id == remote.id }
                if (local != null) {
                    remote.copy(
                        isBookmarked = local.isBookmarked || remote.isBookmarked,
                        isLiked = local.isLiked || remote.isLiked,
                        isHidden = local.isHidden || remote.isHidden,
                        isReported = local.isReported || remote.isReported
                    )
                } else {
                    remote
                }
            }
            val localOnly = localQuestions.filter { it.id !in remoteIds }
            mergedRemote + localOnly
        }
    }

    val bookmarkedQuestions: Flow<List<QuestionEntity>> = allQuestions.map { list -> list.filter { it.isBookmarked } }
    val smartPracticeQuestions: Flow<List<QuestionEntity>> = combine(
        allQuestions,
        questionProgressDao.getAllProgress()
    ) { questions, progressList ->
        val progressMap = progressList.associateBy { it.questionId }
        val eligible = questions.filter { q ->
            if (q.isHidden || q.isReported) return@filter false
            val p = progressMap[q.id]
            val isIncorrect = p?.let { (it.everGotWrong || it.firstAttemptCorrect == false) && !it.isMastered } ?: false
            val isSaved = q.isBookmarked || q.isLiked
            val isNotMastered = p?.isMastered != true
            isIncorrect || isSaved || isNotMastered
        }
        if (eligible.isNotEmpty()) eligible else questions.filter { !it.isHidden && !it.isReported }
    }
    val hiddenQuestions: Flow<List<QuestionEntity>> = allQuestions.map { list -> list.filter { it.isHidden } }

    val allMockTests: Flow<List<MockTestEntity>> = combine(
        firebaseRepository.observeMockTests(),
        mockTestDao.getAllMockTests()
    ) { remoteMocks, localMocks ->
        if (remoteMocks.isEmpty()) {
            localMocks
        } else {
            remoteMocks.map { remote ->
                val local = localMocks.find { it.id == remote.id }
                if (local != null) {
                    remote.copy(
                        isCompleted = local.isCompleted,
                        userScore = local.userScore,
                        userAccuracy = local.userAccuracy,
                        userRank = local.userRank,
                        userPercentile = local.userPercentile,
                        inProgress = local.inProgress,
                        questionsAnswered = local.questionsAnswered,
                        timeRemainingSeconds = local.timeRemainingSeconds,
                        questionIds = local.questionIds
                    )
                } else {
                    remote
                }
            }
        }
    }

    val allNotes: Flow<List<StudyNoteEntity>> = combine(
        firebaseRepository.observeStudyNotes(),
        studyNoteDao.getAllNotes()
    ) { remoteNotes, localNotes ->
        if (remoteNotes.isEmpty()) {
            localNotes
        } else {
            remoteNotes.map { remote ->
                val local = localNotes.find { it.id == remote.id }
                if (local != null) {
                    remote.copy(
                        isBookmarked = local.isBookmarked,
                        isDownloaded = local.isDownloaded
                    )
                } else {
                    remote
                }
            }
        }
    }

    val savedNotes: Flow<List<StudyNoteEntity>> = allNotes.map { list -> list.filter { it.isBookmarked || it.isDownloaded } }

    val allExamUpdates: Flow<List<ExamUpdateEntity>> = combine(
        firebaseRepository.observeExamUpdates(),
        examUpdateDao.getAllUpdates()
    ) { remote, local ->
        if (remote.isEmpty()) local
        else {
            val remoteIds = remote.map { it.id }.toSet()
            val combined = remote.toMutableList()
            local.forEach { loc -> if (!remoteIds.contains(loc.id)) combined.add(loc) }
            combined
        }
    }

    val allBanners: Flow<List<BannerEntity>> = combine(
        firebaseRepository.observeBanners(),
        bannerDao.getAllBanners()
    ) { remote, local ->
        if (remote.isEmpty()) local
        else {
            val remoteIds = remote.map { it.id }.toSet()
            val combined = remote.toMutableList()
            local.forEach { loc -> if (!remoteIds.contains(loc.id)) combined.add(loc) }
            combined
        }
    }
    val activeBanners: Flow<List<BannerEntity>> = allBanners.map { list -> list.filter { it.isActive } }

    val allNotifications: Flow<List<NotificationEntity>> = combine(
        firebaseRepository.observeNotifications(),
        notificationDao.getAllNotifications()
    ) { remoteNotifications, localNotifications ->
        if (remoteNotifications.isEmpty()) {
            localNotifications
        } else {
            val remoteIds = remoteNotifications.map { it.id }.toSet()
            val mergedRemote = remoteNotifications.map { remote ->
                val local = localNotifications.find { it.id == remote.id }
                if (local != null) {
                    remote.copy(
                        isRead = local.isRead || remote.isRead
                    )
                } else {
                    GlobalScope.launch(Dispatchers.IO) {
                        try { notificationDao.insertNotification(remote) } catch (e: Throwable) {}
                    }
                    remote
                }
            }
            val localOnly = localNotifications.filter { it.id !in remoteIds }
            (mergedRemote + localOnly).sortedByDescending { it.id }
        }
    }
    val userProfile: Flow<UserProfileEntity?> = userProfileDao.getUserProfile()
    
    fun observeUserProfile(email: String, uid: String?): Flow<UserProfileEntity?> =
        firebaseRepository.observeUserProfile(email, uid)
    
    fun getUserEntitlement(userId: String): Flow<EntitlementEntity?> {
        return entitlementDao.getEntitlement(userId)
    }
    
    val aboutConfig: Flow<AboutConfigEntity?> = combine(
        firebaseRepository.observeAboutConfig(),
        aboutConfigDao.getAboutConfig()
    ) { remote, local ->
        if (remote != null) {
            aboutConfigDao.insertOrUpdateAboutConfig(remote)
            remote
        } else {
            local
        }
    }

    val allPlans: Flow<List<PlanEntity>> = combine(
        firebaseRepository.observePlans(),
        planDao.getAllPlans()
    ) { remote, local ->
        if (remote.isEmpty()) {
            local
        } else if (local.isEmpty()) {
            remote
        } else {
            val remoteMap = remote.associateBy { it.id }
            local.map { loc -> remoteMap[loc.id] ?: loc }
        }
    }

    val allExams: Flow<List<ExamEntity>> = firebaseRepository.observeExams()
    val allSubjectsChapters: Flow<List<SubjectChapterEntity>> = firebaseRepository.observeSubjectsChapters()
    val allPendingRequests: Flow<List<PendingRequestEntity>> = combine(
        firebaseRepository.observePendingRequests(),
        pendingRequestDao.getAllPendingRequests()
    ) { remote, local ->
        val map = HashMap<Long, PendingRequestEntity>()
        local.forEach { map[it.id] = it }
        remote.forEach { map[it.id] = it }
        map.values.sortedByDescending { it.id }
    }

    val allFaqs: Flow<List<FaqEntity>> = combine(
        firebaseRepository.observeFaqs(),
        faqDao.getAllFaqs()
    ) { remote, local ->
        if (remote.isEmpty()) {
            local
        } else if (local.isEmpty()) {
            remote
        } else {
            val remoteMap = remote.associateBy { it.id }
            local.map { loc -> remoteMap[loc.id] ?: loc }
        }
    }

    suspend fun initializeSeedDataIfNeeded() {
        if (userProfileDao.getUserProfileDirect() == null) {
            userProfileDao.insertOrUpdateProfile(SampleData.initialUserProfile)
        }
        if (aboutConfigDao.getAboutConfigDirect() == null) {
            aboutConfigDao.insertOrUpdateAboutConfig(SampleData.initialAboutConfig)
        }
        val currentFaqs = faqDao.getAllFaqs().firstOrNull()
        if (currentFaqs.isNullOrEmpty()) {
            faqDao.insertAll(SampleData.initialFaqs)
        }
        val currentSubjects = subjectChapterDao.getAllSubjectsChapters().firstOrNull()
        if (currentSubjects.isNullOrEmpty()) {
            subjectChapterDao.insertAll(SampleData.sampleSubjectsChapters)
        }
    }

    // Question Actions
    suspend fun toggleBookmarkQuestion(question: QuestionEntity) {
        val updated = question.copy(isBookmarked = !question.isBookmarked)
        val existing = questionDao.getAllQuestions().firstOrNull()?.find { it.id == question.id }
        if (existing != null) {
            questionDao.updateQuestion(updated)
        } else {
            questionDao.insertQuestion(updated)
        }
        syncManager.enqueueAndSync("QUESTION", updated.id.toString(), "UPDATE", syncManager.questionToMap(updated))
    }

    suspend fun toggleLikeQuestion(question: QuestionEntity) {
        val updated = question.copy(isLiked = !question.isLiked)
        val existing = questionDao.getAllQuestions().firstOrNull()?.find { it.id == question.id }
        if (existing != null) {
            questionDao.updateQuestion(updated)
        } else {
            questionDao.insertQuestion(updated)
        }
        syncManager.enqueueAndSync("QUESTION", updated.id.toString(), "UPDATE", syncManager.questionToMap(updated))
    }

    suspend fun toggleHideQuestion(question: QuestionEntity) {
        val updated = question.copy(isHidden = !question.isHidden)
        val existing = questionDao.getAllQuestions().firstOrNull()?.find { it.id == question.id }
        if (existing != null) {
            questionDao.updateQuestion(updated)
        } else {
            questionDao.insertQuestion(updated)
        }
    }

    suspend fun unhideAllQuestions() {
        val hiddenList = questionDao.getHiddenQuestions().firstOrNull() ?: emptyList()
        hiddenList.forEach { q ->
            questionDao.updateQuestion(q.copy(isHidden = false))
        }
    }

    suspend fun addQuestion(question: QuestionEntity): Pair<Boolean, String> {
        val newId = if (question.id == 0L) System.currentTimeMillis() else question.id
        val updated = question.copy(id = newId)
        questionDao.insertQuestion(updated)
        return syncManager.enqueueAndSync("QUESTION", newId.toString(), "CREATE", syncManager.questionToMap(updated))
    }

    suspend fun updateQuestion(question: QuestionEntity): Pair<Boolean, String> {
        questionDao.updateQuestion(question)
        return syncManager.enqueueAndSync("QUESTION", question.id.toString(), "UPDATE", syncManager.questionToMap(question))
    }

    suspend fun deleteQuestion(question: QuestionEntity): Pair<Boolean, String> {
        questionDao.deleteQuestion(question)
        return syncManager.enqueueAndSync("QUESTION", question.id.toString(), "DELETE")
    }

    suspend fun bulkInsertQuestions(questions: List<QuestionEntity>): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (questions.isEmpty()) return@withContext Pair(true, "No questions to insert.")
        val baseTime = System.currentTimeMillis()
        val updatedList = questions.mapIndexed { index, q ->
            val id = if (q.id == 0L) baseTime + index + 1 else q.id
            q.copy(id = id)
        }
        questionDao.insertAll(updatedList)

        val now = System.currentTimeMillis()
        val syncItems = updatedList.map { q ->
            SyncQueueEntity(
                entityId = q.id.toString(),
                dataType = "QUESTION",
                operation = "CREATE",
                payloadJson = syncManager.mapToJson(syncManager.questionToMap(q)),
                createdAt = now,
                updatedAt = now,
                syncStatus = "PENDING"
            )
        }
        syncManager.enqueueBatch(syncItems)

        return@withContext syncManager.uploadAllWorkspaceChangesToFirebase()
    }

    // Mock Actions
    suspend fun addMockTest(mock: MockTestEntity): Pair<Boolean, String> {
        val newId = if (mock.id == 0L) System.currentTimeMillis() else mock.id
        val updated = mock.copy(id = newId)
        mockTestDao.insertMockTest(updated)
        return syncManager.enqueueAndSync("MOCK_TEST", newId.toString(), "CREATE", syncManager.mockTestToMap(updated))
    }

    suspend fun updateMockTest(mock: MockTestEntity): Pair<Boolean, String> {
        mockTestDao.updateMockTest(mock)
        return syncManager.enqueueAndSync("MOCK_TEST", mock.id.toString(), "UPDATE", syncManager.mockTestToMap(mock))
    }

    suspend fun deleteMockTest(mock: MockTestEntity): Pair<Boolean, String> {
        mockTestDao.deleteMockTest(mock)
        return syncManager.enqueueAndSync("MOCK_TEST", mock.id.toString(), "DELETE")
    }

    suspend fun submitMockResult(
        mockId: Long,
        score: Int,
        accuracy: Float,
        timeSpentMins: Int,
        totalAttempted: Int = 0,
        correctCount: Int = 0
    ) {
        val mock = allMockTests.firstOrNull()?.find { it.id == mockId }
        if (mock != null) {
            val scorePercentage = if (mock.totalMarks > 0) ((score.toFloat() / mock.totalMarks.toFloat()) * 100f) else 0f
            val calculatedPercentile = (scorePercentage * 0.7f + accuracy * 0.3f).coerceIn(5.0f, 99.9f)
            val updated = mock.copy(
                isCompleted = true,
                userScore = score,
                userAccuracy = accuracy,
                userRank = 0,
                userPercentile = calculatedPercentile
            )
            val existing = mockTestDao.getAllMockTests().firstOrNull()?.find { it.id == mockId }
            if (existing != null) {
                mockTestDao.updateMockTest(updated)
            } else {
                mockTestDao.insertMockTest(updated)
            }
            
            // Calculate XP reward from mock test performance
            val scorePercentageInt = scorePercentage.toInt()
            val mockXp = 20 + (scorePercentageInt / 5)
            
            // Update user profile statistics: totalSolved, correctCount, time, XP and level
            val profile = userProfileDao.getUserProfileDirect() ?: SampleData.initialUserProfile
            val newTotalSolved = profile.totalSolved + totalAttempted
            val newCorrectCount = profile.correctCount + correctCount
            val newTotalTime = profile.totalTimeMinutes + timeSpentMins
            val safeAddedXp = mockXp.coerceIn(0, 1000)
            val newXp = profile.xp + safeAddedXp
            
            var newLevel = 1
            while (true) {
                val nextLevel = newLevel + 1
                val currentLvl = nextLevel - 1
                val requiredXp = 50 * currentLvl + 10 * (currentLvl - 1) * (currentLvl - 1)
                if (newXp >= requiredXp) {
                    newLevel = nextLevel
                } else {
                    break
                }
            }
            
            val updatedProfile = profile.copy(
                xp = newXp,
                level = newLevel,
                totalSolved = newTotalSolved,
                correctCount = newCorrectCount,
                totalTimeMinutes = newTotalTime
            )
            userProfileDao.insertOrUpdateProfile(updatedProfile)
            firebaseRepository.saveUserProfile(updatedProfile, merge = true)
        }
    }

    // Study Note Actions
    suspend fun toggleBookmarkNote(note: StudyNoteEntity) {
        val updated = note.copy(isBookmarked = !note.isBookmarked)
        val existing = studyNoteDao.getAllNotes().firstOrNull()?.find { it.id == note.id }
        if (existing != null) {
            studyNoteDao.updateNote(updated)
        } else {
            studyNoteDao.insertNote(updated)
        }
    }

    suspend fun toggleDownloadNote(note: StudyNoteEntity) {
        val updated = note.copy(isDownloaded = !note.isDownloaded)
        val existing = studyNoteDao.getAllNotes().firstOrNull()?.find { it.id == note.id }
        if (existing != null) {
            studyNoteDao.updateNote(updated)
        } else {
            studyNoteDao.insertNote(updated)
        }
    }

    suspend fun addStudyNote(note: StudyNoteEntity): Pair<Boolean, String> {
        val newId = if (note.id == 0L) System.currentTimeMillis() else note.id
        val updated = note.copy(id = newId)
        studyNoteDao.insertNote(updated)
        return syncManager.enqueueAndSync("STUDY_NOTE", newId.toString(), "CREATE", syncManager.studyNoteToMap(updated))
    }

    suspend fun updateStudyNote(note: StudyNoteEntity): Pair<Boolean, String> {
        studyNoteDao.updateNote(note)
        return syncManager.enqueueAndSync("STUDY_NOTE", note.id.toString(), "UPDATE", syncManager.studyNoteToMap(note))
    }

    suspend fun deleteStudyNote(note: StudyNoteEntity): Pair<Boolean, String> {
        studyNoteDao.deleteNote(note)
        return syncManager.enqueueAndSync("STUDY_NOTE", note.id.toString(), "DELETE")
    }

    // Exam Updates
    suspend fun addExamUpdate(update: ExamUpdateEntity): Pair<Boolean, String> {
        val newId = if (update.id == 0L) System.currentTimeMillis() else update.id
        val updated = update.copy(id = newId)
        examUpdateDao.insertUpdate(updated)
        return syncManager.enqueueAndSync("EXAM_UPDATE", newId.toString(), "CREATE", syncManager.examUpdateToMap(updated))
    }

    suspend fun updateExamUpdate(update: ExamUpdateEntity): Pair<Boolean, String> {
        examUpdateDao.insertUpdate(update)
        return syncManager.enqueueAndSync("EXAM_UPDATE", update.id.toString(), "UPDATE", syncManager.examUpdateToMap(update))
    }

    suspend fun deleteExamUpdate(update: ExamUpdateEntity): Pair<Boolean, String> {
        examUpdateDao.deleteUpdate(update)
        return syncManager.enqueueAndSync("EXAM_UPDATE", update.id.toString(), "DELETE")
    }

    // Banners
    suspend fun addBanner(banner: BannerEntity): Pair<Boolean, String> {
        val newId = if (banner.id == 0L) System.currentTimeMillis() else banner.id
        val updated = banner.copy(id = newId)
        bannerDao.insertBanner(updated)
        return syncManager.enqueueAndSync("BANNER", newId.toString(), "CREATE", syncManager.bannerToMap(updated))
    }

    suspend fun updateBanner(banner: BannerEntity): Pair<Boolean, String> {
        bannerDao.insertBanner(banner)
        return syncManager.enqueueAndSync("BANNER", banner.id.toString(), "UPDATE", syncManager.bannerToMap(banner))
    }

    suspend fun deleteBanner(banner: BannerEntity): Pair<Boolean, String> {
        bannerDao.deleteById(banner.id)
        bannerDao.deleteBanner(banner)
        return syncManager.enqueueAndSync("BANNER", banner.id.toString(), "DELETE")
    }

    // Notifications
    suspend fun sendNotification(title: String, body: String, category: String) {
        val notification = NotificationEntity(
            title = title,
            body = body,
            timestamp = "Just now",
            category = category
        )
        val insertedId = notificationDao.insertNotification(notification)
        val updatedNotif = notification.copy(id = if (insertedId != 0L) insertedId else System.currentTimeMillis())
        syncManager.enqueueAndSync("NOTIFICATION", updatedNotif.id.toString(), "CREATE", syncManager.notificationToMap(updatedNotif))
    }

    suspend fun deleteNotification(notification: NotificationEntity) {
        notificationDao.deleteNotification(notification)
        syncManager.enqueueAndSync("NOTIFICATION", notification.id.toString(), "DELETE")
    }

    // User Profile & XP
    suspend fun updateUserProfile(profile: UserProfileEntity) {
        val auth = try { com.google.firebase.auth.FirebaseAuth.getInstance() } catch (e: Exception) { null }
        val authEmail = auth?.currentUser?.email
        val authUid = auth?.currentUser?.uid ?: profile.uid
        val finalProfile = if ((profile.email.isBlank() || profile.email == "scholar@jukti.in") && !authEmail.isNullOrBlank()) {
            profile.copy(email = authEmail, uid = authUid, isLoggedIn = true)
        } else {
            profile.copy(uid = if (profile.uid.isBlank()) authUid else profile.uid)
        }
        userProfileDao.insertOrUpdateProfile(finalProfile)
        try {
            firebaseRepository.saveUserProfile(finalProfile, merge = true)
        } catch (e: Throwable) {
            android.util.Log.e("JuktiRepository", "Failed to save profile to Firebase, local Room updated", e)
        }
    }

    suspend fun clearUserEntitlements(userId: String) {
        try {
            if (userId.isNotBlank()) {
                entitlementDao.deleteEntitlement(userId)
            }
        } catch (e: Exception) {
            android.util.Log.e("JuktiRepository", "Error clearing user entitlement", e)
        }
    }

    suspend fun loadUserProfileForAuth(
        uid: String,
        email: String,
        googleName: String,
        deviceId: String,
        defaultRole: String
    ): UserProfileEntity {
        val remoteProfile = firebaseRepository.fetchUserProfile(email, uid)
        val remoteEntitlement = firebaseRepository.fetchUserEntitlement(email, uid)

        if (remoteEntitlement != null) {
            entitlementDao.insertEntitlement(remoteEntitlement)
        } else {
            entitlementDao.deleteEntitlement(uid)
        }

        val resolvedProfile = if (remoteProfile != null) {
            remoteProfile.copy(
                uid = uid,
                email = email,
                googleName = if (googleName.isNotBlank()) googleName else remoteProfile.googleName,
                isLoggedIn = true,
                currentDeviceId = deviceId,
                activeDeviceId = deviceId,
                role = if (defaultRole == "OWNER" || defaultRole == "ADMIN") defaultRole else remoteProfile.role
            )
        } else {
            val defaultDisplayName = if (googleName.isNotBlank()) {
                googleName
            } else {
                email.substringBefore("@").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
            UserProfileEntity(
                id = 1,
                uid = uid,
                email = email,
                name = defaultDisplayName,
                googleName = googleName,
                registrationName = defaultDisplayName,
                profileName = defaultDisplayName,
                mobile = "",
                district = "",
                examGoal = "",
                xp = 0,
                level = 1,
                dailyStreak = 0,
                totalSolved = 0,
                correctCount = 0,
                totalTimeMinutes = 0,
                isPremium = (defaultRole == "OWNER" || defaultRole == "ADMIN"),
                role = defaultRole,
                firebaseProjectId = "jukti-26035",
                joinedDate = java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.US).format(java.util.Date()),
                isLoggedIn = true,
                currentDeviceId = deviceId,
                activeDeviceId = deviceId
            )
        }

        userProfileDao.insertOrUpdateProfile(resolvedProfile)
        firebaseRepository.saveUserProfile(resolvedProfile, merge = true)
        return resolvedProfile
    }

    suspend fun syncUserProfileWithFirebase(email: String) {
        if (email.isBlank()) return
        try {
            val auth = try { com.google.firebase.auth.FirebaseAuth.getInstance() } catch (e: Exception) { null }
            val currentUid = auth?.currentUser?.uid
            val localProfile = userProfileDao.getUserProfileDirect()
            val remoteProfile = firebaseRepository.fetchUserProfile(email, currentUid)
            val remoteEntitlement = firebaseRepository.fetchUserEntitlement(email, currentUid)
            
            val docKey = currentUid ?: firebaseRepository.getSanitizedUserDocId(email)
            if (remoteEntitlement != null) {
                entitlementDao.insertEntitlement(remoteEntitlement)
            } else {
                entitlementDao.deleteEntitlement(docKey)
            }

            if (remoteProfile != null) {
                val isSameUser = localProfile != null && (localProfile.email.equals(email, ignoreCase = true) || (currentUid != null && localProfile.uid == currentUid))
                val safeTotalSolved = if (isSameUser && localProfile != null) maxOf(localProfile.totalSolved, remoteProfile.totalSolved) else remoteProfile.totalSolved
                val safeCorrectCount = if (isSameUser && localProfile != null) maxOf(localProfile.correctCount, remoteProfile.correctCount).coerceAtMost(safeTotalSolved) else remoteProfile.correctCount.coerceAtMost(safeTotalSolved)
                val merged = if (isSameUser && localProfile != null) {
                    localProfile.copy(
                        uid = currentUid ?: remoteProfile.uid,
                        email = email,
                        xp = maxOf(localProfile.xp, remoteProfile.xp),
                        level = maxOf(localProfile.level, remoteProfile.level),
                        dailyStreak = maxOf(localProfile.dailyStreak, remoteProfile.dailyStreak),
                        totalSolved = safeTotalSolved,
                        correctCount = safeCorrectCount,
                        totalTimeMinutes = maxOf(localProfile.totalTimeMinutes, remoteProfile.totalTimeMinutes),
                        isPremium = localProfile.isPremium || remoteProfile.isPremium,
                        role = if (localProfile.role == "OWNER" || remoteProfile.role == "OWNER" || localProfile.role == "ADMIN" || remoteProfile.role == "ADMIN") {
                            if (localProfile.role == "OWNER" || remoteProfile.role == "OWNER") "OWNER" else "ADMIN"
                        } else "USER",
                        isLoggedIn = true,
                        currentDeviceId = localProfile.currentDeviceId.ifBlank { remoteProfile.currentDeviceId },
                        activeDeviceId = localProfile.activeDeviceId.ifBlank { remoteProfile.activeDeviceId },
                        name = if (localProfile.name.isNotBlank() && localProfile.name != "Assam Scholar") localProfile.name else remoteProfile.name.ifBlank { localProfile.name },
                        mobile = localProfile.mobile.ifBlank { remoteProfile.mobile },
                        district = localProfile.district.ifBlank { remoteProfile.district },
                        examGoal = localProfile.examGoal.ifBlank { remoteProfile.examGoal },
                        profileName = localProfile.profileName.ifBlank { remoteProfile.profileName },
                        registrationName = localProfile.registrationName.ifBlank { remoteProfile.registrationName },
                        googleName = localProfile.googleName.ifBlank { remoteProfile.googleName }
                    )
                } else {
                    remoteProfile.copy(
                        uid = currentUid ?: remoteProfile.uid,
                        email = email,
                        totalSolved = safeTotalSolved,
                        correctCount = safeCorrectCount,
                        isLoggedIn = true
                    )
                }
                userProfileDao.insertOrUpdateProfile(merged)
                firebaseRepository.saveUserProfile(merged, merge = true)
            } else if (localProfile != null && (localProfile.email.equals(email, ignoreCase = true) || (currentUid != null && localProfile.uid == currentUid))) {
                firebaseRepository.saveUserProfile(localProfile.copy(uid = currentUid ?: localProfile.uid, email = email, isLoggedIn = true), merge = true)
            }
        } catch (e: Exception) {
            android.util.Log.e("JuktiRepository", "Error during syncUserProfileWithFirebase", e)
        }
    }

    suspend fun awardXp(addedXp: Int, addedTimeMins: Int = 0) {
        val profile = userProfileDao.getUserProfileDirect() ?: SampleData.initialUserProfile
        val safeAddedXp = addedXp.coerceIn(0, 1000)
        val newXp = profile.xp + safeAddedXp
        
        var newLevel = 1
        while (true) {
            val nextLevel = newLevel + 1
            val currentLvl = nextLevel - 1
            val requiredXp = 50 * currentLvl + 10 * (currentLvl - 1) * (currentLvl - 1)
            if (newXp >= requiredXp) {
                newLevel = nextLevel
            } else {
                break
            }
        }
        
        val updated = profile.copy(
            xp = newXp,
            level = newLevel,
            totalTimeMinutes = profile.totalTimeMinutes + addedTimeMins
        )
        userProfileDao.insertOrUpdateProfile(updated)
        firebaseRepository.saveUserProfile(updated, merge = true)
    }

    suspend fun updateFirebaseProjectId(newProjectId: String) {
        val profile = userProfileDao.getUserProfileDirect() ?: SampleData.initialUserProfile
        userProfileDao.insertOrUpdateProfile(profile.copy(firebaseProjectId = newProjectId))
    }

    suspend fun fetchAllUsersDirect(): List<UserProfileEntity> {
        return firebaseRepository.fetchAllUsers()
    }

    suspend fun updateUserRoleInFirebase(email: String, newRole: String): Boolean {
        return try {
            val users = firebaseRepository.fetchAllUsers()
            val target = users.find { it.email.equals(email, ignoreCase = true) }
            if (target != null) {
                firebaseRepository.saveUserProfile(target.copy(role = newRole), merge = true)
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun retrySingleSync(item: SyncQueueEntity): Pair<Boolean, String> {
        return syncManager.retrySingleItem(item)
    }

    suspend fun runMinimalDiagnosticTest(): Pair<Boolean, String> {
        return syncManager.runMinimalDiagnosticTest()
    }

    suspend fun updateAboutConfig(config: AboutConfigEntity): Pair<Boolean, String> {
        aboutConfigDao.insertOrUpdateAboutConfig(config)
        return syncManager.enqueueAndSync("ABOUT_CONFIG", "1", "UPDATE", syncManager.aboutConfigToMap(config))
    }

    suspend fun insertPlan(plan: PlanEntity): Pair<Boolean, String> { 
        val id = if (plan.id == 0L) System.currentTimeMillis() else plan.id
        val updated = plan.copy(id = id)
        planDao.insertPlan(updated)
        return syncManager.enqueueAndSync("PLAN", id.toString(), "CREATE", syncManager.planToMap(updated))
    }

    suspend fun updatePlan(plan: PlanEntity): Pair<Boolean, String> {
        planDao.insertPlan(plan)
        return syncManager.enqueueAndSync("PLAN", plan.id.toString(), "UPDATE", syncManager.planToMap(plan))
    }

    suspend fun deletePlan(plan: PlanEntity): Pair<Boolean, String> { 
        try {
            planDao.deletePlan(plan)
        } catch (e: Throwable) {
            android.util.Log.e("JuktiRepository", "Error deleting plan locally", e)
        }
        return syncManager.enqueueAndSync("PLAN", plan.id.toString(), "DELETE")
    }

    suspend fun insertExam(exam: ExamEntity): Pair<Boolean, String> {
        return syncManager.addExam(exam)
    }

    suspend fun updateExam(exam: ExamEntity): Pair<Boolean, String> {
        return syncManager.updateExam(exam)
    }

    suspend fun deleteExam(exam: ExamEntity): Pair<Boolean, String> {
        return syncManager.deleteExam(exam)
    }

    suspend fun addSubjectChapter(subjectChapter: SubjectChapterEntity): Pair<Boolean, String> {
        val id = if (subjectChapter.id == 0L) System.currentTimeMillis() else subjectChapter.id
        val updated = subjectChapter.copy(id = id)
        subjectChapterDao.insertSubjectChapter(updated)
        return syncManager.enqueueAndSync("SUBJECT_CHAPTER", id.toString(), "CREATE", syncManager.subjectChapterToMap(updated))
    }

    suspend fun deleteSubjectChapter(subjectChapter: SubjectChapterEntity): Pair<Boolean, String> {
        try {
            subjectChapterDao.deleteSubjectChapter(subjectChapter)
        } catch (e: Throwable) {
            android.util.Log.e("JuktiRepository", "Error deleting subject chapter locally", e)
        }
        return syncManager.enqueueAndSync("SUBJECT_CHAPTER", subjectChapter.id.toString(), "DELETE")
    }

    // Notification Categories
    val allNotificationCategories = notificationCategoryDao.getAllNotificationCategories()
    suspend fun insertNotificationCategory(category: NotificationCategoryEntity) = notificationCategoryDao.insertNotificationCategory(category)
    suspend fun deleteNotificationCategory(category: NotificationCategoryEntity) = notificationCategoryDao.deleteNotificationCategory(category)

    // Pending Requests
    suspend fun insertPendingRequest(request: PendingRequestEntity): Long {
        val id = pendingRequestDao.insertRequest(request)
        val updated = request.copy(id = id)
        syncManager.enqueueAndSync("PENDING_REQUEST", id.toString(), "CREATE", syncManager.pendingRequestToMap(updated))
        return id
    }

    suspend fun updatePendingRequest(request: PendingRequestEntity) {
        pendingRequestDao.updateRequest(request)
        syncManager.enqueueAndSync("PENDING_REQUEST", request.id.toString(), "UPDATE", syncManager.pendingRequestToMap(request))
    }

    suspend fun deletePendingRequest(request: PendingRequestEntity) {
        pendingRequestDao.deleteRequest(request)
        syncManager.enqueueAndSync("PENDING_REQUEST", request.id.toString(), "DELETE", emptyMap())
    }

    suspend fun deleteQuestionById(id: Long) {
        val question = questionDao.getAllQuestions().firstOrNull()?.find { it.id == id }
        if (question != null) {
            deleteQuestion(question)
        } else {
            firebaseRepository.deleteQuestion(id)
            questionDao.deleteQuestionById(id)
        }
    }

    suspend fun resetUserProgress() {
        val currentProfile = userProfileDao.getUserProfileDirect()
        if (currentProfile != null) {
            val resetProfile = currentProfile.copy(
                xp = 0,
                level = 1,
                dailyStreak = 0,
                totalSolved = 0,
                correctCount = 0,
                totalTimeMinutes = 0
            )
            userProfileDao.insertOrUpdateProfile(resetProfile)
            firebaseRepository.saveUserProfile(resetProfile, merge = true)
        }

        val allMocks = mockTestDao.getAllMockTests().firstOrNull() ?: emptyList()
        val resetMocks = allMocks.map { 
            it.copy(
                isCompleted = false,
                userScore = 0,
                userAccuracy = 0f,
                userRank = 0,
                userPercentile = 0f
            )
        }
        resetMocks.forEach { mockTestDao.updateMockTest(it) }
    }

    suspend fun deleteMockTestById(id: Long) {
        val test = mockTestDao.getAllMockTests().firstOrNull()?.find { it.id == id }
        if (test != null) {
            deleteMockTest(test)
        } else {
            firebaseRepository.deleteMockTest(id)
        }
    }

    // FAQ Actions
    suspend fun addFaq(faq: FaqEntity): Pair<Boolean, String> {
        val newId = if (faq.id == 0L) System.currentTimeMillis() else faq.id
        val updated = faq.copy(id = newId)
        faqDao.insertFaq(updated)
        return syncManager.enqueueAndSync("FAQ", newId.toString(), "CREATE", syncManager.faqToMap(updated))
    }

    suspend fun updateFaq(faq: FaqEntity): Pair<Boolean, String> {
        faqDao.insertFaq(faq)
        return syncManager.enqueueAndSync("FAQ", faq.id.toString(), "UPDATE", syncManager.faqToMap(faq))
    }

    suspend fun deleteFaq(faq: FaqEntity): Pair<Boolean, String> {
        faqDao.deleteFaq(faq)
        return syncManager.enqueueAndSync("FAQ", faq.id.toString(), "DELETE")
    }

    suspend fun recordQuestionAnswer(
        questionId: Long,
        isCorrect: Boolean,
        timeSpentSec: Int = 10,
        todayStr: String
    ): Int {
        val progress = questionProgressDao.getProgress(questionId) ?: QuestionProgressEntity(questionId = questionId)
        var xpToAward = 0

        val alreadyMastered = progress.isMastered
        val alreadyAttemptedToday = (progress.lastAttemptDateStr == todayStr)

        var newTotalCorrectDays = progress.totalCorrectDays
        var newFirstAttemptCorrect = progress.firstAttemptCorrect
        var newEverGotWrong = progress.everGotWrong
        var newIsMastered = progress.isMastered

        if (isCorrect) {
            newTotalCorrectDays += 1
            if (!alreadyMastered && !alreadyAttemptedToday) {
                if (progress.firstAttemptCorrect == null) {
                    newFirstAttemptCorrect = true
                    xpToAward = 5 // First correct answer
                } else if (progress.everGotWrong) {
                    xpToAward = 8 // Correct after previously getting it wrong
                } else {
                    xpToAward = 5 // Just another correct answer
                }
                
                if (newTotalCorrectDays >= 3) {
                    newIsMastered = true
                    xpToAward += 10 // Master a question
                }
            }
        } else {
            if (progress.firstAttemptCorrect == null) {
                newFirstAttemptCorrect = false
            }
            newEverGotWrong = true
            newIsMastered = false // Reset mastery when wrong
            newTotalCorrectDays = 0
        }

        val updatedProgress = progress.copy(
            firstAttemptCorrect = newFirstAttemptCorrect,
            everGotWrong = newEverGotWrong,
            totalCorrectDays = newTotalCorrectDays,
            lastAttemptDateStr = todayStr,
            isMastered = newIsMastered
        )
        questionProgressDao.insertOrUpdate(updatedProgress)

        // Atomically update user profile statistics: totalSolved, correctCount, totalTimeMinutes, XP, and Level
        val profile = userProfileDao.getUserProfileDirect() ?: SampleData.initialUserProfile
        val newTotalSolved = profile.totalSolved + 1
        val newCorrectCount = if (isCorrect) (profile.correctCount + 1) else profile.correctCount
        val timeMinsToAdd = (timeSpentSec / 60).coerceAtLeast(if (timeSpentSec > 0) 1 else 0)
        val newTotalTime = profile.totalTimeMinutes + timeMinsToAdd
        
        val safeAddedXp = xpToAward.coerceIn(0, 1000)
        val newXp = profile.xp + safeAddedXp
        
        var newLevel = 1
        while (true) {
            val nextLevel = newLevel + 1
            val currentLvl = nextLevel - 1
            val requiredXp = 50 * currentLvl + 10 * (currentLvl - 1) * (currentLvl - 1)
            if (newXp >= requiredXp) {
                newLevel = nextLevel
            } else {
                break
            }
        }

        val updatedProfile = profile.copy(
            xp = newXp,
            level = newLevel,
            totalSolved = newTotalSolved,
            correctCount = newCorrectCount,
            totalTimeMinutes = newTotalTime
        )
        userProfileDao.insertOrUpdateProfile(updatedProfile)
        firebaseRepository.saveUserProfile(updatedProfile, merge = true)

        return xpToAward
    }

    suspend fun processQuestionAnswerForXp(questionId: Long, isCorrect: Boolean, todayStr: String): Int {
        return recordQuestionAnswer(questionId, isCorrect, 10, todayStr)
    }

    suspend fun incrementDailyStreak() {
        val profile = userProfileDao.getUserProfileDirect() ?: return
        val newStreak = profile.dailyStreak + 1
        val updated = profile.copy(dailyStreak = newStreak)
        userProfileDao.insertOrUpdateProfile(updated)
        
        if (newStreak % 7 == 0) {
            awardXp(30, 0)
        }
    }

    suspend fun insertActivityLog(log: ActivityLogEntity) {
        val id = activityLogDao.insertLog(log)
        val updated = log.copy(id = id)
        syncManager.enqueueAndSync("ACTIVITY_LOG", id.toString(), "CREATE", syncManager.activityLogToMap(updated))
    }

    suspend fun deleteOldAdminLogs(thresholdTime: Long) {
        activityLogDao.deleteOldAdminLogs(thresholdTime)
    }

    suspend fun deleteOldOwnerLogs(thresholdTime: Long) {
        activityLogDao.deleteOldOwnerLogs(thresholdTime)
    }

    suspend fun refreshDataFromFirebase(): Result<String> {
        return try {
            syncManager.fetchAllExams()
            
            val questions = firebaseRepository.fetchAllQuestions()
            if (questions.isNotEmpty()) {
                questionDao.insertAll(questions)
            }

            val mocks = firebaseRepository.fetchAllMockTests()
            if (mocks.isNotEmpty()) {
                mockTestDao.insertAll(mocks)
            }

            val notes = firebaseRepository.fetchAllStudyNotes()
            if (notes.isNotEmpty()) {
                studyNoteDao.insertAll(notes)
            }

            val updates = firebaseRepository.fetchAllExamUpdates()
            if (updates.isNotEmpty()) {
                examUpdateDao.insertAll(updates)
            }

            val banners = firebaseRepository.fetchAllBanners()
            if (banners.isNotEmpty()) {
                bannerDao.insertAll(banners)
            }

            val plans = firebaseRepository.fetchAllPlans()
            if (plans.isNotEmpty()) {
                planDao.insertAll(plans)
            }

            val config = firebaseRepository.fetchAboutConfig()
            if (config != null) {
                aboutConfigDao.insertOrUpdateAboutConfig(config)
            }

            Result.success("All data fetched and updated from Firebase!")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadAllWorkspaceChangesToFirebase(): Pair<Boolean, String> {
        val queueResult = syncManager.uploadAllWorkspaceChangesToFirebase()
        // If queue had items, return the exact status message
        if (queueResult.second != "☁️ All changes synced! No pending updates.") {
            return queueResult
        }
        
        // If queue was empty, sync all current local workspace entities to ensure Firestore is up to date
        return try {
            val questions = questionDao.getAllQuestions().firstOrNull() ?: emptyList()
            val mockTests = mockTestDao.getAllMockTests().firstOrNull() ?: emptyList()
            val studyNotes = studyNoteDao.getAllNotes().firstOrNull() ?: emptyList()
            val plans = planDao.getAllPlans().firstOrNull() ?: emptyList()
            val subjectChapters = subjectChapterDao.getAllSubjectsChapters().firstOrNull() ?: emptyList()
            val banners = bannerDao.getAllBanners().firstOrNull() ?: emptyList()
            val examUpdates = examUpdateDao.getAllUpdates().firstOrNull() ?: emptyList()
            val faqs = faqDao.getAllFaqs().firstOrNull() ?: emptyList()

            val itemCount = firebaseRepository.batchSaveAllData(
                questions = questions,
                mockTests = mockTests,
                studyNotes = studyNotes,
                plans = plans,
                subjectChapters = subjectChapters,
                banners = banners,
                examUpdates = examUpdates,
                faqs = faqs
            )

            val exams = examDao.getAllExams().firstOrNull() ?: emptyList()
            exams.forEach { 
                try { syncManager.updateExam(it) } catch (e: Throwable) {}
            }

            val totalCount = itemCount + exams.size
            Pair(true, "✅ Firebase Updated Successfully\nAll $totalCount workspace items uploaded to Firebase.")
        } catch (e: Exception) {
            Pair(false, "❌ Firebase Update Failed: ${e.localizedMessage ?: e.javaClass.simpleName}\nYour changes are safely saved locally. Firebase upload will be retried automatically.")
        }
    }
}
