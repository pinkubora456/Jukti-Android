package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        QuestionEntity::class,
        MockTestEntity::class,
        StudyNoteEntity::class,
        ExamUpdateEntity::class,
        BannerEntity::class,
        NotificationEntity::class,
        NotificationCategoryEntity::class,
        UserProfileEntity::class,
        AboutConfigEntity::class,
        PlanEntity::class,
        ExamEntity::class,
        SubjectChapterEntity::class,
        PendingRequestEntity::class,
        FaqEntity::class,
        QuestionProgressEntity::class,
        ActivityLogEntity::class
    ],
    version = 20,
    exportSchema = false
)
abstract class JuktiDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun mockTestDao(): MockTestDao
    abstract fun studyNoteDao(): StudyNoteDao
    abstract fun examUpdateDao(): ExamUpdateDao
    abstract fun bannerDao(): BannerDao
    abstract fun notificationDao(): NotificationDao
    abstract fun notificationCategoryDao(): NotificationCategoryDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun aboutConfigDao(): AboutConfigDao
    abstract fun planDao(): PlanDao
    abstract fun examDao(): ExamDao
    abstract fun subjectChapterDao(): SubjectChapterDao
    abstract fun pendingRequestDao(): PendingRequestDao
    abstract fun faqDao(): FaqDao
    abstract fun questionProgressDao(): QuestionProgressDao
    abstract fun activityLogDao(): ActivityLogDao

    companion object {
        @Volatile
        private var INSTANCE: JuktiDatabase? = null

        fun getDatabase(context: Context): JuktiDatabase {
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JuktiDatabase::class.java,
                    "jukti_exam_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = database.notificationCategoryDao()
                                listOf("General", "Mock Test", "Exam Update", "Study Note", "Announcement").forEach {
                                    dao.insertNotificationCategory(NotificationCategoryEntity(name = it))
                                }
                            }
                        }
                    }
                })
                .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
