package com.example.data.repository

import com.example.data.local.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class JuktiRepository(
    private val questionDao: QuestionDao,
    private val mockTestDao: MockTestDao,
    private val studyNoteDao: StudyNoteDao,
    private val examUpdateDao: ExamUpdateDao,
    private val bannerDao: BannerDao,
    private val notificationDao: NotificationDao,
    private val userProfileDao: UserProfileDao,
    private val aboutConfigDao: AboutConfigDao,
    private val planDao: PlanDao,
    private val examDao: ExamDao,
    private val subjectChapterDao: SubjectChapterDao,
    private val pendingRequestDao: PendingRequestDao,
    private val faqDao: FaqDao,
    private val questionProgressDao: QuestionProgressDao,
    private val activityLogDao: ActivityLogDao
) {
    private val firebaseRepository = FirebaseRepository()

    val activityLogs: Flow<List<ActivityLogEntity>> = activityLogDao.getAllLogs()

    val allQuestions: Flow<List<QuestionEntity>> = questionDao.getAllQuestions()
    val bookmarkedQuestions: Flow<List<QuestionEntity>> = questionDao.getBookmarkedQuestions()
    val hiddenQuestions: Flow<List<QuestionEntity>> = questionDao.getHiddenQuestions()
    val allMockTests: Flow<List<MockTestEntity>> = mockTestDao.getAllMockTests()
    val allNotes: Flow<List<StudyNoteEntity>> = studyNoteDao.getAllNotes()
    val savedNotes: Flow<List<StudyNoteEntity>> = studyNoteDao.getSavedNotes()
    val allExamUpdates: Flow<List<ExamUpdateEntity>> = examUpdateDao.getAllUpdates()
    val activeBanners: Flow<List<BannerEntity>> = bannerDao.getActiveBanners()
    val allBanners: Flow<List<BannerEntity>> = bannerDao.getAllBanners()
    val allNotifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()
    val userProfile: Flow<UserProfileEntity?> = userProfileDao.getUserProfile()
    val aboutConfig: Flow<AboutConfigEntity?> = aboutConfigDao.getAboutConfig()
    val allPlans: Flow<List<PlanEntity>> = planDao.getAllPlans()
    val allExams: Flow<List<ExamEntity>> = examDao.getAllExams()
    val allSubjectsChapters: Flow<List<SubjectChapterEntity>> = subjectChapterDao.getAllSubjectsChapters()
    val allPendingRequests: Flow<List<PendingRequestEntity>> = pendingRequestDao.getAllPendingRequests()
    val allFaqs: Flow<List<FaqEntity>> = faqDao.getAllFaqs()

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
        val currentQuestions = questionDao.getAllQuestions().firstOrNull()
        if (currentQuestions.isNullOrEmpty()) {
            questionDao.insertAll(SampleData.sampleQuestions)
        }
        val currentMocks = mockTestDao.getAllMockTests().firstOrNull()
        if (currentMocks.isNullOrEmpty()) {
            mockTestDao.insertAll(SampleData.sampleMockTests)
        }
        val currentNotes = studyNoteDao.getAllNotes().firstOrNull()
        if (currentNotes.isNullOrEmpty()) {
            studyNoteDao.insertAll(SampleData.sampleStudyNotes)
        }
        val currentUpdates = examUpdateDao.getAllUpdates().firstOrNull()
        if (currentUpdates.isNullOrEmpty()) {
            examUpdateDao.insertAll(SampleData.sampleExamUpdates)
        }
        val currentBanners = bannerDao.getAllBanners().firstOrNull()
        if (currentBanners.isNullOrEmpty()) {
            bannerDao.insertAll(SampleData.sampleBanners)
        }
        val currentNotifications = notificationDao.getAllNotifications().firstOrNull()
        if (currentNotifications.isNullOrEmpty()) {
            notificationDao.insertAll(SampleData.sampleNotifications)
        }
        val currentExams = examDao.getAllExams().firstOrNull()
        if (currentExams.isNullOrEmpty()) {
            examDao.insertAll(SampleData.sampleExams)
        }
        val currentSubjectsChapters = subjectChapterDao.getAllSubjectsChapters().firstOrNull()
        if (currentSubjectsChapters.isNullOrEmpty()) {
            subjectChapterDao.insertAll(SampleData.sampleSubjectsChapters)
        }
        val currentPendingRequests = pendingRequestDao.getAllPendingRequests().firstOrNull()
        if (currentPendingRequests.isNullOrEmpty()) {
            val initialReqs = emptyList<PendingRequestEntity>()
            pendingRequestDao.insertAll(initialReqs)
        }
    }

    // Question Actions
    suspend fun toggleBookmarkQuestion(question: QuestionEntity) {
        val updated = question.copy(isBookmarked = !question.isBookmarked)
        questionDao.updateQuestion(updated)
    }

    suspend fun toggleLikeQuestion(question: QuestionEntity) {
        val updated = question.copy(isLiked = !question.isLiked)
        questionDao.updateQuestion(updated)
    }

    suspend fun toggleHideQuestion(question: QuestionEntity) {
        val updated = question.copy(isHidden = !question.isHidden)
        questionDao.updateQuestion(updated)
    }

    suspend fun unhideAllQuestions() {
        val hiddenList = questionDao.getHiddenQuestions().firstOrNull() ?: emptyList()
        hiddenList.forEach { q ->
            questionDao.updateQuestion(q.copy(isHidden = false))
        }
    }

    suspend fun addQuestion(question: QuestionEntity): Long {
        val id = questionDao.insertQuestion(question)
        firebaseRepository.saveQuestion(question.copy(id = id))
        return id
    }

    suspend fun updateQuestion(question: QuestionEntity) {
        questionDao.updateQuestion(question)
        firebaseRepository.saveQuestion(question)
    }

    suspend fun deleteQuestion(question: QuestionEntity) {
        questionDao.deleteQuestion(question)
        // Optionally delete from Firestore too, but not required by user
    }

    suspend fun bulkInsertQuestions(questions: List<QuestionEntity>) {
        questionDao.insertAll(questions)
        // Should probably sync here too but keeping it simple for now
    }

    // Mock Actions
    suspend fun addMockTest(mock: MockTestEntity): Long {
        return mockTestDao.insertMockTest(mock)
    }

    suspend fun updateMockTest(mock: MockTestEntity) {
        mockTestDao.updateMockTest(mock)
    }

    suspend fun deleteMockTest(mock: MockTestEntity) {
        mockTestDao.deleteMockTest(mock)
    }

    suspend fun submitMockResult(mockId: Long, score: Int, accuracy: Float, timeSpentMins: Int) {
        val mock = mockTestDao.getAllMockTests().firstOrNull()?.find { it.id == mockId }
        if (mock != null) {
            val updated = mock.copy(
                isCompleted = true,
                userScore = score,
                userAccuracy = accuracy,
                userRank = (10..35).random(),
                userPercentile = 92.5f
            )
            mockTestDao.updateMockTest(updated)
            // Reward XP
            val scorePercentage = if (mock.totalMarks > 0) ((score.toFloat() / mock.totalMarks.toFloat()) * 100f).toInt() else 0
            val mockXp = 20 + (scorePercentage / 5)
            awardXp(mockXp, timeSpentMins)
        }
    }

    // Study Note Actions
    suspend fun toggleBookmarkNote(note: StudyNoteEntity) {
        studyNoteDao.updateNote(note.copy(isBookmarked = !note.isBookmarked))
    }

    suspend fun toggleDownloadNote(note: StudyNoteEntity) {
        studyNoteDao.updateNote(note.copy(isDownloaded = !note.isDownloaded))
    }

    suspend fun addStudyNote(note: StudyNoteEntity): Long {
        val id = studyNoteDao.insertNote(note)
        firebaseRepository.saveStudyNote(note.copy(id = id))
        return id
    }

    suspend fun updateStudyNote(note: StudyNoteEntity) {
        studyNoteDao.updateNote(note)
        firebaseRepository.saveStudyNote(note)
    }

    suspend fun deleteStudyNote(note: StudyNoteEntity) {
        studyNoteDao.deleteNote(note)
    }

    // Exam Updates
    suspend fun addExamUpdate(update: ExamUpdateEntity): Long {
        return examUpdateDao.insertUpdate(update)
    }

    suspend fun updateExamUpdate(update: ExamUpdateEntity) {
        examUpdateDao.updateExamUpdate(update)
    }

    suspend fun deleteExamUpdate(update: ExamUpdateEntity) {
        examUpdateDao.deleteUpdate(update)
    }

    // Banners
    suspend fun addBanner(banner: BannerEntity): Long {
        return bannerDao.insertBanner(banner)
    }

    suspend fun updateBanner(banner: BannerEntity) {
        bannerDao.updateBanner(banner)
    }

    suspend fun deleteBanner(banner: BannerEntity) {
        bannerDao.deleteBanner(banner)
    }

    // Notifications
    suspend fun sendNotification(title: String, body: String, category: String) {
        notificationDao.insertNotification(
            NotificationEntity(
                title = title,
                body = body,
                timestamp = "Just now",
                category = category
            )
        )
    }

    suspend fun deleteNotification(notification: NotificationEntity) {
        notificationDao.deleteNotification(notification)
    }

    // User Profile & XP
    suspend fun updateUserProfile(profile: UserProfileEntity) {
        userProfileDao.insertOrUpdateProfile(profile)
    }

    suspend fun awardXp(addedXp: Int, addedTimeMins: Int = 1) {
        val profile = userProfileDao.getUserProfileDirect() ?: SampleData.initialUserProfile
        val newXp = profile.xp + addedXp
        
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
    }

    suspend fun updateFirebaseProjectId(newProjectId: String) {
        val profile = userProfileDao.getUserProfileDirect() ?: SampleData.initialUserProfile
        userProfileDao.insertOrUpdateProfile(profile.copy(firebaseProjectId = newProjectId))
    }

    suspend fun updateAboutConfig(config: AboutConfigEntity) {
        aboutConfigDao.insertOrUpdateAboutConfig(config)
    }
    suspend fun insertPlan(plan: PlanEntity) { planDao.insertPlan(plan) }

    suspend fun deletePlan(plan: PlanEntity) { planDao.deletePlan(plan) }

    suspend fun insertExam(exam: ExamEntity) { examDao.insertExam(exam) }
    suspend fun updateExam(exam: ExamEntity) { examDao.updateExam(exam) }
    suspend fun deleteExam(exam: ExamEntity) { examDao.deleteExam(exam) }

    suspend fun addSubjectChapter(subjectChapter: SubjectChapterEntity): Long {
        return subjectChapterDao.insertSubjectChapter(subjectChapter)
    }

    suspend fun deleteSubjectChapter(subjectChapter: SubjectChapterEntity) {
        subjectChapterDao.deleteSubjectChapter(subjectChapter)
    }

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
        questionDao.deleteQuestionById(id)
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
            mockTestDao.deleteMockTest(test)
        }
    }

    // FAQ Actions
    suspend fun addFaq(faq: FaqEntity): Long = faqDao.insertFaq(faq)
    suspend fun updateFaq(faq: FaqEntity) = faqDao.updateFaq(faq)
    suspend fun deleteFaq(faq: FaqEntity) = faqDao.deleteFaq(faq)

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
}
