package com.example.data.repository

import android.util.Log
import com.example.data.local.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose

class FirebaseRepository {
    private val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Firestore not available", e)
            null
        }

    fun getSanitizedUserDocId(email: String): String {
        val auth = try { com.google.firebase.auth.FirebaseAuth.getInstance() } catch (e: Exception) { null }
        val uid = auth?.currentUser?.uid
        if (uid != null) {
            return uid
        }
        val trimmed = email.trim().lowercase()
        if (trimmed.isBlank()) return "scholar_jukti_in"
        return trimmed.replace("@", "_at_").replace(".", "_dot_")
    }

    suspend fun saveUserProfile(profile: UserProfileEntity, merge: Boolean = true) {
        try {
            val docId = getSanitizedUserDocId(profile.email)
            val auth = try { com.google.firebase.auth.FirebaseAuth.getInstance() } catch (e: Exception) { null }
            val authUid = auth?.currentUser?.uid ?: docId
            val userMap = mutableMapOf<String, Any?>(
                "id" to profile.id,
                "name" to profile.name,
                "email" to profile.email,
                "mobile" to profile.mobile,
                "district" to profile.district,
                "examGoal" to profile.examGoal,
                "xp" to profile.xp,
                "level" to profile.level,
                "dailyStreak" to profile.dailyStreak,
                "totalSolved" to profile.totalSolved,
                "correctCount" to profile.correctCount,
                "totalTimeMinutes" to profile.totalTimeMinutes,
                "firebaseProjectId" to profile.firebaseProjectId,
                "joinedDate" to profile.joinedDate,
                "isLoggedIn" to profile.isLoggedIn,
                "currentDeviceId" to profile.currentDeviceId,
                "activeDeviceId" to profile.activeDeviceId,
                "uid" to profile.uid.ifBlank { authUid },
                "profileName" to profile.profileName,
                "registrationName" to profile.registrationName,
                "googleName" to profile.googleName,
                "lastSyncedAt" to System.currentTimeMillis()
            )
            val isOwnerUser = profile.role == "OWNER" || auth?.currentUser?.email?.contains("juktieducation", ignoreCase = true) == true
            if (isOwnerUser || !merge) {
                userMap["isPremium"] = profile.isPremium
                userMap["role"] = profile.role
            }
            if (merge) {
                firestore?.collection("users")?.document(docId)?.set(userMap, SetOptions.merge())?.await()
            } else {
                firestore?.collection("users")?.document(docId)?.set(userMap)?.await()
            }
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error saving user profile to Firebase", e)
        }
    }

    suspend fun fetchUserProfile(email: String, explicitUid: String? = null): UserProfileEntity? {
        return try {
            val auth = try { com.google.firebase.auth.FirebaseAuth.getInstance() } catch (e: Exception) { null }
            val currentUid = explicitUid ?: auth?.currentUser?.uid
            val trimmedEmail = email.trim().lowercase()
            val sanitizedEmailDocId = if (trimmedEmail.isNotBlank()) trimmedEmail.replace("@", "_at_").replace(".", "_dot_") else ""

            var snapshot = if (!currentUid.isNullOrBlank()) {
                firestore?.collection("users")?.document(currentUid)?.get()?.await()
            } else null

            // Fallback to legacy document ID if not found under UID
            if ((snapshot == null || !snapshot.exists()) && sanitizedEmailDocId.isNotBlank()) {
                val legacySnap = firestore?.collection("users")?.document(sanitizedEmailDocId)?.get()?.await()
                if (legacySnap != null && legacySnap.exists()) {
                    snapshot = legacySnap
                }
            }

            if (snapshot != null && snapshot.exists()) {
                val foundProfile = UserProfileEntity(
                    id = snapshot.getLong("id")?.toInt() ?: 1,
                    name = snapshot.getString("name") ?: "Assam Scholar",
                    email = snapshot.getString("email") ?: email,
                    mobile = snapshot.getString("mobile") ?: "",
                    district = snapshot.getString("district") ?: "",
                    examGoal = snapshot.getString("examGoal") ?: "",
                    xp = snapshot.getLong("xp")?.toInt() ?: 0,
                    level = snapshot.getLong("level")?.toInt() ?: 1,
                    dailyStreak = snapshot.getLong("dailyStreak")?.toInt() ?: 0,
                    totalSolved = snapshot.getLong("totalSolved")?.toInt() ?: 0,
                    correctCount = snapshot.getLong("correctCount")?.toInt() ?: 0,
                    totalTimeMinutes = snapshot.getLong("totalTimeMinutes")?.toInt() ?: 0,
                    isPremium = snapshot.getBoolean("isPremium") ?: false,
                    role = snapshot.getString("role") ?: "USER",
                    firebaseProjectId = snapshot.getString("firebaseProjectId") ?: "jukti-26035",
                    joinedDate = snapshot.getString("joinedDate") ?: "Jul 2026",
                    isLoggedIn = snapshot.getBoolean("isLoggedIn") ?: true,
                    currentDeviceId = snapshot.getString("currentDeviceId") ?: "",
                    activeDeviceId = snapshot.getString("activeDeviceId") ?: "",
                    uid = currentUid ?: snapshot.getString("uid") ?: snapshot.id,
                    profileName = snapshot.getString("profileName") ?: "",
                    registrationName = snapshot.getString("registrationName") ?: "",
                    googleName = snapshot.getString("googleName") ?: ""
                )
                // If found via legacy doc, migrate to current UID
                if (!currentUid.isNullOrBlank() && snapshot.id != currentUid) {
                    saveUserProfile(foundProfile.copy(uid = currentUid), merge = true)
                }
                foundProfile
            } else null
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error fetching user profile from Firebase", e)
            null
        }
    }

    suspend fun fetchUserEntitlement(email: String, explicitUid: String? = null): EntitlementEntity? {
        return try {
            val auth = try { com.google.firebase.auth.FirebaseAuth.getInstance() } catch (e: Exception) { null }
            val currentUid = explicitUid ?: auth?.currentUser?.uid
            val trimmedEmail = email.trim().lowercase()
            val sanitizedEmailDocId = if (trimmedEmail.isNotBlank()) trimmedEmail.replace("@", "_at_").replace(".", "_dot_") else ""

            var snapshot = if (!currentUid.isNullOrBlank()) {
                firestore?.collection("users")?.document(currentUid)
                    ?.collection("entitlements")?.document("current")?.get()?.await()
            } else null

            if ((snapshot == null || !snapshot.exists()) && sanitizedEmailDocId.isNotBlank()) {
                val legacySnap = firestore?.collection("users")?.document(sanitizedEmailDocId)
                    ?.collection("entitlements")?.document("current")?.get()?.await()
                if (legacySnap != null && legacySnap.exists()) {
                    snapshot = legacySnap
                }
            }
            
            if (snapshot != null && snapshot.exists()) {
                EntitlementEntity(
                    userId = currentUid ?: snapshot.reference.parent.parent?.id ?: getSanitizedUserDocId(email),
                    planId = snapshot.getString("planId") ?: "",
                    planName = snapshot.getString("planName") ?: "",
                    status = snapshot.getString("status") ?: "EXPIRED",
                    validFrom = snapshot.getLong("validFrom") ?: 0L,
                    validUntil = snapshot.getLong("validUntil") ?: 0L,
                    benefits = (snapshot.get("benefits") as? List<*>)?.joinToString(",") ?: "",
                    source = snapshot.getString("source") ?: "",
                    purchaseId = snapshot.getString("purchaseId") ?: "",
                    updatedAt = snapshot.getLong("updatedAt") ?: 0L
                )
            } else null
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error fetching user entitlement", e)
            null
        }
    }

    suspend fun fetchAllUsers(): List<UserProfileEntity> {
        return try {
            val snapshot = firestore?.collection("users")?.get()?.await()
            val users = mutableListOf<UserProfileEntity>()
            snapshot?.documents?.forEach { doc ->
                users.add(UserProfileEntity(
                    id = doc.getLong("id")?.toInt() ?: 1,
                    name = doc.getString("name") ?: "Assam Scholar",
                    email = doc.getString("email") ?: "",
                    mobile = doc.getString("mobile") ?: "",
                    district = doc.getString("district") ?: "",
                    examGoal = doc.getString("examGoal") ?: "",
                    xp = doc.getLong("xp")?.toInt() ?: 0,
                    level = doc.getLong("level")?.toInt() ?: 1,
                    dailyStreak = doc.getLong("dailyStreak")?.toInt() ?: 0,
                    totalSolved = doc.getLong("totalSolved")?.toInt() ?: 0,
                    correctCount = doc.getLong("correctCount")?.toInt() ?: 0,
                    totalTimeMinutes = doc.getLong("totalTimeMinutes")?.toInt() ?: 0,
                    isPremium = doc.getBoolean("isPremium") ?: false,
                    role = doc.getString("role") ?: "USER",
                    firebaseProjectId = doc.getString("firebaseProjectId") ?: "jukti-26035",
                    joinedDate = doc.getString("joinedDate") ?: "Jul 2026",
                    isLoggedIn = doc.getBoolean("isLoggedIn") ?: true,
                    currentDeviceId = doc.getString("currentDeviceId") ?: "",
                    activeDeviceId = doc.getString("activeDeviceId") ?: "",
                    uid = doc.getString("uid") ?: doc.id
                ))
            }
            users
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error fetching all users", e)
            emptyList()
        }
    }

    suspend fun saveQuestionProgress(email: String, progress: QuestionProgressEntity) {
        try {
            val docId = getSanitizedUserDocId(email)
            val map = mapOf(
                "questionId" to progress.questionId,
                "firstAttemptCorrect" to progress.firstAttemptCorrect,
                "everGotWrong" to progress.everGotWrong,
                "totalCorrectDays" to progress.totalCorrectDays,
                "lastAttemptDateStr" to progress.lastAttemptDateStr,
                "isMastered" to progress.isMastered,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore?.collection("users")?.document(docId)
                ?.collection("question_progress")?.document(progress.questionId.toString())
                ?.set(map, SetOptions.merge())?.await()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Exception) {
            Log.e("FirebaseRepository", "Error saving question progress to Firebase", e)
        }
    }

    suspend fun fetchQuestionProgressList(email: String): List<QuestionProgressEntity> {
        return try {
            val docId = getSanitizedUserDocId(email)
            val snapshot = firestore?.collection("users")?.document(docId)
                ?.collection("question_progress")?.get()?.await()
            snapshot?.documents?.mapNotNull { doc ->
                try {
                    QuestionProgressEntity(
                        questionId = doc.getLong("questionId") ?: 0L,
                        firstAttemptCorrect = doc.getBoolean("firstAttemptCorrect"),
                        everGotWrong = doc.getBoolean("everGotWrong") ?: false,
                        totalCorrectDays = doc.getLong("totalCorrectDays")?.toInt() ?: 0,
                        lastAttemptDateStr = doc.getString("lastAttemptDateStr") ?: "",
                        isMastered = doc.getBoolean("isMastered") ?: false
                    )
                } catch (e: Exception) { null }
            } ?: emptyList()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Exception) {
            Log.e("FirebaseRepository", "Error fetching question progress from Firebase", e)
            emptyList()
        }
    }

    private fun questionToMap(q: QuestionEntity): Map<String, Any?> = mapOf(
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
        "updatedAt" to q.updatedAt,
        "firebaseId" to q.firebaseId
    )

    private fun mockTestToMap(m: MockTestEntity): Map<String, Any?> = mapOf(
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

    private fun studyNoteToMap(n: StudyNoteEntity): Map<String, Any?> = mapOf(
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

    private fun examUpdateToMap(u: ExamUpdateEntity): Map<String, Any?> = mapOf(
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

    private fun bannerToMap(b: BannerEntity): Map<String, Any?> = mapOf(
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

    private fun planToMap(p: PlanEntity): Map<String, Any?> = mapOf(
        "id" to p.id,
        "planName" to p.planName,
        "planPrice" to p.planPrice,
        "discount" to p.discount,
        "finalPrice" to p.finalPrice,
        "offerValidity" to p.offerValidity,
        "planValidity" to p.planValidity,
        "contents" to p.contents,
        "features" to p.features,
        "isActive" to p.isActive,
        "imageUrl" to p.imageUrl,
        "examTarget" to p.examTarget
    )

    private fun faqToMap(f: FaqEntity): Map<String, Any?> = mapOf(
        "id" to f.id,
        "questionEn" to f.questionEn,
        "questionAs" to f.questionAs,
        "answerEn" to f.answerEn,
        "answerAs" to f.answerAs
    )

    private fun subjectChapterToMap(sc: SubjectChapterEntity): Map<String, Any?> = mapOf(
        "id" to sc.id,
        "subject" to sc.subject,
        "chapter" to sc.chapter
    )

    suspend fun saveQuestion(question: QuestionEntity) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            db.collection("questions").document(question.id.toString())
                .set(questionToMap(question), SetOptions.merge())
                .await()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Exception) {
            Log.e("FirebaseRepository", "Error saving question", e)
        }
    }

    suspend fun deleteQuestion(id: Long) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            db.collection("questions").document(id.toString()).delete().await()
            val query = db.collection("questions").whereEqualTo("id", id).get().await()
            query.documents.forEach { it.reference.delete().await() }
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error deleting question", e)
        }
    }

    suspend fun saveMockTest(mock: MockTestEntity) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            db.collection("mock_tests").document(mock.id.toString())
                .set(mockTestToMap(mock), SetOptions.merge())
                .await()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error saving mock test", e)
        }
    }

    suspend fun deleteMockTest(id: Long) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            db.collection("mock_tests").document(id.toString()).delete().await()
            val query = db.collection("mock_tests").whereEqualTo("id", id).get().await()
            query.documents.forEach { it.reference.delete().await() }
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error deleting mock test", e)
        }
    }

    suspend fun saveStudyNote(note: StudyNoteEntity) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            db.collection("study_notes").document(note.id.toString())
                .set(studyNoteToMap(note), SetOptions.merge())
                .await()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error saving study note", e)
        }
    }

    suspend fun deleteStudyNote(id: Long) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            db.collection("study_notes").document(id.toString()).delete().await()
            val query = db.collection("study_notes").whereEqualTo("id", id).get().await()
            query.documents.forEach { it.reference.delete().await() }
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error deleting study note", e)
        }
    }

    suspend fun saveExamUpdate(update: ExamUpdateEntity) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            db.collection("exam_updates").document(update.id.toString())
                .set(examUpdateToMap(update), SetOptions.merge())
                .await()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error saving exam update", e)
        }
    }

    suspend fun deleteExamUpdate(id: Long) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            db.collection("exam_updates").document(id.toString()).delete().await()
            val query = db.collection("exam_updates").whereEqualTo("id", id).get().await()
            query.documents.forEach { it.reference.delete().await() }
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error deleting exam update", e)
        }
    }

    suspend fun saveBanner(banner: BannerEntity) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            db.collection("banners").document(banner.id.toString())
                .set(bannerToMap(banner), SetOptions.merge())
                .await()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error saving banner", e)
        }
    }

    suspend fun deleteBanner(id: Long) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            db.collection("banners").document(id.toString()).delete().await()
            val query = db.collection("banners").whereEqualTo("id", id).get().await()
            query.documents.forEach { it.reference.delete().await() }
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error deleting banner", e)
        }
    }

    suspend fun savePlan(plan: PlanEntity) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            db.collection("plans").document(plan.id.toString())
                .set(planToMap(plan), SetOptions.merge())
                .await()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error saving plan", e)
        }
    }

    suspend fun deletePlan(id: Long) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            db.collection("plans").document(id.toString()).delete().await()
            val query = db.collection("plans").whereEqualTo("id", id).get().await()
            query.documents.forEach { it.reference.delete().await() }
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error deleting plan", e)
        }
    }

    suspend fun saveFaq(faq: FaqEntity) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            db.collection("faqs").document(faq.id.toString())
                .set(faqToMap(faq), SetOptions.merge())
                .await()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error saving faq", e)
        }
    }

    suspend fun deleteFaq(id: Long) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            db.collection("faqs").document(id.toString()).delete().await()
            val query = db.collection("faqs").whereEqualTo("id", id).get().await()
            query.documents.forEach { it.reference.delete().await() }
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error deleting faq", e)
        }
    }

    suspend fun saveSubjectChapter(sc: SubjectChapterEntity) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            db.collection("subjects_chapters").document(sc.id.toString())
                .set(subjectChapterToMap(sc), SetOptions.merge())
                .await()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error saving subject chapter", e)
        }
    }

    suspend fun deleteSubjectChapter(id: Long) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            db.collection("subjects_chapters").document(id.toString()).delete().await()
            val query = db.collection("subjects_chapters").whereEqualTo("id", id).get().await()
            query.documents.forEach { it.reference.delete().await() }
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error deleting subject chapter", e)
        }
    }

    suspend fun batchSaveAllData(
        questions: List<QuestionEntity>,
        mockTests: List<MockTestEntity>,
        studyNotes: List<StudyNoteEntity>,
        plans: List<PlanEntity>,
        subjectChapters: List<SubjectChapterEntity>,
        banners: List<BannerEntity>,
        examUpdates: List<ExamUpdateEntity>,
        faqs: List<FaqEntity>
    ): Int = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val db = firestore ?: return@withContext 0
        var totalUploaded = 0

        suspend fun saveCollection(collectionName: String, items: List<Pair<com.google.firebase.firestore.DocumentReference, Map<String, Any?>>>) {
            if (items.isEmpty()) return
            val chunks = items.chunked(400)
            chunks.forEach { chunk ->
                try {
                    val batch = db.batch()
                    chunk.forEach { (docRef, dataMap) ->
                        batch.set(docRef, dataMap, SetOptions.merge())
                    }
                    batch.commit().await()
                    totalUploaded += chunk.size
                    Log.d("FirebaseRepository", "Successfully synced ${chunk.size} documents for collection: $collectionName")
                } catch (e: kotlinx.coroutines.CancellationException) { 
                    throw e 
                } catch (e: Throwable) {
                    Log.e("FirebaseRepository", "Error committing batch chunk for collection $collectionName. Error: ${e.localizedMessage ?: e.javaClass.simpleName}", e)
                }
            }
        }

        saveCollection("questions", questions.map { db.collection("questions").document(it.id.toString()) to questionToMap(it) })
        saveCollection("mock_tests", mockTests.map { db.collection("mock_tests").document(it.id.toString()) to mockTestToMap(it) })
        saveCollection("study_notes", studyNotes.map { db.collection("study_notes").document(it.id.toString()) to studyNoteToMap(it) })
        saveCollection("plans", plans.map { db.collection("plans").document(it.id.toString()) to planToMap(it) })
        saveCollection("subjects_chapters", subjectChapters.map { db.collection("subjects_chapters").document(it.id.toString()) to subjectChapterToMap(it) })
        saveCollection("banners", banners.map { db.collection("banners").document(it.id.toString()) to bannerToMap(it) })
        saveCollection("exam_updates", examUpdates.map { db.collection("exam_updates").document(it.id.toString()) to examUpdateToMap(it) })
        saveCollection("faqs", faqs.map { db.collection("faqs").document(it.id.toString()) to faqToMap(it) })

        return@withContext totalUploaded
    }

    suspend fun fetchAllBanners(): List<BannerEntity> {
        return try {
            val snapshot = firestore?.collection("banners")?.get()?.await()
            snapshot?.documents?.mapNotNull { doc ->
                BannerEntity(
                    id = doc.getLong("id") ?: 0L,
                    titleEn = doc.getString("titleEn") ?: "",
                    titleAs = doc.getString("titleAs") ?: "",
                    subtitleEn = doc.getString("subtitleEn") ?: "",
                    subtitleAs = doc.getString("subtitleAs") ?: "",
                    badgeText = doc.getString("badgeText") ?: "",
                    type = doc.getString("type") ?: "INFORMATION",
                    actionUrl = doc.getString("actionUrl") ?: "",
                    isActive = doc.getBoolean("isActive") ?: true,
                    imageUrl = doc.getString("imageUrl") ?: "",
                    actionType = doc.getString("actionType") ?: "Link",
                    offerValidity = doc.getString("offerValidity") ?: "",
                    planPrice = doc.getString("planPrice") ?: "",
                    discount = doc.getString("discount") ?: "",
                    finalPrice = doc.getString("finalPrice") ?: ""
                )
            } ?: emptyList()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Exception) {
            Log.e("FirebaseRepository", "Error fetching banners", e)
            emptyList()
        }
    }

    suspend fun fetchAllPlans(): List<PlanEntity> {
        return try {
            val snapshot = firestore?.collection("plans")?.get()?.await()
            snapshot?.documents?.mapNotNull { doc ->
                PlanEntity(
                    id = doc.getLong("id") ?: 0L,
                    planName = doc.getString("planName") ?: "",
                    planPrice = doc.getString("planPrice") ?: "",
                    discount = doc.getString("discount") ?: "",
                    finalPrice = doc.getString("finalPrice") ?: "",
                    offerValidity = doc.getString("offerValidity") ?: "",
                    planValidity = doc.getString("planValidity") ?: "",
                    features = doc.getString("features") ?: "",
                    contents = doc.getString("contents") ?: "",
                    isActive = doc.getBoolean("isActive") ?: true,
                    imageUrl = doc.getString("imageUrl") ?: "",
                    examTarget = doc.getString("examTarget") ?: ""
                )
            } ?: emptyList()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Exception) {
            Log.e("FirebaseRepository", "Error fetching plans", e)
            emptyList()
        }
    }

    suspend fun fetchAllQuestions(): List<QuestionEntity> {
        return try {
            val snapshot = firestore?.collection("questions")?.get()?.await()
            snapshot?.documents?.mapNotNull { doc ->
                try {
                    QuestionEntity(
                        id = doc.getLong("id") ?: 0L,
                        subject = doc.getString("subject") ?: "",
                        topic = doc.getString("topic") ?: "",
                        difficulty = doc.getString("difficulty") ?: "Medium",
                        questionEn = doc.getString("questionEn") ?: "",
                        questionAs = doc.getString("questionAs") ?: "",
                        optionAEn = doc.getString("optionAEn") ?: "",
                        optionBEn = doc.getString("optionBEn") ?: "",
                        optionCEn = doc.getString("optionCEn") ?: "",
                        optionDEn = doc.getString("optionDEn") ?: "",
                        optionAAs = doc.getString("optionAAs") ?: "",
                        optionBAs = doc.getString("optionBAs") ?: "",
                        optionCAs = doc.getString("optionCAs") ?: "",
                        optionDAs = doc.getString("optionDAs") ?: "",
                        correctOptionIndex = doc.getLong("correctOptionIndex")?.toInt() ?: 0,
                        explanationEn = doc.getString("explanationEn") ?: "",
                        explanationAs = doc.getString("explanationAs") ?: "",
                        isBookmarked = doc.getBoolean("isBookmarked") ?: false,
                        isLiked = doc.getBoolean("isLiked") ?: false,
                        isHidden = doc.getBoolean("isHidden") ?: false,
                        examCategory = doc.getString("examCategory") ?: "ADRE",
                        isPremium = doc.getBoolean("isPremium") ?: false,
                        questionType = doc.getString("questionType") ?: "Expected",
                        isReported = doc.getBoolean("isReported") ?: false,
                        cachedAt = doc.getLong("cachedAt") ?: System.currentTimeMillis(),
                        lastAccessedAt = doc.getLong("lastAccessedAt") ?: System.currentTimeMillis(),
                        version = doc.getLong("version")?.toInt() ?: 1,
                        updatedAt = doc.getLong("updatedAt") ?: 0L,
                        firebaseId = doc.getString("firebaseId") ?: doc.id
                    )
                } catch (e: Exception) { null }
            } ?: emptyList()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Exception) {
            Log.e("FirebaseRepository", "Error fetching questions", e)
            emptyList()
        }
    }

    suspend fun fetchAllMockTests(): List<MockTestEntity> {
        return try {
            val snapshot = firestore?.collection("mock_tests")?.get()?.await()
            snapshot?.documents?.mapNotNull { doc ->
                try {
                    MockTestEntity(
                        id = doc.getLong("id") ?: 0L,
                        titleEn = doc.getString("titleEn") ?: "",
                        titleAs = doc.getString("titleAs") ?: "",
                        category = doc.getString("category") ?: "ADRE",
                        durationMinutes = doc.getLong("durationMinutes")?.toInt() ?: 45,
                        totalQuestions = doc.getLong("totalQuestions")?.toInt() ?: 10,
                        totalMarks = doc.getLong("totalMarks")?.toInt() ?: 10,
                        isScheduled = doc.getBoolean("isScheduled") ?: false,
                        scheduledDate = doc.getString("scheduledDate") ?: "",
                        isCompleted = doc.getBoolean("isCompleted") ?: false,
                        userScore = doc.getLong("userScore")?.toInt() ?: 0,
                        userAccuracy = doc.getDouble("userAccuracy")?.toFloat() ?: 0f,
                        userRank = doc.getLong("userRank")?.toInt() ?: 0,
                        userPercentile = doc.getDouble("userPercentile")?.toFloat() ?: 0f,
                        isPublished = doc.getBoolean("isPublished") ?: true,
                        testType = doc.getString("testType") ?: "Full-Length",
                        subjectOrChapter = doc.getString("subjectOrChapter") ?: "",
                        negativeMarking = doc.getString("negativeMarking") ?: "0.25 Marks",
                        difficulty = doc.getString("difficulty") ?: "Medium",
                        isPremium = doc.getBoolean("isPremium") ?: false,
                        inProgress = doc.getBoolean("inProgress") ?: false,
                        questionsAnswered = doc.getLong("questionsAnswered")?.toInt() ?: 0,
                        timeRemainingSeconds = doc.getLong("timeRemainingSeconds")?.toInt() ?: 0,
                        questionIds = doc.getString("questionIds") ?: "",
                        markPerQuestion = doc.getDouble("markPerQuestion")?.toFloat() ?: 1f
                    )
                } catch (e: Exception) { null }
            } ?: emptyList()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Exception) {
            Log.e("FirebaseRepository", "Error fetching mock tests", e)
            emptyList()
        }
    }

    suspend fun fetchAllStudyNotes(): List<StudyNoteEntity> {
        return try {
            val snapshot = firestore?.collection("study_notes")?.get()?.await()
            snapshot?.documents?.mapNotNull { doc ->
                try {
                    StudyNoteEntity(
                        id = doc.getLong("id") ?: 0L,
                        subject = doc.getString("subject") ?: "",
                        topic = doc.getString("topic") ?: "",
                        titleEn = doc.getString("titleEn") ?: "",
                        titleAs = doc.getString("titleAs") ?: "",
                        contentEn = doc.getString("contentEn") ?: "",
                        contentAs = doc.getString("contentAs") ?: "",
                        isBookmarked = doc.getBoolean("isBookmarked") ?: false,
                        isDownloaded = doc.getBoolean("isDownloaded") ?: false,
                        readTimeMinutes = doc.getLong("readTimeMinutes")?.toInt() ?: 5,
                        isPremium = doc.getBoolean("isPremium") ?: false
                    )
                } catch (e: Exception) { null }
            } ?: emptyList()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Exception) {
            Log.e("FirebaseRepository", "Error fetching study notes", e)
            emptyList()
        }
    }

    suspend fun fetchAllExamUpdates(): List<ExamUpdateEntity> {
        return try {
            val snapshot = firestore?.collection("exam_updates")?.get()?.await()
            snapshot?.documents?.mapNotNull { doc ->
                try {
                    ExamUpdateEntity(
                        id = doc.getLong("id") ?: 0L,
                        examName = doc.getString("examName") ?: "",
                        category = doc.getString("category") ?: "",
                        titleEn = doc.getString("titleEn") ?: "",
                        titleAs = doc.getString("titleAs") ?: "",
                        updateDate = doc.getString("updateDate") ?: "",
                        detailEn = doc.getString("detailEn") ?: "",
                        detailAs = doc.getString("detailAs") ?: "",
                        officialLink = doc.getString("officialLink") ?: "https://assam.gov.in",
                        isImportantNotice = doc.getBoolean("isImportantNotice") ?: false
                    )
                } catch (e: Exception) { null }
            } ?: emptyList()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Exception) {
            Log.e("FirebaseRepository", "Error fetching exam updates", e)
            emptyList()
        }
    }

    // Real-time Observers using addSnapshotListener

    fun observeQuestions(): Flow<List<QuestionEntity>> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            val db = firestore
            if (db == null) {
                trySend(emptyList())
            } else {
                listener = db.collection("questions")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("FirebaseRepository", "Error observing questions", error)
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val list = snapshot.documents.mapNotNull { doc ->
                                try {
                                    QuestionEntity(
                                        id = doc.getLong("id") ?: 0L,
                                        subject = doc.getString("subject") ?: "",
                                        topic = doc.getString("topic") ?: "",
                                        difficulty = doc.getString("difficulty") ?: "Medium",
                                        questionEn = doc.getString("questionEn") ?: "",
                                        questionAs = doc.getString("questionAs") ?: "",
                                        optionAEn = doc.getString("optionAEn") ?: "",
                                        optionBEn = doc.getString("optionBEn") ?: "",
                                        optionCEn = doc.getString("optionCEn") ?: "",
                                        optionDEn = doc.getString("optionDEn") ?: "",
                                        optionAAs = doc.getString("optionAAs") ?: "",
                                        optionBAs = doc.getString("optionBAs") ?: "",
                                        optionCAs = doc.getString("optionCAs") ?: "",
                                        optionDAs = doc.getString("optionDAs") ?: "",
                                        correctOptionIndex = doc.getLong("correctOptionIndex")?.toInt() ?: 0,
                                        explanationEn = doc.getString("explanationEn") ?: "",
                                        explanationAs = doc.getString("explanationAs") ?: "",
                                        isBookmarked = doc.getBoolean("isBookmarked") ?: false,
                                        isLiked = doc.getBoolean("isLiked") ?: false,
                                        isHidden = doc.getBoolean("isHidden") ?: false,
                                        examCategory = doc.getString("examCategory") ?: "ADRE",
                                        isPremium = doc.getBoolean("isPremium") ?: false,
                                        questionType = doc.getString("questionType") ?: "Expected",
                                        isReported = doc.getBoolean("isReported") ?: false,
                                        cachedAt = doc.getLong("cachedAt") ?: System.currentTimeMillis(),
                                        lastAccessedAt = doc.getLong("lastAccessedAt") ?: System.currentTimeMillis(),
                                        version = doc.getLong("version")?.toInt() ?: 1,
                                        updatedAt = doc.getLong("updatedAt") ?: 0L,
                                        firebaseId = doc.getString("firebaseId") ?: doc.id
                                    )
                                } catch (e: Throwable) {
                                    null
                                }
                            }
                            trySend(list)
                        }
                    }
            }
        } catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error setting up questions observer", e)
            trySend(emptyList())
        }
        awaitClose { listener?.remove() }
    }

    fun observeMockTests(): Flow<List<MockTestEntity>> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            val db = firestore
            if (db == null) {
                trySend(emptyList())
            } else {
                listener = db.collection("mock_tests")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("FirebaseRepository", "Error observing mock tests", error)
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val list = snapshot.documents.mapNotNull { doc ->
                                try {
                                    MockTestEntity(
                                        id = doc.getLong("id") ?: 0L,
                                        titleEn = doc.getString("titleEn") ?: "",
                                        titleAs = doc.getString("titleAs") ?: "",
                                        category = doc.getString("category") ?: "ADRE",
                                        durationMinutes = doc.getLong("durationMinutes")?.toInt() ?: 45,
                                        totalQuestions = doc.getLong("totalQuestions")?.toInt() ?: 10,
                                        totalMarks = doc.getLong("totalMarks")?.toInt() ?: 10,
                                        isScheduled = doc.getBoolean("isScheduled") ?: false,
                                        scheduledDate = doc.getString("scheduledDate") ?: "",
                                        isCompleted = doc.getBoolean("isCompleted") ?: false,
                                        userScore = doc.getLong("userScore")?.toInt() ?: 0,
                                        userAccuracy = doc.getDouble("userAccuracy")?.toFloat() ?: 0f,
                                        userRank = doc.getLong("userRank")?.toInt() ?: 0,
                                        userPercentile = doc.getDouble("userPercentile")?.toFloat() ?: 0f,
                                        isPublished = doc.getBoolean("isPublished") ?: true,
                                        testType = doc.getString("testType") ?: "Full-Length",
                                        subjectOrChapter = doc.getString("subjectOrChapter") ?: "",
                                        negativeMarking = doc.getString("negativeMarking") ?: "0.25 Marks",
                                        difficulty = doc.getString("difficulty") ?: "Medium",
                                        isPremium = doc.getBoolean("isPremium") ?: false,
                                        inProgress = doc.getBoolean("inProgress") ?: false,
                                        questionsAnswered = doc.getLong("questionsAnswered")?.toInt() ?: 0,
                                        timeRemainingSeconds = doc.getLong("timeRemainingSeconds")?.toInt() ?: 0,
                                        questionIds = doc.getString("questionIds") ?: "",
                                        markPerQuestion = doc.getDouble("markPerQuestion")?.toFloat() ?: 1f
                                    )
                                } catch (e: Throwable) {
                                    null
                                }
                            }
                            trySend(list)
                        }
                    }
            }
        } catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error setting up mock tests observer", e)
            trySend(emptyList())
        }
        awaitClose { listener?.remove() }
    }

    fun observeStudyNotes(): Flow<List<StudyNoteEntity>> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            val db = firestore
            if (db == null) {
                trySend(emptyList())
            } else {
                listener = db.collection("study_notes")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("FirebaseRepository", "Error observing study notes", error)
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val list = snapshot.documents.mapNotNull { doc ->
                                try {
                                    StudyNoteEntity(
                                        id = doc.getLong("id") ?: 0L,
                                        subject = doc.getString("subject") ?: "",
                                        topic = doc.getString("topic") ?: "",
                                        titleEn = doc.getString("titleEn") ?: "",
                                        titleAs = doc.getString("titleAs") ?: "",
                                        contentEn = doc.getString("contentEn") ?: "",
                                        contentAs = doc.getString("contentAs") ?: "",
                                        isBookmarked = doc.getBoolean("isBookmarked") ?: false,
                                        isDownloaded = doc.getBoolean("isDownloaded") ?: false,
                                        readTimeMinutes = doc.getLong("readTimeMinutes")?.toInt() ?: 5,
                                        isPremium = doc.getBoolean("isPremium") ?: false
                                    )
                                } catch (e: Throwable) {
                                    null
                                }
                            }
                            trySend(list)
                        }
                    }
            }
        } catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error setting up study notes observer", e)
            trySend(emptyList())
        }
        awaitClose { listener?.remove() }
    }

    fun observeExamUpdates(): Flow<List<ExamUpdateEntity>> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            val db = firestore
            if (db == null) {
                trySend(emptyList())
            } else {
                listener = db.collection("exam_updates")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("FirebaseRepository", "Error observing exam updates", error)
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val list = snapshot.documents.mapNotNull { doc ->
                                try {
                                    ExamUpdateEntity(
                                        id = doc.getLong("id") ?: 0L,
                                        examName = doc.getString("examName") ?: "",
                                        category = doc.getString("category") ?: "",
                                        titleEn = doc.getString("titleEn") ?: "",
                                        titleAs = doc.getString("titleAs") ?: "",
                                        updateDate = doc.getString("updateDate") ?: "",
                                        detailEn = doc.getString("detailEn") ?: "",
                                        detailAs = doc.getString("detailAs") ?: "",
                                        officialLink = doc.getString("officialLink") ?: "https://assam.gov.in",
                                        isImportantNotice = doc.getBoolean("isImportantNotice") ?: false
                                    )
                                } catch (e: Throwable) {
                                    null
                                }
                            }
                            trySend(list)
                        }
                    }
            }
        } catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error setting up exam updates observer", e)
            trySend(emptyList())
        }
        awaitClose { listener?.remove() }
    }

    fun observeBanners(): Flow<List<BannerEntity>> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            val db = firestore
            if (db == null) {
                trySend(emptyList())
            } else {
                listener = db.collection("banners")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("FirebaseRepository", "Error observing banners", error)
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val list = snapshot.documents.mapNotNull { doc ->
                                try {
                                    BannerEntity(
                                        id = doc.getLong("id") ?: 0L,
                                        titleEn = doc.getString("titleEn") ?: "",
                                        titleAs = doc.getString("titleAs") ?: "",
                                        subtitleEn = doc.getString("subtitleEn") ?: "",
                                        subtitleAs = doc.getString("subtitleAs") ?: "",
                                        badgeText = doc.getString("badgeText") ?: "",
                                        type = doc.getString("type") ?: "INFORMATION",
                                        actionUrl = doc.getString("actionUrl") ?: "",
                                        isActive = doc.getBoolean("isActive") ?: true,
                                        imageUrl = doc.getString("imageUrl") ?: "",
                                        actionType = doc.getString("actionType") ?: "Link",
                                        offerValidity = doc.getString("offerValidity") ?: "",
                                        planPrice = doc.getString("planPrice") ?: "",
                                        discount = doc.getString("discount") ?: "",
                                        finalPrice = doc.getString("finalPrice") ?: ""
                                    )
                                } catch (e: Throwable) {
                                    null
                                }
                            }
                            trySend(list)
                        }
                    }
            }
        } catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error setting up banners observer", e)
            trySend(emptyList())
        }
        awaitClose { listener?.remove() }
    }

    fun observePlans(): Flow<List<PlanEntity>> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            val db = firestore
            if (db == null) {
                trySend(emptyList())
            } else {
                listener = db.collection("plans")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("FirebaseRepository", "Error observing plans", error)
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val list = snapshot.documents.mapNotNull { doc ->
                                try {
                                    PlanEntity(
                                        id = doc.getLong("id")?.takeIf { it != 0L } ?: (doc.id.hashCode().toLong().let { if (it < 0) -it else it }),
                                        planName = doc.getString("planName") ?: "",
                                        planPrice = doc.getString("planPrice") ?: "",
                                        discount = doc.getString("discount") ?: "",
                                        finalPrice = doc.getString("finalPrice") ?: "",
                                        offerValidity = doc.getString("offerValidity") ?: "",
                                        planValidity = doc.getString("planValidity") ?: "",
                                        features = doc.getString("features") ?: "",
                                        contents = doc.getString("contents") ?: "",
                                        isActive = doc.getBoolean("isActive") ?: true,
                                        imageUrl = doc.getString("imageUrl") ?: ""
                                    )
                                } catch (e: Throwable) {
                                    null
                                }
                            }
                            trySend(list)
                        }
                    }
            }
        } catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error setting up plans observer", e)
            trySend(emptyList())
        }
        awaitClose { listener?.remove() }
    }

    fun observeExams(): Flow<List<ExamEntity>> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            val db = firestore
            if (db == null) {
                trySend(emptyList())
            } else {
                listener = db.collection("exams")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("FirebaseRepository", "Error observing exams", error)
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val list = snapshot.documents.mapNotNull { doc ->
                                try {
                                    ExamEntity(
                                        id = doc.getLong("id")?.takeIf { it != 0L } ?: (doc.id.hashCode().toLong().let { if (it < 0) -it else it }),
                                        firebaseId = doc.id,
                                        title = doc.getString("title") ?: "",
                                        subtitle = doc.getString("subtitle") ?: "",
                                        status = doc.getString("status") ?: "Active",
                                        updatedAt = doc.getLong("updatedAt") ?: 0L,
                                        version = doc.getLong("version")?.toInt() ?: 1,
                                        syncStatus = "SYNCED"
                                    )
                                } catch (e: Throwable) {
                                    null
                                }
                            }
                            trySend(list)
                        }
                    }
            }
        } catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error setting up exams observer", e)
            trySend(emptyList())
        }
        awaitClose { listener?.remove() }
    }

    fun observeSubjectsChapters(): Flow<List<SubjectChapterEntity>> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            val db = firestore
            if (db == null) {
                trySend(emptyList())
            } else {
                listener = db.collection("subjects_chapters")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("FirebaseRepository", "Error observing subjects chapters", error)
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val list = snapshot.documents.mapNotNull { doc ->
                                try {
                                    SubjectChapterEntity(
                                        id = doc.getLong("id") ?: 0L,
                                        subject = doc.getString("subject") ?: "",
                                        chapter = doc.getString("chapter") ?: ""
                                    )
                                } catch (e: Throwable) {
                                    null
                                }
                            }
                            trySend(list)
                        }
                    }
            }
        } catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error setting up subjects chapters observer", e)
            trySend(emptyList())
        }
        awaitClose { listener?.remove() }
    }

    fun observeFaqs(): Flow<List<FaqEntity>> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            val db = firestore
            if (db == null) {
                trySend(emptyList())
            } else {
                listener = db.collection("faqs")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("FirebaseRepository", "Error observing faqs", error)
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val list = snapshot.documents.mapNotNull { doc ->
                                try {
                                    FaqEntity(
                                        id = doc.getLong("id") ?: 0L,
                                        questionEn = doc.getString("questionEn") ?: "",
                                        questionAs = doc.getString("questionAs") ?: "",
                                        answerEn = doc.getString("answerEn") ?: "",
                                        answerAs = doc.getString("answerAs") ?: ""
                                    )
                                } catch (e: Throwable) {
                                    null
                                }
                            }
                            trySend(list)
                        }
                    }
            }
        } catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error setting up faqs observer", e)
            trySend(emptyList())
        }
        awaitClose { listener?.remove() }
    }

    fun observePendingRequests(): Flow<List<PendingRequestEntity>> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            val db = firestore
            if (db == null) {
                trySend(emptyList())
            } else {
                listener = db.collection("pending_requests")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) return@addSnapshotListener
                        if (snapshot != null) {
                            val list = snapshot.documents.mapNotNull { doc ->
                                try {
                                    PendingRequestEntity(
                                        id = doc.getLong("id") ?: (doc.id.hashCode().toLong().let { if (it < 0) -it else it }),
                                        requestType = doc.getString("requestType") ?: "",
                                        title = doc.getString("title") ?: "",
                                        description = doc.getString("description") ?: "",
                                        targetId = doc.getString("targetId") ?: "",
                                        payloadJson = doc.getString("payloadJson") ?: "",
                                        requestedBy = doc.getString("requestedBy") ?: "",
                                        timestamp = doc.getString("timestamp") ?: "",
                                        status = doc.getString("status") ?: "PENDING"
                                    )
                                } catch (e: Throwable) { null }
                            }
                            trySend(list)
                        }
                    }
            }
        } catch (e: Throwable) { trySend(emptyList()) }
        awaitClose { listener?.remove() }
    }

    fun observeActivityLogs(): Flow<List<ActivityLogEntity>> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            val db = firestore
            if (db == null) {
                trySend(emptyList())
            } else {
                listener = db.collection("activity_logs")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) return@addSnapshotListener
                        if (snapshot != null) {
                            val list = snapshot.documents.mapNotNull { doc ->
                                try {
                                    ActivityLogEntity(
                                        id = doc.getLong("id") ?: (doc.id.hashCode().toLong().let { if (it < 0) -it else it }),
                                        role = doc.getString("role") ?: "",
                                        actionDetails = doc.getString("action") ?: doc.getString("details") ?: "",
                                        userEmail = doc.getString("userEmail") ?: "",
                                        timestamp = doc.getLong("timestamp") ?: 0L
                                    )
                                } catch (e: Throwable) { null }
                            }
                            trySend(list)
                        }
                    }
            }
        } catch (e: Throwable) { trySend(emptyList()) }
        awaitClose { listener?.remove() }
    }

    fun observeNotifications(): Flow<List<NotificationEntity>> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            val db = firestore
            if (db == null) {
                trySend(emptyList())
            } else {
                listener = db.collection("notifications")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) return@addSnapshotListener
                        if (snapshot != null) {
                            val list = snapshot.documents.mapNotNull { doc ->
                                try {
                                    NotificationEntity(
                                        id = doc.getLong("id") ?: (doc.id.hashCode().toLong().let { if (it < 0) -it else it }),
                                        title = doc.getString("title") ?: "",
                                        body = doc.getString("body") ?: "",
                                        timestamp = doc.getString("timestamp") ?: "Just now",
                                        category = doc.getString("category") ?: "General",
                                        isRead = doc.getBoolean("isRead") ?: false
                                    )
                                } catch (e: Throwable) { null }
                            }
                            trySend(list)
                        }
                    }
            }
        } catch (e: Throwable) { trySend(emptyList()) }
        awaitClose { listener?.remove() }
    }

    private fun docToAboutConfig(doc: com.google.firebase.firestore.DocumentSnapshot): AboutConfigEntity {
        return AboutConfigEntity(
            id = doc.getLong("id")?.toInt() ?: 1,
            appTitle = doc.getString("appTitle") ?: "Jukti",
            appSubtitleEn = doc.getString("appSubtitleEn") ?: "Test Your Knowledge",
            appSubtitleAs = doc.getString("appSubtitleAs") ?: "অসমৰ সৰ্ববৃহৎ পৰীক্ষা প্ৰস্তুতি এপ্প",
            versionText = doc.getString("versionText") ?: "Version 2026.1.0",
            missionEn = doc.getString("missionEn") ?: "Jukti is engineered to democratize competitive exam preparation for aspirants across Assam...",
            missionAs = doc.getString("missionAs") ?: "যুক্তি এপ্পৰ প্ৰধান উদ্দেশ্য হৈছে অসমৰ সকলো প্ৰতিযোগীতামূলক পৰীক্ষাৰ...",
            logoIconName = doc.getString("logoIconName") ?: "School",
            logoUrl = doc.getString("logoUrl") ?: "",
            logoUpdatedAt = doc.getLong("logoUpdatedAt") ?: 0L,
            copyrightText = doc.getString("copyrightText") ?: "Copyright © 2026 Jukti Education Portal. All rights reserved.",
            developerTagline = doc.getString("developerTagline") ?: "Designed & Developed for Assam Aspirants",
            contactEmail = doc.getString("contactEmail") ?: "juktieducation@gmail.com",
            contactPhone = doc.getString("contactPhone") ?: "+91 98765 43210",
            contactTelegram = doc.getString("contactTelegram") ?: "t.me/JuktiAssam",
            contactWhatsapp = doc.getString("contactWhatsapp") ?: "Community Group",
            adminEmails = doc.getString("adminEmails") ?: "",
            refundPolicyEn = doc.getString("refundPolicyEn") ?: "Our policy lasts 7 days...",
            refundPolicyAs = doc.getString("refundPolicyAs") ?: "আমাৰ ৰিফাণ্ড পলিচি ক্ৰয় কৰাৰ ৭ দিনৰ বাবে প্ৰযোজ্য...",
            founderName = doc.getString("founderName") ?: "Pinku Bora",
            founderTitle = doc.getString("founderTitle") ?: "Founder & Creator of Jukti",
            founderCredential = doc.getString("founderCredential") ?: "ADRE 2022 Qualifier",
            founderDescription = doc.getString("founderDescription") ?: "Jukti was created with a simple vision...",
            founderPhotoUrl = doc.getString("founderPhotoUrl") ?: "",
            founderTagline = doc.getString("founderTagline") ?: "Jukti — Test Your Knowledge.",
            privacyPolicyContent = doc.getString("privacyPolicyContent") ?: "",
            termsConditionsContent = doc.getString("termsConditionsContent") ?: "",
            playStoreUrl = doc.getString("playStoreUrl") ?: "https://ais-dev-mbq2e6ge5z4qs5wk3gkstx-397582032913.asia-southeast1.run.app"
        )
    }

    suspend fun fetchAboutConfig(): AboutConfigEntity? {
        return try {
            val db = firestore ?: return null
            val doc1 = db.collection("app_config").document("1").get().await()
            if (doc1.exists()) return docToAboutConfig(doc1)
            val docMain = db.collection("app_config").document("main_config").get().await()
            if (docMain.exists()) return docToAboutConfig(docMain)
            null
        } catch (e: Throwable) {
            Log.e("FirebaseRepository", "Error fetching AboutConfig", e)
            null
        }
    }

    fun observeAboutConfig(): Flow<AboutConfigEntity?> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            val db = firestore
            if (db == null) {
                trySend(null)
            } else {
                listener = db.collection("app_config").document("1")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("FirebaseRepository", "Error observing app_config/1", error)
                            return@addSnapshotListener
                        }
                        if (snapshot != null && snapshot.exists()) {
                            trySend(docToAboutConfig(snapshot))
                        }
                    }
            }
        } catch (e: Throwable) {
            trySend(null)
        }
        awaitClose { listener?.remove() }
    }

    fun observeUserProfile(email: String, explicitUid: String? = null): Flow<UserProfileEntity?> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            val db = firestore
            val auth = try { com.google.firebase.auth.FirebaseAuth.getInstance() } catch (e: Exception) { null }
            val currentUid = explicitUid ?: auth?.currentUser?.uid
            val trimmedEmail = email.trim().lowercase()
            val sanitizedEmailDocId = if (trimmedEmail.isNotBlank()) trimmedEmail.replace("@", "_at_").replace(".", "_dot_") else ""
            val docId = if (!currentUid.isNullOrBlank()) currentUid else sanitizedEmailDocId

            if (db == null || docId.isBlank()) {
                trySend(null)
            } else {
                listener = db.collection("users").document(docId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("FirebaseRepository", "Error observing user profile", error)
                            return@addSnapshotListener
                        }
                        if (snapshot != null && snapshot.exists()) {
                            val foundProfile = UserProfileEntity(
                                id = snapshot.getLong("id")?.toInt() ?: 1,
                                name = snapshot.getString("name") ?: "Assam Scholar",
                                email = snapshot.getString("email") ?: email,
                                mobile = snapshot.getString("mobile") ?: "",
                                district = snapshot.getString("district") ?: "",
                                examGoal = snapshot.getString("examGoal") ?: "",
                                xp = snapshot.getLong("xp")?.toInt() ?: 0,
                                level = snapshot.getLong("level")?.toInt() ?: 1,
                                dailyStreak = snapshot.getLong("dailyStreak")?.toInt() ?: 0,
                                totalSolved = snapshot.getLong("totalSolved")?.toInt() ?: 0,
                                correctCount = snapshot.getLong("correctCount")?.toInt() ?: 0,
                                totalTimeMinutes = snapshot.getLong("totalTimeMinutes")?.toInt() ?: 0,
                                isPremium = snapshot.getBoolean("isPremium") ?: false,
                                role = snapshot.getString("role") ?: "USER",
                                firebaseProjectId = snapshot.getString("firebaseProjectId") ?: "jukti-26035",
                                joinedDate = snapshot.getString("joinedDate") ?: "Jul 2026",
                                isLoggedIn = snapshot.getBoolean("isLoggedIn") ?: true,
                                currentDeviceId = snapshot.getString("currentDeviceId") ?: "",
                                activeDeviceId = snapshot.getString("activeDeviceId") ?: "",
                                uid = currentUid ?: snapshot.getString("uid") ?: snapshot.id,
                                profileName = snapshot.getString("profileName") ?: "",
                                registrationName = snapshot.getString("registrationName") ?: "",
                                googleName = snapshot.getString("googleName") ?: ""
                            )
                            trySend(foundProfile)
                        } else {
                            trySend(null)
                        }
                    }
            }
        } catch (e: Throwable) {
            trySend(null)
        }
        awaitClose { listener?.remove() }
    }
}
