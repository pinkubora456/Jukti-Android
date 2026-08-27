package com.example.ui.viewmodel

import android.app.Activity
import com.example.util.NetworkMonitor
import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.JuktiApplication
import com.example.auth.GoogleAuthManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.repository.JuktiRepository
import com.example.data.repository.SampleData
import com.example.data.repository.UserSessionManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.isActive
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

enum class AppLanguage {
    ENGLISH,
    ASSAMESE,
    BOTH
}

enum class UserRole {
    OWNER,
    ADMIN,
    USER
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
    MANAGE_EXAM_PATTERN_CUTOFF_UPDATE,
    MANAGE_EXAM_PATTERN_CUTOFF_VIEW,
    MANAGE_BANNERS,
    PRIVACY_POLICY,
    TERMS_CONDITIONS,
    HELP_SUPPORT,
    ABOUT_LEGAL,
    SHARE_SUPPORT,
    FAQ
}

class JuktiViewModel(application: Application) : AndroidViewModel(application) {

    private val timePrefs by lazy { getApplication<Application>().getSharedPreferences("jukti_time_prefs", android.content.Context.MODE_PRIVATE) }
    
    private var sessionTrustedServerTime: Long = 0L
    private var sessionTrustedRealtime: Long = 0L


    private val networkMonitor = NetworkMonitor(application)
    val isConnected = networkMonitor.isConnected

    private fun syncTrustedTime() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // Fetch header from a reliable Google endpoint
                val url = java.net.URL("https://us-central1-jukti-examprep.cloudfunctions.net")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.requestMethod = "HEAD"
                conn.connectTimeout = 3000
                conn.readTimeout = 3000
                val dateStr = conn.getHeaderField("Date")
                if (dateStr != null) {
                    val format = java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", java.util.Locale.US)
                    val serverTime = format.parse(dateStr)?.time ?: return@launch
                    
                    sessionTrustedServerTime = serverTime
                    sessionTrustedRealtime = android.os.SystemClock.elapsedRealtime()
                    
                    timePrefs.edit()
                        .putLong("last_server_time", serverTime)
                        .putLong("last_device_realtime", sessionTrustedRealtime)
                        .putLong("max_seen_time", serverTime)
                        .apply()
                }
            } catch (e: Exception) {
                // Ignore, will fallback to cached or monotonic
            }
        }
    }

    fun getTrustedTime(): Long {
        val currentRealtime = android.os.SystemClock.elapsedRealtime()
        
        // 1. If we have a session-synced server time, use it + monotonic elapsed time
        if (sessionTrustedServerTime > 0L) {
            val elapsed = currentRealtime - sessionTrustedRealtime
            if (elapsed >= 0) {
                val calculated = sessionTrustedServerTime + elapsed
                updateMaxSeenTime(calculated)
                return calculated
            }
        }
        
        // 2. Fallback to persisted synced time if device hasn't rebooted since last sync
        val lastRealtime = timePrefs.getLong("last_device_realtime", 0L)
        val lastServerTime = timePrefs.getLong("last_server_time", 0L)
        if (lastServerTime > 0L && lastRealtime > 0L && currentRealtime >= lastRealtime) {
            val elapsed = currentRealtime - lastRealtime
            // basic sanity check: if elapsed is > 30 days without reboot, maybe suspicious, but let's trust it
            if (elapsed < 30L * 24 * 60 * 60 * 1000) {
                val calculated = lastServerTime + elapsed
                updateMaxSeenTime(calculated)
                return calculated
            }
        }
        
        // 3. Last resort fallback: System time, but protected against backwards tampering
        val currentSystemTime = System.currentTimeMillis()
        return updateMaxSeenTime(currentSystemTime)
    }
    
    private fun updateMaxSeenTime(time: Long): Long {
        val maxSeen = timePrefs.getLong("max_seen_time", 0L)
        if (time > maxSeen) {
            timePrefs.edit().putLong("max_seen_time", time).apply()
            return time
        }
        return maxSeen
    }


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
        database.userQuestionStateDao(),
        database.activityLogDao(),
        database.mockAttemptDao(),
        database.entitlementDao(),
        database.entitlementHistoryDao(),
        com.example.data.repository.FirebaseSyncManager(database)
    )

    // Data Flows from Repository
    val plans = repository.allPlans.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )



    val examsList: StateFlow<List<ExamEntity>> = repository.allExams.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allSubjectsChapters = repository.allSubjectsChapters.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    fun addSubjectChapter(subject: String, chapter: String) {
        viewModelScope.launch {
            val res = repository.addSubjectChapter(SubjectChapterEntity(subject = subject, chapter = chapter))
            _syncToastMessage.value = res.second
        }
    }

    fun deleteSubjectChapter(subjectChapter: SubjectChapterEntity) {
        viewModelScope.launch {
            val res = repository.deleteSubjectChapter(subjectChapter)
            _syncToastMessage.value = res.second
        }
    }

    fun addExam(title: String, subtitle: String, status: String = "Active") {
        logActivity("Added exam: $title")
        viewModelScope.launch {
            val res = repository.insertExam(ExamEntity(title = title, subtitle = subtitle, status = status))
            _syncToastMessage.value = res.second
        }
    }

    fun updateExam(exam: ExamEntity) {
        viewModelScope.launch {
            val res = repository.updateExam(exam)
            _syncToastMessage.value = res.second
        }
    }

    fun deleteExam(exam: ExamEntity) {
        viewModelScope.launch {
            val res = repository.deleteExam(exam)
            _syncToastMessage.value = res.second
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

    private val _studySubView = MutableStateFlow<String?>(null)
    val studySubView: StateFlow<String?> = _studySubView.asStateFlow()

    private val _openedStudyDirectly = MutableStateFlow(false)
    val openedStudyDirectly: StateFlow<Boolean> = _openedStudyDirectly.asStateFlow()

    fun setStudySubView(subView: String?, fromHome: Boolean = false) {
        _studySubView.value = subView
        _openedStudyDirectly.value = fromHome
    }

    fun openStudyMcq(fromHome: Boolean = true) {
        _studySubView.value = "STUDY_MCQS"
        _openedStudyDirectly.value = fromHome
        navigateTo(Screen.MCQ_STUDY)
    }

    fun openStudyHub() {
        _studySubView.value = null
        _openedStudyDirectly.value = false
        navigateTo(Screen.MCQ_STUDY)
    }

    fun finishSplash() {
        splashFinished = true
        val prof = userProfile.value
        if (prof != null) {
            _currentScreen.value = if (prof.isLoggedIn) Screen.HOME else Screen.AUTH
        }
    }
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _sessionMessage = MutableStateFlow<String?>(null)
    val sessionMessage: StateFlow<String?> = _sessionMessage.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    private val _googleAccountsToSelect = MutableStateFlow<List<String>?>(null)
    val googleAccountsToSelect: StateFlow<List<String>?> = _googleAccountsToSelect.asStateFlow()

    fun dismissGoogleAccountChooser() {
        _googleAccountsToSelect.value = null
    }

    private val _isRefreshingFromFirebase = MutableStateFlow(false)
    val isRefreshingFromFirebase: StateFlow<Boolean> = _isRefreshingFromFirebase.asStateFlow()

    private val _refreshStatusMessage = MutableStateFlow<String?>(null)
    val refreshStatusMessage: StateFlow<String?> = _refreshStatusMessage.asStateFlow()

    fun clearRefreshStatusMessage() {
        _refreshStatusMessage.value = null
    }

    fun refreshDataFromFirebase() {
        viewModelScope.launch {
            _isRefreshingFromFirebase.value = true
            try {
                val result = repository.refreshDataFromFirebase(getTrustedTime())
                if (result.isSuccess) {
                    _refreshStatusMessage.value = "App data refreshed successfully!"
                } else {
                    _refreshStatusMessage.value = "Failed to refresh app data. Please check your network connection and try again."
                }
            } catch (e: Exception) {
                Log.e("JuktiViewModel", "Exception refreshing data", e)
                _refreshStatusMessage.value = "Failed to refresh app data. Please try again."
            } finally {
                _isRefreshingFromFirebase.value = false
            }
        }
    }

    fun uploadWorkspaceChangesToFirebase(onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val (success, message) = repository.uploadAllWorkspaceChangesToFirebase()
                showSyncToast(message)
                onComplete(success, message)
            } catch (e: Exception) {
                Log.e("JuktiViewModel", "Error uploading workspace changes", e)
                val msg = "Upload error: ${e.localizedMessage ?: "Unknown error"}"
                showSyncToast(msg)
                onComplete(false, msg)
            }
        }
    }

    fun retrySingleSyncItem(item: com.example.data.local.SyncQueueEntity, onComplete: ((Boolean, String) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val (success, message) = repository.retrySingleSync(item)
                showSyncToast(message)
                onComplete?.invoke(success, message)
            } catch (e: Exception) {
                Log.e("JuktiViewModel", "Error retrying sync item #${item.syncId}", e)
                val msg = "Retry error: ${e.localizedMessage ?: "Unknown error"}"
                showSyncToast(msg)
                onComplete?.invoke(false, msg)
            }
        }
    }

    fun runMinimalDiagnosticTest(onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val (success, message) = repository.runMinimalDiagnosticTest()
                onComplete(success, message)
            } catch (e: Exception) {
                onComplete(false, "Test exception: ${e.message}")
            }
        }
    }

    private val _showPremiumPaywall = MutableStateFlow(false)
    val showPremiumPaywall: StateFlow<Boolean> = _showPremiumPaywall.asStateFlow()

    private val _planPurchaseSummaryState = MutableStateFlow(PlanPurchaseSummaryState())
    val planPurchaseSummaryState: StateFlow<PlanPurchaseSummaryState> = _planPurchaseSummaryState.asStateFlow()

    private val _syncToastMessage = MutableStateFlow<String?>(null)
    val syncToastMessage: StateFlow<String?> = _syncToastMessage.asStateFlow()

    val pendingSyncQueue = repository.syncManager.allSyncQueueFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val isSyncUploading = repository.syncManager.isUploading.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )

    val syncProgressState = repository.syncManager.syncProgressState.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.data.repository.SyncProgressState()
    )

    fun clearSyncToastMessage() {
        _syncToastMessage.value = null
    }

    fun showSyncToast(message: String) {
        _syncToastMessage.value = message
    }

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

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val bookmarkedIds: StateFlow<Set<Long>> = userProfile.flatMapLatest { profile ->
        val uid = profile?.uid ?: FirebaseAuth.getInstance().currentUser?.uid
        if (uid.isNullOrBlank()) flowOf(emptySet())
        else repository.getUserStates(uid).map { list ->
            list.filter { it.isBookmarked }.mapNotNull { it.questionId.toLongOrNull() }.toSet()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val hiddenIds: StateFlow<Set<Long>> = userProfile.flatMapLatest { profile ->
        val uid = profile?.uid ?: FirebaseAuth.getInstance().currentUser?.uid
        if (uid.isNullOrBlank()) flowOf(emptySet())
        else repository.getUserStates(uid).map { list ->
            list.filter { it.isHidden }.mapNotNull { it.questionId.toLongOrNull() }.toSet()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val likedIds: StateFlow<Set<Long>> = userProfile.flatMapLatest { profile ->
        val uid = profile?.uid ?: FirebaseAuth.getInstance().currentUser?.uid
        if (uid.isNullOrBlank()) flowOf(emptySet())
        else repository.getUserStates(uid).map { list ->
            list.filter { it.isLiked }.mapNotNull { it.questionId.toLongOrNull() }.toSet()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())


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
            val res = repository.addFaq(
                FaqEntity(
                    questionEn = questionEn,
                    questionAs = questionAs,
                    answerEn = answerEn,
                    answerAs = answerAs
                )
            )
            _syncToastMessage.value = res.second
        }
    }

    fun updateFaq(faq: FaqEntity) {
        viewModelScope.launch {
            val res = repository.updateFaq(faq)
            _syncToastMessage.value = res.second
        }
    }

    fun deleteFaq(faq: FaqEntity) {
        viewModelScope.launch {
            val res = repository.deleteFaq(faq)
            _syncToastMessage.value = res.second
        }
    }

    val isAdminOrOwner: StateFlow<Boolean> = combine(userProfile, aboutConfig) { profile, config ->
        val role = profile?.role?.uppercase(java.util.Locale.ROOT) ?: ""
        val isRoleAdminOrOwner = role == "ADMIN" || role == "OWNER"
        val email = profile?.email?.trim()?.lowercase() ?: ""
        val isOwnerEmail = email == "juktieducation@gmail.com" || email == "borapinku151@gmail.com"
        val adminEmails = config.adminEmails.split(",").map { it.trim().lowercase() }.filter { it.isNotBlank() }
        val isAdminEmail = email.isNotBlank() && adminEmails.contains(email)
        isRoleAdminOrOwner || isOwnerEmail || isAdminEmail
    }.stateIn(
        viewModelScope, SharingStarted.Eagerly, false
    )

    val isOwner: StateFlow<Boolean> = userProfile.map { profile ->
        val role = profile?.role?.uppercase(java.util.Locale.ROOT) ?: ""
        val isRoleOwner = role == "OWNER"
        val email = profile?.email?.trim()?.lowercase() ?: ""
        val isOwnerEmail = email == "juktieducation@gmail.com" || email == "borapinku151@gmail.com"
        isRoleOwner || isOwnerEmail
    }.stateIn(
        viewModelScope, SharingStarted.Eagerly, false
    )

    suspend fun getUserRoleDirect(email: String, roleInProfile: String? = null): UserRole {
        val roleUpper = roleInProfile?.uppercase(java.util.Locale.ROOT) ?: ""
        if (roleUpper == "OWNER") return UserRole.OWNER
        if (roleUpper == "ADMIN") return UserRole.ADMIN

        val config = repository.getAboutConfigDirect()
        val trimmed = email.trim().lowercase()
        if (trimmed == "juktieducation@gmail.com" || trimmed == "borapinku151@gmail.com") {
            return UserRole.OWNER
        }
        val adminEmails = config?.adminEmails?.split(",")?.map { it.trim().lowercase() }?.filter { it.isNotBlank() } ?: emptyList()
        if (adminEmails.contains(trimmed)) {
            return UserRole.ADMIN
        }
        return UserRole.USER
    }

    fun getUserRole(email: String, roleInProfile: String? = null): UserRole {
        val roleUpper = roleInProfile?.uppercase(java.util.Locale.ROOT) ?: ""
        if (roleUpper == "OWNER") return UserRole.OWNER
        if (roleUpper == "ADMIN") return UserRole.ADMIN

        val trimmed = email.trim().lowercase()
        if (trimmed == "juktieducation@gmail.com" || trimmed == "borapinku151@gmail.com") {
            return UserRole.OWNER
        }
        val adminEmails = aboutConfig.value.adminEmails.split(",").map { it.trim().lowercase() }.filter { it.isNotBlank() }
        if (adminEmails.contains(trimmed)) {
            return UserRole.ADMIN
        }
        return UserRole.USER
    }

    fun getCurrentActorRole(): UserRole {
        val profile = userProfile.value
        val email = profile?.email ?: ""
        return getUserRole(email, profile?.role)
    }

    fun canPerformDeleteOrBan(actorRole: UserRole, targetRole: UserRole): Boolean {
        if (targetRole == UserRole.OWNER) {
            return false // Owner can NEVER be deleted or banned by anyone
        }
        return when (actorRole) {
            UserRole.OWNER -> targetRole == UserRole.ADMIN || targetRole == UserRole.USER
            UserRole.ADMIN -> targetRole == UserRole.USER
            UserRole.USER -> false
        }
    }

    fun canActorDeleteOrBanUser(targetEmail: String, targetRoleInProfile: String? = null): Boolean {
        val actorRole = getCurrentActorRole()
        val targetRole = getUserRole(targetEmail, targetRoleInProfile)
        return canPerformDeleteOrBan(actorRole, targetRole)
    }

    private val _userEntitlements = MutableStateFlow<List<EntitlementEntity>>(emptyList())
    val userEntitlements: StateFlow<List<EntitlementEntity>> = _userEntitlements.asStateFlow()

    fun validateEntitlements(entitlements: List<EntitlementEntity>?, currentTime: Long = getTrustedTime()): Boolean {
        val effective = com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(entitlements, plans.value, currentTime)
        return effective.isPremium
    }

    fun isSpecificPlanActive(plan: com.example.data.local.PlanEntity): Boolean {
        val entitlements = userEntitlements.value
        val now = getTrustedTime()
        val effective = com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(entitlements, plans.value, now)
        return effective.activePlans.any { entitlement ->
            entitlement.planId == plan.id.toString() || entitlement.planId.equals(plan.planName, ignoreCase = true) || entitlement.planName.equals(plan.planName, ignoreCase = true)
        }
    }

    suspend fun validatePurchaseEligibility(plan: com.example.data.local.PlanEntity): Pair<Boolean, String> {
        val prof = userProfile.value
        if (prof != null && prof.email.isNotBlank()) {
            val docId = com.example.data.repository.FirebaseRepository().getSanitizedUserDocId(prof.email)
            try {
                val db = com.example.JuktiApplication.getFirestore(getApplication())
                    ?: return Pair(false, "Firebase unavailable")
                val doc = db.collection("users").document(docId)
                    .collection("entitlements").document("current").get().await()
                if (doc.exists()) {
                    val status = doc.getString("status") ?: ""
                    val planId = doc.getString("planId") ?: ""
                    val planName = doc.getString("planName") ?: ""
                    val validUntil = doc.getLong("validUntil") ?: 0L

                    val isMatch = (planId == plan.id.toString()) ||
                                  (planId.equals(plan.planName, ignoreCase = true)) ||
                                  (planName.equals(plan.planName, ignoreCase = true))
                    val isValid = validUntil <= 0L || validUntil > getTrustedTime()

                    if (status == "ACTIVE" && isMatch && isValid) {
                        return Pair(false, "You already have an active subscription for '${plan.planName}'. Duplicate purchases of the same plan are not allowed.")
                    }
                }
            } catch (e: Exception) {
                // Fallback to local check if offline
            }
        }

        if (isSpecificPlanActive(plan)) {
            return Pair(false, "You already have an active subscription for '${plan.planName}'.")
        }

        return Pair(true, "")
    }

    val isUserPremium: StateFlow<Boolean> = combine(userProfile, isAdminOrOwner, userEntitlements, plans) { profile, admin, entitlements, allPlans ->
        val email = profile?.email?.trim()?.lowercase() ?: ""
        val isOwner = email == "juktieducation@gmail.com" || email == "borapinku151@gmail.com"
        if (isOwner || admin) {
            true
        } else {
            com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(entitlements, allPlans, getTrustedTime(), isAdminOrOwner = admin).isPremium
        }
    }.stateIn(
        viewModelScope, SharingStarted.Eagerly, false
    )

    val effectiveEntitlement: StateFlow<com.example.data.util.EffectiveUserEntitlement?> = combine(
        userProfile, isAdminOrOwner, userEntitlements, plans
    ) { profile, admin, entitlements, allPlans ->
        val email = profile?.email?.trim()?.lowercase() ?: ""
        val isOwner = email == "juktieducation@gmail.com" || email == "borapinku151@gmail.com"
        val isUserAdminOrOwner = isOwner || admin
        com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(entitlements, allPlans, getTrustedTime(), isAdminOrOwner = isUserAdminOrOwner)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    
    fun canAccessQuestion(question: com.example.data.local.QuestionEntity): Boolean {
        val effective = effectiveEntitlement.value ?: com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(userEntitlements.value, plans.value, getTrustedTime(), isAdminOrOwner = isAdminOrOwner.value)
        return com.example.data.util.PlanValidityEngine.isQuestionAccessible(question, effective, isAdminOrOwner.value)
    }
    
    fun canAccessMockTest(mock: com.example.data.local.MockTestEntity): Boolean {
        val effective = effectiveEntitlement.value ?: com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(userEntitlements.value, plans.value, getTrustedTime(), isAdminOrOwner = isAdminOrOwner.value)
        return com.example.data.util.PlanValidityEngine.isMockTestAccessible(mock, effective, isAdminOrOwner.value)
    }
    
    fun canAccessStudyNote(note: com.example.data.local.StudyNoteEntity): Boolean {
        val effective = effectiveEntitlement.value ?: com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(userEntitlements.value, plans.value, getTrustedTime())
        return com.example.data.util.PlanValidityEngine.isStudyNoteAccessible(note, effective, isAdminOrOwner.value)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val smartPracticeQuestions: StateFlow<List<QuestionEntity>> = combine(
        userProfile.flatMapLatest { profile ->
            val uid = profile?.uid ?: FirebaseAuth.getInstance().currentUser?.uid
            if (uid.isNullOrBlank()) flowOf(emptyList())
            else repository.getSmartPracticeQuestions(uid)
        },
        effectiveEntitlement,
        isAdminOrOwner,
        networkMonitor.isConnected
    ) { questions, effective, isAdmin, isConnected ->
        val eff = effective ?: com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(userEntitlements.value, plans.value, getTrustedTime())
        questions.filter { q ->
            !q.isPremium || (isConnected && com.example.data.util.PlanValidityEngine.isQuestionAccessible(q, eff, isAdmin))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())









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
    val mockTests: StateFlow<List<MockTestEntity>> = combine(
        kotlinx.coroutines.flow.combine(repository.allMockTests, repository.premiumMockTests) { f, p -> f + p }, effectiveEntitlement, isAdminOrOwner, networkMonitor.isConnected
    ) { list, effective, isAdmin, isConnected ->
        val eff = effective ?: com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(userEntitlements.value, plans.value, getTrustedTime())
        list.map { m ->
            if (!m.isPremium || (isConnected && com.example.data.util.PlanValidityEngine.isMockTestAccessible(m, eff, isAdmin))) m
            else m.copy(
                titleEn = "Premium Content 🔒", titleAs = "প্ৰিমিয়াম সমল 🔒"
            )
        }
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val accessibleMockTests: StateFlow<List<MockTestEntity>> = combine(
        userProfile,
        userEntitlements,
        plans,
        mockTests,
        isAdminOrOwner
    ) { args: Array<Any?> ->
        val profile = args[0] as? UserProfileEntity
        @Suppress("UNCHECKED_CAST")
        val entitlements = args[1] as? List<EntitlementEntity> ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val allPlans = args[2] as? List<PlanEntity> ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val mocks = args[3] as? List<MockTestEntity> ?: emptyList()
        val admin = args[4] as? Boolean ?: false

        com.example.data.util.PlanValidityEngine.filterAccessibleMockTests(
            userProfile = profile,
            entitlements = entitlements,
            plans = allPlans,
            mockTests = mocks,
            isAdminOrOwner = admin,
            currentTime = getTrustedTime()
        )
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val questions: StateFlow<List<QuestionEntity>> = combine(kotlinx.coroutines.flow.combine(repository.allQuestions, repository.premiumQuestions) { f, p -> f + p }, effectiveEntitlement, isAdminOrOwner, networkMonitor.isConnected) { list: List<QuestionEntity>, effective: com.example.data.util.EffectiveUserEntitlement?, isAdmin: Boolean, isConnected: Boolean ->
        val eff = effective ?: com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(userEntitlements.value, plans.value, getTrustedTime())
        list.filter { !it.isReported }.map { q ->
            if (!q.isPremium || (isConnected && com.example.data.util.PlanValidityEngine.isQuestionAccessible(q, eff, isAdmin))) q
            else q.copy(
                questionEn = "Premium Content 🔒", questionAs = "প্ৰিমিয়াম সমল 🔒",
                optionAEn = "Unlock to view", optionAAs = "Unlock to view",
                optionBEn = "Unlock to view", optionBAs = "Unlock to view",
                optionCEn = "Unlock to view", optionCAs = "Unlock to view",
                optionDEn = "Unlock to view", optionDAs = "Unlock to view",
                correctOptionIndex = -1, explanationEn = "Locked", explanationAs = "Locked"
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reportedQuestions = questions.map { list -> list.filter { it.isReported } }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )






    val studyNotes: StateFlow<List<StudyNoteEntity>> = combine(kotlinx.coroutines.flow.combine(repository.allNotes, repository.premiumStudyNotes) { f, p -> f + p }, effectiveEntitlement, isAdminOrOwner, networkMonitor.isConnected) { list: List<StudyNoteEntity>, effective: com.example.data.util.EffectiveUserEntitlement?, isAdmin: Boolean, isConnected: Boolean ->
        val eff = effective ?: com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(userEntitlements.value, plans.value, getTrustedTime())
        list.map { n ->
            if (!n.isPremium || (isConnected && com.example.data.util.PlanValidityEngine.isStudyNoteAccessible(n, eff, isAdmin))) n
            else n.copy(contentEn = "Premium Content 🔒", contentAs = "Premium Content 🔒")
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedNotes = studyNotes.map { list -> list.filter { it.isBookmarked || it.isDownloaded } }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val accessibleContentCounts: StateFlow<com.example.data.util.PlanAccessibleContentCounts> = combine(
        userProfile,
        userEntitlements,
        plans,
        mockTests,
        studyNotes,
        questions,
        isAdminOrOwner
    ) { args: Array<Any?> ->
        val profile = args[0] as? UserProfileEntity
        @Suppress("UNCHECKED_CAST")
        val entitlements = args[1] as? List<EntitlementEntity> ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val allPlans = args[2] as? List<PlanEntity> ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val mocks = args[3] as? List<MockTestEntity> ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val notes = args[4] as? List<StudyNoteEntity> ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val qs = args[5] as? List<QuestionEntity> ?: emptyList()
        val admin = args[6] as? Boolean ?: false

        com.example.data.util.PlanValidityEngine.calculateAccessibleCounts(
            userProfile = profile,
            entitlements = entitlements,
            plans = allPlans,
            mockTests = mocks,
            studyNotes = notes,
            questions = qs,
            isAdminOrOwner = admin,
            currentTime = getTrustedTime()
        )
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.data.util.PlanAccessibleContentCounts()
    )

    val accessibleStudyNotes: StateFlow<List<StudyNoteEntity>> = combine(
        userProfile,
        userEntitlements,
        plans,
        studyNotes,
        isAdminOrOwner
    ) { args: Array<Any?> ->
        val profile = args[0] as? UserProfileEntity
        @Suppress("UNCHECKED_CAST")
        val entitlements = args[1] as? List<EntitlementEntity> ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val allPlans = args[2] as? List<PlanEntity> ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val notes = args[3] as? List<StudyNoteEntity> ?: emptyList()
        val admin = args[4] as? Boolean ?: false

        com.example.data.util.PlanValidityEngine.filterAccessibleStudyNotes(
            userProfile = profile,
            entitlements = entitlements,
            plans = allPlans,
            studyNotes = notes,
            isAdminOrOwner = admin,
            currentTime = getTrustedTime()
        )
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val accessibleQuestions: StateFlow<List<QuestionEntity>> = combine(
        userProfile,
        userEntitlements,
        plans,
        questions,
        isAdminOrOwner
    ) { args: Array<Any?> ->
        val profile = args[0] as? UserProfileEntity
        @Suppress("UNCHECKED_CAST")
        val entitlements = args[1] as? List<EntitlementEntity> ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val allPlans = args[2] as? List<PlanEntity> ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val qs = args[3] as? List<QuestionEntity> ?: emptyList()
        val admin = args[4] as? Boolean ?: false

        com.example.data.util.PlanValidityEngine.filterAccessibleQuestions(
            userProfile = profile,
            entitlements = entitlements,
            plans = allPlans,
            questions = qs,
            isAdminOrOwner = admin,
            currentTime = getTrustedTime()
        )
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val bookmarkedQuestions: StateFlow<List<QuestionEntity>> = combine(
        questions,
        bookmarkedIds
    ) { qs, ids ->
        qs.filter { it.id in ids }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hiddenQuestions: StateFlow<List<QuestionEntity>> = combine(
        questions,
        hiddenIds
    ) { qs, ids ->
        qs.filter { it.id in ids }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isGuestMode = MutableStateFlow(false)
    val isGuestMode: StateFlow<Boolean> = _isGuestMode.asStateFlow()

    // Active Mock Test Session State
    private val _activeMockQuestions = MutableStateFlow<List<QuestionEntity>>(emptyList())
    val activeMockQuestions: StateFlow<List<QuestionEntity>> = _activeMockQuestions.asStateFlow()

    private val _currentMockAttempt = MutableStateFlow<MockAttemptEntity?>(null)
    val currentMockAttempt: StateFlow<MockAttemptEntity?> = _currentMockAttempt.asStateFlow()

    private val _mockUserAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val mockUserAnswers: StateFlow<Map<Int, Int>> = _mockUserAnswers.asStateFlow()

    private val _mockMarkedForReview = MutableStateFlow<Set<Int>>(emptySet())
    val mockMarkedForReview: StateFlow<Set<Int>> = _mockMarkedForReview.asStateFlow()

    private val _mockTimeRemainingSeconds = MutableStateFlow(5400) // 90 mins
    val mockTimeRemainingSeconds: StateFlow<Int> = _mockTimeRemainingSeconds.asStateFlow()

    private var mockTimerJob: kotlinx.coroutines.Job? = null
    private var mockEndTimeMillis: Long = 0L

    private val _mockSessionTotalSeconds = MutableStateFlow(0)
    val mockSessionTotalSeconds: StateFlow<Int> = _mockSessionTotalSeconds.asStateFlow()

    private val _isSubmittingMock = MutableStateFlow(false)
    val isSubmittingMock: StateFlow<Boolean> = _isSubmittingMock.asStateFlow()

    val activityLogs: StateFlow<List<ActivityLogEntity>> = repository.activityLogs.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    private var isLoggingOutDueToDevice = false




    init {
        com.example.JuktiApplication.ensureFirebaseInitialized(application)
        syncTrustedTime()

        viewModelScope.launch {
            networkMonitor.isConnected.collect { online ->
                if (!online) {
                    repository.clearPremiumCache()
                    
                    if (_activeMockQuestions.value.any { it.isPremium }) {
                        _activeMockQuestions.value = emptyList()
                        _currentMockAttempt.value = null
                        _sessionMessage.value = "Premium Content is unavailable offline."
                        if (_currentScreen.value == Screen.MOCK_PLAYER || _currentScreen.value == Screen.MOCK_RESULT) {
                            navigateTo(Screen.HOME)
                        }
                    }
                }
            }
        }
        
        viewModelScope.launch {
            combine(effectiveEntitlement, isAdminOrOwner, networkMonitor.isConnected) { eff, admin, online -> Triple(eff, admin, online) }
                .collect { (eff, admin, online) ->
                    if (online && (admin || (eff != null && eff.isPremium))) {
                        repository.refreshPremiumContent()
                    } else if (!online) {
                        // Retain cached content offline
                    } else {
                        repository.clearPremiumCache()
                    }
                }
        }

        viewModelScope.launch {
            effectiveEntitlement.collect { eff ->
                val currentEff = eff ?: com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(userEntitlements.value, plans.value, getTrustedTime())
                val isAdmin = isAdminOrOwner.value
                if (_activeMockQuestions.value.any { q -> q.isPremium && !com.example.data.util.PlanValidityEngine.isQuestionAccessible(q, currentEff, isAdmin) }) {
                     _activeMockQuestions.value = emptyList()
                     _currentMockAttempt.value = null
                     _sessionMessage.value = "Your Premium Entitlement has expired."
                     if (_currentScreen.value == Screen.MOCK_PLAYER || _currentScreen.value == Screen.MOCK_RESULT) {
                          navigateTo(Screen.HOME)
                     }
                }
                if (_selectedStudyNote.value?.isPremium == true && !com.example.data.util.PlanValidityEngine.isStudyNoteAccessible(_selectedStudyNote.value!!, currentEff, isAdmin)) {
                     _selectedStudyNote.value = null
                     _sessionMessage.value = "Your Premium Entitlement has expired."
                     if (_currentScreen.value == Screen.STUDY_NOTES) {
                          navigateTo(Screen.HOME)
                     }
                }
            }
        }

        viewModelScope.launch {
            aboutConfig.collect { config ->
                if (config.logoUrl.isNotEmpty()) {
                    syncLogoLocally(config.logoUrl, config.logoUpdatedAt)
                }
            }
        }

        viewModelScope.launch {
            userProfile.collectLatest { prof ->
                if (prof != null) {
                    val sanitizedDocId = com.example.data.repository.FirebaseRepository().getSanitizedUserDocId(prof.email)
                    val email = prof.email.trim().lowercase()
                    val uid = prof.uid.trim()
                    repository.getUserEntitlements(sanitizedDocId, uid, email).collectLatest { ents ->
                        _userEntitlements.value = ents
                    }
                }
            }
        }
        viewModelScope.launch {
            userProfile.collectLatest { prof ->
                if (prof != null && prof.isLoggedIn && prof.email.isNotBlank() && prof.currentDeviceId.isNotBlank()) {
                    repository.observeUserProfile(prof.email, prof.uid).collectLatest { remoteProf ->
                        if (remoteProf != null && remoteProf.currentDeviceId.isNotBlank() && remoteProf.currentDeviceId != prof.currentDeviceId) {
                            logoutDueToOtherDeviceLogin()
                        }
                    }
                }
            }
        }
        viewModelScope.launch {
            com.example.data.migration.AppVersionMigrationManager.checkAndRunAppMigrations(getApplication(), database)
            repository.initializeSeedDataIfNeeded()
            clearOldActivityLogs(getApplication())
        }
        viewModelScope.launch {
            userProfile.collect { prof ->
                if (prof != null) {
                    val fbUser = try { com.example.JuktiApplication.getAuth(getApplication())?.currentUser } catch (e: Throwable) { null }
                    val needsForcedLogout = prof.isLoggedIn && fbUser == null && prof.email.isNotBlank() && prof.email != "guest@jukti.in"

                    if (needsForcedLogout) {
                         // Force logout because Firebase session is missing
                         val updatedProf = SampleData.initialUserProfile.copy(
                             id = 1,
                             isLoggedIn = false,
                             currentDeviceId = "",
                             activeDeviceId = "",
                             email = "",
                             name = "Guest User",
                             uid = ""
                         )
                         launch(Dispatchers.IO) { repository.updateUserProfile(updatedProf) }
                         _sessionMessage.value = "Firebase session expired. Please sign in again."
                         if (_currentScreen.value != Screen.AUTH) {
                             _currentScreen.value = Screen.AUTH
                         }
                    } else if (splashFinished) {
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
                                }
                            }
                        }
                    } else {
                        if (prof.isLoggedIn && prof.email.isNotBlank() && prof.currentDeviceId.isNotBlank()) {
                            val activeInManager = UserSessionManager.getActiveDeviceId(prof.email)
                            if (activeInManager == null) {
                                UserSessionManager.registerSession(prof.email, prof.currentDeviceId)
                            }
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
                        timestamp = getTrustedTime()
                    )
                )
            }
        }
    }

    fun clearOldActivityLogs(context: android.content.Context) {
        viewModelScope.launch {
            try {
                val sevenDaysAgo = getTrustedTime() - (7L * 24 * 60 * 60 * 1000)
                repository.deleteOldAdminLogs(sevenDaysAgo)
                repository.deleteOldOwnerLogs(sevenDaysAgo)
                // We should also delete them from Firebase using SyncQueue, but since they are logs we can just delete from Firebase directly.
                val db = com.example.JuktiApplication.getFirestore(context) ?: return@launch
                val snapshot = db.collection("activity_logs").whereLessThan("timestamp", sevenDaysAgo).get().await()
                for (doc in snapshot.documents) {
                    doc.reference.delete().await()
                }
                
                // Pending Requests cleanup
                val prSnapshot = db.collection("pending_requests").whereIn("status", listOf("APPROVED", "REJECTED", "FAILED")).get().await()
                for (doc in prSnapshot.documents) {
                    doc.reference.delete().await()
                }

                launchOnMain {
                    android.widget.Toast.makeText(context, "Storage cleared", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                if (e is com.google.firebase.firestore.FirebaseFirestoreException && e.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    android.util.Log.w("JuktiViewModel", "Storage cleanup skipped: Firestore permission required")
                } else {
                    android.util.Log.e("JuktiViewModel", "Storage cleanup failed", e)
                }
            }
        }
    }

    fun clearCacheFiles(context: android.content.Context) {
        viewModelScope.launch {
            try {
                val cacheDir = context.cacheDir
                cacheDir.deleteRecursively()
                launchOnMain {
                    android.widget.Toast.makeText(context, "Cache cleared", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("JuktiViewModel", "Cache clear failed", e)
            }
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
    private val navBackStack = mutableListOf<Screen>()

    fun navigateTo(screen: Screen) {
        val currentTime = getTrustedTime()
        if (currentTime - lastNavTime < 500) return
        lastNavTime = currentTime

        val current = _currentScreen.value
        if (current == screen) return

        if (screen != Screen.MCQ_STUDY) {
            _studySubView.value = null
            _openedStudyDirectly.value = false
        }

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

        if (current != Screen.SPLASH && current != Screen.AUTH) {
            if (screen == Screen.HOME) {
                navBackStack.clear()
            } else {
                if (navBackStack.lastOrNull() != current) {
                    navBackStack.add(current)
                }
            }
        }

        _currentScreen.value = screen
    }

    fun goBack(): Boolean {
        val current = _currentScreen.value
        if (current == Screen.HOME || current == Screen.AUTH || current == Screen.SPLASH) {
            return false
        }
        if (current == Screen.MCQ_STUDY && _studySubView.value != null) {
            if (_openedStudyDirectly.value) {
                _studySubView.value = null
                _openedStudyDirectly.value = false
                _currentScreen.value = Screen.HOME
                navBackStack.clear()
                return true
            } else {
                _studySubView.value = null
                return true
            }
        }
        if (navBackStack.isNotEmpty()) {
            val previous = navBackStack.removeAt(navBackStack.size - 1)
            if (previous != current) {
                _currentScreen.value = previous
                return true
            }
        }
        if (current != Screen.HOME) {
            _currentScreen.value = Screen.HOME
            navBackStack.clear()
            return true
        }
        return false
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

    private fun parseNegativeMarkingRate(value: String): Float {
        if (value.equals("None", ignoreCase = true) || value.isBlank()) return 0.0f
        val match = Regex("""([0-9]+(?:\.[0-9]+)?)""").find(value)
        return match?.value?.toFloatOrNull() ?: 0.0f
    }

    private fun serializeMockAttemptAnswers(
        answers: Map<Int, Int>,
        questionIds: List<Long>,
        timeSpentSeconds: Int,
        rawScore: Float
    ): String {
        val root = org.json.JSONObject()
        val answersObj = org.json.JSONObject()
        answers.forEach { (idx, opt) ->
            val qId = questionIds.getOrNull(idx)
            answersObj.put(idx.toString(), opt)
            if (qId != null) {
                answersObj.put("qid_$qId", opt)
            }
        }
        root.put("answers", answersObj)
        root.put("timeSpentSeconds", timeSpentSeconds)
        root.put("rawScore", rawScore.toDouble())
        return root.toString()
    }

    private fun deserializeMockAttemptAnswers(
        jsonStr: String,
        questionIds: List<Long>
    ): Pair<Map<Int, Int>, Int> {
        if (jsonStr.isBlank()) return Pair(emptyMap(), 0)
        val map = mutableMapOf<Int, Int>()
        var timeSpent = 0
        try {
            val root = org.json.JSONObject(jsonStr)
            if (root.has("answers")) {
                val answersObj = root.getJSONObject("answers")
                questionIds.forEachIndexed { index, qId ->
                    if (answersObj.has(index.toString())) {
                        map[index] = answersObj.getInt(index.toString())
                    } else if (answersObj.has("qid_$qId")) {
                        map[index] = answersObj.getInt("qid_$qId")
                    }
                }
                timeSpent = root.optInt("timeSpentSeconds", 0)
            } else {
                val keys = root.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val idx = k.toIntOrNull()
                    if (idx != null) {
                        map[idx] = root.getInt(k)
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore format errors
        }
        return Pair(map, timeSpent)
    }

    fun selectMockTest(mock: MockTestEntity) {
        if (!canAccessMockTest(mock)) {
            _showPremiumPaywall.value = true
            return
        }
        _selectedMockTest.value = mock
        _currentMockAttempt.value = null
        _mockUserAnswers.value = emptyMap()
        _mockMarkedForReview.value = emptySet()
        val totalSec = (mock.durationMinutes * 60).coerceAtLeast(60)
        _mockTimeRemainingSeconds.value = totalSec
        _mockSessionTotalSeconds.value = 0
        _isSubmittingMock.value = false
        mockEndTimeMillis = System.currentTimeMillis() + (totalSec * 1000L)
        startMockTimer()

        // Resolve and lock the EXACT questions belonging to this mock test session
        val allQuestionsList = questions.value
        val resolvedQuestions: List<QuestionEntity> = when {
            mock.questionIds.isNotBlank() -> {
                val ids = mock.questionIds.split(",").mapNotNull { it.trim().toLongOrNull() }
                val idMap = allQuestionsList.associateBy { it.id }
                val fromIds = ids.mapNotNull { idMap[it] }
                if (fromIds.isNotEmpty()) {
                    fromIds
                } else {
                    allQuestionsList.take(mock.totalQuestions)
                }
            }
            mock.testType.equals("Subject-wise", ignoreCase = true) && mock.subjectOrChapter.isNotBlank() -> {
                val matching = allQuestionsList.filter { 
                    it.subject.equals(mock.subjectOrChapter.trim(), ignoreCase = true) 
                }
                if (matching.isNotEmpty()) matching.take(mock.totalQuestions) else allQuestionsList.take(mock.totalQuestions)
            }
            mock.testType.equals("Chapter-wise", ignoreCase = true) && mock.subjectOrChapter.isNotBlank() -> {
                val parts = mock.subjectOrChapter.split("||")
                val subj = parts.getOrNull(0)?.trim() ?: ""
                val chap = parts.getOrNull(1)?.trim() ?: ""
                val matching = allQuestionsList.filter { 
                    it.subject.equals(subj, ignoreCase = true) && (chap.isBlank() || it.topic.equals(chap, ignoreCase = true))
                }
                if (matching.isNotEmpty()) matching.take(mock.totalQuestions) else allQuestionsList.take(mock.totalQuestions)
            }
            else -> {
                val matching = if (mock.category.isNotBlank() && !mock.category.equals("All", ignoreCase = true)) {
                    allQuestionsList.filter { 
                        it.examCategory.equals(mock.category.trim(), ignoreCase = true) || it.subject.equals(mock.category.trim(), ignoreCase = true) 
                    }
                } else {
                    allQuestionsList
                }
                if (matching.isNotEmpty()) matching.take(mock.totalQuestions) else allQuestionsList.take(mock.totalQuestions)
            }
        }
        
        _activeMockQuestions.value = resolvedQuestions
        navigateTo(Screen.MOCK_PLAYER)
    }

    private fun startMockTimer() {
        mockTimerJob?.cancel()
        mockTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(500L)
                if (mockEndTimeMillis > 0L) {
                    val now = System.currentTimeMillis()
                    val remainingSec = ((mockEndTimeMillis - now + 999L) / 1000L).coerceAtLeast(0L).toInt()
                    _mockTimeRemainingSeconds.value = remainingSec
                    if (remainingSec <= 0) {
                        if (!_isSubmittingMock.value) {
                            submitCurrentMockTest()
                        }
                        break
                    }
                }
            }
        }
    }

    fun decrementMockTimer() {
        if (mockEndTimeMillis > 0L) {
            val now = System.currentTimeMillis()
            val remainingSec = ((mockEndTimeMillis - now + 999L) / 1000L).coerceAtLeast(0L).toInt()
            _mockTimeRemainingSeconds.value = remainingSec
            if (remainingSec <= 0 && !_isSubmittingMock.value) {
                submitCurrentMockTest()
            }
        } else if (_mockTimeRemainingSeconds.value > 0) {
            _mockTimeRemainingSeconds.value -= 1
            _mockSessionTotalSeconds.value += 1
            if (_mockTimeRemainingSeconds.value == 0 && !_isSubmittingMock.value) {
                submitCurrentMockTest()
            }
        }
    }

    fun cancelMockTimer() {
        mockTimerJob?.cancel()
    }

    fun recordMockAnswer(questionIndex: Int, optionIndex: Int) {
        val currentAnswers = _mockUserAnswers.value
        // If clicking already selected option, toggle / keep or re-select
        _mockUserAnswers.value = currentAnswers + (questionIndex to optionIndex)
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
        if (note != null) {
            if (!canAccessStudyNote(note)) {
                _showPremiumPaywall.value = true
                return
            }
        }
        _selectedStudyNote.value = note
        if (note != null) {
            navigateTo(Screen.STUDY_NOTE_DETAIL)
        } else {
            navigateTo(Screen.STUDY_NOTES)
        }
    }

    fun viewMockResultForTest(mock: MockTestEntity) {
        viewModelScope.launch {
            val userId = userProfile.value?.uid?.ifBlank { "local_user" } ?: "local_user"
            val latestAttempt = repository.getLatestAttemptForMock(mock.id, userId)
            if (latestAttempt != null) {
                viewMockAttemptResult(mock, latestAttempt)
            } else {
                selectMockTest(mock)
            }
        }
    }

    fun viewMockAttemptResult(mock: MockTestEntity, attempt: MockAttemptEntity) {
        _selectedMockTest.value = mock
        _currentMockAttempt.value = attempt
        
        val allQuestionsList = questions.value
        val idMap = allQuestionsList.associateBy { it.id }
        val ids = attempt.questionIds.split(",").mapNotNull { it.trim().toLongOrNull() }
        val attemptQuestions = ids.mapNotNull { idMap[it] }
        _activeMockQuestions.value = if (attemptQuestions.isNotEmpty()) attemptQuestions else allQuestionsList.take(mock.totalQuestions)
        
        val (restoredAnswers, _) = deserializeMockAttemptAnswers(attempt.userAnswersJson, ids)
        _mockUserAnswers.value = restoredAnswers
        _mockMarkedForReview.value = emptySet()
        
        navigateTo(Screen.MOCK_RESULT)
    }

    fun analyzeMockFromHistory(
        titleEn: String,
        titleAs: String,
        score: Float,
        totalMarks: Float,
        accuracy: Int,
        rank: Int,
        percentile: Float,
        mockTestId: Long = 0L
    ) {
        viewModelScope.launch {
            val existingMock = mockTests.value.firstOrNull { (mockTestId != 0L && it.id == mockTestId) || it.titleEn == titleEn }
            val userId = userProfile.value?.uid?.ifBlank { "local_user" } ?: "local_user"
            val targetMockId = existingMock?.id ?: mockTestId
            
            val attempt = if (targetMockId != 0L) {
                repository.getLatestAttemptForMock(targetMockId, userId)
            } else null

            val mockEntity = existingMock ?: MockTestEntity(
                id = if (targetMockId != 0L) targetMockId else 999L,
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

            if (attempt != null) {
                viewMockAttemptResult(mockEntity, attempt)
            } else {
                _selectedMockTest.value = mockEntity
                val allQ = questions.value
                val resolvedQ = if (mockEntity.questionIds.isNotBlank()) {
                    val ids = mockEntity.questionIds.split(",").mapNotNull { it.trim().toLongOrNull() }
                    val idMap = allQ.associateBy { it.id }
                    ids.mapNotNull { idMap[it] }
                } else {
                    allQ.take(mockEntity.totalQuestions.coerceAtLeast(10))
                }
                _activeMockQuestions.value = resolvedQ

                val answerMap = mutableMapOf<Int, Int>()
                val totalQ = resolvedQ.size
                val targetCorrect = ((accuracy / 100f) * totalQ).toInt().coerceIn(0, totalQ)
                val targetIncorrect = ((mockEntity.totalQuestions - targetCorrect) / 2).coerceIn(0, totalQ - targetCorrect)

                resolvedQ.forEachIndexed { index, q ->
                    when {
                        index < targetCorrect -> answerMap[index] = q.correctOptionIndex
                        index < targetCorrect + targetIncorrect -> answerMap[index] = (q.correctOptionIndex + 1) % 4
                    }
                }
                _mockUserAnswers.value = answerMap

                val synthAttempt = MockAttemptEntity(
                    id = System.currentTimeMillis(),
                    mockTestId = mockEntity.id,
                    userId = userId,
                    timestamp = System.currentTimeMillis(),
                    questionIds = resolvedQ.map { it.id }.joinToString(","),
                    userAnswersJson = serializeMockAttemptAnswers(answerMap, resolvedQ.map { it.id }, 1800, score.toFloat()),
                    score = score,
                    totalMarks = totalMarks,
                    accuracy = accuracy.toFloat(),
                    correctCount = targetCorrect,
                    totalAttempted = targetCorrect + targetIncorrect,
                    questionMarksJson = mockEntity.questionMarksJson
                )
                _currentMockAttempt.value = synthAttempt
                navigateTo(Screen.MOCK_RESULT)
            }
        }
    }

    fun submitCurrentMockTest() {
        if (_isSubmittingMock.value) return
        val test = _selectedMockTest.value ?: return
        val currentQuestionsList = _activeMockQuestions.value
        if (currentQuestionsList.isEmpty()) return

        cancelMockTimer()
        _isSubmittingMock.value = true
        val answers = _mockUserAnswers.value

        val totalQ = currentQuestionsList.size
        
        var qMarksMap: org.json.JSONObject? = null
        try {
            if (test.questionMarksJson.isNotBlank() && test.questionMarksJson != "{}") {
                qMarksMap = org.json.JSONObject(test.questionMarksJson)
            }
        } catch (e: Exception) {}
        
        val markPerQuestion = if (test.markPerQuestion > 0f) test.markPerQuestion else (test.totalMarks / totalQ.coerceAtLeast(1).toFloat())
        val negPerQuestion = parseNegativeMarkingRate(test.negativeMarking)

        var correctCount = 0
        var incorrectCount = 0
        var skippedCount = 0
        var earnedMarks = 0f
        var deductedMarks = 0f

        val userId = userProfile.value?.uid?.ifBlank { "local_user" } ?: "local_user"

        currentQuestionsList.forEachIndexed { index, q ->
            val userChoice = answers[index]
            var actualMarkForQ = markPerQuestion
            try {
                if (qMarksMap != null && qMarksMap.has(q.id.toString())) {
                    actualMarkForQ = qMarksMap.getDouble(q.id.toString()).toFloat()
                }
            } catch (e: Exception) {}
            
            when {
                userChoice == q.correctOptionIndex -> {
                    correctCount++
                    earnedMarks += actualMarkForQ
                    viewModelScope.launch {
                        try {
                            repository.recordQuestionAnswer(userId = userId, questionId = q.id.toString(), isCorrect = true)
                        } catch (e: Exception) {}
                    }
                }
                userChoice != null -> {
                    incorrectCount++
                    deductedMarks += negPerQuestion
                    viewModelScope.launch {
                        try {
                            repository.recordQuestionAnswer(userId = userId, questionId = q.id.toString(), isCorrect = false)
                        } catch (e: Exception) {}
                    }
                }
                else -> {
                    skippedCount++
                }
            }
        }

        val totalAttempted = correctCount + incorrectCount
        val rawScore = earnedMarks - deductedMarks
        val finalScore = rawScore.coerceAtLeast(0f)
        val accuracy = if (totalAttempted > 0) ((correctCount.toFloat() / totalAttempted.toFloat()) * 100f).coerceIn(0f, 100f) else 0f
        val timeSpentSec = ((test.durationMinutes * 60) - _mockTimeRemainingSeconds.value).coerceAtLeast(1)

        val questionIdsList = currentQuestionsList.map { it.id }
        val answersJson = serializeMockAttemptAnswers(answers, questionIdsList, timeSpentSec, rawScore)

        val attempt = MockAttemptEntity(
            id = 0,
            mockTestId = test.id,
            userId = userId,
            timestamp = System.currentTimeMillis(),
            questionIds = questionIdsList.joinToString(","),
            userAnswersJson = answersJson,
            score = finalScore,
            totalMarks = test.totalMarks,
            accuracy = accuracy,
            correctCount = correctCount,
            totalAttempted = totalAttempted,
            questionMarksJson = test.questionMarksJson
        )

        viewModelScope.launch {
            try {
                // 1. Save attempt record locally and sync to Firebase
                val insertedAttemptId = repository.saveMockAttempt(attempt)
                val savedAttempt = attempt.copy(id = insertedAttemptId)
                _currentMockAttempt.value = savedAttempt

                // 2. Compute dynamic rank & percentile against all completed attempts
                val attemptsForMock = repository.getAllAttemptsForMock(test.id).firstOrNull() ?: emptyList()
                val scorePercentage = if (test.totalMarks > 0f) ((finalScore.toFloat() / test.totalMarks.toFloat()) * 100f) else 0f
                val dynamicPercentile = if (attemptsForMock.size > 1) {
                    val lowerScoreCount = attemptsForMock.count { it.score < finalScore }
                    ((lowerScoreCount.toFloat() / (attemptsForMock.size - 1).toFloat()) * 100f).coerceIn(5.0f, 99.9f)
                } else {
                    (scorePercentage * 0.7f + accuracy * 0.3f).coerceIn(5.0f, 99.9f)
                }

                val rank = if (attemptsForMock.isNotEmpty()) {
                    val higherCount = attemptsForMock.count { it.score > finalScore }
                    higherCount + 1
                } else 1

                // 3. Update mock test in database with submission results
                repository.submitMockResult(
                    mockId = test.id,
                    score = finalScore,
                    accuracy = accuracy,
                    timeSpentMins = (timeSpentSec / 60).coerceAtLeast(1),
                    totalAttempted = totalAttempted,

                    correctCount = correctCount
                )

                _selectedMockTest.value = test.copy(
                    isCompleted = true,
                    userScore = finalScore,
                    userAccuracy = accuracy,
                    userRank = rank,
                    userPercentile = dynamicPercentile
                )
            } catch (e: Exception) {
                Log.e("JuktiViewModel", "Error submitting mock test", e)
            } finally {
                _isSubmittingMock.value = false
                navigateTo(Screen.MOCK_RESULT)
            }
        }
    }

    fun addMockTest(mock: MockTestEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val res = repository.addMockTest(mock)
                _syncToastMessage.value = res.second
            } catch (e: Exception) {
                Log.e("JuktiViewModel", "Error adding mock test", e)
                _syncToastMessage.value = "Error: ${e.localizedMessage}"
            } finally {
                onComplete()
            }
        }
    }

    fun updateMockTest(mock: MockTestEntity, onComplete: () -> Unit = {}) {
        logActivity("Updated mock test: ${mock.titleEn}")
        viewModelScope.launch {
            try {
                val res = repository.updateMockTest(mock)
                _syncToastMessage.value = res.second
            } catch (e: Exception) {
                Log.e("JuktiViewModel", "Error updating mock test", e)
                _syncToastMessage.value = "Error: ${e.localizedMessage}"
            } finally {
                onComplete()
            }
        }
    }

    fun deleteMockTest(mock: MockTestEntity) {
        viewModelScope.launch {
            val res = repository.deleteMockTest(mock)
            _syncToastMessage.value = res.second
        }
    }

    fun addStudyNote(note: StudyNoteEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val res = repository.addStudyNote(note)
                _syncToastMessage.value = res.second
            } catch (e: Exception) {
                Log.e("JuktiViewModel", "Error adding study note", e)
                _syncToastMessage.value = "Error: ${e.localizedMessage}"
            } finally {
                onComplete()
            }
        }
    }

    fun updateStudyNote(note: StudyNoteEntity, onComplete: () -> Unit = {}) {
        logActivity("Updated study note: ${note.titleEn}")
        viewModelScope.launch {
            try {
                val res = repository.updateStudyNote(note)
                _syncToastMessage.value = res.second
            } catch (e: Exception) {
                Log.e("JuktiViewModel", "Error updating study note", e)
                _syncToastMessage.value = "Error: ${e.localizedMessage}"
            } finally {
                onComplete()
            }
        }
    }

    fun deleteStudyNote(note: StudyNoteEntity) {
        viewModelScope.launch {
            val res = repository.deleteStudyNote(note)
            _syncToastMessage.value = res.second
        }
    }

    fun toggleBookmarkQuestion(q: QuestionEntity) {
        viewModelScope.launch {
            userProfile.value?.uid?.let { userId ->
                repository.toggleBookmarkQuestion(q.id.toString(), userId)
            }
        }
    }

    fun toggleHideQuestion(q: QuestionEntity) {
        viewModelScope.launch {
            userProfile.value?.uid?.let { userId ->
                repository.toggleHideQuestion(q.id.toString(), userId)
            }
        }
    }

    fun unhideAllQuestions() {
        viewModelScope.launch {
            userProfile.value?.uid?.let { userId ->
                repository.unhideAllQuestions(userId)
            }
        }
    }

    fun toggleLikeQuestion(q: QuestionEntity) {
        viewModelScope.launch {
            userProfile.value?.uid?.let { userId ->
                repository.toggleLikeQuestion(q.id.toString(), userId)
            }
        }
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
        val isUserAdmin = isAdminOrOwner.value
        val isAccessible = isUserAdmin || accessibleStudyNotes.value.any { it.id == n.id }
        if (!isAccessible || (n.isPremium && !isUserPremium.value && !isUserAdmin)) {
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

    fun submitQuestionAnswer(questionId: Long, isCorrect: Boolean, timeSpentSec: Int = 10) {
        viewModelScope.launch {
            val userId = userProfile.value?.uid ?: FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
            repository.recordQuestionAnswer(userId, questionId.toString(), isCorrect, timeSpentSec, today)
        }
    }

    fun recordQuestionStudied(questionId: Long, timeSpentSec: Int = 10) {
        viewModelScope.launch {
            val userId = userProfile.value?.uid ?: FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            repository.recordQuestionStudied(userId, questionId.toString(), timeSpentSec)
        }
    }

    fun updateFirebaseProjectId(projectId: String) {
        viewModelScope.launch {
            repository.updateFirebaseProjectId(projectId)
        }
    }

    suspend fun fetchAllUsersDirect(): List<com.example.data.local.UserProfileEntity> {
        return repository.fetchAllUsersDirect()
    }

    suspend fun fetchUserEntitlementDirect(email: String): com.example.data.local.EntitlementEntity? {
        return fetchUserEntitlementsDirect(email).firstOrNull()
    }

    suspend fun fetchUserEntitlementsDirect(email: String): List<com.example.data.local.EntitlementEntity> {
        return try {
            val sanitizedDocId = com.example.data.repository.FirebaseRepository().getSanitizedUserDocId(email)
            val trimmedEmail = email.trim().lowercase()
            
            val local = repository.getUserEntitlementsDirect(sanitizedDocId, trimmedEmail)
            if (local.isNotEmpty() && validateEntitlements(local)) {
                return local
            }
            
            val remote = repository.fetchUserEntitlementsFromFirebase(email)
            if (remote.isNotEmpty()) {
                repository.insertEntitlements(remote)
                val mapped = if (sanitizedDocId.isNotBlank()) remote.map { it.copy(userId = sanitizedDocId) } else emptyList()
                if (mapped.isNotEmpty()) {
                    repository.insertEntitlements(mapped)
                }
                return remote
            }
            local
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun loginWithGoogle(activity: Activity) {
        viewModelScope.launch {
            isLoggingOutDueToDevice = false
            _isAuthLoading.value = true
            _sessionMessage.value = null
            try {
                val googleAuthManager = GoogleAuthManager(getApplication())
                val result = googleAuthManager.signInWithGoogle(activity)

                if (result.isCancelled) {
                    _isAuthLoading.value = false
                    return@launch
                }

                if (result.needsAccountSelection) {
                    _isAuthLoading.value = false
                    _googleAccountsToSelect.value = result.availableAccounts
                    return@launch
                }

                val fbUser = result.firebaseUser
                val uid = fbUser?.uid
                val email = fbUser?.email?.trim().takeIf { !it.isNullOrBlank() } ?: result.fallbackEmail?.trim() ?: ""
                val displayName = fbUser?.displayName?.trim().takeIf { !it.isNullOrBlank() } ?: result.fallbackName?.trim() ?: ""

                if (email.isBlank() || !email.contains("@")) {
                    _sessionMessage.value = result.errorMessage ?: "Google Sign-In failed. Please try again."
                    _isAuthLoading.value = false
                    return@launch
                }

                val finalUid = uid ?: ("google_" + java.util.UUID.nameUUIDFromBytes(email.toByteArray()).toString().replace("-", "").take(16))
                val deviceId = java.util.UUID.randomUUID().toString()

                val isOwnerEmail = email.equals("juktieducation@gmail.com", ignoreCase = true) || email.equals("borapinku151@gmail.com", ignoreCase = true)
                val defaultRole = if (isOwnerEmail) "OWNER" else "USER"

                withContext(Dispatchers.IO) {
                    repository.loadUserProfileForAuth(
                        uid = finalUid,
                        email = email,
                        googleName = displayName.ifBlank { email.substringBefore("@") },
                        deviceId = deviceId,
                        defaultRole = defaultRole
                    )
                    UserSessionManager.registerSession(email, deviceId)
                }

                _isGuestMode.value = false
                _sessionMessage.value = null
                _currentScreen.value = Screen.HOME
            } catch (e: Exception) {
                Log.e("JuktiViewModel", "Google Sign-In flow error", e)
                _sessionMessage.value = "Sign-In error: ${e.localizedMessage ?: "Unexpected error"}"
            } finally {
                _isAuthLoading.value = false
            }
        }
    }

    fun selectGoogleAccount(email: String) {
        _googleAccountsToSelect.value = null
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) {
            _sessionMessage.value = "Please enter a valid Google email address"
            return
        }
        viewModelScope.launch {
            isLoggingOutDueToDevice = false
            _isAuthLoading.value = true
            _sessionMessage.value = null
            try {
                val displayName = trimmedEmail.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }
                val finalUid = "google_" + java.util.UUID.nameUUIDFromBytes(trimmedEmail.toByteArray()).toString().replace("-", "").take(16)
                val deviceId = java.util.UUID.randomUUID().toString()

                val isOwnerEmail = trimmedEmail.equals("juktieducation@gmail.com", ignoreCase = true) || trimmedEmail.equals("borapinku151@gmail.com", ignoreCase = true)
                val defaultRole = if (isOwnerEmail) "OWNER" else "USER"

                withContext(Dispatchers.IO) {
                    repository.loadUserProfileForAuth(
                        uid = finalUid,
                        email = trimmedEmail,
                        googleName = displayName,
                        deviceId = deviceId,
                        defaultRole = defaultRole
                    )
                    UserSessionManager.registerSession(trimmedEmail, deviceId)
                }

                _isGuestMode.value = false
                _sessionMessage.value = null
                _currentScreen.value = Screen.HOME
            } catch (e: Exception) {
                Log.e("JuktiViewModel", "Google account selection error", e)
                _sessionMessage.value = "Sign-In error: ${e.localizedMessage ?: "Unexpected error"}"
            } finally {
                _isAuthLoading.value = false
            }
        }
    }

    fun loginAsGuest() {
        viewModelScope.launch {
            isLoggingOutDueToDevice = false
            _isAuthLoading.value = true
            _sessionMessage.value = null
            try {
                val guestUid = "guest_" + getTrustedTime()
                val guestEmail = "guest@jukti.in"
                val deviceId = java.util.UUID.randomUUID().toString()
                withContext(Dispatchers.IO) {
                    repository.loadUserProfileForAuth(
                        uid = guestUid,
                        email = guestEmail,
                        googleName = "Guest Student",
                        deviceId = deviceId,
                        defaultRole = "USER"
                    )
                }
                _isGuestMode.value = true
                _sessionMessage.value = null
                _currentScreen.value = Screen.HOME
            } catch (e: Exception) {
                Log.e("JuktiViewModel", "Guest login error", e)
                _sessionMessage.value = "Guest login error: ${e.localizedMessage}"
            } finally {
                _isAuthLoading.value = false
            }
        }
    }

    fun loginWithEmail(emailInput: String, nameInput: String = "", passwordInput: String = "", isRegister: Boolean = false) {
        val trimmedEmail = emailInput.trim().lowercase()
        val deviceId = java.util.UUID.randomUUID().toString()

        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) {
            _sessionMessage.value = "Please enter a valid email address."
            return
        }

        viewModelScope.launch {
            _isAuthLoading.value = true
            _sessionMessage.value = null
            try {
                val auth = com.example.JuktiApplication.getAuth(getApplication())
                var uid: String? = null
                var fbUserEmail: String? = null
                var fbDisplayName: String? = null

                if (auth != null) {
                    try {
                        val authResult = withContext(Dispatchers.IO) {
                            if (passwordInput.isNotBlank()) {
                                if (isRegister) {
                                    auth.createUserWithEmailAndPassword(trimmedEmail, passwordInput).await()
                                } else {
                                    auth.signInWithEmailAndPassword(trimmedEmail, passwordInput).await()
                                }
                            } else if (!isRegister) {
                                auth.signInAnonymously().await()
                            } else {
                                throw IllegalArgumentException("Password is required.")
                            }
                        }
                        val fbUser = authResult.user
                        uid = fbUser?.uid
                        fbUserEmail = fbUser?.email
                        fbDisplayName = fbUser?.displayName
                    } catch (e: Exception) {
                        Log.w("JuktiViewModel", "Firebase Auth call failed: ${e.message}", e)
                        throw e
                    }
                } else {
                    throw Exception("Firebase Authentication is not available.")
                }

                val finalUid = uid ?: ("user_" + java.util.UUID.nameUUIDFromBytes(trimmedEmail.toByteArray()).toString().replace("-", "").take(16))
                val effectiveEmail = fbUserEmail?.trim()?.ifBlank { trimmedEmail } ?: trimmedEmail
                val gName = fbDisplayName?.trim()?.ifBlank { nameInput.trim() } ?: nameInput.trim()

                val isOwnerEmail = effectiveEmail.equals("juktieducation@gmail.com", ignoreCase = true) || effectiveEmail.equals("borapinku151@gmail.com", ignoreCase = true)
                val defaultRole = if (isOwnerEmail) "OWNER" else "USER"

                withContext(Dispatchers.IO) {
                    repository.loadUserProfileForAuth(
                        uid = finalUid,
                        email = effectiveEmail,
                        googleName = gName,
                        deviceId = deviceId,
                        defaultRole = defaultRole
                    )
                    UserSessionManager.registerSession(effectiveEmail, deviceId)
                }

                _isGuestMode.value = false
                _sessionMessage.value = null
                _currentScreen.value = Screen.HOME
            } catch (e: Exception) {
                Log.e("JuktiViewModel", "Email authentication error", e)
                _sessionMessage.value = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> "Incorrect email or password."
                    is FirebaseAuthInvalidUserException -> "No account found with this email."
                    is FirebaseAuthUserCollisionException -> "An account already exists with this email."
                    else -> "Authentication failed. Please try again."
                }
            } finally {
                _isAuthLoading.value = false
            }
        }
    }

    fun sendPasswordResetEmail(emailInput: String, onResult: (Boolean, String) -> Unit) {
        val trimmedEmail = emailInput.trim().lowercase()
        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) {
            onResult(false, "Please enter a valid email address.")
            return
        }
        viewModelScope.launch {
            try {
                val auth = com.example.JuktiApplication.getAuth(getApplication())
                if (auth == null) {
                    onResult(false, "Authentication service is currently unavailable.")
                    return@launch
                }
                withContext(Dispatchers.IO) {
                    auth.sendPasswordResetEmail(trimmedEmail).await()
                }
                onResult(true, "Password reset email sent. Please check your inbox and follow the instructions to create a new password. If you don't see it, check your Spam or Junk folder.")
            } catch (e: Exception) {
                Log.e("JuktiViewModel", "Password reset error", e)
                val translated = LocalMessageTranslator.translateAuthError(getApplication(), e.message ?: e.localizedMessage)
                onResult(false, translated)
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val auth = com.example.JuktiApplication.getAuth(getApplication())
                val user = auth?.currentUser
                if (user == null) {
                    onResult(false, "Your session has expired. Please sign in again.")
                    return@launch
                }
                val isEmailUser = user.providerData.any { it.providerId == "password" }
                if (!isEmailUser) {
                    onResult(false, "Your account is signed in with an external provider (such as Google). Password management is handled by your provider.")
                    return@launch
                }
                if (currentPassword.isBlank()) {
                    onResult(false, "The current password cannot be empty.")
                    return@launch
                }
                if (newPassword.length < 6) {
                    onResult(false, "Your new password does not meet the password requirements (at least 6 characters).")
                    return@launch
                }
                val email = user.email
                if (email.isNullOrBlank()) {
                    onResult(false, "User email not found.")
                    return@launch
                }
                withContext(Dispatchers.IO) {
                    val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, currentPassword)
                    user.reauthenticate(credential).await()
                    user.updatePassword(newPassword).await()
                }
                onResult(true, "Password successfully updated.")
            } catch (e: Exception) {
                Log.e("JuktiViewModel", "Change password error", e)
                val translated = LocalMessageTranslator.translateAuthError(getApplication(), e.message ?: e.localizedMessage)
                onResult(false, translated)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _isAuthLoading.value = true
            try {
                val googleAuthManager = GoogleAuthManager(getApplication())
                googleAuthManager.signOut()
                repository.clearPremiumCache()
            } catch (e: Throwable) {
                Log.e("JuktiViewModel", "Error during sign out", e)
            }

            val currentProf = userProfile.value
            if (currentProf != null && currentProf.email.isNotBlank()) {
                UserSessionManager.unregisterSession(currentProf.email)
            }

            val cleanProfile = SampleData.initialUserProfile.copy(
                id = 1,
                isLoggedIn = false,
                currentDeviceId = "",
                activeDeviceId = "",
                email = "",
                name = "Guest User",
                uid = ""
            )
            withContext(Dispatchers.IO) {
                repository.updateUserProfile(cleanProfile)
                if (currentProf != null) {
                    val key = currentProf.uid.ifBlank { currentProf.email }
                    repository.clearUserEntitlements(key)
                }
            }

            _userEntitlements.value = emptyList()
            _sessionMessage.value = null
            _isGuestMode.value = false
            _currentMockAttempt.value = null
            _selectedMockTest.value = null
            _mockUserAnswers.value = emptyMap()
            _mockMarkedForReview.value = emptySet()
            _activeMockQuestions.value = emptyList()
            _currentScreen.value = Screen.AUTH
            _isAuthLoading.value = false
        }
    }

    private fun logoutDueToOtherDeviceLogin() {
        if (isLoggingOutDueToDevice || _currentScreen.value == Screen.AUTH) return
        isLoggingOutDueToDevice = true
        viewModelScope.launch {
            try {
                val currentProf = userProfile.value ?: return@launch
                if (!currentProf.isLoggedIn) return@launch
                if (currentProf.email.isNotBlank()) {
                    UserSessionManager.unregisterSession(currentProf.email)
                }
                repository.clearPremiumCache()
                val cleanProfile = SampleData.initialUserProfile.copy(
                    id = 1,
                    isLoggedIn = false,
                    currentDeviceId = "",
                    activeDeviceId = "",
                    email = "",
                    name = "Guest User",
                    uid = ""
                )
                _sessionMessage.value = "Your account was logged in on another device. You have been logged out automatically."
                _currentMockAttempt.value = null
                _selectedMockTest.value = null
                _mockUserAnswers.value = emptyMap()
                _mockMarkedForReview.value = emptySet()
                _activeMockQuestions.value = emptyList()
                _currentScreen.value = Screen.AUTH
                
                withContext(Dispatchers.IO) {
                    repository.updateUserProfile(cleanProfile)
                    val key = currentProf.uid.ifBlank { currentProf.email }
                    repository.clearUserEntitlements(key)
                }
            } finally {
                isLoggingOutDueToDevice = false
            }
        }
    }

    fun updateUserProfile(profile: UserProfileEntity) {
        viewModelScope.launch {
            repository.updateUserProfile(profile)
        }
    }

    fun updateUserName(newName: String) {
        viewModelScope.launch {
            val prof = userProfile.value ?: SampleData.initialUserProfile
            val updated = prof.copy(profileName = newName.trim())
            repository.updateUserProfile(updated)
        }
    }

    fun updateUserRole(role: String) {
        viewModelScope.launch {
            val prof = userProfile.value ?: SampleData.initialUserProfile
            repository.updateUserProfile(prof.copy(role = role))
        }
    }

    
    fun uploadLogoAndSaveConfig(uri: android.net.Uri, config: com.example.data.local.AboutConfigEntity, context: android.content.Context) {
        viewModelScope.launch {
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@launch
                val storage = com.example.JuktiApplication.getStorage(context) ?: return@launch
                val ref = storage.reference.child("assets/logo.png")
                val uploadTask = ref.putBytes(bytes).await()
                val downloadUrl = ref.downloadUrl.await().toString()
                
                val updatedConfig = config.copy(
                    logoUrl = downloadUrl,
                    logoUpdatedAt = getTrustedTime()
                )
                updateAboutConfig(updatedConfig)
                // Also cache locally
                try {
                    val file = java.io.File(context.filesDir, "cached_logo.png")
                    java.io.FileOutputStream(file).use { it.write(bytes) }
                } catch(e: Exception) {}
            } catch (e: Exception) {
                android.util.Log.e("JuktiViewModel", "Error uploading logo", e)
            }
        }
    }

    fun uploadImageFile(
        uri: android.net.Uri,
        folder: String,
        context: android.content.Context,
        onLocalSaved: (String) -> Unit,
        onRemoteUploaded: ((String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@launch
                val localFileName = "${folder}_${getTrustedTime()}.png"
                val file = java.io.File(context.filesDir, localFileName)
                java.io.FileOutputStream(file).use { it.write(bytes) }
                val localPath = file.absolutePath
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onLocalSaved(localPath)
                }

                val storage = com.example.JuktiApplication.getStorage(context)
                if (storage != null) {
                    val ref = storage.reference.child("$folder/${getTrustedTime()}.png")
                    ref.putBytes(bytes).await()
                    val downloadUrl = ref.downloadUrl.await().toString()
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        onRemoteUploaded?.invoke(downloadUrl)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("JuktiViewModel", "Error in uploadImageFile", e)
            }
        }
    }

    



    private fun syncLogoLocally(url: String, updatedAt: Long) {
        val app = getApplication<android.app.Application>()
        val prefs = app.getSharedPreferences("jukti_prefs", android.content.Context.MODE_PRIVATE)
        val cachedUpdatedAt = prefs.getLong("logo_updated_at", 0L)
        
        if (updatedAt > cachedUpdatedAt) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val request = okhttp3.Request.Builder().url(url).build()
                    val client = okhttp3.OkHttpClient()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        response.body?.byteStream()?.use { input ->
                            val file = java.io.File(app.filesDir, "cached_logo.png")
                            java.io.FileOutputStream(file).use { output ->
                                input.copyTo(output)
                            }
                        }
                        prefs.edit().putLong("logo_updated_at", updatedAt).apply()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("JuktiViewModel", "Error syncing logo", e)
                }
            }
        }
    }

    fun updateAboutConfig(config: AboutConfigEntity, onComplete: ((Boolean, String) -> Unit)? = null) {
        logActivity("Updated About/Config settings")
        viewModelScope.launch {
            val res = repository.updateAboutConfig(config)
            onComplete?.invoke(res.first, res.second)
        }
    }

    fun verifyAndSetAdminRole(passcode: String): Boolean {
        // Client-side passcode elevation is permanently disabled for security.
        // Role changes must be provisioned via authenticated backend/admin channels.
        Log.w("JuktiViewModel", "Client-side passcode role elevation attempt rejected.")
        return false
    }

    fun clearUserProgressData() {
        viewModelScope.launch {
            repository.resetUserProgress()
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            repository.deleteAccount()
            try {
                GoogleAuthManager(getApplication()).signOut()
            } catch (e: Exception) {}
            _currentScreen.value = Screen.AUTH
            _sessionMessage.value = "Your account has been deleted and you have been logged out."
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
            var assignedId = question.id.takeIf { it != 0L } ?: getTrustedTime()
            try {
                val res = repository.addQuestion(question)
                _syncToastMessage.value = res.second
            } catch (e: Exception) {
                Log.e("JuktiViewModel", "Error adding question", e)
                _syncToastMessage.value = "Error: ${e.localizedMessage}"
            } finally {
                onComplete(assignedId)
            }
        }
    }

    fun batchImportQuestionsForMock(
        questionsToInsert: List<QuestionEntity>,
        reusableExistingIds: List<Long>,
        addToQuestionBank: Boolean,
        onComplete: (List<Long>, Int, String) -> Unit
    ) {
        viewModelScope.launch {
            if (!isAdminOrOwner.value) {
                onComplete(emptyList(), 0, "Unauthorized: Only Admin/Owner can batch import questions.")
                return@launch
            }
            try {
                val (newIds, msg) = repository.batchImportMockQuestions(questionsToInsert, addToQuestionBank)
                val allAssignedIds = (reusableExistingIds + newIds).distinct()
                val newQBankCount = if (addToQuestionBank) newIds.size else 0
                _syncToastMessage.value = msg
                onComplete(allAssignedIds, newQBankCount, msg)
            } catch (e: Exception) {
                Log.e("JuktiViewModel", "Error batch importing mock questions", e)
                val errMsg = "Error: ${e.localizedMessage ?: "Unknown error"}"
                _syncToastMessage.value = errMsg
                onComplete(emptyList(), 0, errMsg)
            }
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
            val res = repository.insertPlan(plan)
            _syncToastMessage.value = res.second
            onComplete() 
        } 
    }

    fun deletePlan(plan: PlanEntity) { 
        viewModelScope.launch { 
            val res = repository.deletePlan(plan)
            _syncToastMessage.value = res.second
        } 
    }

    fun reportQuestion(question: QuestionEntity) {
        viewModelScope.launch {
            val res = repository.updateQuestion(question.copy(isReported = true))
            _syncToastMessage.value = res.second
        }
    }
    
    fun resolveReportedQuestion(question: QuestionEntity) {
        viewModelScope.launch {
            val res = repository.updateQuestion(question.copy(isReported = false))
            _syncToastMessage.value = res.second
        }
    }
    
    fun deleteQuestion(question: QuestionEntity) {
        logActivity("Deleted question ID: ${question.id}")
        viewModelScope.launch {
            val res = repository.deleteQuestion(question)
            _syncToastMessage.value = res.second
        }
    }

    fun updateQuestion(question: QuestionEntity) {
        viewModelScope.launch {
            val res = repository.updateQuestion(question)
            _syncToastMessage.value = res.second
        }
    }

    fun updateQuestionAndResolve(question: QuestionEntity) {
        viewModelScope.launch {
            val res = repository.updateQuestion(question.copy(isReported = false))
            _syncToastMessage.value = res.second
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
            notificationManager.notify(getTrustedTime().toInt(), builder.build())
        }
    }

    fun deleteNotification(notification: com.example.data.local.NotificationEntity) {
        viewModelScope.launch {
            repository.deleteNotification(notification)
        }
    }

    fun addExamUpdate(update: ExamUpdateEntity) {
        viewModelScope.launch {
            val res = repository.addExamUpdate(update)
            _syncToastMessage.value = res.second
        }
    }

    fun updateExamUpdate(update: ExamUpdateEntity) {
        viewModelScope.launch {
            val res = repository.updateExamUpdate(update)
            _syncToastMessage.value = res.second
        }
    }

    fun deleteExamUpdate(update: ExamUpdateEntity) {
        viewModelScope.launch {
            val res = repository.deleteExamUpdate(update)
            _syncToastMessage.value = res.second
        }
    }

    fun addBanner(banner: BannerEntity) {
        viewModelScope.launch {
            val res = repository.addBanner(banner)
            _syncToastMessage.value = res.second
        }
    }

    fun updateBanner(banner: BannerEntity) {
        viewModelScope.launch {
            val res = repository.updateBanner(banner)
            _syncToastMessage.value = res.second
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
            repository.updateUserRoleInFirebase(email, "ADMIN")
            logActivity("Promoted $email to ADMIN")
        }
    }

    fun removeAdminEmail(email: String) {
        val actorRole = getCurrentActorRole()
        val targetRole = getUserRole(email)

        if (targetRole == UserRole.OWNER) {
            _syncToastMessage.value = "This account is protected and cannot be deleted or banned."
            return
        }

        if (!canPerformDeleteOrBan(actorRole, targetRole)) {
            _syncToastMessage.value = "You don't have permission to perform this action."
            return
        }

        val trimmed = email.trim().lowercase()
        viewModelScope.launch {
            val currentConfig = aboutConfig.value
            val currentEmails = currentConfig.adminEmails.split(",")
                .map { it.trim().lowercase() }
                .filter { it.isNotEmpty() && it != trimmed }
            val updatedString = currentEmails.joinToString(",")
            repository.updateAboutConfig(currentConfig.copy(adminEmails = updatedString))
            repository.updateUserRoleInFirebase(email, "USER")
            logActivity("Removed ADMIN role from $email")
        }
    }

    fun deleteBanner(banner: BannerEntity) {
        viewModelScope.launch {
            val res = repository.deleteBanner(banner)
            _syncToastMessage.value = res.second
        }
    }

    // Pending Requests & Actions (Delete user, Delete question, Block user, Upgrade plan, Create plan, Delete mock)
    suspend fun toggleUserBlockState(userEmail: String, block: Boolean): Boolean {
        val actorRole = getCurrentActorRole()
        val targetRole = getUserRole(userEmail)

        if (targetRole == UserRole.OWNER) {
            Log.w("JuktiViewModel", "Attempted to block Owner account $userEmail - REJECTED")
            return false
        }

        if (!canPerformDeleteOrBan(actorRole, targetRole)) {
            Log.w("JuktiViewModel", "Unauthorized block attempt by $actorRole on $targetRole ($userEmail) - REJECTED")
            return false
        }

        return try {
            val newRole = if (block) "BLOCKED" else "USER"
            val success = repository.updateUserRoleInFirebase(userEmail, newRole)
            if (success) {
                logActivity("${if (block) "Blocked" else "Unblocked"} user $userEmail")
            }
            success
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteUserCompletely(userEmail: String, explicitUserId: String? = null): Boolean {
        val users = repository.fetchAllUsersDirect()
        val targetUser = users.find { it.email.equals(userEmail, ignoreCase = true) }
        val profile = repository.getUserProfileDirect()
        val actorRole = getUserRoleDirect(profile?.email ?: "", profile?.role)
        val targetRole = getUserRoleDirect(userEmail, targetUser?.role)
        
        Log.d("JuktiViewModel", "deleteUserCompletely: actorRole=$actorRole, targetRole=$targetRole, targetEmail=$userEmail")

        if (targetRole == UserRole.OWNER) {
            Log.w("JuktiViewModel", "Attempted to delete Owner account $userEmail - REJECTED")
            return false
        }

        if (!canPerformDeleteOrBan(actorRole, targetRole)) {
            Log.w("JuktiViewModel", "Unauthorized delete attempt by $actorRole on $targetRole ($userEmail) - REJECTED")
            return false
        }

        return try {
            var uid = explicitUserId ?: ""
            if (uid.isBlank()) {
                uid = targetUser?.uid ?: ""
            }
            val sanitizedEmail = userEmail.trim().lowercase().replace("@", "_at_").replace(".", "_dot_")
            if (uid.isBlank()) {
                uid = sanitizedEmail
            }

            Log.d("JuktiViewModel", "Executing direct user deletion for UID: $uid, email: $userEmail")
            repository.deleteUserAccount(uid, userEmail)
            
            logActivity("Deleted user $userEmail completely from app and Firestore")
            true
        } catch (e: Exception) {
            Log.e("JuktiViewModel", "Error deleting user completely", e)
            false
        }
    }

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

    fun requestOrDeleteUser(userId: String, userName: String, userEmail: String, targetRoleInProfile: String? = null, onResult: (Boolean, String) -> Unit) {
        val actorRole = getCurrentActorRole()
        val targetRole = getUserRole(userEmail, targetRoleInProfile)

        if (targetRole == UserRole.OWNER) {
            onResult(false, "This account is protected and cannot be deleted or banned.")
            return
        }

        if (!canPerformDeleteOrBan(actorRole, targetRole)) {
            onResult(false, "You don't have permission to perform this action.")
            return
        }

        viewModelScope.launch {
            val success = deleteUserCompletely(userEmail, userId)
            if (success) {
                onResult(true, "User $userName deleted successfully.")
            } else {
                onResult(false, "Unable to delete the user. Please try again.")
            }
        }
    }

    fun requestOrBlockUser(userId: String, userName: String, userEmail: String, targetRoleInProfile: String? = null, onResult: (Boolean, String) -> Unit) {
        val actorRole = getCurrentActorRole()
        val targetRole = getUserRole(userEmail, targetRoleInProfile)

        if (targetRole == UserRole.OWNER) {
            onResult(false, "This account is protected and cannot be deleted or banned.")
            return
        }

        if (!canPerformDeleteOrBan(actorRole, targetRole)) {
            onResult(false, "You don't have permission to perform this action.")
            return
        }

        viewModelScope.launch {
            val success = toggleUserBlockState(userEmail, true)
            if (success) {
                onResult(true, "User $userName blocked successfully.")
            } else {
                onResult(false, "Unable to save your changes. Please try again.")
            }
        }
    }

    fun requestOrCreatePlan(plan: PlanEntity, onResult: (Boolean, String) -> Unit) {
        if (isAdminOrOwner.value) {
            viewModelScope.launch {
                try {
                    repository.insertPlan(plan)
                    onResult(true, "Plan created successfully.")
                } catch (e: Exception) {
                    android.util.Log.e("JuktiViewModel", "Error creating plan", e)
                    onResult(false, e.localizedMessage ?: "Failed to create plan in Firebase/Database.")
                }
            }
        } else {
            viewModelScope.launch {
                try {
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
                    onResult(true, "Sent a request to Owner Dashboard to approve or reject.")
                } catch (e: Exception) {
                    android.util.Log.e("JuktiViewModel", "Error submitting plan request", e)
                    onResult(false, e.localizedMessage ?: "Failed to submit plan request.")
                }
            }
        }
    }

    fun requestOrUpgradePlan(
        userId: String,
        userName: String,
        userEmail: String,
        newPlanName: String,
        validity: String,
        validityType: String = "",
        validityValue: Int = 0,
        isLifetime: Boolean = false,
        explicitValidUntil: Long = 0L,
        onResult: (Boolean, String) -> Unit
    ) {
        val canDirectlyAssign = isAdminOrOwner.value
        if (canDirectlyAssign) {
            viewModelScope.launch {
                val success = grantPlanToUser(
                    email = userEmail,
                    planName = newPlanName,
                    validity = validity,
                    validityType = validityType,
                    validityValue = validityValue,
                    isLifetime = isLifetime,
                    explicitValidUntil = explicitValidUntil,
                    source = "ADMIN_ASSIGNED"
                )
                if (success) {
                    logActivity("Assigned plan $newPlanName ($validity) to $userEmail")
                    onResult(true, "Plan updated successfully for $userName.")
                } else {
                    onResult(false, "Failed to update plan.")
                }
            }
        } else {
            viewModelScope.launch {
                val req = PendingRequestEntity(
                    requestType = "UPGRADE_PLAN",
                    title = "Upgrade Plan for $userName",
                    description = "Request to upgrade user ($userEmail) to $newPlanName ($validity)",
                    targetId = userId,
                    payloadJson = "$newPlanName|$validity|$userEmail|$validityType|$validityValue|$isLifetime|$explicitValidUntil",
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
        if (request.status != "PENDING") return
        viewModelScope.launch {
            val db = com.example.JuktiApplication.getFirestore(getApplication()) ?: return@launch
            val docId = request.id.toString()
            val docRef = db.collection("pending_requests").document(docId)
            
            var success = false
            try {
                // Atomic transaction: verify status == PENDING and transition to PROCESSING
                db.runTransaction { transaction ->
                    val snapshot = transaction.get(docRef)
                    if (snapshot.exists()) {
                        val currentStatus = snapshot.getString("status") ?: "PENDING"
                        if (currentStatus != "PENDING") {
                            throw com.google.firebase.firestore.FirebaseFirestoreException(
                                "Request is already processed or processing by another owner",
                                com.google.firebase.firestore.FirebaseFirestoreException.Code.ABORTED
                            )
                        }
                    }
                    transaction.update(docRef, mapOf("status" to "PROCESSING"))
                }.await()
                
                // Update local status
                val processingRequest = request.copy(status = "PROCESSING")
                repository.updatePendingRequest(processingRequest)
                
                // Execute actual operation
                var executeSuccess = true
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
                    "UPGRADE_PLAN" -> {
                        val parts = request.payloadJson.split("|")
                        val planName = parts.getOrNull(0) ?: ""
                        val validity = parts.getOrNull(1) ?: "1 year"
                        val userEmail = parts.getOrNull(2) ?: ""
                        val validityType = parts.getOrNull(3) ?: ""
                        val validityValue = parts.getOrNull(4)?.toIntOrNull() ?: 0
                        val isLifetime = parts.getOrNull(5)?.toBooleanStrictOrNull() ?: false
                        val explicitValidUntil = parts.getOrNull(6)?.toLongOrNull() ?: 0L
                        if (planName.isNotEmpty() && userEmail.isNotEmpty()) {
                            executeSuccess = grantPlanToUser(
                                email = userEmail,
                                planName = planName,
                                validity = validity,
                                validityType = validityType,
                                validityValue = validityValue,
                                isLifetime = isLifetime,
                                explicitValidUntil = explicitValidUntil,
                                source = "OWNER_ASSIGNED"
                            )
                        } else {
                            executeSuccess = false
                        }
                    }
                    "BLOCK_USER" -> {
                        val parts = request.payloadJson.split("|")
                        val userEmail = parts.getOrNull(0) ?: ""
                        if (userEmail.isNotEmpty()) {
                            executeSuccess = toggleUserBlockState(userEmail, true)
                        }
                    }
                    "DELETE_USER" -> {
                        val parts = request.payloadJson.split("|")
                        val userEmail = parts.getOrNull(0) ?: ""
                        if (userEmail.isNotEmpty()) {
                            executeSuccess = deleteUserCompletely(userEmail)
                        }
                    }
                }
                
                val finalStatus = if (executeSuccess) "APPROVED" else "FAILED"
                val finalReq = request.copy(status = finalStatus)
                repository.updatePendingRequest(finalReq)
                
                db.collection("pending_requests").document(docId).update(mapOf("status" to finalStatus)).await()
                logActivity("${if (executeSuccess) "Approved" else "Failed to execute"} request: ${request.title}")
                success = executeSuccess
                
            } catch (e: Exception) {
                android.util.Log.e("JuktiViewModel", "Atomic approval transaction failed", e)
                success = false
            }
        }
    }

    fun verifyAndProvisionPurchase(
        purchaseToken: String,
        purchaseId: String,
        planId: String,
        planName: String,
        validity: String = "1 year",
        validityType: String = "MONTHS",
        validityValue: Int = 1,
        isLifetime: Boolean = false,
        productId: String = ""
    ) {
        viewModelScope.launch {
            val user = userProfile.value ?: return@launch
            val email = user.email.trim().lowercase(java.util.Locale.ROOT)
            if (email.isBlank()) return@launch
            val uid = email.replace("@", "_at_").replace(".", "_dot_")
            
            val now = getTrustedTime()
            
            val requestData = mapOf(
                "purchaseId" to purchaseId,
                "purchaseToken" to purchaseToken,
                "planId" to planId,
                "planName" to planName,
                "productId" to productId,
                "packageName" to "com.aistudio.jukti.examprep.app",
                "validity" to validity,
                "validityType" to validityType,
                "validityValue" to validityValue,
                "isLifetime" to isLifetime,
                "userEmail" to email,
                "status" to "PENDING_VERIFICATION",
                "createdAt" to now,
                "updatedAt" to now
            )
            
            try {
                val db = com.example.JuktiApplication.getFirestore(getApplication()) ?: return@launch
                val reqRef = db.collection("users").document(uid).collection("purchaseRequests").document(purchaseId)
                
                reqRef.set(requestData, com.google.firebase.firestore.SetOptions.merge()).await()
                android.util.Log.d("JuktiViewModel", "Submitted purchase verification request for $purchaseId")

                // Pre-activate locally or sync from server
                grantPlanToUser(
                    email = email,
                    planName = planName,
                    validity = validity,
                    validityType = validityType,
                    validityValue = validityValue,
                    isLifetime = isLifetime,
                    source = "GOOGLE_PLAY"
                )

                // Sync local profile from server to pick up any server-verified entitlements
                repository.syncUserProfileWithFirebase(email)
                
            } catch (e: Exception) {
                android.util.Log.e("JuktiViewModel", "Error submitting purchase verification request", e)
            }
        }
    }
    
    suspend fun grantPlanToUser(
        email: String,
        planName: String,
        validity: String = "1 year",
        validityType: String = "",
        validityValue: Int = 0,
        isLifetime: Boolean = false,
        explicitValidUntil: Long = 0L,
        source: String = "OWNER_ASSIGNED"
    ): Boolean {
        return try {
            val trimmedEmail = email.trim().lowercase(java.util.Locale.ROOT)
            if (trimmedEmail.isBlank()) return false
            val now = getTrustedTime()

            val vType = if (validityType.isNotBlank()) validityType else com.example.data.util.PlanValidityEngine.inferValidityType(validity)
            val vVal = if (validityValue > 0) validityValue else com.example.data.util.PlanValidityEngine.inferValidityValue(validity)
            val isLife = isLifetime || vType == "LIFETIME" || validity.equals("Lifetime", ignoreCase = true) || planName.contains("Lifetime", ignoreCase = true) || planName.contains("Free Plan", ignoreCase = true)
            val finalValidityLabel = if (isLife) "Lifetime" else if (validity.isNotBlank() && !validity.equals("Custom", ignoreCase = true)) validity else com.example.data.util.PlanValidityEngine.formatValidityLabel(vType, vVal)

            val targetValidUntil: Long = if (explicitValidUntil > 0L) {
                explicitValidUntil
            } else {
                com.example.data.util.PlanValidityEngine.calculateExpiryTimestamp(
                    activationTime = now,
                    validityType = vType,
                    validityValue = vVal,
                    isLifetime = isLife
                )
            }

            val actorEmail = userProfile.value?.email ?: "OWNER"
            val sanitizedEmailDocId = com.example.data.repository.FirebaseRepository().getSanitizedUserDocId(trimmedEmail)
            val planId = planName.lowercase(java.util.Locale.ROOT).replace(" ", "_")

            // 1. Save directly to Firebase (both entitlements subdoc, user document, and entitlement_history)
            repository.saveUserEntitlementToFirebase(
                email = trimmedEmail,
                planName = planName,
                validUntil = targetValidUntil,
                validFrom = now,
                validity = finalValidityLabel,
                validityType = vType,
                validityValue = vVal,
                isLifetime = isLife,
                assignedBy = actorEmail,
                source = source,
                purchaseId = "ASSIGNMENT_${now}"
            )

            // 2. Insert into local Room database for entitlement & history
            val newEntitlement = EntitlementEntity(
                userId = sanitizedEmailDocId,
                planId = planId,
                planName = planName,
                status = if (isLife || targetValidUntil > now) "ACTIVE" else "EXPIRED",
                validFrom = now,
                validUntil = targetValidUntil,
                validityType = vType,
                validityValue = vVal,
                validityLabel = finalValidityLabel,
                isLifetime = isLife,
                benefits = "All Premium MCQs, Mock Tests, Notes, Analytics",
                source = source,
                purchaseId = "ASSIGNMENT_${now}",
                activatedAt = now,
                updatedAt = now
            )
            repository.insertEntitlement(newEntitlement)

            val historyEntity = EntitlementHistoryEntity(
                userId = sanitizedEmailDocId,
                userEmail = trimmedEmail,
                eventType = if (source == "GOOGLE_PLAY") "PURCHASED" else if (planName.equals("Free Plan", ignoreCase = true)) "FREE_PLAN_ASSIGNED" else "MANUALLY_ASSIGNED",
                newPlan = planName,
                newExpiry = targetValidUntil,
                validityGranted = finalValidityLabel,
                validityType = vType,
                validityValue = vVal,
                isLifetime = isLife,
                source = source,
                actor = actorEmail,
                timestamp = now
            )
            repository.insertEntitlementHistory(historyEntity)

            // 3. If target user is the currently logged in user, update StateFlows and local profile
            val currentProf = userProfile.value
            if (currentProf != null && currentProf.email.equals(trimmedEmail, ignoreCase = true)) {
                _userEntitlements.value = _userEntitlements.value + newEntitlement
                val updatedProf = currentProf.copy(isPremium = (newEntitlement.status == "ACTIVE" && !planName.equals("Free Plan", ignoreCase = true)))
                repository.updateUserProfile(updatedProf)
                // Download premium data since entitlement changed
                refreshDataFromFirebase()
            }

            // 4. Optionally invoke Cloud Function if present
            try {
                val durationMs = if (targetValidUntil <= 0L) 100L * 365 * 24 * 60 * 60 * 1000L else (targetValidUntil - now).coerceAtLeast(0L)
                val data = mapOf(
                    "targetEmail" to trimmedEmail,
                    "planName" to planName,
                    "durationMs" to durationMs
                )
                val functions = com.google.firebase.functions.FirebaseFunctions.getInstance()
                functions.getHttpsCallable("grantPlanToUser").call(data).await()
            } catch (e: Exception) {
                // Cloud Function is optional, direct Firestore write is already complete
            }

            true
        } catch (e: Exception) {
            android.util.Log.e("JuktiViewModel", "Error granting plan to user", e)
            false
        }
    }

    private fun escapeCsv(value: Any?): String {
        if (value == null) return ""
        val str = value.toString()
        if (str.contains(",") || str.contains("\"") || str.contains("\n") || str.contains("\r")) {
            return "\"" + str.replace("\"", "\"\"") + "\""
        }
        return str
    }

    private fun formatDate(timestamp: Long?): String {
        if (timestamp == null || timestamp <= 0L) return "N/A"
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'IST'", java.util.Locale.US)
        sdf.timeZone = java.util.TimeZone.getTimeZone("Asia/Kolkata")
        return sdf.format(java.util.Date(timestamp))
    }

    private fun showToastOnMain(context: android.content.Context, message: String) {
        launchOnMain {
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    fun exportUsersCsv(context: android.content.Context) {
        if (!isAdminOrOwner.value) {
            showToastOnMain(context, "Access Denied: Owner/Admin privilege required.")
            return
        }
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                showToastOnMain(context, "Generating User Report...")
                val db = com.example.JuktiApplication.getFirestore(context) ?: return@launch

                val usersSnap = db.collection("users").get().await()
                val entitlementsSnap = db.collectionGroup("entitlements").get().await()
                val purchasesSnap = db.collectionGroup("purchases").get().await()
                val mockAttemptsSnap = try { db.collectionGroup("mock_attempts").get().await() } catch (e: Exception) { null }

                val entitlementsByUser = mutableMapOf<String, MutableList<Map<String, Any>>>()
                for (doc in entitlementsSnap.documents) {
                    val parentId = doc.reference.parent.parent?.id ?: doc.getString("userId") ?: ""
                    val userId = doc.getString("userId")?.ifBlank { parentId } ?: parentId
                    if (userId.isNotBlank()) {
                        entitlementsByUser.getOrPut(userId) { mutableListOf() }.add(doc.data ?: emptyMap())
                    }
                }

                val purchaseCountByUser = mutableMapOf<String, Int>()
                for (doc in purchasesSnap.documents) {
                    val parentId = doc.reference.parent.parent?.id ?: doc.getString("userId") ?: ""
                    val userId = doc.getString("userId")?.ifBlank { parentId } ?: parentId
                    if (userId.isNotBlank()) {
                        purchaseCountByUser[userId] = (purchaseCountByUser[userId] ?: 0) + 1
                    }
                }

                val mockAttemptsByUser = mutableMapOf<String, MutableList<Double>>()
                if (mockAttemptsSnap != null) {
                    for (doc in mockAttemptsSnap.documents) {
                        val parentId = doc.reference.parent.parent?.id ?: doc.getString("userId") ?: ""
                        val userId = doc.getString("userId")?.ifBlank { parentId } ?: parentId
                        val score = doc.getDouble("scorePercentage") ?: doc.getDouble("score") ?: 0.0
                        if (userId.isNotBlank()) {
                            mockAttemptsByUser.getOrPut(userId) { mutableListOf() }.add(score)
                        }
                    }
                }

                val csv = StringBuilder()
                csv.append("User ID,Email,Name,Role,Account Status,Registration Date,Last Login,Last Active,Active Plan(s),Target Exam(s),Entitlement Status,Plan Start Date,Plan Expiry Date,Total Purchases,Total Questions Attempted,Correct Questions,Incorrect Questions,Accuracy %,Mock Tests Completed,Average Mock Score,Saved Questions Count,Incorrect Questions Count,XP,Level,Last Activity,IsPremium\n")

                val now = System.currentTimeMillis()
                for (doc in usersSnap.documents) {
                    val uid = doc.id
                    val email = doc.getString("email") ?: ""
                    val pName = doc.getString("profileName") ?: ""
                    val rName = doc.getString("registrationName") ?: ""
                    val gName = doc.getString("googleName") ?: ""
                    val legacyName = doc.getString("name") ?: ""
                    val name = when {
                        pName.isNotBlank() && !pName.equals("Guest User", ignoreCase = true) && !pName.equals("Assam Scholar", ignoreCase = true) -> pName
                        rName.isNotBlank() && !rName.equals("Guest User", ignoreCase = true) && !rName.equals("Assam Scholar", ignoreCase = true) -> rName
                        gName.isNotBlank() && !gName.equals("Guest User", ignoreCase = true) && !gName.equals("Assam Scholar", ignoreCase = true) -> gName
                        legacyName.isNotBlank() && !legacyName.equals("Guest User", ignoreCase = true) && !legacyName.equals("Assam Scholar", ignoreCase = true) -> legacyName
                        email.contains("@") -> email.substringBefore("@")
                        else -> "User"
                    }

                    val role = doc.getString("role") ?: "USER"
                    val isBlocked = doc.getBoolean("isBlocked") ?: false
                    val accStatusStr = doc.getString("accountStatus") ?: ""
                    val accountStatus = if (isBlocked || accStatusStr.equals("BLOCKED", ignoreCase = true)) "BLOCKED" else "ACTIVE"

                    val joinedDate = doc.getString("joinedDate") ?: formatDate(doc.getLong("createdAt") ?: doc.getLong("joinedAt"))
                    val lastLogin = doc.getString("lastLogin") ?: formatDate(doc.getLong("lastLoginAt"))
                    val lastActive = doc.getString("lastActive") ?: formatDate(doc.getLong("lastActiveAt"))

                    val userEnts = entitlementsByUser[uid] ?: emptyList()
                    val activePlansList = userEnts.filter { ent ->
                        val status = ent["status"] as? String ?: ""
                        val validUntil = (ent["validUntil"] as? Number)?.toLong() ?: Long.MAX_VALUE
                        val isLifetime = ent["isLifetime"] as? Boolean ?: false
                        status.equals("ACTIVE", ignoreCase = true) && (isLifetime || validUntil > now)
                    }

                    val activePlanNames = if (activePlansList.isNotEmpty()) {
                        activePlansList.mapNotNull { it["planName"] as? String }.distinct().joinToString(", ")
                    } else {
                        "Free Plan"
                    }

                    val targetExam = doc.getString("examGoal") ?: doc.getString("targetExam") ?: "N/A"
                    val entitlementStatus = if (activePlansList.isNotEmpty()) "ACTIVE" else "FREE"

                    val primaryActive = activePlansList.firstOrNull()
                    val planStartDate = formatDate((primaryActive?.get("validFrom") as? Number)?.toLong())
                    val planExpiryDate = if (primaryActive?.get("isLifetime") == true) "Lifetime" else formatDate((primaryActive?.get("validUntil") as? Number)?.toLong())

                    val totalPurchases = purchaseCountByUser[uid] ?: 0

                    val totalSolved = (doc.getLong("totalSolved") ?: 0L).toInt()
                    val correctCount = (doc.getLong("correctCount") ?: 0L).toInt()
                    val incorrectQuestions = maxOf(0, totalSolved - correctCount)
                    val accuracyStr = if (totalSolved > 0) String.format(java.util.Locale.US, "%.2f%%", correctCount * 100.0 / totalSolved) else "0.00%"

                    val userMocks = mockAttemptsByUser[uid] ?: emptyList()
                    val mocksCompleted = userMocks.size
                    val avgMockScoreStr = if (userMocks.isNotEmpty()) String.format(java.util.Locale.US, "%.1f%%", userMocks.average()) else "N/A"

                    val savedCount = doc.getLong("savedQuestionsCount")?.toString() ?: "N/A"
                    val incorrectCount = maxOf(0, totalSolved - correctCount)

                    val xp = (doc.getLong("xp") ?: 0L).toInt()
                    val level = (doc.getLong("level") ?: 1L).toInt()
                    val lastActivity = doc.getString("lastActivity") ?: lastActive
                    val isPremiumStr = if (doc.getBoolean("isPremium") == true) "Yes" else "No"

                    csv.append("${escapeCsv(uid)},${escapeCsv(email)},${escapeCsv(name)},${escapeCsv(role)},${escapeCsv(accountStatus)},${escapeCsv(joinedDate)},${escapeCsv(lastLogin)},${escapeCsv(lastActive)},${escapeCsv(activePlanNames)},${escapeCsv(targetExam)},${escapeCsv(entitlementStatus)},${escapeCsv(planStartDate)},${escapeCsv(planExpiryDate)},$totalPurchases,$totalSolved,$correctCount,$incorrectQuestions,${escapeCsv(accuracyStr)},$mocksCompleted,${escapeCsv(avgMockScoreStr)},${escapeCsv(savedCount)},$incorrectCount,$xp,$level,${escapeCsv(lastActivity)},${escapeCsv(isPremiumStr)}\n")
                }

                saveCsvToFile(context, "jukti_user_report.csv", csv.toString())
            } catch (e: Exception) {
                android.util.Log.e("JuktiViewModel", "Export users failed", e)
                showToastOnMain(context, "Export failed: ${e.localizedMessage}")
            }
        }
    }

    fun exportPurchasesCsv(context: android.content.Context) {
        if (!isAdminOrOwner.value) {
            showToastOnMain(context, "Access Denied: Owner/Admin privilege required.")
            return
        }
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                showToastOnMain(context, "Generating Purchase Report...")
                val db = com.example.JuktiApplication.getFirestore(context) ?: return@launch

                val purchasesSnap = db.collectionGroup("purchases").get().await()
                val usersSnap = db.collection("users").get().await()
                val userMap = usersSnap.documents.associateBy { it.id }

                val csv = StringBuilder()
                csv.append("Purchase ID,User ID,Email,Name,Plan ID,Plan Name,Google Play Product ID,Order ID / Transaction ID,Purchase Token,Purchase Type,Purchase Status,Verification Status,Purchase Date & Time,Activation Date & Time,Expiry Date & Time,Cancellation Date,Revocation Date,Amount,Currency,Target Exam(s),Entitlement Granted,Entitlement Status,Created At,Updated At\n")

                for (doc in purchasesSnap.documents) {
                    val pid = doc.getString("purchaseId") ?: doc.id
                    val uid = doc.getString("userId") ?: doc.reference.parent.parent?.id ?: ""
                    val userDoc = userMap[uid]
                    val email = doc.getString("userEmail") ?: doc.getString("email") ?: userDoc?.getString("email") ?: ""
                    val name = doc.getString("userName") ?: doc.getString("name") ?: userDoc?.getString("profileName") ?: "User"

                    val planId = doc.getString("planId") ?: ""
                    val planName = doc.getString("planName") ?: ""
                    val productId = doc.getString("productId") ?: doc.getString("googlePlayProductId") ?: ""
                    val orderId = doc.getString("orderId") ?: doc.getString("transactionId") ?: "N/A"

                    val rawToken = doc.getString("purchaseToken") ?: ""
                    val maskedToken = if (rawToken.length > 12) {
                        rawToken.take(6) + "..." + rawToken.takeLast(4)
                    } else if (rawToken.isNotBlank()) {
                        "pTok_masked"
                    } else {
                        "N/A"
                    }

                    val pType = doc.getString("purchaseType") ?: "SUBSCRIPTION"
                    val status = doc.getString("status") ?: "ACTIVE"
                    val verStatus = doc.getString("verificationStatus") ?: "VERIFIED"

                    val purchaseTime = formatDate(doc.getLong("purchaseTime") ?: doc.getLong("timestamp"))
                    val activatedAt = formatDate(doc.getLong("activatedAt") ?: doc.getLong("validFrom"))
                    val isLifetime = doc.getBoolean("isLifetime") ?: false
                    val expiryTime = if (isLifetime) "Lifetime" else formatDate(doc.getLong("expiryTime") ?: doc.getLong("validUntil"))
                    val cancelledAt = formatDate(doc.getLong("cancelledAt"))
                    val revokedAt = formatDate(doc.getLong("revokedAt"))

                    val amountVal = doc.getDouble("amount") ?: doc.getLong("amount")?.toDouble() ?: 0.0
                    val amountStr = String.format(java.util.Locale.US, "%.2f", amountVal)
                    val currency = doc.getString("currency") ?: "INR"

                    val targetExam = doc.getString("targetExam") ?: doc.getString("examGoal") ?: userDoc?.getString("examGoal") ?: "N/A"
                    val entitlementGranted = doc.getString("entitlementGranted") ?: planName
                    val entStatus = doc.getString("entitlementStatus") ?: status

                    val createdAt = formatDate(doc.getLong("createdAt") ?: doc.getLong("timestamp"))
                    val updatedAt = formatDate(doc.getLong("updatedAt") ?: doc.getLong("timestamp"))

                    csv.append("${escapeCsv(pid)},${escapeCsv(uid)},${escapeCsv(email)},${escapeCsv(name)},${escapeCsv(planId)},${escapeCsv(planName)},${escapeCsv(productId)},${escapeCsv(orderId)},${escapeCsv(maskedToken)},${escapeCsv(pType)},${escapeCsv(status)},${escapeCsv(verStatus)},${escapeCsv(purchaseTime)},${escapeCsv(activatedAt)},${escapeCsv(expiryTime)},${escapeCsv(cancelledAt)},${escapeCsv(revokedAt)},${escapeCsv(amountStr)},${escapeCsv(currency)},${escapeCsv(targetExam)},${escapeCsv(entitlementGranted)},${escapeCsv(entStatus)},${escapeCsv(createdAt)},${escapeCsv(updatedAt)}\n")
                }

                saveCsvToFile(context, "jukti_purchase_report.csv", csv.toString())
            } catch (e: Exception) {
                android.util.Log.e("JuktiViewModel", "Export purchases failed", e)
                showToastOnMain(context, "Export failed: ${e.localizedMessage}")
            }
        }
    }

    fun exportRefundsCsv(context: android.content.Context) {
        if (!isAdminOrOwner.value) {
            showToastOnMain(context, "Access Denied: Owner/Admin privilege required.")
            return
        }
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                showToastOnMain(context, "Generating Refund Report...")
                val db = com.example.JuktiApplication.getFirestore(context) ?: return@launch

                val historySnap = db.collectionGroup("entitlement_history").get().await()
                val purchasesSnap = db.collectionGroup("purchases").get().await()

                val refundDocs = historySnap.documents.filter { doc ->
                    val eventType = doc.getString("eventType")?.uppercase(java.util.Locale.ROOT) ?: ""
                    val status = doc.getString("status")?.uppercase(java.util.Locale.ROOT) ?: ""
                    eventType in listOf("REVOKED", "REFUNDED", "CANCELLED") || status in listOf("REFUNDED", "REVOKED")
                }.toMutableList()

                for (pDoc in purchasesSnap.documents) {
                    val status = pDoc.getString("status")?.uppercase(java.util.Locale.ROOT) ?: ""
                    if (status == "REFUNDED" || status == "REVOKED") {
                        if (refundDocs.none { it.id == pDoc.id || it.getString("purchaseId") == pDoc.getString("purchaseId") }) {
                            refundDocs.add(pDoc)
                        }
                    }
                }

                val csv = StringBuilder()
                csv.append("Refund ID,Purchase ID,User ID,Email,Plan ID,Plan Name,Original Purchase Date,Original Purchase Amount,Currency,Refund Amount,Refund Date,Refund Status,Refund/Revocation Reason,Entitlement Revoked,Entitlement Revoked Date,Days Used Before Refund,Created At,Updated At\n")

                for (doc in refundDocs) {
                    val refundId = doc.id
                    val pid = doc.getString("purchaseId") ?: doc.id
                    val uid = doc.getString("userId") ?: doc.reference.parent.parent?.id ?: ""
                    val email = doc.getString("userEmail") ?: doc.getString("email") ?: ""

                    val planId = doc.getString("planId") ?: ""
                    val planName = doc.getString("planName") ?: doc.getString("newPlan") ?: doc.getString("previousPlan") ?: ""

                    val origPurchaseTime = doc.getLong("originalPurchaseDate") ?: doc.getLong("validFrom") ?: doc.getLong("timestamp") ?: 0L
                    val origPurchaseDateStr = formatDate(origPurchaseTime)

                    val amountVal = doc.getDouble("originalAmount") ?: doc.getDouble("amount") ?: doc.getLong("amount")?.toDouble() ?: 0.0
                    val origAmountStr = String.format(java.util.Locale.US, "%.2f", amountVal)
                    val currency = doc.getString("currency") ?: "INR"

                    val refundAmountVal = doc.getDouble("refundAmount") ?: amountVal
                    val refundAmountStr = String.format(java.util.Locale.US, "%.2f", refundAmountVal)

                    val refundTime = doc.getLong("refundDate") ?: doc.getLong("revokedAt") ?: doc.getLong("timestamp") ?: 0L
                    val refundDateStr = formatDate(refundTime)

                    val status = doc.getString("status") ?: doc.getString("eventType") ?: "REFUNDED"
                    val reason = doc.getString("reason") ?: doc.getString("revocationReason") ?: "User Refund / Admin Revocation"
                    val entRevoked = "Yes"
                    val entRevokedDate = formatDate(doc.getLong("revokedAt") ?: refundTime)

                    val daysUsed = if (origPurchaseTime > 0L && refundTime > origPurchaseTime) {
                        ((refundTime - origPurchaseTime) / 86400000L).coerceAtLeast(0L)
                    } else {
                        0L
                    }

                    val createdAt = formatDate(doc.getLong("createdAt") ?: doc.getLong("timestamp"))
                    val updatedAt = formatDate(doc.getLong("updatedAt") ?: doc.getLong("timestamp"))

                    csv.append("${escapeCsv(refundId)},${escapeCsv(pid)},${escapeCsv(uid)},${escapeCsv(email)},${escapeCsv(planId)},${escapeCsv(planName)},${escapeCsv(origPurchaseDateStr)},${escapeCsv(origAmountStr)},${escapeCsv(currency)},${escapeCsv(refundAmountStr)},${escapeCsv(refundDateStr)},${escapeCsv(status)},${escapeCsv(reason)},${escapeCsv(entRevoked)},${escapeCsv(entRevokedDate)},$daysUsed,${escapeCsv(createdAt)},${escapeCsv(updatedAt)}\n")
                }

                saveCsvToFile(context, "jukti_refund_report.csv", csv.toString())
            } catch (e: Exception) {
                android.util.Log.e("JuktiViewModel", "Export refunds failed", e)
                showToastOnMain(context, "Export failed: ${e.localizedMessage}")
            }
        }
    }

    fun exportFullQuestionBankCsv(context: android.content.Context) {
        if (!isAdminOrOwner.value) {
            showToastOnMain(context, "Access Denied: Owner/Admin privilege required.")
            return
        }
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                showToastOnMain(context, "Generating Full Question Bank Report...")
                val allQuestions = repository.getAllQuestionsForAdmin()

                val csv = StringBuilder()
                csv.append("Question ID,Target Exam,Subject,Chapter,Topic,Category,Difficulty,Access Type,Question (English),Question (Assamese),Option A (English),Option A (Assamese),Option B (English),Option B (Assamese),Option C (English),Option C (Assamese),Option D (English),Option D (Assamese),Option E,Correct Answer,Explanation (English),Explanation (Assamese),Solution,Image/Attachment Reference,Question Status,Created At,Updated At\n")

                for (q in allQuestions) {
                    val qId = q.id
                    val examCat = q.examCategory
                    val subject = q.subject
                    val chapter = q.topic
                    val topic = q.topic
                    val category = q.examCategory
                    val difficulty = q.difficulty
                    val accessType = q.accessType

                    val qEn = q.questionEn
                    val qAs = q.questionAs

                    val optAEn = q.optionAEn
                    val optAAs = q.optionAAs
                    val optBEn = q.optionBEn
                    val optBAs = q.optionBAs
                    val optCEn = q.optionCEn
                    val optCAs = q.optionCAs
                    val optDEn = q.optionDEn
                    val optDAs = q.optionDAs
                    val optE = "N/A"

                    val correctAns = when (q.correctOptionIndex) {
                        0 -> "A"
                        1 -> "B"
                        2 -> "C"
                        3 -> "D"
                        else -> "A"
                    }

                    val expEn = q.explanationEn
                    val expAs = q.explanationAs
                    val solution = q.explanationEn
                    val imageRef = "N/A"
                    val status = if (q.isReported) "REPORTED" else q.status.ifBlank { "ACTIVE" }

                    val createdAt = formatDate(q.cachedAt)
                    val updatedAt = formatDate(q.updatedAt)

                    csv.append("${qId},${escapeCsv(examCat)},${escapeCsv(subject)},${escapeCsv(chapter)},${escapeCsv(topic)},${escapeCsv(category)},${escapeCsv(difficulty)},${escapeCsv(accessType)},${escapeCsv(qEn)},${escapeCsv(qAs)},${escapeCsv(optAEn)},${escapeCsv(optAAs)},${escapeCsv(optBEn)},${escapeCsv(optBAs)},${escapeCsv(optCEn)},${escapeCsv(optCAs)},${escapeCsv(optDEn)},${escapeCsv(optDAs)},${escapeCsv(optE)},${escapeCsv(correctAns)},${escapeCsv(expEn)},${escapeCsv(expAs)},${escapeCsv(solution)},${escapeCsv(imageRef)},${escapeCsv(status)},${escapeCsv(createdAt)},${escapeCsv(updatedAt)}\n")
                }

                saveCsvToFile(context, "jukti_full_question_bank_report.csv", csv.toString())
            } catch (e: Exception) {
                android.util.Log.e("JuktiViewModel", "Export full question bank failed", e)
                showToastOnMain(context, "Export failed: ${e.localizedMessage}")
            }
        }
    }

    fun exportMockTestQuestionsCsv(context: android.content.Context) {
        if (!isAdminOrOwner.value) {
            showToastOnMain(context, "Access Denied: Owner/Admin privilege required.")
            return
        }
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                showToastOnMain(context, "Generating Mock Test-wise Questions Report...")
                val mocks = repository.allMockTests.firstOrNull() ?: emptyList()
                val allQuestions = repository.getAllQuestionsForAdmin()
                val questionMap = allQuestions.associateBy { it.id }

                val csv = StringBuilder()
                csv.append("Mock ID,Mock Title (English),Mock Title (Assamese),Target Exam,Category,Description,Total Questions,Duration (Mins),Maximum Marks,Negative Marking,Passing Marks,Access Type,Mock Status,Created At,Updated At,Question Order,Question ID,Subject,Chapter,Topic,Difficulty,Question (English),Question (Assamese),Option A (English),Option A (Assamese),Option B (English),Option B (Assamese),Option C (English),Option C (Assamese),Option D (English),Option D (Assamese),Option E,Correct Answer,Explanation (English),Explanation (Assamese),Solution,Image/Attachment Reference\n")

                for (mock in mocks) {
                    val mockId = mock.id
                    val titleEn = mock.titleEn
                    val titleAs = mock.titleAs
                    val targetExam = mock.category
                    val category = mock.category
                    val description = mock.subjectOrChapter
                    val totalQs = mock.totalQuestions
                    val duration = mock.durationMinutes
                    val maxMarks = mock.totalMarks
                    val negMarking = mock.negativeMarking
                    val passingMarks = "33%"
                    val accessType = mock.accessType
                    val status = if (mock.isPublished) "PUBLISHED" else "DRAFT"
                    val mockCreatedAt = formatDate(System.currentTimeMillis())
                    val mockUpdatedAt = formatDate(System.currentTimeMillis())

                    val qIds = mock.questionIds.split(",").map { it.trim() }.filter { it.isNotBlank() }

                    var order = 1
                    for (qIdStr in qIds) {
                        val qIdLong = qIdStr.toLongOrNull() ?: continue
                        val q = questionMap[qIdLong] ?: continue

                        val qOrder = order++
                        val qSubject = q.subject
                        val qChapter = q.topic
                        val qTopic = q.topic
                        val qDifficulty = q.difficulty
                        val qEn = q.questionEn
                        val qAs = q.questionAs

                        val optAEn = q.optionAEn
                        val optAAs = q.optionAAs
                        val optBEn = q.optionBEn
                        val optBAs = q.optionBAs
                        val optCEn = q.optionCEn
                        val optCAs = q.optionCAs
                        val optDEn = q.optionDEn
                        val optDAs = q.optionDAs
                        val optE = "N/A"

                        val correctAns = when (q.correctOptionIndex) {
                            0 -> "A"
                            1 -> "B"
                            2 -> "C"
                            3 -> "D"
                            else -> "A"
                        }

                        val expEn = q.explanationEn
                        val expAs = q.explanationAs
                        val solution = q.explanationEn
                        val imageRef = "N/A"

                        csv.append("${mockId},${escapeCsv(titleEn)},${escapeCsv(titleAs)},${escapeCsv(targetExam)},${escapeCsv(category)},${escapeCsv(description)},$totalQs,$duration,$maxMarks,${escapeCsv(negMarking)},${escapeCsv(passingMarks)},${escapeCsv(accessType)},${escapeCsv(status)},${escapeCsv(mockCreatedAt)},${escapeCsv(mockUpdatedAt)},$qOrder,${q.id},${escapeCsv(qSubject)},${escapeCsv(qChapter)},${escapeCsv(qTopic)},${escapeCsv(qDifficulty)},${escapeCsv(qEn)},${escapeCsv(qAs)},${escapeCsv(optAEn)},${escapeCsv(optAAs)},${escapeCsv(optBEn)},${escapeCsv(optBAs)},${escapeCsv(optCEn)},${escapeCsv(optCAs)},${escapeCsv(optDEn)},${escapeCsv(optDAs)},${escapeCsv(optE)},${escapeCsv(correctAns)},${escapeCsv(expEn)},${escapeCsv(expAs)},${escapeCsv(solution)},${escapeCsv(imageRef)}\n")
                    }
                }

                saveCsvToFile(context, "jukti_mock_test_questions_report.csv", csv.toString())
            } catch (e: Exception) {
                android.util.Log.e("JuktiViewModel", "Export mock test questions failed", e)
                showToastOnMain(context, "Export failed: ${e.localizedMessage}")
            }
        }
    }

    fun exportRevenueCsv(context: android.content.Context) {
        if (!isAdminOrOwner.value) {
            showToastOnMain(context, "Access Denied: Owner/Admin privilege required.")
            return
        }
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                showToastOnMain(context, "Generating Revenue & Settlement Report...")
                val db = com.example.JuktiApplication.getFirestore(context) ?: return@launch

                val purchasesSnap = db.collectionGroup("purchases").get().await()

                var totalGross = 0.0
                var totalRefunds = 0.0

                val csv = StringBuilder()
                csv.append("Revenue Record ID,Purchase ID,Order/Transaction ID,Date,Time,Google Play Product ID,Plan ID,Plan Name,Currency,Gross Sale Amount,Gross Revenue,Discount/Offer Amount,Tax Amount,Tax Rate,GST,CGST,SGST,IGST,Other Tax,Google Play Service Fee,Payment Processing Fee,Other Platform Fee,Other Deductions,Refund Amount,Tax Reversal,Fee Reversal,Other Adjustments,Net Refund Deduction,Net Revenue,Amount Credited,Settlement Amount,Settlement Date,Settlement Status,Revenue Status,Purchase Status,Refund Status,Reconciliation Status,Last Updated\n")

                for (doc in purchasesSnap.documents) {
                    val revRecordId = doc.id
                    val pid = doc.getString("purchaseId") ?: doc.id
                    val orderId = doc.getString("orderId") ?: doc.getString("transactionId") ?: "N/A"

                    val ts = doc.getLong("purchaseTime") ?: doc.getLong("timestamp") ?: 0L
                    val dtStr = formatDate(ts)
                    val dateStr = if (dtStr.contains(" ")) dtStr.substringBefore(" ") else dtStr
                    val timeStr = if (dtStr.contains(" ")) dtStr.substringAfter(" ") else "00:00:00"

                    val productId = doc.getString("productId") ?: doc.getString("googlePlayProductId") ?: ""
                    val planId = doc.getString("planId") ?: ""
                    val planName = doc.getString("planName") ?: ""
                    val currency = doc.getString("currency") ?: "INR"

                    val amt = doc.getDouble("amount") ?: doc.getLong("amount")?.toDouble() ?: 0.0
                    val grossAmtStr = String.format(java.util.Locale.US, "%.2f", amt)

                    val status = doc.getString("status")?.uppercase(java.util.Locale.ROOT) ?: "COMPLETED"
                    val isRefunded = status == "REFUNDED" || status == "REVOKED"

                    if (isRefunded) {
                        totalRefunds += amt
                    } else {
                        totalGross += amt
                    }

                    val refundAmtStr = if (isRefunded) grossAmtStr else "0.00"
                    val netRevStr = if (!isRefunded) grossAmtStr else "0.00"

                    val unavailTax = "N/A (Google Play Financial API Required)"
                    val unavailFee = "N/A (Google Play Financial API Required)"
                    val unavailSettle = "N/A (Google Play Financial API Required)"

                    val revStatus = if (isRefunded) "REFUNDED" else "SETTLED_APP"
                    val refundStatus = if (isRefunded) "REFUNDED" else "NONE"
                    val reconStatus = "APP_RECORDED_ONLY (PLAY_FINANCIAL_API_PENDING)"

                    val updatedAt = formatDate(doc.getLong("updatedAt") ?: ts)

                    csv.append("${escapeCsv(revRecordId)},${escapeCsv(pid)},${escapeCsv(orderId)},${escapeCsv(dateStr)},${escapeCsv(timeStr)},${escapeCsv(productId)},${escapeCsv(planId)},${escapeCsv(planName)},${escapeCsv(currency)},${escapeCsv(grossAmtStr)},${escapeCsv(grossAmtStr)},0.00,${escapeCsv(unavailTax)},${escapeCsv(unavailTax)},${escapeCsv(unavailTax)},${escapeCsv(unavailTax)},${escapeCsv(unavailTax)},${escapeCsv(unavailTax)},${escapeCsv(unavailTax)},${escapeCsv(unavailFee)},${escapeCsv(unavailFee)},${escapeCsv(unavailFee)},${escapeCsv(unavailFee)},${escapeCsv(refundAmtStr)},${escapeCsv(unavailTax)},${escapeCsv(unavailFee)},0.00,${escapeCsv(refundAmtStr)},${escapeCsv(netRevStr)},${escapeCsv(unavailSettle)},${escapeCsv(unavailSettle)},${escapeCsv(unavailSettle)},${escapeCsv(unavailSettle)},${escapeCsv(revStatus)},${escapeCsv(status)},${escapeCsv(refundStatus)},${escapeCsv(reconStatus)},${escapeCsv(updatedAt)}\n")
                }

                val estNet = totalGross - totalRefunds
                csv.append("\nSUMMARY METRICS:\n")
                csv.append("TOTAL GROSS REVENUE (INR),${String.format(java.util.Locale.US, "%.2f", totalGross)}\n")
                csv.append("TOTAL REFUNDS / REVOCATIONS (INR),${String.format(java.util.Locale.US, "%.2f", totalRefunds)}\n")
                csv.append("ESTIMATED APP NET REVENUE (INR),${String.format(java.util.Locale.US, "%.2f", estNet)}\n")
                csv.append("SETTLEMENT NOTE,Google Play Financial Reports API not integrated. Play fees, tax withholdings, and bank settlement credits are marked N/A.\n")

                saveCsvToFile(context, "jukti_revenue_settlement_report.csv", csv.toString())
            } catch (e: Exception) {
                android.util.Log.e("JuktiViewModel", "Export revenue failed", e)
                showToastOnMain(context, "Export failed: ${e.localizedMessage}")
            }
        }
    }

    fun exportMocksCsv(context: android.content.Context) { exportMockTestQuestionsCsv(context) }
    fun exportQuestionsCsv(context: android.content.Context) { exportFullQuestionBankCsv(context) }

    private fun saveCsvToFile(context: android.content.Context, filename: String, content: String) {
        try {
            val contentWithBom = "\uFEFF" + content
            val bytes = contentWithBom.toByteArray(Charsets.UTF_8)
            var savedUri: android.net.Uri? = null
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
                }
                savedUri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (savedUri != null) {
                    resolver.openOutputStream(savedUri)?.use { it.write(bytes) }
                }
            } else {
                val dir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                if (!dir.exists()) dir.mkdirs()
                val file = java.io.File(dir, filename)
                java.io.FileOutputStream(file).use { it.write(bytes) }
                savedUri = android.net.Uri.fromFile(file)
            }

            launchOnMain {
                android.widget.Toast.makeText(
                    context,
                    "Saved $filename to Downloads folder!",
                    android.widget.Toast.LENGTH_LONG
                ).show()

                if (savedUri != null) {
                    try {
                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(android.content.Intent.EXTRA_STREAM, savedUri)
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        val chooser = android.content.Intent.createChooser(shareIntent, "Open or Share $filename")
                        chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(chooser)
                    } catch (e: Exception) {
                        android.util.Log.e("JuktiViewModel", "Share intent failed", e)
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("JuktiViewModel", "Error saving CSV", e)
            launchOnMain {
                android.widget.Toast.makeText(context, "Failed to save report: ${e.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun launchOnMain(block: () -> Unit) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Main) { block() }
    }

    fun rejectPendingRequest(request: PendingRequestEntity) {
        viewModelScope.launch {
            repository.updatePendingRequest(request.copy(status = "REJECTED"))
        }
    }

    fun fetchPlanPurchaseSummary(context: android.content.Context? = null) {
        if (!isAdminOrOwner.value) return
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _planPurchaseSummaryState.value = PlanPurchaseSummaryState(isLoading = true)
            try {
                val ctx = context ?: getApplication<android.app.Application>().applicationContext
                val db = com.example.JuktiApplication.getFirestore(ctx)
                if (db == null) {
                    _planPurchaseSummaryState.value = PlanPurchaseSummaryState(
                        isLoading = false,
                        error = "Firestore database unavailable"
                    )
                    return@launch
                }

                // 1. Gather all plan names from Firestore 'plans' and local repository 'plans'
                val plansSnap = try { db.collection("plans").get().await() } catch (e: Exception) { null }
                val firebasePlanNames = plansSnap?.documents?.mapNotNull { it.getString("planName") } ?: emptyList()

                val allKnownPlanNames = LinkedHashSet<String>()
                plans.value.forEach { if (it.planName.isNotBlank()) allKnownPlanNames.add(it.planName) }
                firebasePlanNames.forEach { if (it.isNotBlank()) allKnownPlanNames.add(it) }

                val planUserSetMap = LinkedHashMap<String, MutableSet<String>>()
                allKnownPlanNames.forEach { planName ->
                    planUserSetMap[planName] = mutableSetOf()
                }

                // 2. Query collectionGroup("purchases") for authoritative purchase records
                val purchasesSnap = db.collectionGroup("purchases").get().await()

                val excludedStatuses = setOf(
                    "PENDING", "FAILED", "CANCELLED", "CANCELLED_UNPAID",
                    "INVALID", "REJECTED", "REFUNDED", "REVOKED", "UNVERIFIED"
                )

                for (doc in purchasesSnap.documents) {
                    val status = (doc.getString("status") ?: "ACTIVE").uppercase(java.util.Locale.ROOT).trim()
                    val verStatus = (doc.getString("verificationStatus") ?: "VERIFIED").uppercase(java.util.Locale.ROOT).trim()
                    val revokedAt = doc.getLong("revokedAt") ?: 0L

                    if (status in excludedStatuses) continue
                    if (verStatus == "FAILED" || verStatus == "REJECTED" || verStatus == "INVALID") continue
                    if (revokedAt > 0L) continue

                    val uid = doc.getString("userId") ?: doc.reference.parent.parent?.id ?: ""
                    if (uid.isBlank()) continue

                    val purchasePlanName = doc.getString("planName") ?: ""
                    val purchasePlanId = doc.getString("planId") ?: ""
                    val productId = doc.getString("productId") ?: doc.getString("googlePlayProductId") ?: ""

                    var matchedPlanName: String? = null

                    // Exact or case-insensitive name match
                    matchedPlanName = allKnownPlanNames.find { it.equals(purchasePlanName, ignoreCase = true) }

                    // Match by planId
                    if (matchedPlanName == null && purchasePlanId.isNotBlank()) {
                        matchedPlanName = allKnownPlanNames.find { pName ->
                            val p = plans.value.find { it.planName == pName }
                            p?.id?.toString() == purchasePlanId || p?.planName?.equals(purchasePlanId, ignoreCase = true) == true
                        }
                    }

                    // Match by productId
                    if (matchedPlanName == null && productId.isNotBlank()) {
                        matchedPlanName = allKnownPlanNames.find { pName ->
                            val p = plans.value.find { it.planName == pName }
                            p?.googlePlayProductId?.equals(productId, ignoreCase = true) == true
                        }
                    }

                    // Fallback to purchase planName if not already known
                    if (matchedPlanName == null && purchasePlanName.isNotBlank()) {
                        matchedPlanName = purchasePlanName
                    }

                    if (matchedPlanName != null) {
                        planUserSetMap.getOrPut(matchedPlanName) { mutableSetOf() }.add(uid)
                    }
                }

                val items = planUserSetMap.map { (planName, userSet) ->
                    PlanPurchaseSummaryItem(
                        planName = planName,
                        purchasedUserCount = userSet.size
                    )
                }

                _planPurchaseSummaryState.value = PlanPurchaseSummaryState(
                    isLoading = false,
                    items = items,
                    error = null
                )
            } catch (e: Exception) {
                android.util.Log.e("JuktiViewModel", "Error fetching plan purchase summary", e)
                _planPurchaseSummaryState.value = PlanPurchaseSummaryState(
                    isLoading = false,
                    error = e.localizedMessage ?: "Failed to calculate purchase summary"
                )
            }
        }
    }
}

data class PlanPurchaseSummaryItem(
    val planName: String,
    val purchasedUserCount: Int
)

data class PlanPurchaseSummaryState(
    val isLoading: Boolean = false,
    val items: List<PlanPurchaseSummaryItem> = emptyList(),
    val error: String? = null
)
