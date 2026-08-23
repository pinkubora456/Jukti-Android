package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,          // e.g., "Assam History", "General Knowledge", "Reasoning"
    val topic: String,            // e.g., "Ahom Dynasty", "Assam Geography", "Indian Economy"
    val difficulty: String,       // "Easy", "Medium", "Hard"
    val questionEn: String,
    val questionAs: String,
    val optionAEn: String,
    val optionBEn: String,
    val optionCEn: String,
    val optionDEn: String,
    val optionAAs: String,
    val optionBAs: String,
    val optionCAs: String,
    val optionDAs: String,
    val correctOptionIndex: Int,  // 0, 1, 2, 3
    val explanationEn: String,
    val explanationAs: String,
    val examCategory: String = "ADRE", // e.g. "ADRE", "APSC", "Assam Police", "TET"
    val isPremium: Boolean = false,
    val questionType: String = "Expected", // "PYQ", "Expected"
    val isReported: Boolean = false,
    val status: String = "ACTIVE", // "ACTIVE", "HIDDEN"
    val cachedAt: Long = System.currentTimeMillis(),
    val lastAccessedAt: Long = System.currentTimeMillis(),
    val version: Int = 1,
    val updatedAt: Long = 0L,
    val firebaseId: String = ""
)

@Entity(tableName = "mock_tests")
data class MockTestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titleEn: String,
    val titleAs: String,
    val category: String,          // "ADRE", "APSC", "Assam Police", "TET", "PYQ"
    val durationMinutes: Int,
    val totalQuestions: Int,
    val totalMarks: Int,
    val isScheduled: Boolean = false,
    val scheduledDate: String = "",
    val isCompleted: Boolean = false,
    val userScore: Int = 0,
    val userAccuracy: Float = 0f,
    val userRank: Int = 0,
    val userPercentile: Float = 0f,
    val isPublished: Boolean = true,
    val testType: String = "Full-Length", // "Full-Length", "Subject-wise", "Chapter-wise"
    val subjectOrChapter: String = "General Studies & Assam GK",
    val negativeMarking: String = "0.25 Marks",
    val difficulty: String = "Medium", // "Easy", "Medium", "Hard"
    val isPremium: Boolean = false,
    val inProgress: Boolean = false,
    val questionsAnswered: Int = 0,
    val timeRemainingSeconds: Int = 0,
    val questionIds: String = "",
    val markPerQuestion: Float = 1f
)

@Entity(tableName = "mock_attempts")
data class MockAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mockTestId: Long,
    val userId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val questionIds: String, // Comma-separated list of question IDs
    val userAnswersJson: String, // JSON mapping of index to option
    val score: Int,
    val totalMarks: Int,
    val accuracy: Float,
    val correctCount: Int,
    val totalAttempted: Int
)

@Entity(tableName = "study_notes")
data class StudyNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val topic: String,
    val titleEn: String,
    val titleAs: String,
    val contentEn: String,
    val contentAs: String,
    val isBookmarked: Boolean = false,
    val isDownloaded: Boolean = false,
    val readTimeMinutes: Int = 5,
    val isPremium: Boolean = false
)

@Entity(tableName = "exam_updates")
data class ExamUpdateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val examName: String,         // e.g. "ADRE 2.0 / 3.0", "APSC CCE 2026", "Assam Police SI"
    val category: String,         // "Notification", "Syllabus", "Pattern", "Cutoff", "Admit Card"
    val titleEn: String,
    val titleAs: String,
    val updateDate: String,
    val detailEn: String,
    val detailAs: String,
    val officialLink: String = "https://assam.gov.in",
    val isImportantNotice: Boolean = false
)

@Entity(tableName = "banners")
data class BannerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titleEn: String,
    val titleAs: String,
    val subtitleEn: String,
    val subtitleAs: String,
    val type: String,             // "PROMOTIONAL", "INFORMATION", "CAROUSEL"
    val badgeText: String = "UPDATED",
    val actionUrl: String = "",
    val isActive: Boolean = true,
    val imageUrl: String = "",
    val actionType: String = "Link", // "Mock Test", "Study Notes", "Link"
    val offerValidity: String = "",
    val planPrice: String = "",
    val discount: String = "",
    val finalPrice: String = ""
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String,
    val timestamp: String,
    val category: String = "General",
    val isRead: Boolean = false
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Assam Scholar",
    val email: String = "scholar@jukti.in",
    val mobile: String = "+91 98765 43210",
    val district: String = "Kamrup Metropolitan",
    val examGoal: String = "ADRE Grade 3 & APSC CCE",
    val xp: Int = 0,
    val level: Int = 1,
    val dailyStreak: Int = 0,
    val totalSolved: Int = 0,
    val correctCount: Int = 0,
    val totalTimeMinutes: Int = 0,
    val isPremium: Boolean = false,
    val role: String = "USER",     // "USER", "ADMIN", "OWNER"
    val firebaseProjectId: String = "jukti-26035",
    val joinedDate: String = "Jul 2026",
    val isLoggedIn: Boolean = false,
    val currentDeviceId: String = "",
    val activeDeviceId: String = "",
    val uid: String = "",
    val profileName: String = "",
    val registrationName: String = "",
    val googleName: String = ""
) {
    fun getResolvedName(): String {
        // Priority 1 — Name added/edited by the user in the Jukti Profile section
        val pName = profileName.trim()
        if (pName.isNotBlank() && 
            !pName.equals("null", ignoreCase = true) && 
            !pName.equals("Guest User", ignoreCase = true) &&
            !pName.equals("Assam Scholar", ignoreCase = true) &&
            !pName.equals("User", ignoreCase = true)) {
            return pName
        }

        // Priority 2 — Name provided during Jukti registration/login setup
        val rName = registrationName.trim()
        if (rName.isNotBlank() && 
            !rName.equals("null", ignoreCase = true) && 
            !rName.equals("Guest User", ignoreCase = true) &&
            !rName.equals("Assam Scholar", ignoreCase = true) &&
            !rName.equals("User", ignoreCase = true)) {
            return rName
        }

        // Priority 3 — Google account name from the local database or Firebase
        val gName = googleName.trim()
        if (gName.isNotBlank() && 
            !gName.equals("null", ignoreCase = true) && 
            !gName.equals("Guest User", ignoreCase = true) &&
            !gName.equals("Assam Scholar", ignoreCase = true) &&
            !gName.equals("User", ignoreCase = true)) {
            return gName
        }

        val fbUser = try { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser } catch (e: Exception) { null }
        val fbDisplayName = fbUser?.displayName?.trim() ?: ""
        if (fbDisplayName.isNotBlank() && 
            !fbDisplayName.equals("null", ignoreCase = true) && 
            !fbDisplayName.equals("Guest User", ignoreCase = true) &&
            !fbDisplayName.equals("Assam Scholar", ignoreCase = true) &&
            !fbDisplayName.equals("User", ignoreCase = true)) {
            return fbDisplayName
        }

        // Legacy name check
        val legacyName = name.trim()
        if (legacyName.isNotBlank() && 
            !legacyName.equals("null", ignoreCase = true) && 
            !legacyName.equals("Guest User", ignoreCase = true) &&
            !legacyName.equals("Assam Scholar", ignoreCase = true) &&
            !legacyName.equals("User", ignoreCase = true) &&
            !legacyName.contains("@")) {
            return legacyName
        }

        return "User"
    }

    fun withResolvedName(): UserProfileEntity {
        return this.copy(name = getResolvedName())
    }
}

@Entity(tableName = "about_config")
data class AboutConfigEntity(
    @PrimaryKey val id: Int = 1,
    val appTitle: String = "Jukti",
    val appSubtitleEn: String = "Test Your Knowledge",
    val appSubtitleAs: String = "অসমৰ সৰ্ববৃহৎ পৰীক্ষা প্ৰস্তুতি এপ্প",
    val versionText: String = "Version 2026.1.0",
    val missionEn: String = "Jukti is engineered to democratize competitive exam preparation for aspirants across Assam. We provide comprehensive practice modules, high-yield Assam history and current affairs notes, full-length timed mock tests, and real-time state ranking analytics.",
    val missionAs: String = "যুক্তি এপ্পৰ প্ৰধান উদ্দেশ্য হৈছে অসমৰ সকলো প্ৰতিযোগীতামূলক পৰীক্ষাৰ (APSC, ADRE 2.0, Assam Police, SLRC, TET) প্ৰাৰ্থীসকলক উচ্চমানদণ্ডৰ মক টেষ্ট, বিগত বৰ্ষৰ প্ৰশ্ন আৰু অধ্যয়ন সমল সম্পূৰ্ণ বিনামূলীয়াকৈ তথা সহজ ভাষাত যোগান ধৰা।",
    val logoIconName: String = "School", // "School", "Book", "Library", "Star", "Sparkles", "Psychology", "Award", "Trophy", "Balance", "Gavel"
    val logoUrl: String = "",
    val logoUpdatedAt: Long = 0L,
    val copyrightText: String = "Copyright © 2026 Jukti Education Portal. All rights reserved.",
    val developerTagline: String = "Designed & Developed for Assam Aspirants",
    val contactEmail: String = "juktieducation@gmail.com",
    val contactPhone: String = "+91 98765 43210",
    val contactTelegram: String = "t.me/JuktiAssam",
    val contactWhatsapp: String = "Community Group",
    val adminEmails: String = "",
    val refundPolicyEn: String = "Our policy lasts 7 days. If 7 days have gone by since your purchase, unfortunately, we cannot offer you a refund. To be eligible for a refund, your request must be due to technical billing issues or double charge. Please contact juktieducation@gmail.com with your transaction details.",
    val refundPolicyAs: String = "আমাৰ ৰিফাণ্ড পলিচি ক্ৰয় কৰাৰ ৭ দিনৰ বাবে প্ৰযোজ্য। ক্ৰয় কৰাৰ ৭ দিন অতিক্ৰম কৰিলে কোনো ৰিফাণ্ড প্ৰদান কৰা নহ'ব। কেৱল কাৰিকৰী অসুবিধা বা ভুলতে দুবাৰ পইচা কটা গ’লেহে আপুনি ৰিফাণ্ডৰ বাবে আবেদন কৰিব পাৰিব। সহায়ৰ বাবে juktieducation@gmail.com ত যোগাযোগ কৰক.",
    val founderName: String = "Pinku Bora",
    val founderTitle: String = "Founder & Creator of Jukti",
    val founderCredential: String = "ADRE 2022 Qualifier",
    val founderDescription: String = "Jukti was created with a simple vision — to make competitive exam preparation smarter, more accessible, and more effective for aspirants.\n\nHaving experienced the competitive exam preparation journey myself, I understand the importance of consistent practice, quality questions, performance analysis, and identifying areas that need improvement.\n\nThrough Jukti, my goal is to provide aspirants with a focused platform where they can practice, test their knowledge, track their progress, and prepare with greater confidence.",
    val founderPhotoUrl: String = "",
    val founderTagline: String = "Jukti — Test Your Knowledge.",
    val privacyPolicyContent: String = "",
    val termsConditionsContent: String = "",
    val playStoreUrl: String = "https://ais-dev-mbq2e6ge5z4qs5wk3gkstx-397582032913.asia-southeast1.run.app"
)


@Entity(tableName = "subscription_plans")
data class PlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planName: String,
    val planPrice: String,
    val discount: String,
    val finalPrice: String,
    val offerValidity: String,
    val planValidity: String = "",
    val validityType: String = "MONTHS", // "DAYS", "MONTHS", "YEARS", "LIFETIME"
    val validityValue: Int = 1,
    val validityLabel: String = "1 Month",
    val isLifetime: Boolean = false,
    val contents: String = "", // JSON string of contents added
    val features: String = "", // JSON string or comma-separated features
    val isActive: Boolean = true,
    val imageUrl: String = "",
    val examTarget: String = "",
    val googlePlayProductId: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

@Entity(tableName = "exams")
data class ExamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subtitle: String,
    val status: String = "Active",
    val syncStatus: String = "SYNCED",
    val firebaseId: String = "",
    val updatedAt: Long = 0L,
    val version: Int = 1
)

@Entity(tableName = "subjects_chapters")
data class SubjectChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val chapter: String
)

@Entity(tableName = "pending_requests")
data class PendingRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val requestType: String, // "DELETE_USER", "DELETE_QUESTION", "BLOCK_USER", "UPGRADE_PLAN", "CREATE_PLAN", "DELETE_MOCK"
    val title: String,
    val description: String,
    val targetId: String = "",
    val payloadJson: String = "",
    val requestedBy: String = "Admin",
    val timestamp: String = "",
    val status: String = "PENDING" // "PENDING", "APPROVED", "REJECTED"
)

@Entity(tableName = "faqs")
data class FaqEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questionEn: String,
    val questionAs: String = "",
    val answerEn: String,
    val answerAs: String = ""
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val role: String, // "ADMIN" or "OWNER"
    val actionDetails: String,
    val timestamp: Long
)

@Entity(tableName = "notification_categories")
data class NotificationCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val syncId: Long = 0,
    val entityId: String,
    val dataType: String, // "QUESTION", "MOCK_TEST", "STUDY_NOTE", "EXAM_UPDATE", "BANNER", "PLAN", "FAQ", "SUBJECT_CHAPTER", "EXAM", "NOTIFICATION"
    val operation: String, // "CREATE", "UPDATE", "DELETE"
    val payloadJson: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val lastAttemptAt: Long = 0L,
    val lastError: String? = null,
    val syncStatus: String = "PENDING", // "PENDING", "UPLOADING", "SYNCED", "FAILED"
    val priority: Int = 1,
    val version: Long = 1L
)

@Entity(tableName = "entitlements")
data class EntitlementEntity(
    @PrimaryKey val userId: String,
    val planId: String,
    val planName: String,
    val status: String = "EXPIRED", // "ACTIVE", "EXPIRED", "REVOKED", "LIFETIME"
    val validFrom: Long = 0L,
    val validUntil: Long = 0L,
    val validityType: String = "MONTHS", // "DAYS", "MONTHS", "YEARS", "LIFETIME"
    val validityValue: Int = 1,
    val validityLabel: String = "1 Month",
    val isLifetime: Boolean = false,
    val benefits: String = "", // comma-separated
    val source: String = "", // "GOOGLE_PLAY", "OWNER_ASSIGNED", "ADMIN_ASSIGNED", "FREE_PLAN", "MIGRATION"
    val purchaseId: String = "",
    val activatedAt: Long = 0L,
    val updatedAt: Long = 0L
)

@Entity(tableName = "entitlement_history")
data class EntitlementHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val userEmail: String,
    val eventType: String, // "PURCHASED", "MANUALLY_ASSIGNED", "PLAN_CHANGED", "PLAN_EXTENDED", "PLAN_EXPIRED", "FREE_PLAN_ASSIGNED", "RESTORED"
    val previousPlan: String = "",
    val newPlan: String = "",
    val previousExpiry: Long = 0L,
    val newExpiry: Long = 0L,
    val validityGranted: String = "",
    val validityType: String = "",
    val validityValue: Int = 0,
    val isLifetime: Boolean = false,
    val source: String = "",
    val actor: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "user_question_states",
    primaryKeys = ["userId", "questionId"]
)
data class UserQuestionStateEntity(
    val userId: String,
    val questionId: String,
    val isBookmarked: Boolean = false,
    val isLiked: Boolean = false,
    val isHidden: Boolean = false,
    val isMastered: Boolean = false,
    val everGotWrong: Boolean = false,
    val incorrectCount: Int = 0,
    val totalAttempts: Int = 0,
    val firstAttemptCorrect: Boolean? = null,
    val lastUpdatedDateStr: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
)


