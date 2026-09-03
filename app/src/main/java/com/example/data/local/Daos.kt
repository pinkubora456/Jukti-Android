package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
abstract class QuestionDao {
    @Query("DELETE FROM questions WHERE isPremium = 1")
    abstract suspend fun deletePremiumQuestions()

    @Query("SELECT * FROM questions ORDER BY id DESC")
    abstract fun getAllQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE subject = :subject")
    abstract fun getQuestionsBySubject(subject: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE id = :id")
    abstract suspend fun getQuestionById(id: Long): QuestionEntity?

    @Query("SELECT * FROM questions WHERE duplicateKey = '' OR duplicateKey IS NULL")
    abstract suspend fun getQuestionsMissingDuplicateKey(): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE duplicateKey = :key LIMIT 1")
    abstract suspend fun getQuestionByDuplicateKey(key: String): QuestionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertQuestionInternal(question: QuestionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAllInternal(questions: List<QuestionEntity>)
    
    @Transaction
    open suspend fun insertQuestion(question: QuestionEntity): Long {
        if (question.accessType == "PREMIUM" || question.isPremium) return -1L // Prevent inserting Premium questions into Room
        return insertQuestionInternal(question)
    }

    @Transaction
    open suspend fun insertAll(questions: List<QuestionEntity>) {
        val freeQuestions = questions.filter { it.accessType != "PREMIUM" && !it.isPremium }
        if (freeQuestions.isNotEmpty()) {
            insertAllInternal(freeQuestions)
        }
    }

    @Update
    abstract suspend fun updateQuestion(question: QuestionEntity)
    
    @Update
    abstract suspend fun updateQuestions(questions: List<QuestionEntity>)

    @Delete
    abstract suspend fun deleteQuestion(question: QuestionEntity)

    @Query("DELETE FROM questions WHERE id = :id")
    abstract suspend fun deleteQuestionById(id: Long)

    @Query("""
        SELECT subject, topic as chapter, COUNT(*) as questionCount 
        FROM questions 
        GROUP BY subject, topic
    """)
    abstract fun getSubjectChapterStats(): Flow<List<SubjectChapterStat>>

    @Query("""
        UPDATE questions 
        SET topic = :newChapter 
        WHERE subject = :subject AND topic = :oldChapter
    """)
    abstract suspend fun mergeChapter(subject: String, oldChapter: String, newChapter: String)

    @Query("""
        UPDATE questions
        SET subject = :newSubject
        WHERE subject = :oldSubject
    """)
    abstract suspend fun renameSubject(oldSubject: String, newSubject: String)

    @Query("""
        UPDATE questions
        SET topic = :newChapter
        WHERE subject = :subject AND topic = :oldChapter
    """)
    abstract suspend fun renameChapter(subject: String, oldChapter: String, newChapter: String)

    
    @Query("""
        SELECT topic as chapter, 
               COUNT(*) as total, 
               SUM(CASE WHEN difficulty = 'Easy' THEN 1 ELSE 0 END) as easy,
               SUM(CASE WHEN difficulty = 'Medium' THEN 1 ELSE 0 END) as medium,
               SUM(CASE WHEN difficulty = 'Hard' THEN 1 ELSE 0 END) as hard
        FROM questions
        WHERE (subject = :subject OR :subject = 'All Subjects')
          AND isReported = 0 
          AND (examCategory LIKE '%' || :exam || '%' OR :exam = 'All Exams')
        GROUP BY topic
        ORDER BY total DESC
    """)
    abstract fun getChapterStatsByExam(subject: String, exam: String): Flow<List<ChapterStatResult>>
}

@Dao
abstract class MockTestDao {
    @Query("DELETE FROM mock_tests WHERE isPremium = 1")
    abstract suspend fun deletePremiumMockTests()

    @Query("SELECT * FROM mock_tests ORDER BY id DESC")
    abstract fun getAllMockTests(): Flow<List<MockTestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertMockTestInternal(test: MockTestEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAllMockTestsInternal(tests: List<MockTestEntity>)

    @Transaction
    open suspend fun insertMockTest(test: MockTestEntity): Long {
        if (test.accessType == "PREMIUM" || test.isPremium) return -1L
        return insertMockTestInternal(test)
    }

    @Transaction
    open suspend fun insertAll(tests: List<MockTestEntity>) {
        val freeTests = tests.filter { it.accessType != "PREMIUM" && !it.isPremium }
        if (freeTests.isNotEmpty()) {
            insertAllMockTestsInternal(freeTests)
        }
    }

    @Update
    abstract suspend fun updateMockTest(test: MockTestEntity)

    @Delete
    abstract suspend fun deleteMockTest(test: MockTestEntity)
}

@Dao
interface MockAttemptDao {
    @Query("SELECT * FROM mock_attempts WHERE mockTestId = :mockTestId AND userId = :userId ORDER BY timestamp DESC")
    abstract fun getAttemptsForMock(mockTestId: Long, userId: String): Flow<List<MockAttemptEntity>>

    @Query("SELECT * FROM mock_attempts WHERE userId = :userId ORDER BY timestamp DESC")
    abstract fun getAllAttemptsForUser(userId: String): Flow<List<MockAttemptEntity>>

    @Query("SELECT * FROM mock_attempts WHERE mockTestId = :mockTestId ORDER BY score DESC, accuracy DESC, timestamp ASC")
    abstract fun getAllAttemptsForMock(mockTestId: Long): Flow<List<MockAttemptEntity>>

    @Query("SELECT * FROM mock_attempts WHERE id = :attemptId")
    abstract suspend fun getAttemptById(attemptId: Long): MockAttemptEntity?

    @Query("SELECT * FROM mock_attempts WHERE mockTestId = :mockTestId AND userId = :userId ORDER BY timestamp DESC LIMIT 1")
    abstract suspend fun getLatestAttemptForMock(mockTestId: Long, userId: String): MockAttemptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAttempt(attempt: MockAttemptEntity): Long

    @Query("DELETE FROM mock_attempts WHERE mockTestId = :mockTestId")
    abstract suspend fun deleteAttemptsForMock(mockTestId: Long)
}


@Dao
interface UserQuestionStateDao {
    @Query("SELECT * FROM user_question_states WHERE userId = :userId")
    abstract fun getUserStates(userId: String): Flow<List<UserQuestionStateEntity>>

    @Query("SELECT * FROM user_question_states WHERE userId = :userId AND questionId = :questionId")
    abstract suspend fun getState(userId: String, questionId: String): UserQuestionStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertState(state: UserQuestionStateEntity)
}

@Dao
abstract class StudyNoteDao {
    @Query("DELETE FROM study_notes WHERE isPremium = 1")
    abstract suspend fun deletePremiumStudyNotes()

    @Query("SELECT * FROM study_notes ORDER BY id DESC")
    abstract fun getAllNotes(): Flow<List<StudyNoteEntity>>

    @Query("SELECT * FROM study_notes WHERE isBookmarked = 1 OR isDownloaded = 1")
    abstract fun getSavedNotes(): Flow<List<StudyNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertNoteInternal(note: StudyNoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAllNotesInternal(notes: List<StudyNoteEntity>)

    @Transaction
    open suspend fun insertNote(note: StudyNoteEntity): Long {
        if (note.accessType == "PREMIUM" || note.isPremium) return -1L
        return insertNoteInternal(note)
    }

    @Transaction
    open suspend fun insertAll(notes: List<StudyNoteEntity>) {
        val freeNotes = notes.filter { it.accessType != "PREMIUM" && !it.isPremium }
        if (freeNotes.isNotEmpty()) {
            insertAllNotesInternal(freeNotes)
        }
    }

    @Update
    abstract suspend fun updateNote(note: StudyNoteEntity)

    @Delete
    abstract suspend fun deleteNote(note: StudyNoteEntity)
}

@Dao
interface ExamUpdateDao {
    @Query("SELECT * FROM exam_updates ORDER BY id DESC")
    abstract fun getAllUpdates(): Flow<List<ExamUpdateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertUpdate(update: ExamUpdateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(updates: List<ExamUpdateEntity>)

    @Update
    abstract suspend fun updateExamUpdate(update: ExamUpdateEntity)

    @Delete
    abstract suspend fun deleteUpdate(update: ExamUpdateEntity)
}

@Dao
interface BannerDao {
    @Query("SELECT * FROM banners WHERE isActive = 1 ORDER BY id ASC")
    abstract fun getActiveBanners(): Flow<List<BannerEntity>>

    @Query("SELECT * FROM banners ORDER BY id DESC")
    abstract fun getAllBanners(): Flow<List<BannerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertBanner(banner: BannerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(banners: List<BannerEntity>)

    @Update
    abstract suspend fun updateBanner(banner: BannerEntity)

    @Query("DELETE FROM banners WHERE id = :id")
    abstract suspend fun deleteById(id: Long)

    @Delete
    abstract suspend fun deleteBanner(banner: BannerEntity)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY id DESC")
    abstract fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertNotification(notification: NotificationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(notifications: List<NotificationEntity>)

    @Delete
    abstract suspend fun deleteNotification(notification: NotificationEntity)
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    abstract fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    abstract suspend fun getUserProfileDirect(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertOrUpdateProfileRaw(profile: UserProfileEntity)

    @Transaction
    open suspend fun insertOrUpdateProfile(profile: UserProfileEntity) {
        insertOrUpdateProfileRaw(profile.withResolvedName())
    }
}

@Dao
interface AboutConfigDao {
    @Query("SELECT * FROM about_config WHERE id = 1")
    abstract fun getAboutConfig(): Flow<AboutConfigEntity?>

    @Query("SELECT * FROM about_config WHERE id = 1")
    abstract suspend fun getAboutConfigDirect(): AboutConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertOrUpdateAboutConfig(config: AboutConfigEntity)
}


@Dao
interface PlanDao {
    @Query("SELECT * FROM subscription_plans ORDER BY id DESC")
    abstract fun getAllPlans(): Flow<List<PlanEntity>>
    
    @Query("SELECT * FROM subscription_plans ORDER BY id DESC")
    abstract suspend fun getAllPlansDirect(): List<PlanEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertPlan(plan: PlanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(plans: List<PlanEntity>)

    @Delete
    abstract suspend fun deletePlan(plan: PlanEntity)
}

@Dao
interface ExamDao {
    @Query("SELECT * FROM exams ORDER BY id DESC")
    abstract fun getAllExams(): Flow<List<ExamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertExam(exam: ExamEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(exams: List<ExamEntity>)

    @Update
    abstract suspend fun updateExam(exam: ExamEntity)

    @Delete
    abstract suspend fun deleteExam(exam: ExamEntity)
}

@Dao
interface SubjectChapterDao {
    @Query("SELECT * FROM subjects_chapters ORDER BY subject ASC, chapter ASC")
    abstract fun getAllSubjectsChapters(): Flow<List<SubjectChapterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertSubjectChapter(subjectChapter: SubjectChapterEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(subjectsChapters: List<SubjectChapterEntity>)

    @Update
    abstract suspend fun updateSubjectChapter(subjectChapter: SubjectChapterEntity)

    @Query("UPDATE subjects_chapters SET subject = :newSubject WHERE subject = :oldSubject")
    abstract suspend fun renameSubject(oldSubject: String, newSubject: String)

    @Query("UPDATE subjects_chapters SET chapter = :newChapter WHERE subject = :subject AND chapter = :oldChapter")
    abstract suspend fun renameChapter(subject: String, oldChapter: String, newChapter: String)

    @Query("DELETE FROM subjects_chapters WHERE subject = :subject AND chapter = :chapter")
    abstract suspend fun deleteSubjectChapterByNames(subject: String, chapter: String)

    @Query("DELETE FROM subjects_chapters WHERE subject = :subject")
    abstract suspend fun deleteSubject(subject: String)

    @Delete
    abstract suspend fun deleteSubjectChapter(subjectChapter: SubjectChapterEntity)

    @Query("DELETE FROM subjects_chapters")
    abstract suspend fun deleteAll()
}

@Dao
interface PendingRequestDao {
    @Query("SELECT * FROM pending_requests ORDER BY id DESC")
    abstract fun getAllPendingRequests(): Flow<List<PendingRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertRequest(request: PendingRequestEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(requests: List<PendingRequestEntity>)

    @Update
    abstract suspend fun updateRequest(request: PendingRequestEntity)

    @Delete
    abstract suspend fun deleteRequest(request: PendingRequestEntity)

    @Query("DELETE FROM pending_requests WHERE status != 'PENDING' AND timestamp < :thresholdTime")
    abstract suspend fun deleteOldRequests(thresholdTime: String)
}

@Dao
interface FaqDao {
    @Query("SELECT * FROM faqs ORDER BY id ASC")
    abstract fun getAllFaqs(): Flow<List<FaqEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertFaq(faq: FaqEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(faqs: List<FaqEntity>)

    @Update
    abstract suspend fun updateFaq(faq: FaqEntity)

    @Delete
    abstract suspend fun deleteFaq(faq: FaqEntity)
}


@Dao
interface ActivityLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertLog(log: ActivityLogEntity): Long

    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    abstract fun getAllLogs(): Flow<List<ActivityLogEntity>>

    @Query("DELETE FROM activity_logs WHERE role = 'ADMIN' AND timestamp < :thresholdTime")
    abstract suspend fun deleteOldAdminLogs(thresholdTime: Long)

    @Query("DELETE FROM activity_logs WHERE role = 'OWNER' AND timestamp < :thresholdTime")
    abstract suspend fun deleteOldOwnerLogs(thresholdTime: Long)

    @Query("DELETE FROM activity_logs WHERE timestamp < :thresholdTime")
    abstract suspend fun deleteLogsOlderThan(thresholdTime: Long)
}


@Dao
interface NotificationCategoryDao {
    @Query("SELECT * FROM notification_categories")
    abstract fun getAllNotificationCategories(): Flow<List<NotificationCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertNotificationCategory(category: NotificationCategoryEntity)

    @Delete
    abstract suspend fun deleteNotificationCategory(category: NotificationCategoryEntity)
}

@Dao
interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue ORDER BY createdAt ASC")
    abstract fun getAllSyncQueueFlow(): Flow<List<SyncQueueEntity>>

    @Query("SELECT * FROM sync_queue WHERE syncStatus = 'PENDING' OR syncStatus = 'FAILED' OR syncStatus = 'UPLOADING' ORDER BY createdAt ASC")
    abstract fun getPendingSyncs(): Flow<List<SyncQueueEntity>>

    @Query("SELECT * FROM sync_queue WHERE syncStatus = 'PENDING' OR syncStatus = 'FAILED' OR syncStatus = 'UPLOADING' ORDER BY createdAt ASC")
    abstract suspend fun getPendingSyncsList(): List<SyncQueueEntity>

    @Query("SELECT * FROM sync_queue WHERE dataType = :dataType AND entityId = :entityId LIMIT 1")
    abstract suspend fun getSyncByEntity(dataType: String, entityId: String): SyncQueueEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertSync(sync: SyncQueueEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAllSyncs(syncs: List<SyncQueueEntity>)

    @Update
    abstract suspend fun updateSync(sync: SyncQueueEntity)

    @Delete
    abstract suspend fun deleteSync(sync: SyncQueueEntity)

    @Query("DELETE FROM sync_queue WHERE syncId = :syncId")
    abstract suspend fun deleteSyncById(syncId: Long)

    @Query("DELETE FROM sync_queue WHERE syncStatus = 'SYNCED'")
    abstract suspend fun clearSyncedItems()

    @Query("DELETE FROM sync_queue")
    abstract suspend fun clearAll()

    @Query("DELETE FROM sync_queue WHERE dataType = :dataType AND entityId = :entityId")
    abstract suspend fun deleteSyncByEntity(dataType: String, entityId: String)
}

@Dao
interface EntitlementDao {
    @Query("SELECT * FROM entitlements WHERE userId = :userId AND :userId != ''")
    abstract fun getEntitlements(userId: String): Flow<List<EntitlementEntity>>

    @Query("SELECT * FROM entitlements WHERE userId = :userId AND :userId != ''")
    abstract suspend fun getEntitlementsDirect(userId: String): List<EntitlementEntity>

    @Query("SELECT * FROM entitlements WHERE (userId = :userId AND :userId != '') OR (userId = :altUserId1 AND :altUserId1 != '') OR (userId = :altUserId2 AND :altUserId2 != '') ORDER BY updatedAt DESC")
    abstract fun getEntitlementsMulti(userId: String, altUserId1: String = "", altUserId2: String = ""): Flow<List<EntitlementEntity>>

    @Query("SELECT * FROM entitlements WHERE (userId = :userId AND :userId != '') OR (userId = :altUserId1 AND :altUserId1 != '') OR (userId = :altUserId2 AND :altUserId2 != '') ORDER BY updatedAt DESC")
    abstract suspend fun getEntitlementsDirectMulti(userId: String, altUserId1: String = "", altUserId2: String = ""): List<EntitlementEntity>

    @Query("SELECT * FROM entitlements WHERE userId = :userId AND :userId != '' LIMIT 1")
    abstract fun getEntitlement(userId: String): Flow<EntitlementEntity?>

    @Query("SELECT * FROM entitlements WHERE userId = :userId AND :userId != '' LIMIT 1")
    abstract suspend fun getEntitlementDirect(userId: String): EntitlementEntity?

    @Query("SELECT * FROM entitlements WHERE (userId = :userId AND :userId != '') OR (userId = :altUserId1 AND :altUserId1 != '') OR (userId = :altUserId2 AND :altUserId2 != '') ORDER BY updatedAt DESC LIMIT 1")
    abstract fun getEntitlementMulti(userId: String, altUserId1: String = "", altUserId2: String = ""): Flow<EntitlementEntity?>

    @Query("SELECT * FROM entitlements WHERE (userId = :userId AND :userId != '') OR (userId = :altUserId1 AND :altUserId1 != '') OR (userId = :altUserId2 AND :altUserId2 != '') ORDER BY updatedAt DESC LIMIT 1")
    abstract suspend fun getEntitlementDirectMulti(userId: String, altUserId1: String = "", altUserId2: String = ""): EntitlementEntity?

    @Query("SELECT * FROM entitlements")
    abstract suspend fun getAllEntitlementsDirect(): List<EntitlementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertEntitlement(entitlement: EntitlementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertEntitlements(entitlements: List<EntitlementEntity>)

    @Query("DELETE FROM entitlements WHERE userId = :userId AND planId = :planId")
    abstract suspend fun deleteEntitlementByPlan(userId: String, planId: String)

    @Query("DELETE FROM entitlements WHERE userId = :userId")
    abstract suspend fun deleteEntitlement(userId: String)
}

@Dao
interface EntitlementHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertHistory(history: EntitlementHistoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(histories: List<EntitlementHistoryEntity>)

    @Query("SELECT * FROM entitlement_history WHERE (userId = :userId AND :userId != '') OR (userEmail = :userEmail AND :userEmail != '') ORDER BY timestamp DESC")
    abstract fun getHistoryForUser(userId: String, userEmail: String = ""): Flow<List<EntitlementHistoryEntity>>

    @Query("SELECT * FROM entitlement_history ORDER BY timestamp DESC LIMIT :limit")
    abstract fun getRecentHistory(limit: Int = 100): Flow<List<EntitlementHistoryEntity>>

    @Query("SELECT * FROM entitlement_history WHERE (userId = :userId AND :userId != '') OR (userEmail = :userEmail AND :userEmail != '') ORDER BY timestamp DESC")
    abstract suspend fun getHistoryForUserDirect(userId: String, userEmail: String = ""): List<EntitlementHistoryEntity>
}

