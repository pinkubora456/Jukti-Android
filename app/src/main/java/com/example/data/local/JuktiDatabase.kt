package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        QuestionEntity::class,
        MockTestEntity::class,
        StudyNoteEntity::class,
        ExamUpdateEntity::class,
        BannerEntity::class,
        NotificationEntity::class,
        UserProfileEntity::class,
        AboutConfigEntity::class,
        PlanEntity::class,
        ExamEntity::class,
        SubjectChapterEntity::class,
        PendingRequestEntity::class,
        FaqEntity::class
    ],
    version = 16,
    exportSchema = false
)
abstract class JuktiDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun mockTestDao(): MockTestDao
    abstract fun studyNoteDao(): StudyNoteDao
    abstract fun examUpdateDao(): ExamUpdateDao
    abstract fun bannerDao(): BannerDao
    abstract fun notificationDao(): NotificationDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun aboutConfigDao(): AboutConfigDao
    abstract fun planDao(): PlanDao
    abstract fun examDao(): ExamDao
    abstract fun subjectChapterDao(): SubjectChapterDao
    abstract fun pendingRequestDao(): PendingRequestDao
    abstract fun faqDao(): FaqDao

    companion object {
        @Volatile
        private var INSTANCE: JuktiDatabase? = null

        fun getDatabase(context: Context): JuktiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JuktiDatabase::class.java,
                    "jukti_exam_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
