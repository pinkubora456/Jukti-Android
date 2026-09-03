package com.example.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

data class GoogleSignInResult(
    val firebaseUser: FirebaseUser? = null,
    val isCancelled: Boolean = false,
    val errorMessage: String? = null,
    val fallbackEmail: String? = null,
    val fallbackName: String? = null,
    val requiresLegacyIntent: Boolean = false
)

class GoogleAuthManager(private val context: Context) {

    companion object {
        private const val TAG = "GoogleAuthManager"
        const val FALLBACK_WEB_CLIENT_ID = "738578156874-ap14l3avofalrmvtgeic8ocuj8m3v5or.apps.googleusercontent.com"

        fun getWebClientId(context: Context): String {
            return try {
                var id = ""
                val resIdApp = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
                if (resIdApp != 0) {
                    id = context.getString(resIdApp)
                }
                if (id.isBlank()) {
                    val resIdExample = context.resources.getIdentifier("default_web_client_id", "string", "com.example")
                    if (resIdExample != 0) {
                        id = context.getString(resIdExample)
                    }
                }
                if (id.isNotBlank()) id else FALLBACK_WEB_CLIENT_ID
            } catch (e: Throwable) {
                FALLBACK_WEB_CLIENT_ID
            }
        }
    }

    private val credentialManager = CredentialManager.create(context)

    fun getGoogleSignInClient(activity: Activity): GoogleSignInClient {
        val serverClientId = getWebClientId(activity)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(serverClientId)
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(activity, gso)
    }

    fun getLegacySignInIntent(activity: Activity): Intent {
        val client = getGoogleSignInClient(activity)
        return client.signInIntent
    }

    suspend fun handleLegacySignInResult(data: Intent?): GoogleSignInResult {
        if (data == null) {
            return GoogleSignInResult(isCancelled = true)
        }
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            val email = account.email
            val displayName = account.displayName

            Log.d(TAG, "Legacy Google Sign-In account: $email, name: $displayName, hasToken: ${!idToken.isNullOrBlank()}")

            val firebaseUser = if (!idToken.isNullOrBlank()) {
                val auth = FirebaseAuth.getInstance()
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                try {
                    val authResult = auth.signInWithCredential(firebaseCredential).await()
                    authResult.user
                } catch (e: Exception) {
                    Log.w(TAG, "Firebase signInWithCredential failed for legacy: ${e.message}", e)
                    auth.currentUser
                }
            } else {
                null
            }

            return GoogleSignInResult(
                firebaseUser = firebaseUser,
                fallbackEmail = email,
                fallbackName = displayName
            )
        } catch (e: ApiException) {
            Log.e(TAG, "Legacy Google Sign-In ApiException statusCode: ${e.statusCode}, message: ${e.message}", e)
            if (e.statusCode == 12501 || e.statusCode == 16) {
                // 12501 is SIGN_IN_CANCELLED
                return GoogleSignInResult(isCancelled = true)
            }
            return GoogleSignInResult(
                errorMessage = "Google account selection error (${e.statusCode}): ${e.localizedMessage ?: "Please try again."}"
            )
        } catch (e: Throwable) {
            Log.e(TAG, "Legacy Google Sign-In error", e)
            return GoogleSignInResult(
                errorMessage = e.localizedMessage ?: "Unable to complete Google Sign-In."
            )
        }
    }

    suspend fun signInWithGoogle(activity: Activity): GoogleSignInResult {
        val serverClientId = getWebClientId(activity)
        Log.d(TAG, "Initiating Google Sign-In with serverClientId: $serverClientId")

        // 1. Primary Attempt: Use GetSignInWithGoogleOption alone (do NOT combine with other options in same request)
        try {
            val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(serverClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = activity
            )
            return processCredentialResult(result)
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "User cancelled Google Sign-In prompt.")
            return GoogleSignInResult(isCancelled = true)
        } catch (e: Throwable) {
            Log.w(TAG, "GetSignInWithGoogleOption failed: ${e.message}, trying GetGoogleIdOption fallback...", e)
        }

        // 2. Secondary Attempt: Use GetGoogleIdOption with filterByAuthorizedAccounts = false to show all device Google accounts
        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = activity
            )
            return processCredentialResult(result)
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "User cancelled Google Sign-In prompt on fallback.")
            return GoogleSignInResult(isCancelled = true)
        } catch (e: Throwable) {
            Log.w(TAG, "GetGoogleIdOption fallback failed: ${e.message}. Triggering legacy GoogleSignIn intent...", e)
            // Signal caller to launch legacy GoogleSignIn intent so device displays Google account picker
            return GoogleSignInResult(
                requiresLegacyIntent = true,
                errorMessage = e.localizedMessage
            )
        }
    }

    private suspend fun processCredentialResult(result: androidx.credentials.GetCredentialResponse): GoogleSignInResult {
        val credential = result.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken
            val email = googleIdTokenCredential.id
            val displayName = googleIdTokenCredential.displayName

            Log.d(TAG, "Google ID Token obtained for email: $email, name: $displayName")

            val auth = FirebaseAuth.getInstance()
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val firebaseUser = try {
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                authResult.user
            } catch (e: Exception) {
                Log.w(TAG, "Firebase signInWithCredential failed: ${e.message}", e)
                auth.currentUser
            }

            return GoogleSignInResult(
                firebaseUser = firebaseUser,
                fallbackEmail = email,
                fallbackName = displayName
            )
        } else {
            Log.e(TAG, "Unexpected credential type: ${credential.type}")
            return GoogleSignInResult(
                firebaseUser = null,
                errorMessage = "Unexpected credential response. Please try again."
            )
        }
    }

    suspend fun signOut(activity: Activity? = null) {
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (e: Throwable) {
            Log.e(TAG, "Error signing out of Firebase Auth", e)
        }
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            Log.d(TAG, "Google Credential State cleared successfully.")
        } catch (e: Throwable) {
            Log.e(TAG, "Error clearing Credential Manager state", e)
        }
        if (activity != null) {
            try {
                getGoogleSignInClient(activity).signOut()
            } catch (e: Throwable) {
                Log.e(TAG, "Error signing out of legacy GoogleSignInClient", e)
            }
        }
    }
}


