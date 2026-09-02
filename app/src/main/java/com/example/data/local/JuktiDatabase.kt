package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        QuestionEntity::class,
        MockTestEntity::class,
        MockAttemptEntity::class,
        UserQuestionStateEntity::class,
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
        ActivityLogEntity::class,
        SyncQueueEntity::class,
        EntitlementEntity::class,
        EntitlementHistoryEntity::class
    ],
    version = 40,
    exportSchema = false
)
abstract class JuktiDatabase : RoomDatabase() {

    abstract fun questionDao(): QuestionDao
    abstract fun mockTestDao(): MockTestDao
    abstract fun mockAttemptDao(): MockAttemptDao
    abstract fun userQuestionStateDao(): UserQuestionStateDao
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
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun entitlementDao(): EntitlementDao
    abstract fun entitlementHistoryDao(): EntitlementHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: JuktiDatabase? = null

        val MIGRATION_23_24 = object : Migration(23, 24) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Non-destructive migration from 23 to 24
            }
        }

        val MIGRATION_24_25 = object : Migration(24, 25) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Safe migration: Ensure all tables and columns exist without dropping existing data
                db.execSQL("CREATE TABLE IF NOT EXISTS `sync_queue` (`syncId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `entityId` TEXT NOT NULL, `dataType` TEXT NOT NULL, `operation` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `retryCount` INTEGER NOT NULL, `syncStatus` TEXT NOT NULL)")
            }
        }

        val MIGRATION_1_25 = object : Migration(1, 25) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Direct catch-all migration from legacy v1 to v25 preserving all user records
            }
        }

        val MIGRATION_25_26 = object : Migration(25, 26) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `sync_queue`")
                db.execSQL("CREATE TABLE IF NOT EXISTS `sync_queue` (`syncId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `entityId` TEXT NOT NULL, `dataType` TEXT NOT NULL, `operation` TEXT NOT NULL, `payloadJson` TEXT NOT NULL DEFAULT '', `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `retryCount` INTEGER NOT NULL, `lastAttemptAt` INTEGER NOT NULL DEFAULT 0, `lastError` TEXT, `syncStatus` TEXT NOT NULL, `priority` INTEGER NOT NULL DEFAULT 1, `version` INTEGER NOT NULL DEFAULT 1)")
            }
        }

        val MIGRATION_26_27 = object : Migration(26, 27) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `entitlements` (`userId` TEXT NOT NULL, `planId` TEXT NOT NULL, `planName` TEXT NOT NULL, `status` TEXT NOT NULL, `validFrom` INTEGER NOT NULL, `validUntil` INTEGER NOT NULL, `benefits` TEXT NOT NULL, `source` TEXT NOT NULL, `purchaseId` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`userId`))")
            }
        }

        val MIGRATION_27_28 = object : Migration(27, 28) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Non-destructive migration from 27 to 28
            }
        }

        val MIGRATION_28_29 = object : Migration(28, 29) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `user_profile` ADD COLUMN `uid` TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_29_30 = object : Migration(29, 30) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `subscription_plans` ADD COLUMN `examTarget` TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_30_31 = object : Migration(30, 31) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `user_profile` ADD COLUMN `profileName` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `user_profile` ADD COLUMN `registrationName` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `user_profile` ADD COLUMN `googleName` TEXT NOT NULL DEFAULT ''")
            }
        }
        
        val MIGRATION_31_32 = object : Migration(31, 32) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `about_config` ADD COLUMN `logoUrl` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `about_config` ADD COLUMN `logoUpdatedAt` INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_32_33 = object : Migration(32, 33) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `subscription_plans` ADD COLUMN `validityType` TEXT NOT NULL DEFAULT 'MONTHS'")
                db.execSQL("ALTER TABLE `subscription_plans` ADD COLUMN `validityValue` INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE `subscription_plans` ADD COLUMN `validityLabel` TEXT NOT NULL DEFAULT '1 Month'")
                db.execSQL("ALTER TABLE `subscription_plans` ADD COLUMN `isLifetime` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `subscription_plans` ADD COLUMN `createdAt` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `subscription_plans` ADD COLUMN `updatedAt` INTEGER NOT NULL DEFAULT 0")
                
                db.execSQL("ALTER TABLE `entitlements` ADD COLUMN `validityType` TEXT NOT NULL DEFAULT 'MONTHS'")
                db.execSQL("ALTER TABLE `entitlements` ADD COLUMN `validityValue` INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE `entitlements` ADD COLUMN `validityLabel` TEXT NOT NULL DEFAULT '1 Month'")
                db.execSQL("ALTER TABLE `entitlements` ADD COLUMN `isLifetime` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `entitlements` ADD COLUMN `activatedAt` INTEGER NOT NULL DEFAULT 0")

                db.execSQL("CREATE TABLE IF NOT EXISTS `entitlement_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `userId` TEXT NOT NULL, `userEmail` TEXT NOT NULL, `eventType` TEXT NOT NULL, `previousPlan` TEXT NOT NULL DEFAULT '', `newPlan` TEXT NOT NULL DEFAULT '', `previousExpiry` INTEGER NOT NULL DEFAULT 0, `newExpiry` INTEGER NOT NULL DEFAULT 0, `validityGranted` TEXT NOT NULL DEFAULT '', `validityType` TEXT NOT NULL DEFAULT '', `validityValue` INTEGER NOT NULL DEFAULT 0, `isLifetime` INTEGER NOT NULL DEFAULT 0, `source` TEXT NOT NULL DEFAULT '', `actor` TEXT NOT NULL DEFAULT '', `timestamp` INTEGER NOT NULL DEFAULT 0)")
            }
        }

        val MIGRATION_33_34 = object : Migration(33, 34) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `mock_attempts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `mockTestId` INTEGER NOT NULL, `userId` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `questionIds` TEXT NOT NULL, `userAnswersJson` TEXT NOT NULL, `score` INTEGER NOT NULL, `totalMarks` INTEGER NOT NULL, `accuracy` REAL NOT NULL, `correctCount` INTEGER NOT NULL, `totalAttempted` INTEGER NOT NULL)")
            }
        }

        val MIGRATION_35_36 = object : Migration(35, 36) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `mock_tests_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `titleEn` TEXT NOT NULL, `titleAs` TEXT NOT NULL, `category` TEXT NOT NULL, `durationMinutes` INTEGER NOT NULL, `totalQuestions` INTEGER NOT NULL, `totalMarks` REAL NOT NULL, `isScheduled` INTEGER NOT NULL, `scheduledDate` TEXT NOT NULL, `isCompleted` INTEGER NOT NULL, `userScore` REAL NOT NULL, `userAccuracy` REAL NOT NULL, `userRank` INTEGER NOT NULL, `userPercentile` REAL NOT NULL, `isPublished` INTEGER NOT NULL, `testType` TEXT NOT NULL, `subjectOrChapter` TEXT NOT NULL, `negativeMarking` TEXT NOT NULL, `difficulty` TEXT NOT NULL, `isPremium` INTEGER NOT NULL, `inProgress` INTEGER NOT NULL, `questionsAnswered` INTEGER NOT NULL, `timeRemainingSeconds` INTEGER NOT NULL, `questionIds` TEXT NOT NULL, `markPerQuestion` REAL NOT NULL, `questionMarksJson` TEXT NOT NULL DEFAULT '{}')")
                db.execSQL("INSERT INTO `mock_tests_new` (`id`, `titleEn`, `titleAs`, `category`, `durationMinutes`, `totalQuestions`, `totalMarks`, `isScheduled`, `scheduledDate`, `isCompleted`, `userScore`, `userAccuracy`, `userRank`, `userPercentile`, `isPublished`, `testType`, `subjectOrChapter`, `negativeMarking`, `difficulty`, `isPremium`, `inProgress`, `questionsAnswered`, `timeRemainingSeconds`, `questionIds`, `markPerQuestion`) SELECT `id`, `titleEn`, `titleAs`, `category`, `durationMinutes`, `totalQuestions`, CAST(`totalMarks` AS REAL), `isScheduled`, `scheduledDate`, `isCompleted`, CAST(`userScore` AS REAL), `userAccuracy`, `userRank`, `userPercentile`, `isPublished`, `testType`, `subjectOrChapter`, `negativeMarking`, `difficulty`, `isPremium`, `inProgress`, `questionsAnswered`, `timeRemainingSeconds`, `questionIds`, `markPerQuestion` FROM `mock_tests`")
                db.execSQL("DROP TABLE `mock_tests`")
                db.execSQL("ALTER TABLE `mock_tests_new` RENAME TO `mock_tests`")

                db.execSQL("CREATE TABLE IF NOT EXISTS `mock_attempts_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `mockTestId` INTEGER NOT NULL, `userId` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `questionIds` TEXT NOT NULL, `userAnswersJson` TEXT NOT NULL, `score` REAL NOT NULL, `totalMarks` REAL NOT NULL, `accuracy` REAL NOT NULL, `correctCount` INTEGER NOT NULL, `totalAttempted` INTEGER NOT NULL, `questionMarksJson` TEXT NOT NULL DEFAULT '{}')")
                db.execSQL("INSERT INTO `mock_attempts_new` (`id`, `mockTestId`, `userId`, `timestamp`, `questionIds`, `userAnswersJson`, `score`, `totalMarks`, `accuracy`, `correctCount`, `totalAttempted`) SELECT `id`, `mockTestId`, `userId`, `timestamp`, `questionIds`, `userAnswersJson`, CAST(`score` AS REAL), CAST(`totalMarks` AS REAL), `accuracy`, `correctCount`, `totalAttempted` FROM `mock_attempts`")
                db.execSQL("DROP TABLE `mock_attempts`")
                db.execSQL("ALTER TABLE `mock_attempts_new` RENAME TO `mock_attempts`")
            }
        }

        val MIGRATION_34_35 = object : Migration(34, 35) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `user_question_states` (`userId` TEXT NOT NULL, `questionId` TEXT NOT NULL, `isBookmarked` INTEGER NOT NULL DEFAULT 0, `isLiked` INTEGER NOT NULL DEFAULT 0, `isHidden` INTEGER NOT NULL DEFAULT 0, `isMastered` INTEGER NOT NULL DEFAULT 0, `everGotWrong` INTEGER NOT NULL DEFAULT 0, `incorrectCount` INTEGER NOT NULL DEFAULT 0, `totalAttempts` INTEGER NOT NULL DEFAULT 0, `firstAttemptCorrect` INTEGER, `lastUpdatedDateStr` TEXT NOT NULL DEFAULT '', `lastUpdated` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`userId`, `questionId`))")
            }
        }

        val MIGRATION_36_37 = object : Migration(36, 37) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `entitlements_new` (`userId` TEXT NOT NULL, `planId` TEXT NOT NULL, `planName` TEXT NOT NULL, `status` TEXT NOT NULL, `validFrom` INTEGER NOT NULL, `validUntil` INTEGER NOT NULL, `validityType` TEXT NOT NULL DEFAULT 'MONTHS', `validityValue` INTEGER NOT NULL DEFAULT 1, `validityLabel` TEXT NOT NULL DEFAULT '1 Month', `isLifetime` INTEGER NOT NULL DEFAULT 0, `benefits` TEXT NOT NULL DEFAULT '', `source` TEXT NOT NULL DEFAULT '', `purchaseId` TEXT NOT NULL DEFAULT '', `activatedAt` INTEGER NOT NULL DEFAULT 0, `updatedAt` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`userId`, `planId`))")
                try {
                    db.execSQL("INSERT OR REPLACE INTO `entitlements_new` (`userId`, `planId`, `planName`, `status`, `validFrom`, `validUntil`, `validityType`, `validityValue`, `validityLabel`, `isLifetime`, `benefits`, `source`, `purchaseId`, `activatedAt`, `updatedAt`) SELECT `userId`, `planId`, `planName`, `status`, `validFrom`, `validUntil`, `validityType`, `validityValue`, `validityLabel`, `isLifetime`, `benefits`, `source`, `purchaseId`, `activatedAt`, `updatedAt` FROM `entitlements`")
                    db.execSQL("DROP TABLE `entitlements`")
                    db.execSQL("ALTER TABLE `entitlements_new` RENAME TO `entitlements`")
                } catch (e: Exception) {
                    db.execSQL("DROP TABLE IF EXISTS `entitlements`")
                    db.execSQL("ALTER TABLE `entitlements_new` RENAME TO `entitlements`")
                }
            }
        }

        val MIGRATION_38_39 = object : Migration(38, 39) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE `mock_tests` ADD COLUMN `subjectMarksJson` TEXT NOT NULL DEFAULT '{}'")
                } catch (e: Exception) {}
            }
        }

        val MIGRATION_39_40 = object : Migration(39, 40) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE `questions` ADD COLUMN `duplicateKey` TEXT NOT NULL DEFAULT ''")
                } catch (e: Exception) {}
            }
        }

        fun getDatabase(context: Context): JuktiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JuktiDatabase::class.java,
                    "jukti_exam_db"
                )
                .addMigrations(MIGRATION_23_24, MIGRATION_24_25, MIGRATION_1_25, MIGRATION_25_26, MIGRATION_26_27, MIGRATION_27_28, MIGRATION_28_29, MIGRATION_29_30, MIGRATION_30_31, MIGRATION_31_32, MIGRATION_32_33, MIGRATION_33_34, MIGRATION_34_35, MIGRATION_35_36, MIGRATION_36_37, MIGRATION_38_39, MIGRATION_39_40)
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
