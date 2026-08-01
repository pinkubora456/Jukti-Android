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
    private val pendingRequestDao: PendingRequestDao
) {
    private val firebaseRepository = FirebaseRepository()
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

    suspend fun initializeSeedDataIfNeeded() {
        if (userProfileDao.getUserProfileDirect() == null) {
            userProfileDao.insertOrUpdateProfile(SampleData.initialUserProfile)
        }
        if (aboutConfigDao.getAboutConfigDirect() == null) {
            aboutConfigDao.insertOrUpdateAboutConfig(SampleData.initialAboutConfig)
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
            val initialReqs = listOf(
                PendingRequestEntity(
                    requestType = "DELETE_USER",
                    title = "Delete User: John Doe",
                    description = "Request to delete user account john@example.com",
                    targetId = "1",
                    payloadJson = "john@example.com",
                    requestedBy = "admin@jukti.in",
                    timestamp = "Today, 10:30 AM",
                    status = "PENDING"
                ),
                PendingRequestEntity(
                    requestType = "DELETE_QUESTION",
                    title = "Delete Question #1",
                    description = "Request to delete question: \"Which Ahom king constructed the Rang Ghar...\"",
                    targetId = "1",
                    payloadJson = "",
                    requestedBy = "admin@jukti.in",
                    timestamp = "Today, 11:15 AM",
                    status = "PENDING"
                ),
                PendingRequestEntity(
                    requestType = "BLOCK_USER",
                    title = "Block User: Bob Jones",
                    description = "Request to block user account bob@example.com due to policy violation",
                    targetId = "3",
                    payloadJson = "bob@example.com",
                    requestedBy = "moderator@jukti.in",
                    timestamp = "Yesterday",
                    status = "PENDING"
                ),
                PendingRequestEntity(
                    requestType = "UPGRADE_PLAN",
                    title = "Upgrade Plan for Alice Smith",
                    description = "Request to upgrade alice@example.com to Premium 1 Year plan",
                    targetId = "2",
                    payloadJson = "Premium 1 Year|Valid till Jun 2027",
                    requestedBy = "admin@jukti.in",
                    timestamp = "Yesterday",
                    status = "PENDING"
                ),
                PendingRequestEntity(
                    requestType = "CREATE_PLAN",
                    title = "Create Plan: Super Pass 2026",
                    description = "Create plan Super Pass 2026 at ₹499 with 1 Year validity",
                    targetId = "",
                    payloadJson = "Super Pass 2026|₹499|₹999|1 Year",
                    requestedBy = "admin@jukti.in",
                    timestamp = "2 days ago",
                    status = "PENDING"
                ),
                PendingRequestEntity(
                    requestType = "DELETE_MOCK",
                    title = "Delete Mock Test #2",
                    description = "Request to delete mock test: \"APSC CCE General Studies Prelims Mock\"",
                    targetId = "2",
                    payloadJson = "",
                    requestedBy = "admin@jukti.in",
                    timestamp = "2 days ago",
                    status = "PENDING"
                )
            )
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
            awardXp(150, timeSpentMins)
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
        val newLevel = (newXp / 200) + 1
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

    suspend fun deleteMockTestById(id: Long) {
        val test = mockTestDao.getAllMockTests().firstOrNull()?.find { it.id == id }
        if (test != null) {
            mockTestDao.deleteMockTest(test)
        }
    }
}
