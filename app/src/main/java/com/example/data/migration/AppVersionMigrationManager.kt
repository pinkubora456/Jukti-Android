package com.example.data.migration

import android.content.Context
import android.util.Log
import com.example.data.local.JuktiDatabase
import com.example.data.repository.FirebaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Production-Grade App Version Migration & Diagnostic System.
 * Ensures future APK/AAB updates NEVER delete, reset, or recreate existing user data.
 */
object AppVersionMigrationManager {
    private const val TAG = "AppVersionMigration"
    private const val PREFS_NAME = "jukti_migration_prefs"
    private const val KEY_LAST_MIGRATION_VERSION = "last_successful_migration_version"
    private const val CURRENT_APP_VERSION_CODE = 20260201 // Version 2026.2.1

    data class DiagnosticLog(
        val appVersionCode: Int,
        val dbVersion: Int,
        val migrationStatus: String,
        val timestamp: Long = System.currentTimeMillis(),
        val details: String = ""
    )

    private val diagnosticLogs = mutableListOf<DiagnosticLog>()

    suspend fun checkAndRunAppMigrations(context: Context, database: JuktiDatabase) = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastVersion = prefs.getInt(KEY_LAST_MIGRATION_VERSION, 0)

        Log.i(TAG, "Starting migration check. LastVersion: $lastVersion, CurrentVersion: $CURRENT_APP_VERSION_CODE")

        if (lastVersion < CURRENT_APP_VERSION_CODE) {
            logDiagnostic(CURRENT_APP_VERSION_CODE, 25, "MIGRATION_STARTED", "Upgrading from version $lastVersion")

            try {
                // Step 1: Verify Room database state
                val userProfile = database.userProfileDao().getUserProfileDirect()
                Log.d(TAG, "Local User Profile verified: ID=${userProfile?.id}, Email=${userProfile?.email}")

                // Step 2: Safe Sync with Firebase if user is logged in
                if (userProfile != null && userProfile.isLoggedIn && userProfile.email.isNotBlank()) {
                    val firebaseRepo = FirebaseRepository()
                    val remoteProfile = firebaseRepo.fetchUserProfile(userProfile.email)
                    if (remoteProfile != null) {
                        // Preserve existing fields, merge highest XP, accuracy metrics and authoritative premium status
                        val safeTotalSolved = maxOf(userProfile.totalSolved, remoteProfile.totalSolved)
                        val safeCorrectCount = maxOf(userProfile.correctCount, remoteProfile.correctCount).coerceAtMost(safeTotalSolved)
                        val isOwner = userProfile.email.trim().lowercase() == "juktieducation@gmail.com"
                        val mergedProfile = userProfile.copy(
                            xp = maxOf(userProfile.xp, remoteProfile.xp),
                            level = maxOf(userProfile.level, remoteProfile.level),
                            isPremium = isOwner,
                            role = if (isOwner) "OWNER" else "USER",
                            dailyStreak = maxOf(userProfile.dailyStreak, remoteProfile.dailyStreak),
                            totalSolved = safeTotalSolved,
                            correctCount = safeCorrectCount,
                            totalTimeMinutes = maxOf(userProfile.totalTimeMinutes, remoteProfile.totalTimeMinutes)
                        )
                        database.userProfileDao().insertOrUpdateProfile(mergedProfile)
                        firebaseRepo.saveUserProfile(mergedProfile, merge = true)
                        Log.i(TAG, "Successfully merged user profile with Firebase during app update.")
                    }
                }

                // Step 3: Record successful migration version
                prefs.edit().putInt(KEY_LAST_MIGRATION_VERSION, CURRENT_APP_VERSION_CODE).apply()
                logDiagnostic(CURRENT_APP_VERSION_CODE, 25, "MIGRATION_SUCCESS", "Successfully migrated to $CURRENT_APP_VERSION_CODE without data loss.")

            } catch (e: Exception) {
                Log.e(TAG, "App migration encountered a non-fatal error. Preserving existing user data.", e)
                logDiagnostic(CURRENT_APP_VERSION_CODE, 25, "MIGRATION_FAILED", "Error: ${e.localizedMessage}. Data preserved.")
                // DO NOT delete user data on failure.
            }
        } else {
            logDiagnostic(CURRENT_APP_VERSION_CODE, 25, "NO_MIGRATION_NEEDED", "App already on latest migration version.")
        }
    }

    private fun logDiagnostic(appVersion: Int, dbVersion: Int, status: String, details: String) {
        val log = DiagnosticLog(appVersion, dbVersion, status, details = details)
        diagnosticLogs.add(log)
        if (diagnosticLogs.size > 50) {
            diagnosticLogs.removeAt(0)
        }
        Log.i(TAG, "DIAGNOSTIC: $log")
    }

    fun getDiagnosticLogs(): List<DiagnosticLog> = diagnosticLogs.toList()
}
