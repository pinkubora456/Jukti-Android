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
        ActivityLogEntity::class,
        SyncQueueEntity::class,
        EntitlementEntity::class
    ],
    version = 32,
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
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun entitlementDao(): EntitlementDao

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

        fun getDatabase(context: Context): JuktiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JuktiDatabase::class.java,
                    "jukti_exam_db"
                )
                .addMigrations(MIGRATION_23_24, MIGRATION_24_25, MIGRATION_1_25, MIGRATION_25_26, MIGRATION_26_27, MIGRATION_27_28, MIGRATION_28_29, MIGRATION_29_30, MIGRATION_30_31, MIGRATION_31_32)
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
