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

    @Delete
    suspend fun deleteBanner(banner: BannerEntity)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY id DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

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

