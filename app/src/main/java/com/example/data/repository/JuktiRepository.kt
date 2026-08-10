package com.example.data.repository

import com.example.data.local.*
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
    val syncManager: FirebaseSyncManager
) {
    private val firebaseRepository = FirebaseRepository()

    val activityLogs: Flow<List<ActivityLogEntity>> = activityLogDao.getAllLogs()

    val allQuestions: Flow<List<QuestionEntity>> = combine(
        firebaseRepository.observeQuestions(),
        questionDao.getAllQuestions()
    ) { remoteQuestions, localQuestions ->
        if (remoteQuestions.isEmpty()) {
            localQuestions
        } else {
            remoteQuestions.map { remote ->
                val local = localQuestions.find { it.id == remote.id }
                if (local != null) {
                    remote.copy(
                        isBookmarked = local.isBookmarked,
                        isLiked = local.isLiked,
                        isHidden = local.isHidden
                    )
                } else {
                    remote
                }
            }
        }
    }

    val bookmarkedQuestions: Flow<List<QuestionEntity>> = allQuestions.map { list -> list.filter { it.isBookmarked } }
    val smartPracticeQuestions: Flow<List<QuestionEntity>> = allQuestions.map { list -> list.filter { it.isLiked || it.isBookmarked } }
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

    val allNotifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()
    val userProfile: Flow<UserProfileEntity?> = userProfileDao.getUserProfile()
    val aboutConfig: Flow<AboutConfigEntity?> = aboutConfigDao.getAboutConfig()

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
    val allPendingRequests: Flow<List<PendingRequestEntity>> = pendingRequestDao.getAllPendingRequests()

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
    }

    suspend fun toggleLikeQuestion(question: QuestionEntity) {
        val updated = question.copy(isLiked = !question.isLiked)
        val existing = questionDao.getAllQuestions().firstOrNull()?.find { it.id == question.id }
        if (existing != null) {
            questionDao.updateQuestion(updated)
        } else {
            questionDao.insertQuestion(updated)
        }
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

    suspend fun bulkInsertQuestions(questions: List<QuestionEntity>): Pair<Boolean, String> {
        val updatedList = questions.map { q ->
            val id = if (q.id == 0L) System.currentTimeMillis() + (0..1000).random() else q.id
            q.copy(id = id)
        }
        questionDao.insertAll(updatedList)
        var lastRes = Pair(true, "✅ ${updatedList.size} questions saved locally.")
        updatedList.forEach { q ->
            lastRes = syncManager.enqueueAndSync("QUESTION", q.id.toString(), "CREATE", syncManager.questionToMap(q))
        }
        return if (lastRes.first) Pair(true, "✅ ${updatedList.size} questions uploaded successfully to Firebase.") else Pair(false, "⚠️ Saved ${updatedList.size} questions locally. Firebase upload will retry automatically.")
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

    suspend fun submitMockResult(mockId: Long, score: Int, accuracy: Float, timeSpentMins: Int) {
        val mock = allMockTests.firstOrNull()?.find { it.id == mockId }
        if (mock != null) {
            val updated = mock.copy(
                isCompleted = true,
                userScore = score,
                userAccuracy = accuracy,
                userRank = (10..35).random(),
                userPercentile = 92.5f
            )
            val existing = mockTestDao.getAllMockTests().firstOrNull()?.find { it.id == mockId }
            if (existing != null) {
                mockTestDao.updateMockTest(updated)
            } else {
                mockTestDao.insertMockTest(updated)
            }
            // Reward XP
            val scorePercentage = if (mock.totalMarks > 0) ((score.toFloat() / mock.totalMarks.toFloat()) * 100f).toInt() else 0
            val mockXp = 20 + (scorePercentage / 5)
            awardXp(mockXp, timeSpentMins)
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
        userProfileDao.insertOrUpdateProfile(profile)
        try {
            firebaseRepository.saveUserProfile(profile, merge = true)
        } catch (e: Throwable) {
            android.util.Log.e("JuktiRepository", "Failed to save profile to Firebase, local Room updated", e)
        }
    }

    suspend fun syncUserProfileWithFirebase(email: String) {
        if (email.isBlank()) return
        try {
            val localProfile = userProfileDao.getUserProfileDirect()
            val remoteProfile = firebaseRepository.fetchUserProfile(email)
            if (remoteProfile != null) {
                val merged = if (localProfile != null) {
                    localProfile.copy(
                        email = email,
                        xp = maxOf(localProfile.xp, remoteProfile.xp),
                        level = maxOf(localProfile.level, remoteProfile.level),
                        dailyStreak = maxOf(localProfile.dailyStreak, remoteProfile.dailyStreak),
                        totalSolved = maxOf(localProfile.totalSolved, remoteProfile.totalSolved),
                        totalTimeMinutes = maxOf(localProfile.totalTimeMinutes, remoteProfile.totalTimeMinutes),
                        isPremium = remoteProfile.isPremium || localProfile.isPremium,
                        role = if (remoteProfile.role != "USER") remoteProfile.role else localProfile.role,
                        isLoggedIn = true,
                        currentDeviceId = localProfile.currentDeviceId.ifBlank { remoteProfile.currentDeviceId },
                        activeDeviceId = localProfile.activeDeviceId.ifBlank { remoteProfile.activeDeviceId }
                    )
                } else {
                    remoteProfile.copy(
                        email = email,
                        isLoggedIn = true
                    )
                }
                userProfileDao.insertOrUpdateProfile(merged)
                firebaseRepository.saveUserProfile(merged, merge = true)
            } else if (localProfile != null) {
                firebaseRepository.saveUserProfile(localProfile.copy(email = email, isLoggedIn = true), merge = true)
            }
        } catch (e: Exception) {
            android.util.Log.e("JuktiRepository", "Error during syncUserProfileWithFirebase", e)
        }
    }

    suspend fun awardXp(addedXp: Int, addedTimeMins: Int = 1) {
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
            totalSolved = profile.totalSolved + 1,
            totalTimeMinutes = profile.totalTimeMinutes + addedTimeMins
        )
        userProfileDao.insertOrUpdateProfile(updated)
        firebaseRepository.saveUserProfile(updated, merge = true)
    }

    suspend fun updateFirebaseProjectId(newProjectId: String) {
        val profile = userProfileDao.getUserProfileDirect() ?: SampleData.initialUserProfile
        userProfileDao.insertOrUpdateProfile(profile.copy(firebaseProjectId = newProjectId))
    }

    suspend fun updateAboutConfig(config: AboutConfigEntity) {
        aboutConfigDao.insertOrUpdateAboutConfig(config)
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
        return pendingRequestDao.insertRequest(request)
    }

    suspend fun updatePendingRequest(request: PendingRequestEntity) {
        pendingRequestDao.updateRequest(request)
    }

    suspend fun deletePendingRequest(request: PendingRequestEntity) {
        pendingRequestDao.deleteRequest(request)
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
            userProfileDao.insertOrUpdateProfile(
                currentProfile.copy(
                    xp = 0,
                    level = 1,
                    dailyStreak = 0,
                    totalSolved = 0,
                    correctCount = 0,
                    totalTimeMinutes = 0
                )
            )
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

    suspend fun processQuestionAnswerForXp(questionId: Long, isCorrect: Boolean, todayStr: String): Int {
        val progress = questionProgressDao.getProgress(questionId) ?: QuestionProgressEntity(questionId = questionId)
        var xpToAward = 0

        if (progress.isMastered) {
            return 0 // No XP for already mastered
        }

        if (progress.lastAttemptDateStr == todayStr) {
            return 0 // No XP for multiple attempts on the same day
        }

        var newTotalCorrectDays = progress.totalCorrectDays
        var newFirstAttemptCorrect = progress.firstAttemptCorrect
        var newEverGotWrong = progress.everGotWrong
        var newIsMastered = progress.isMastered

        if (isCorrect) {
            newTotalCorrectDays += 1
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
        } else {
            if (progress.firstAttemptCorrect == null) {
                newFirstAttemptCorrect = false
            }
            newEverGotWrong = true
            newIsMastered = false // Reset mastery when wrong
            newTotalCorrectDays = 0
        }

        questionProgressDao.insertOrUpdate(progress.copy(firstAttemptCorrect = newFirstAttemptCorrect, everGotWrong = newEverGotWrong, totalCorrectDays = newTotalCorrectDays, lastAttemptDateStr = todayStr, isMastered = newIsMastered))
        if (xpToAward > 0) awardXp(xpToAward, 0)
        return xpToAward
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
        activityLogDao.insertLog(log)
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
            Pair(false, "❌ Firebase Update Failed\nYour changes are safely saved locally. Firebase upload will be retried automatically.")
        }
    }
}
