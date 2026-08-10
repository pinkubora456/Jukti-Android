package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions ORDER BY id DESC")
    fun getAllQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE subject = :subject")
    fun getQuestionsBySubject(subject: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE isBookmarked = 1")
    fun getBookmarkedQuestions(): Flow<List<QuestionEntity>>

    @Query("""
        SELECT q.* FROM questions q 
        LEFT JOIN question_progress p ON q.id = p.questionId 
        WHERE q.isBookmarked = 1 OR (p.everGotWrong = 1 AND p.isMastered = 0)
    """)
    fun getSmartPracticeQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE isHidden = 1")
    fun getHiddenQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE id = :id")
    suspend fun getQuestionById(id: Long): QuestionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<QuestionEntity>)

    @Update
    suspend fun updateQuestion(question: QuestionEntity)

    @Delete
    suspend fun deleteQuestion(question: QuestionEntity)

    @Query("DELETE FROM questions WHERE id = :id")
    suspend fun deleteQuestionById(id: Long)
}

@Dao
interface MockTestDao {
    @Query("SELECT * FROM mock_tests ORDER BY id DESC")
    fun getAllMockTests(): Flow<List<MockTestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMockTest(test: MockTestEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tests: List<MockTestEntity>)

    @Update
    suspend fun updateMockTest(test: MockTestEntity)

    @Delete
    suspend fun deleteMockTest(test: MockTestEntity)
}

@Dao
interface StudyNoteDao {
    @Query("SELECT * FROM study_notes ORDER BY id DESC")
    fun getAllNotes(): Flow<List<StudyNoteEntity>>

    @Query("SELECT * FROM study_notes WHERE isBookmarked = 1 OR isDownloaded = 1")
    fun getSavedNotes(): Flow<List<StudyNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: StudyNoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<StudyNoteEntity>)

    @Update
    suspend fun updateNote(note: StudyNoteEntity)

    @Delete
    suspend fun deleteNote(note: StudyNoteEntity)
}

@Dao
interface ExamUpdateDao {
    @Query("SELECT * FROM exam_updates ORDER BY id DESC")
    fun getAllUpdates(): Flow<List<ExamUpdateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpdate(update: ExamUpdateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(updates: List<ExamUpdateEntity>)

    @Update
    suspend fun updateExamUpdate(update: ExamUpdateEntity)

    @Delete
    suspend fun deleteUpdate(update: ExamUpdateEntity)
}

@Dao
interface BannerDao {
    @Query("SELECT * FROM banners WHERE isActive = 1 ORDER BY id ASC")
    fun getActiveBanners(): Flow<List<BannerEntity>>

    @Query("SELECT * FROM banners ORDER BY id DESC")
    fun getAllBanners(): Flow<List<BannerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBanner(banner: BannerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(banners: List<BannerEntity>)

    @Update
    suspend fun updateBanner(banner: BannerEntity)

    @Query("DELETE FROM banners WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Delete
    suspend fun deleteBanner(banner: BannerEntity)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY id DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifications: List<NotificationEntity>)

    @Delete
    suspend fun deleteNotification(notification: NotificationEntity)
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfileDirect(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)
}

@Dao
interface AboutConfigDao {
    @Query("SELECT * FROM about_config WHERE id = 1")
    fun getAboutConfig(): Flow<AboutConfigEntity?>

    @Query("SELECT * FROM about_config WHERE id = 1")
    suspend fun getAboutConfigDirect(): AboutConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAboutConfig(config: AboutConfigEntity)
}


@Dao
interface PlanDao {
    @Query("SELECT * FROM subscription_plans ORDER BY id DESC")
    fun getAllPlans(): Flow<List<PlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: PlanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(plans: List<PlanEntity>)

    @Delete
    suspend fun deletePlan(plan: PlanEntity)
}

@Dao
interface ExamDao {
    @Query("SELECT * FROM exams ORDER BY id DESC")
    fun getAllExams(): Flow<List<ExamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ExamEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exams: List<ExamEntity>)

    @Update
    suspend fun updateExam(exam: ExamEntity)

    @Delete
    suspend fun deleteExam(exam: ExamEntity)
}

@Dao
interface SubjectChapterDao {
    @Query("SELECT * FROM subjects_chapters ORDER BY subject ASC, chapter ASC")
    fun getAllSubjectsChapters(): Flow<List<SubjectChapterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjectChapter(subjectChapter: SubjectChapterEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(subjectsChapters: List<SubjectChapterEntity>)

    @Delete
    suspend fun deleteSubjectChapter(subjectChapter: SubjectChapterEntity)
}

@Dao
interface PendingRequestDao {
    @Query("SELECT * FROM pending_requests ORDER BY id DESC")
    fun getAllPendingRequests(): Flow<List<PendingRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: PendingRequestEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(requests: List<PendingRequestEntity>)

    @Update
    suspend fun updateRequest(request: PendingRequestEntity)

    @Delete
    suspend fun deleteRequest(request: PendingRequestEntity)
}

@Dao
interface FaqDao {
    @Query("SELECT * FROM faqs ORDER BY id ASC")
    fun getAllFaqs(): Flow<List<FaqEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaq(faq: FaqEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(faqs: List<FaqEntity>)

    @Update
    suspend fun updateFaq(faq: FaqEntity)

    @Delete
    suspend fun deleteFaq(faq: FaqEntity)
}

@Dao
interface QuestionProgressDao {
    @Query("SELECT * FROM question_progress WHERE questionId = :questionId")
    suspend fun getProgress(questionId: Long): QuestionProgressEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(progress: QuestionProgressEntity)
}


@Dao
interface ActivityLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLogEntity)

    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ActivityLogEntity>>

    @Query("DELETE FROM activity_logs WHERE role = 'ADMIN' AND timestamp < :thresholdTime")
    suspend fun deleteOldAdminLogs(thresholdTime: Long)

    @Query("DELETE FROM activity_logs WHERE role = 'OWNER' AND timestamp < :thresholdTime")
    suspend fun deleteOldOwnerLogs(thresholdTime: Long)
}


@Dao
interface NotificationCategoryDao {
    @Query("SELECT * FROM notification_categories")
    fun getAllNotificationCategories(): Flow<List<NotificationCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationCategory(category: NotificationCategoryEntity)

    @Delete
    suspend fun deleteNotificationCategory(category: NotificationCategoryEntity)
}

@Dao
interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue ORDER BY createdAt ASC")
    fun getAllSyncQueueFlow(): Flow<List<SyncQueueEntity>>

    @Query("SELECT * FROM sync_queue WHERE syncStatus = 'PENDING' OR syncStatus = 'FAILED' OR syncStatus = 'UPLOADING' ORDER BY createdAt ASC")
    fun getPendingSyncs(): Flow<List<SyncQueueEntity>>

    @Query("SELECT * FROM sync_queue WHERE syncStatus = 'PENDING' OR syncStatus = 'FAILED' OR syncStatus = 'UPLOADING' ORDER BY createdAt ASC")
    suspend fun getPendingSyncsList(): List<SyncQueueEntity>

    @Query("SELECT * FROM sync_queue WHERE dataType = :dataType AND entityId = :entityId LIMIT 1")
    suspend fun getSyncByEntity(dataType: String, entityId: String): SyncQueueEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSync(sync: SyncQueueEntity): Long

    @Update
    suspend fun updateSync(sync: SyncQueueEntity)

    @Delete
    suspend fun deleteSync(sync: SyncQueueEntity)

    @Query("DELETE FROM sync_queue WHERE syncId = :syncId")
    suspend fun deleteSyncById(syncId: Long)

    @Query("DELETE FROM sync_queue WHERE syncStatus = 'SYNCED'")
    suspend fun clearSyncedItems()

    @Query("DELETE FROM sync_queue WHERE dataType = :dataType AND entityId = :entityId")
    suspend fun deleteSyncByEntity(dataType: String, entityId: String)
}
