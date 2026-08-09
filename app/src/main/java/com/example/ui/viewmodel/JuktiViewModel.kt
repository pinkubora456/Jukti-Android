package com.example.ui.viewmodel

import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.MainActivity

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.repository.JuktiRepository
import com.example.data.repository.SampleData
import com.example.data.repository.UserSessionManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppLanguage {
    ENGLISH,
    ASSAMESE,
    BOTH
}

enum class Screen {
    SPLASH,
    HOME,
    MCQ_STUDY,
    PRACTICE,
    SMART_PRACTICE,
    MOCK_TESTS,
    MOCK_PLAYER,
    MOCK_RESULT,
    STUDY_NOTES,
    STUDY_NOTE_DETAIL,
    LEADERBOARD,
    MY_ANALYTICS,
    EXAM_INFO,
    PROFILE,
    PREMIUM_PLANS,
    GLOBAL_SEARCH,
    FIREBASE_CONFIG,
    AUTH,
    MENU,
    SETTINGS,
    ABOUT,
    CONTACT_US,
    WORKSPACE,
    OWNER_DASHBOARD,
    MANAGE_QBANK,
    MANAGE_MOCK,
    MANAGE_PLAN,
    MANAGE_ADMIN,
    ADMIN_ACTIVITY_LOG,
    EXPORT_REPORTS,
    PENDING_REQUESTS,
    SINGLE_QUESTION_UPLOAD,
    BATCH_IMPORT_QUESTION,
    ALL_QUESTIONS,
    CREATE_PLAN,
    EDIT_PLAN,
    MANAGE_USER_LOG,
    REPORTED_QUESTIONS,
    MANAGE_EXAMS,
    CREATE_MOCK,
    EDIT_MOCK,
    MANAGE_STUDY_NOTES,
    MANAGE_CURRENT_AFFAIRS,
    MANAGE_SUBJECTS_CHAPTERS,
    MANAGE_NOTIFICATIONS,
    STORAGE_MANAGEMENT,
    USER_NOTIFICATIONS,
    MANAGE_EXAM_PATTERN_CUTOFF,
    MANAGE_BANNERS,
    PRIVACY_POLICY,
    TERMS_CONDITIONS
}

class JuktiViewModel(application: Application) : AndroidViewModel(application) {

    private val database = JuktiDatabase.getDatabase(application)
    
    private val prefs = application.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
    private val _lastSessionType = MutableStateFlow(prefs.getString("last_session_type", "mock") ?: "mock")
    val lastSessionType: StateFlow<String> = _lastSessionType.asStateFlow()

    val repository = JuktiRepository(
        database.questionDao(),
        database.mockTestDao(),
        database.studyNoteDao(),
        database.examUpdateDao(),
        database.bannerDao(),
        database.notificationDao(),
        database.notificationCategoryDao(),
        database.userProfileDao(),
        database.aboutConfigDao(),
        database.planDao(),
        database.examDao(),
        database.subjectChapterDao(),
        database.pendingRequestDao(),
        database.faqDao(),
        database.questionProgressDao(),
        database.activityLogDao(),
        com.example.data.repository.FirebaseSyncManager(database)
    )

    val examsList: StateFlow<List<ExamEntity>> = repository.allExams.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allSubjectsChapters = repository.allSubjectsChapters.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    fun addSubjectChapter(subject: String, chapter: String) {
        viewModelScope.launch {
            repository.addSubjectChapter(SubjectChapterEntity(subject = subject, chapter = chapter))
        }
    }

    fun deleteSubjectChapter(subjectChapter: SubjectChapterEntity) {
        viewModelScope.launch {
            repository.deleteSubjectChapter(subjectChapter)
        }
    }

    fun addExam(title: String, subtitle: String, status: String = "Active") {
        logActivity("Added exam: $title")
        viewModelScope.launch {
            repository.insertExam(ExamEntity(title = title, subtitle = subtitle, status = status))
        }
    }

    fun updateExam(exam: ExamEntity) {
        viewModelScope.launch {
            repository.updateExam(exam)
        }
    }

    fun deleteExam(exam: ExamEntity) {
        viewModelScope.launch {
            repository.deleteExam(exam)
        }
    }

    // Language & Theme State
    private val _language = MutableStateFlow(AppLanguage.ENGLISH)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _questionLanguage = MutableStateFlow(AppLanguage.BOTH)
    val questionLanguage: StateFlow<AppLanguage> = _questionLanguage.asStateFlow()

    private val _isDarkTheme = MutableStateFlow<Boolean?>(
        if (prefs.contains("is_dark_theme")) prefs.getBoolean("is_dark_theme", false) else null
    )
    val isDarkTheme: StateFlow<Boolean?> = _isDarkTheme.asStateFlow()

    // Navigation State
    private val _currentScreen = MutableStateFlow(Screen.SPLASH)
    private var splashFinished = false

    fun finishSplash() {
        splashFinished = true
        val prof = userProfile.value
        if (prof != null) {
            _currentScreen.value = if (prof.isLoggedIn) Screen.HOME else Screen.AUTH
        } else {
            _currentScreen.value = Screen.AUTH
        }
    }
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _sessionMessage = MutableStateFlow<String?>(null)
    val sessionMessage: StateFlow<String?> = _sessionMessage.asStateFlow()

    private val _showPremiumPaywall = MutableStateFlow(false)
    val showPremiumPaywall: StateFlow<Boolean> = _showPremiumPaywall.asStateFlow()

    fun showPaywall() {
        if (isUserPremium.value) return
        _showPremiumPaywall.value = true
    }

    fun dismissPaywall() {
        _showPremiumPaywall.value = false
    }

    fun clearSessionMessage() {
        _sessionMessage.value = null
    }

    val userProfile = repository.userProfile.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    // Data Flows from Repository
    val plans = repository.allPlans.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val questions = repository.allQuestions.map { list ->
        list.filter { !it.isReported }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reportedQuestions = repository.allQuestions.map { list -> list.filter { it.isReported } }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val bookmarkedQuestions = repository.bookmarkedQuestions.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val smartPracticeQuestions = repository.smartPracticeQuestions.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val hiddenQuestions = repository.hiddenQuestions.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val mockTests = repository.allMockTests.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val studyNotes = repository.allNotes.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val savedNotes = repository.savedNotes.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val examUpdates = repository.allExamUpdates.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val banners = repository.activeBanners.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val allBanners = repository.allBanners.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allNotificationCategories = repository.allNotificationCategories.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    fun addNotificationCategory(name: String) {
        viewModelScope.launch {
            repository.insertNotificationCategory(NotificationCategoryEntity(name = name))
        }
    }

    fun deleteNotificationCategory(category: NotificationCategoryEntity) {
        viewModelScope.launch {
            repository.deleteNotificationCategory(category)
        }
    }

    val notifications = repository.allNotifications.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val aboutConfig: StateFlow<AboutConfigEntity> = repository.aboutConfig.map {
        val config = it ?: SampleData.initialAboutConfig
        if (config.appTitle == "Jukti (যুক্তি)") {
            val newConfig = config.copy(appTitle = "Jukti")
            viewModelScope.launch { repository.updateAboutConfig(newConfig) }
            newConfig
        } else {
            config
        }
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), SampleData.initialAboutConfig
    )

    val pendingRequests: StateFlow<List<PendingRequestEntity>> = repository.allPendingRequests.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val faqs: StateFlow<List<FaqEntity>> = repository.allFaqs.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    fun addFaq(questionEn: String, questionAs: String, answerEn: String, answerAs: String) {
        viewModelScope.launch {
            repository.addFaq(
                FaqEntity(
                    questionEn = questionEn,
                    questionAs = questionAs,
                    answerEn = answerEn,
                    answerAs = answerAs
                )
            )
        }
    }

    fun updateFaq(faq: FaqEntity) {
        viewModelScope.launch {
            repository.updateFaq(faq)
        }
    }

    fun deleteFaq(faq: FaqEntity) {
        viewModelScope.launch {
            repository.deleteFaq(faq)
        }
    }

    val isAdminOrOwner: StateFlow<Boolean> = combine(userProfile, aboutConfig) { profile, config ->
        val email = profile?.email?.trim()
        val isOwnerEmail = email?.equals("juktieducation@gmail.com", ignoreCase = true) == true
        val adminEmails = config.adminEmails.split(",").map { it.trim() }
        profile?.role == "ADMIN" || profile?.role == "OWNER" || isOwnerEmail || (email != null && adminEmails.contains(email))
    }.stateIn(
        viewModelScope, SharingStarted.Eagerly, false
    )

    val isOwner: StateFlow<Boolean> = userProfile.map { profile ->
        val email = profile?.email?.trim()
        val isOwnerEmail = email?.equals("juktieducation@gmail.com", ignoreCase = true) == true
        profile?.role == "OWNER" || isOwnerEmail
    }.stateIn(
        viewModelScope, SharingStarted.Eagerly, false
    )

    val isUserPremium: StateFlow<Boolean> = combine(userProfile, isAdminOrOwner) { profile, admin ->
        profile?.isPremium == true || admin || profile?.role == "ADMIN" || profile?.role == "OWNER"
    }.stateIn(
        viewModelScope, SharingStarted.Eagerly, false
    )

    // Selection & Filter States
    private val _selectedSubject = MutableStateFlow("All")
    val selectedSubject: StateFlow<String> = _selectedSubject.asStateFlow()

    private val _selectedDifficulty = MutableStateFlow("All")
    val selectedDifficulty: StateFlow<String> = _selectedDifficulty.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedMockTest = MutableStateFlow<MockTestEntity?>(null)
    val selectedMockTest: StateFlow<MockTestEntity?> = _selectedMockTest.asStateFlow()

    private val _selectedStudyNote = MutableStateFlow<StudyNoteEntity?>(null)
    val selectedStudyNote: StateFlow<StudyNoteEntity?> = _selectedStudyNote.asStateFlow()

    // Guest Mode
    private val _isGuestMode = MutableStateFlow(false)
    val isGuestMode: StateFlow<Boolean> = _isGuestMode.asStateFlow()

    // Active Mock Test Session State
    private val _mockUserAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val mockUserAnswers: StateFlow<Map<Int, Int>> = _mockUserAnswers.asStateFlow()

    private val _mockMarkedForReview = MutableStateFlow<Set<Int>>(emptySet())
    val mockMarkedForReview: StateFlow<Set<Int>> = _mockMarkedForReview.asStateFlow()

    private val _mockTimeRemainingSeconds = MutableStateFlow(5400) // 90 mins
    val mockTimeRemainingSeconds: StateFlow<Int> = _mockTimeRemainingSeconds.asStateFlow()

    val activityLogs: StateFlow<List<ActivityLogEntity>> = repository.activityLogs.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    init {
        viewModelScope.launch {
            repository.initializeSeedDataIfNeeded()
            cleanUpOldLogs()
        }
        viewModelScope.launch {
            userProfile.collect { prof ->
                if (prof != null) {
                    if (splashFinished) {
                        if (!prof.isLoggedIn) {
                            if (_currentScreen.value != Screen.AUTH) {
                                _currentScreen.value = Screen.AUTH
                            }
                        } else {
                            if (_currentScreen.value == Screen.AUTH || _currentScreen.value == Screen.SPLASH) {
                                _currentScreen.value = Screen.HOME
                            }
                            if (prof.email.isNotBlank() && prof.currentDeviceId.isNotBlank()) {
                                val activeInManager = UserSessionManager.getActiveDeviceId(prof.email)
                                if (activeInManager == null) {
                                    UserSessionManager.registerSession(prof.email, prof.currentDeviceId)
                                } else if (activeInManager != prof.currentDeviceId) {
                                    logoutDueToOtherDeviceLogin()
                                }
                            }
                        }
                    } else {
                        // Just update the session manager quietly
                        if (prof.isLoggedIn && prof.email.isNotBlank() && prof.currentDeviceId.isNotBlank()) {
                            val activeInManager = UserSessionManager.getActiveDeviceId(prof.email)
                            if (activeInManager == null) {
                                UserSessionManager.registerSession(prof.email, prof.currentDeviceId)
                            } else if (activeInManager != prof.currentDeviceId) {
                                logoutDueToOtherDeviceLogin()
                            }
                        }
                    }
                }
            }
        }
        viewModelScope.launch {
            userProfile.collect { prof ->
                if (prof != null && prof.isLoggedIn && prof.email.isNotBlank()) {
                    UserSessionManager.observeActiveDeviceId(prof.email).collect { activeId ->
                        if (activeId != null && prof.currentDeviceId.isNotBlank() && activeId != prof.currentDeviceId) {
                            logoutDueToOtherDeviceLogin()
                        }
                    }
                }
            }
        }
    }

    fun logActivity(actionDetails: String) {
        val user = userProfile.value ?: return
        if (user.role == "ADMIN" || user.role == "OWNER") {
            viewModelScope.launch {
                repository.insertActivityLog(
                    ActivityLogEntity(
                        userEmail = user.email,
                        role = user.role,
                        actionDetails = actionDetails,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun cleanUpOldLogs() {
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            val adminThreshold = currentTime - (45L * 24 * 60 * 60 * 1000)
            val ownerThreshold = currentTime - (10L * 24 * 60 * 60 * 1000)
            repository.deleteOldAdminLogs(adminThreshold)
            repository.deleteOldOwnerLogs(ownerThreshold)
        }
    }

    fun toggleLanguage() {
        _language.value = if (_language.value == AppLanguage.ENGLISH) AppLanguage.ASSAMESE else AppLanguage.ENGLISH
    }

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
    }

    fun setQuestionLanguage(lang: AppLanguage) {
        _questionLanguage.value = lang
    }


    fun setDarkTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
        prefs.edit().putBoolean("is_dark_theme", isDark).apply()
    }


    private var lastNavTime = 0L

    fun navigateTo(screen: Screen) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastNavTime < 500) return
        lastNavTime = currentTime

        when (screen) {
            Screen.MOCK_TESTS, Screen.MOCK_PLAYER -> {
                _lastSessionType.value = "mock"
                prefs.edit().putString("last_session_type", "mock").apply()
            }
            Screen.PRACTICE -> {
                _lastSessionType.value = "practice"
                prefs.edit().putString("last_session_type", "practice").apply()
            }
            Screen.STUDY_NOTES, Screen.STUDY_NOTE_DETAIL -> {
                _lastSessionType.value = "study"
                prefs.edit().putString("last_session_type", "study").apply()
            }
            else -> {}
        }
        
        val isLoggedIn = userProfile.value?.isLoggedIn ?: false
        if (!isLoggedIn && screen != Screen.AUTH) {
            _currentScreen.value = Screen.AUTH
            return
        }
        if ((screen == Screen.LEADERBOARD || screen == Screen.MY_ANALYTICS) && !isUserPremium.value) {
            _showPremiumPaywall.value = true
            return
        }
        _currentScreen.value = screen
    }

    fun setSubjectFilter(subject: String) {
        _selectedSubject.value = subject
    }

    fun setDifficultyFilter(difficulty: String) {
        _selectedDifficulty.value = difficulty
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleGuestMode(isGuest: Boolean) {
        _isGuestMode.value = false
    }

    fun selectMockTest(mock: MockTestEntity) {
        if (mock.isPremium && !isUserPremium.value) {
            _showPremiumPaywall.value = true
            return
        }
        _selectedMockTest.value = mock
        _mockUserAnswers.value = emptyMap()
        _mockMarkedForReview.value = emptySet()
        _mockTimeRemainingSeconds.value = mock.durationMinutes * 60
        navigateTo(Screen.MOCK_PLAYER)
    }

    fun recordMockAnswer(questionIndex: Int, optionIndex: Int) {
        _mockUserAnswers.value = _mockUserAnswers.value + (questionIndex to optionIndex)
    }

    fun toggleMarkForReview(questionIndex: Int) {
        val current = _mockMarkedForReview.value
        _mockMarkedForReview.value = if (current.contains(questionIndex)) {
            current - questionIndex
        } else {
            current + questionIndex
        }
    }

    fun selectStudyNote(note: StudyNoteEntity?) {
        if (note != null && note.isPremium && !isUserPremium.value) {
            _showPremiumPaywall.value = true
            return
        }
        _selectedStudyNote.value = note
        if (note != null) {
            navigateTo(Screen.STUDY_NOTE_DETAIL)
        } else {
            navigateTo(Screen.STUDY_NOTES)
        }
    }

    fun analyzeMockFromHistory(
        titleEn: String,
        titleAs: String,
        score: Int,
        totalMarks: Int,
        accuracy: Int,
        rank: Int,
        percentile: Float
    ) {
        val existingMock = mockTests.value.firstOrNull { it.titleEn == titleEn }
        val mockEntity = existingMock ?: MockTestEntity(
            id = 999,
            titleEn = titleEn,
            titleAs = titleAs,
            category = "Mock History",
            durationMinutes = 90,
            totalQuestions = 10,
            totalMarks = totalMarks,
            userScore = score,
            userAccuracy = accuracy.toFloat(),
            userRank = rank,
            userPercentile = percentile,
            isCompleted = true
        )
        _selectedMockTest.value = mockEntity

        // Generate realistic user answers for question analysis based on accuracy
        val currentQuestionsList = questions.value.take(10)
        val answerMap = mutableMapOf<Int, Int>()
        if (currentQuestionsList.isNotEmpty()) {
            val totalQ = currentQuestionsList.size
            val targetCorrect = ((accuracy / 100f) * totalQ).toInt().coerceAtLeast(1).coerceAtMost(totalQ)
            val targetIncorrect = (totalQ - targetCorrect - 1).coerceAtLeast(0)

            currentQuestionsList.forEachIndexed { index, q ->
                when {
                    index < targetCorrect -> {
                        answerMap[index] = q.correctOptionIndex
                    }
                    index < targetCorrect + targetIncorrect -> {
                        answerMap[index] = (q.correctOptionIndex + 1) % 4
                    }
                    else -> {
                        // Skipped
                    }
                }
            }
        }
        _mockUserAnswers.value = answerMap
        navigateTo(Screen.MOCK_RESULT)
    }

    fun submitCurrentMockTest() {
        val test = _selectedMockTest.value ?: return
        val currentQuestionsList = questions.value
        val answers = _mockUserAnswers.value
        var correct = 0
        currentQuestionsList.take(test.totalQuestions).forEachIndexed { index, q ->
            if (answers[index] == q.correctOptionIndex) {
                correct++
            }
        }
        val score = (correct * (test.totalMarks / test.totalQuestions.coerceAtLeast(1)))
        val accuracy = if (answers.isNotEmpty()) (correct.toFloat() / answers.size.toFloat()) * 100f else 0f

        viewModelScope.launch {
            repository.submitMockResult(test.id, score, accuracy, test.durationMinutes)
            navigateTo(Screen.MOCK_RESULT)
        }
    }

    fun addMockTest(mock: MockTestEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.addMockTest(mock)
            onComplete()
        }
    }

    fun updateMockTest(mock: MockTestEntity, onComplete: () -> Unit = {}) {
        logActivity("Updated mock test: ${mock.titleEn}")
        viewModelScope.launch {
            repository.updateMockTest(mock)
            onComplete()
        }
    }

    fun deleteMockTest(mock: MockTestEntity) {
        viewModelScope.launch {
            repository.deleteMockTest(mock)
        }
    }

    fun addStudyNote(note: StudyNoteEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.addStudyNote(note)
            onComplete()
        }
    }

    fun updateStudyNote(note: StudyNoteEntity, onComplete: () -> Unit = {}) {
        logActivity("Updated study note: ${note.titleEn}")
        viewModelScope.launch {
            repository.updateStudyNote(note)
            onComplete()
        }
    }

    fun deleteStudyNote(note: StudyNoteEntity) {
        viewModelScope.launch {
            repository.deleteStudyNote(note)
        }
    }

    fun toggleBookmarkQuestion(q: QuestionEntity) {
        viewModelScope.launch { repository.toggleBookmarkQuestion(q) }
    }

    fun toggleHideQuestion(q: QuestionEntity) {
        viewModelScope.launch { repository.toggleHideQuestion(q) }
    }

    fun unhideAllQuestions() {
        viewModelScope.launch { repository.unhideAllQuestions() }
    }

    fun toggleLikeQuestion(q: QuestionEntity) {
        viewModelScope.launch { repository.toggleLikeQuestion(q) }
    }

    fun toggleBookmarkNote(n: StudyNoteEntity) {
        viewModelScope.launch { repository.toggleBookmarkNote(n) }
    }

    fun recordStudyProgress(questionsStudiedDelta: Int, secondsSpentDelta: Int) {
        viewModelScope.launch {
            val prof = userProfile.value ?: return@launch
            val newSolved = prof.totalSolved + questionsStudiedDelta
            val minutesToAdd = (secondsSpentDelta / 60).coerceAtLeast(if (secondsSpentDelta > 0 && questionsStudiedDelta > 0) 1 else 0)
            val newTime = prof.totalTimeMinutes + minutesToAdd
            val updated = prof.copy(
                totalSolved = newSolved,
                totalTimeMinutes = newTime
            )
            repository.updateUserProfile(updated)
        }
    }

    fun toggleDownloadNote(n: StudyNoteEntity) {
        if (n.isPremium && !isUserPremium.value) {
            _showPremiumPaywall.value = true
            return
        }
        viewModelScope.launch { repository.toggleDownloadNote(n) }
    }

    fun awardChapterCompletionXp() {
        viewModelScope.launch {
            repository.awardXp(50, 0)
        }
    }

    fun incrementDailyStreak() {
        viewModelScope.launch {
            repository.incrementDailyStreak()
        }
    }

    fun submitQuestionAnswer(questionId: Long, isCorrect: Boolean) {
        viewModelScope.launch {
            val today = java.time.LocalDate.now().toString()
            repository.processQuestionAnswerForXp(questionId, isCorrect, today)
        }
    }

    fun updateFirebaseProjectId(projectId: String) {
        viewModelScope.launch {
            repository.updateFirebaseProjectId(projectId)
        }
    }

    fun loginWithEmail(emailInput: String, nameInput: String = "") {
        viewModelScope.launch {
            val trimmedEmail = emailInput.trim().ifBlank { "scholar@jukti.in" }
            val currentProf = userProfile.value ?: SampleData.initialUserProfile
            val isOwnerEmail = trimmedEmail.equals("juktieducation@gmail.com", ignoreCase = true)
            val isAdminEmail = trimmedEmail.equals("borapinku151@gmail.com", ignoreCase = true)
            val newRole = when {
                isOwnerEmail -> "OWNER"
                isAdminEmail -> "ADMIN"
                else -> currentProf.role.ifBlank { "USER" }
            }
            val defaultName = when {
                isOwnerEmail -> "Jukti Education"
                isAdminEmail -> "Pinku Bora"
                else -> trimmedEmail.substringBefore("@").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
            val newName = if (nameInput.isNotBlank()) nameInput else defaultName
            val deviceId = java.util.UUID.randomUUID().toString()

            UserSessionManager.registerSession(trimmedEmail, deviceId)

            val updatedProf = currentProf.copy(
                email = trimmedEmail,
                role = newRole,
                name = newName,
                isLoggedIn = true,
                currentDeviceId = deviceId,
                activeDeviceId = deviceId
            )
            repository.updateUserProfile(updatedProf)
            _sessionMessage.value = null
            _currentScreen.value = Screen.HOME
        }
    }

    fun logout() {
        viewModelScope.launch {
            val currentProf = userProfile.value ?: SampleData.initialUserProfile
            val updatedProf = currentProf.copy(
                isLoggedIn = false,
                currentDeviceId = "",
                activeDeviceId = ""
            )
            repository.updateUserProfile(updatedProf)
            _sessionMessage.value = null
            _currentScreen.value = Screen.AUTH
        }
    }

    private fun logoutDueToOtherDeviceLogin() {
        viewModelScope.launch {
            val currentProf = userProfile.value ?: return@launch
            if (!currentProf.isLoggedIn) return@launch
            val updatedProf = currentProf.copy(
                isLoggedIn = false,
                currentDeviceId = "",
                activeDeviceId = ""
            )
            repository.updateUserProfile(updatedProf)
            _sessionMessage.value = "Your account was logged in on another device. You have been logged out automatically."
            _currentScreen.value = Screen.AUTH
        }
    }

    fun updateUserRole(role: String) {
        viewModelScope.launch {
            val prof = userProfile.value ?: SampleData.initialUserProfile
            repository.updateUserProfile(prof.copy(role = role))
        }
    }

    fun updateAboutConfig(config: AboutConfigEntity) {
        logActivity("Updated About/Config settings")
        viewModelScope.launch {
            repository.updateAboutConfig(config)
        }
    }

    fun verifyAndSetAdminRole(passcode: String): Boolean {
        val trimmed = passcode.trim()
        if (trimmed == "1234" || trimmed.lowercase() == "admin" || trimmed == "2026") {
            updateUserRole("ADMIN")
            return true
        } else if (trimmed.lowercase() == "owner" || trimmed == "owner123") {
            updateUserRole("OWNER")
            return true
        }
        return false
    }

    fun clearUserProgressData() {
        viewModelScope.launch {
            repository.resetUserProgress()
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            repository.updateUserProfile(
                UserProfileEntity(
                    id = 1,
                    name = "Scholar User",
                    email = "scholar@jukti.in",
                    mobile = "",
                    district = "",
                    examGoal = "ADRE & APSC",
                    xp = 0,
                    level = 1,
                    dailyStreak = 0,
                    isPremium = false,
                    role = "USER",
                    firebaseProjectId = "jukti-26035",
                    totalSolved = 0,
                    totalTimeMinutes = 0
                )
            )
            _currentScreen.value = Screen.HOME
        }
    }
    fun importCsvQuestions(csvData: String, targetExam: String, questionFor: String, questionType: String, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            var count = 0
            try {
                val lines = csvData.lines().map { it.trim() }.filter { it.isNotEmpty() }
                if (lines.size <= 1) {
                    onComplete(0)
                    return@launch // Only header or empty
                }
                
                val questions = mutableListOf<QuestionEntity>()
                val isPremium = questionFor.equals("Premium", ignoreCase = true)
                
                // Csv parsing logic taking care of quotes
                for (i in 1 until lines.size) {
                    val line = lines[i]
                    val tokens = parseCsvLine(line)
                    if (tokens.size >= 17) {
                        val statement = tokens[0]
                        val statementAssamese = tokens[1]
                        val a = tokens[2]
                        val a_as = tokens[3]
                        val b = tokens[4]
                        val b_as = tokens[5]
                        val c = tokens[6]
                        val c_as = tokens[7]
                        val d = tokens[8]
                        val d_as = tokens[9]
                        val correctAnswer = tokens[10].uppercase()
                        val explanation = tokens[11]
                        val explanationAssamese = tokens[12]
                        val subject = tokens[13]
                        val topic = tokens[14]
                        val tags = tokens[15]
                        val difficulty = tokens[16]
                        
                        val correctOptionIndex = when(correctAnswer) {
                            "A" -> 0
                            "B" -> 1
                            "C" -> 2
                            "D" -> 3
                            else -> 0
                        }
                        
                        questions.add(
                            QuestionEntity(
                                subject = subject,
                                topic = topic,
                                difficulty = difficulty,
                                questionEn = statement,
                                questionAs = statementAssamese,
                                optionAEn = a,
                                optionBEn = b,
                                optionCEn = c,
                                optionDEn = d,
                                optionAAs = a_as,
                                optionBAs = b_as,
                                optionCAs = c_as,
                                optionDAs = d_as,
                                correctOptionIndex = correctOptionIndex,
                                explanationEn = explanation,
                                explanationAs = explanationAssamese,
                                examCategory = targetExam,
                                isPremium = isPremium,
                                questionType = if (tags.isNotBlank()) tags else questionType
                            )
                        )
                    }
                }
                if (questions.isNotEmpty()) {
                    repository.bulkInsertQuestions(questions)
                    count = questions.size
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            onComplete(count)
        }
    }

    fun addQuestion(question: QuestionEntity, onComplete: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.addQuestion(question)
            onComplete(id)
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        for (i in line.indices) {
            val char = line[i]
            if (char == '\"') {
                inQuotes = !inQuotes
            } else if (char == ',' && !inQuotes) {
                result.add(current.toString().trim())
                current.clear()
            } else {
                current.append(char)
            }
        }
        result.add(current.toString().trim())
        return result
    }

    fun addPlan(plan: PlanEntity, onComplete: () -> Unit) { 
        viewModelScope.launch { 
            repository.insertPlan(plan)
            onComplete() 
        } 
    }

    fun deletePlan(plan: PlanEntity) { 
        viewModelScope.launch { 
            repository.deletePlan(plan) 
        } 
    }

    fun reportQuestion(question: QuestionEntity) {
        viewModelScope.launch {
            repository.updateQuestion(question.copy(isReported = true))
        }
    }
    
    fun resolveReportedQuestion(question: QuestionEntity) {
        viewModelScope.launch {
            repository.updateQuestion(question.copy(isReported = false))
        }
    }
    
    fun deleteQuestion(question: QuestionEntity) {
        logActivity("Deleted question ID: ${question.id}")
        viewModelScope.launch {
            repository.deleteQuestion(question)
        }
    }


    fun updateQuestionAndResolve(question: QuestionEntity) {
        viewModelScope.launch {
            repository.updateQuestion(question.copy(isReported = false))
        }
    }

    fun sendNotification(title: String, body: String, category: String) {
        viewModelScope.launch {
            repository.sendNotification(title, body, category)
            
            val intent = Intent(getApplication(), MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent: PendingIntent = PendingIntent.getActivity(getApplication(), 0, intent, PendingIntent.FLAG_IMMUTABLE)

            val builder = NotificationCompat.Builder(getApplication(), "jukti_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            val notificationManager = getApplication<Application>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }

    fun deleteNotification(notification: com.example.data.local.NotificationEntity) {
        viewModelScope.launch {
            repository.deleteNotification(notification)
        }
    }

    fun addExamUpdate(update: ExamUpdateEntity) {
        viewModelScope.launch {
            repository.addExamUpdate(update)
        }
    }

    fun updateExamUpdate(update: ExamUpdateEntity) {
        viewModelScope.launch {
            repository.updateExamUpdate(update)
        }
    }

    fun deleteExamUpdate(update: ExamUpdateEntity) {
        viewModelScope.launch {
            repository.deleteExamUpdate(update)
        }
    }

    fun addBanner(banner: BannerEntity) {
        viewModelScope.launch {
            repository.addBanner(banner)
        }
    }

    fun updateBanner(banner: BannerEntity) {
        viewModelScope.launch {
            repository.updateBanner(banner)
        }
    }

    fun addAdminEmail(email: String) {
        val trimmed = email.trim().lowercase()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val currentConfig = aboutConfig.value
            val currentEmails = currentConfig.adminEmails.split(",")
                .map { it.trim().lowercase() }
                .filter { it.isNotEmpty() }
                .toMutableSet()
            currentEmails.add(trimmed)
            val updatedString = currentEmails.joinToString(",")
            repository.updateAboutConfig(currentConfig.copy(adminEmails = updatedString))
        }
    }

    fun removeAdminEmail(email: String) {
        val trimmed = email.trim().lowercase()
        viewModelScope.launch {
            val currentConfig = aboutConfig.value
            val currentEmails = currentConfig.adminEmails.split(",")
                .map { it.trim().lowercase() }
                .filter { it.isNotEmpty() && it != trimmed }
            val updatedString = currentEmails.joinToString(",")
            repository.updateAboutConfig(currentConfig.copy(adminEmails = updatedString))
        }
    }

    fun deleteBanner(banner: BannerEntity) {
        viewModelScope.launch {
            repository.deleteBanner(banner)
        }
    }

    // Pending Requests & Actions (Delete user, Delete question, Block user, Upgrade plan, Create plan, Delete mock)
    fun requestOrDeleteQuestion(question: QuestionEntity, onResult: (Boolean, String) -> Unit) {
        if (isOwner.value) {
            viewModelScope.launch {
                repository.deleteQuestion(question)
                onResult(true, "Question deleted successfully.")
            }
        } else {
            viewModelScope.launch {
                val req = PendingRequestEntity(
                    requestType = "DELETE_QUESTION",
                    title = "Delete Question #${question.id}",
                    description = "Request to delete question: \"${question.questionEn.take(60)}\" (${question.subject})",
                    targetId = question.id.toString(),
                    payloadJson = "",
                    requestedBy = userProfile.value?.email ?: "admin@jukti.in",
                    timestamp = "Just now",
                    status = "PENDING"
                )
                repository.insertPendingRequest(req)
                onResult(false, "Sent a request to Owner Dashboard to approve or reject.")
            }
        }
    }

    fun requestOrDeleteMock(mock: MockTestEntity, onResult: (Boolean, String) -> Unit) {
        if (isOwner.value) {
            viewModelScope.launch {
                repository.deleteMockTest(mock)
                onResult(true, "Mock test deleted successfully.")
            }
        } else {
            viewModelScope.launch {
                val req = PendingRequestEntity(
                    requestType = "DELETE_MOCK",
                    title = "Delete Mock Test #${mock.id}",
                    description = "Request to delete mock test: \"${mock.titleEn}\" (${mock.category})",
                    targetId = mock.id.toString(),
                    payloadJson = "",
                    requestedBy = userProfile.value?.email ?: "admin@jukti.in",
                    timestamp = "Just now",
                    status = "PENDING"
                )
                repository.insertPendingRequest(req)
                onResult(false, "Sent a request to Owner Dashboard to approve or reject.")
            }
        }
    }

    fun requestOrDeleteUser(userId: String, userName: String, userEmail: String, onResult: (Boolean, String) -> Unit) {
        if (isOwner.value) {
            viewModelScope.launch {
                onResult(true, "User $userName deleted successfully.")
            }
        } else {
            viewModelScope.launch {
                val req = PendingRequestEntity(
                    requestType = "DELETE_USER",
                    title = "Delete User: $userName",
                    description = "Request to delete user account ($userEmail)",
                    targetId = userId,
                    payloadJson = userEmail,
                    requestedBy = userProfile.value?.email ?: "admin@jukti.in",
                    timestamp = "Just now",
                    status = "PENDING"
                )
                repository.insertPendingRequest(req)
                onResult(false, "Sent a request to Owner Dashboard to approve or reject.")
            }
        }
    }

    fun requestOrBlockUser(userId: String, userName: String, userEmail: String, onResult: (Boolean, String) -> Unit) {
        if (isOwner.value) {
            viewModelScope.launch {
                onResult(true, "User $userName blocked successfully.")
            }
        } else {
            viewModelScope.launch {
                val req = PendingRequestEntity(
                    requestType = "BLOCK_USER",
                    title = "Block User: $userName",
                    description = "Request to block user account ($userEmail)",
                    targetId = userId,
                    payloadJson = userEmail,
                    requestedBy = userProfile.value?.email ?: "admin@jukti.in",
                    timestamp = "Just now",
                    status = "PENDING"
                )
                repository.insertPendingRequest(req)
                onResult(false, "Sent a request to Owner Dashboard to approve or reject.")
            }
        }
    }

    fun requestOrCreatePlan(plan: PlanEntity, onResult: (Boolean, String) -> Unit) {
        if (isOwner.value) {
            viewModelScope.launch {
                repository.insertPlan(plan)
                onResult(true, "Plan created successfully.")
            }
        } else {
            viewModelScope.launch {
                val req = PendingRequestEntity(
                    requestType = "CREATE_PLAN",
                    title = "Create Plan: ${plan.planName}",
                    description = "Create plan ${plan.planName} at ${plan.finalPrice} with validity ${plan.offerValidity}",
                    targetId = "",
                    payloadJson = "${plan.planName}|${plan.finalPrice}|${plan.planPrice}|${plan.offerValidity}",
                    requestedBy = userProfile.value?.email ?: "admin@jukti.in",
                    timestamp = "Just now",
                    status = "PENDING"
                )
                repository.insertPendingRequest(req)
                onResult(false, "Sent a request to Owner Dashboard to approve or reject.")
            }
        }
    }

    fun requestOrUpgradePlan(userId: String, userName: String, userEmail: String, newPlanName: String, validity: String, onResult: (Boolean, String) -> Unit) {
        if (isOwner.value) {
            viewModelScope.launch {
                onResult(true, "Plan upgraded for $userName.")
            }
        } else {
            viewModelScope.launch {
                val req = PendingRequestEntity(
                    requestType = "UPGRADE_PLAN",
                    title = "Upgrade Plan for $userName",
                    description = "Request to upgrade user ($userEmail) to $newPlanName ($validity)",
                    targetId = userId,
                    payloadJson = "$newPlanName|$validity",
                    requestedBy = userProfile.value?.email ?: "admin@jukti.in",
                    timestamp = "Just now",
                    status = "PENDING"
                )
                repository.insertPendingRequest(req)
                onResult(false, "Sent a request to Owner Dashboard to approve or reject.")
            }
        }
    }

    fun approvePendingRequest(request: PendingRequestEntity) {
        viewModelScope.launch {
            when (request.requestType) {
                "DELETE_QUESTION" -> {
                    val qId = request.targetId.toLongOrNull()
                    if (qId != null) repository.deleteQuestionById(qId)
                }
                "DELETE_MOCK" -> {
                    val mId = request.targetId.toLongOrNull()
                    if (mId != null) repository.deleteMockTestById(mId)
                }
                "CREATE_PLAN" -> {
                    val parts = request.payloadJson.split("|")
                    val name = parts.getOrNull(0) ?: request.title.removePrefix("Create Plan: ")
                    val price = parts.getOrNull(1) ?: "₹299"
                    val origPrice = parts.getOrNull(2) ?: "₹599"
                    val vality = parts.getOrNull(3) ?: "1 Year"
                    val newPlan = PlanEntity(
                        planName = name,
                        planPrice = origPrice,
                        discount = "50% OFF",
                        finalPrice = price,
                        offerValidity = vality
                    )
                    repository.insertPlan(newPlan)
                }
            }
            repository.updatePendingRequest(request.copy(status = "APPROVED"))
        }
    }

    fun rejectPendingRequest(request: PendingRequestEntity) {
        viewModelScope.launch {
            repository.updatePendingRequest(request.copy(status = "REJECTED"))
        }
    }
}
