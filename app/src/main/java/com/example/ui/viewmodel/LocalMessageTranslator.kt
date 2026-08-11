package com.example.ui.viewmodel

import android.content.Context
import com.example.R

object LocalMessageTranslator {
    
    fun getAuthErrorMessageId(errorMsg: String?): Int {
        val msg = errorMsg ?: return R.string.auth_unknown_error
        val lower = msg.lowercase()
        return when {
            lower.contains("api key not valid") || lower.contains("invalid_key") -> R.string.db_unknown_failure
            lower.contains("user-not-found") || lower.contains("wrong-password") || lower.contains("invalid-credential") || lower.contains("invalid credentials") -> R.string.auth_invalid_credentials
            lower.contains("invalid otp") || lower.contains("invalid-otp") || lower.contains("incorrect otp") -> R.string.auth_invalid_otp
            lower.contains("expired otp") || lower.contains("otp-expired") || lower.contains("otp has expired") -> R.string.auth_expired_otp
            lower.contains("too many attempts") || lower.contains("too-many-requests") -> R.string.auth_too_many_attempts
            lower.contains("network") || lower.contains("connection") || lower.contains("offline") -> R.string.auth_failed_network
            lower.contains("session expired") || lower.contains("session-expired") -> R.string.auth_session_expired
            else -> R.string.auth_unknown_error
        }
    }

    fun getDatabaseErrorMessageId(errorMsg: String?): Int {
        val msg = errorMsg ?: return R.string.db_unknown_failure
        val lower = msg.lowercase()
        return when {
            lower.contains("protected") || lower.contains("owner account") || lower.contains("cannot be deleted or banned") -> R.string.error_owner_protected
            lower.contains("permission-denied") || lower.contains("permission denied") || lower.contains("don't have permission") || lower.contains("unauthorized") -> R.string.db_permission_denied
            lower.contains("read failure") || lower.contains("unable to load") || lower.contains("failed to get") || lower.contains("not found") -> R.string.db_read_failure
            lower.contains("write failure") || lower.contains("unable to save") || lower.contains("failed to write") || lower.contains("failed to set") || lower.contains("save plan failed") -> R.string.db_write_failure
            lower.contains("delete failure") || lower.contains("unable to delete") || lower.contains("failed to delete") -> R.string.db_delete_failure
            lower.contains("upload failure") || lower.contains("upload failed") -> R.string.db_upload_failure
            lower.contains("service unavailable") || lower.contains("unavailable") -> R.string.db_service_unavailable
            else -> R.string.db_unknown_failure
        }
    }

    fun translateAuthError(context: Context, errorMsg: String?): String {
        return context.getString(getAuthErrorMessageId(errorMsg))
    }

    fun translateDatabaseError(context: Context, errorMsg: String?): String {
        return context.getString(getDatabaseErrorMessageId(errorMsg))
    }

    fun getSuccessMessage(context: Context, key: String): String {
        return when (key) {
            "plan_created" -> context.getString(R.string.success_plan_created)
            "plan_updated" -> context.getString(R.string.success_plan_updated)
            "plan_deleted" -> context.getString(R.string.success_plan_deleted)
            "user_updated" -> context.getString(R.string.success_user_updated)
            "changes_saved" -> context.getString(R.string.success_changes_saved)
            "upload_completed" -> context.getString(R.string.success_upload_completed)
            "sync_completed" -> context.getString(R.string.success_sync_completed)
            "question_deleted" -> context.getString(R.string.success_question_deleted)
            "mock_uploaded" -> context.getString(R.string.success_mock_uploaded)
            "mock_updated" -> context.getString(R.string.success_mock_updated)
            "question_reported" -> context.getString(R.string.success_question_reported)
            "current_affairs_added" -> context.getString(R.string.success_current_affairs_added)
            "current_affairs_updated" -> context.getString(R.string.success_current_affairs_updated)
            "study_note_updated" -> context.getString(R.string.success_study_note_updated)
            "notifications_sent" -> context.getString(R.string.success_notifications_sent)
            "offline_saved" -> context.getString(R.string.offline_saved_locally)
            "sync_partial_failure" -> context.getString(R.string.sync_partial_failure)
            else -> key
        }
    }

    fun translateGeneralMessage(context: Context, msg: String): String {
        val lower = msg.lowercase()
        return when {
            // Success matches
            lower.contains("created successfully") || lower.contains("success") || lower.contains("imported") || lower.contains("saved") || lower.contains("synchronized successfully") -> {
                when {
                    lower.contains("plan created") -> context.getString(R.string.success_plan_created)
                    lower.contains("plan updated") -> context.getString(R.string.success_plan_updated)
                    lower.contains("plan deleted") -> context.getString(R.string.success_plan_deleted)
                    lower.contains("user updated") || lower.contains("user blocked") || lower.contains("user deleted") || lower.contains("user role updated") -> context.getString(R.string.success_user_updated)
                    lower.contains("changes saved") || lower.contains("saved successfully") -> context.getString(R.string.success_changes_saved)
                    lower.contains("upload completed") || lower.contains("uploaded successfully") -> context.getString(R.string.success_upload_completed)
                    lower.contains("data synchronized") || lower.contains("synchronized successfully") -> context.getString(R.string.success_sync_completed)
                    lower.contains("question deleted") -> context.getString(R.string.success_question_deleted)
                    lower.contains("mock uploaded") || lower.contains("mock test created") -> context.getString(R.string.success_mock_uploaded)
                    lower.contains("mock test updated") -> context.getString(R.string.success_mock_updated)
                    lower.contains("reported successfully") || lower.contains("report question success") -> context.getString(R.string.success_question_reported)
                    lower.contains("current affairs added") -> context.getString(R.string.success_current_affairs_added)
                    lower.contains("current affairs updated") -> context.getString(R.string.success_current_affairs_updated)
                    lower.contains("study note updated") || lower.contains("study note created") -> context.getString(R.string.success_study_note_updated)
                    lower.contains("notification sent") -> context.getString(R.string.success_notifications_sent)
                    lower.contains("saved locally") -> context.getString(R.string.offline_saved_locally)
                    lower.contains("could not be synced") -> context.getString(R.string.sync_partial_failure)
                    else -> msg // keep the success/import count message
                }
            }
            // Error matches
            lower.contains("error") || lower.contains("failed") || lower.contains("unable to") || lower.contains("exception") -> {
                translateDatabaseError(context, msg)
            }
            else -> msg
        }
    }
}
