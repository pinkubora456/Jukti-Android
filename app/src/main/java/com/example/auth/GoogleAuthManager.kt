package com.example.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

data class GoogleSignInResult(
    val firebaseUser: FirebaseUser?,
    val isCancelled: Boolean = false,
    val errorMessage: String? = null
)

class GoogleAuthManager(private val context: Context) {

    companion object {
        private const val TAG = "GoogleAuthManager"
        const val FALLBACK_WEB_CLIENT_ID = "738578156874-ap14l3avofalrmvtgeic8ocuj8m3v5or.apps.googleusercontent.com"

        fun getWebClientId(context: Context): String {
            return try {
                val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
                if (resId != 0) {
                    val id = context.getString(resId)
                    if (id.isNotBlank()) id else FALLBACK_WEB_CLIENT_ID
                } else {
                    FALLBACK_WEB_CLIENT_ID
                }
            } catch (e: Throwable) {
                FALLBACK_WEB_CLIENT_ID
            }
        }
    }

    private val credentialManager = CredentialManager.create(context)

    suspend fun signInWithGoogle(activity: Activity): GoogleSignInResult {
        try {
            val serverClientId = getWebClientId(activity)
            Log.d(TAG, "Initiating Google Sign-In with serverClientId: $serverClientId")

            val rawNonce = UUID.randomUUID().toString()
            val bytes = rawNonce.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false) // Allows user to select any Google account on device
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false) // Ensures prompt is shown so user can choose another account
                .setNonce(hashedNonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = activity
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val email = googleIdTokenCredential.id
                val displayName = googleIdTokenCredential.displayName

                Log.d(TAG, "Google ID Token obtained for email: $email, name: $displayName")

                val auth = FirebaseAuth.getInstance()
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    Log.d(TAG, "Firebase Auth successful. UID: ${firebaseUser.uid}, Email: ${firebaseUser.email}")
                    return GoogleSignInResult(firebaseUser = firebaseUser)
                } else {
                    return GoogleSignInResult(
                        firebaseUser = null,
                        errorMessage = "Firebase user authentication returned null."
                    )
                }
            } else {
                Log.e(TAG, "Unexpected credential type: ${credential.type}")
                return GoogleSignInResult(
                    firebaseUser = null,
                    errorMessage = "Unexpected credential returned by Google. Please try again."
                )
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "User cancelled Google Sign-In prompt.")
            return GoogleSignInResult(firebaseUser = null, isCancelled = true)
        } catch (e: NoCredentialException) {
            Log.w(TAG, "No Google accounts available on device: ${e.message}")
            return GoogleSignInResult(
                firebaseUser = null,
                errorMessage = "No Google account found on device. Please add a Google account in your phone settings."
            )
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Credential Manager error: ${e.type} - ${e.message}", e)
            val userFriendlyMsg = when {
                e.message?.contains("16", ignoreCase = true) == true || e.message?.contains("canceled", ignoreCase = true) == true -> "Sign-in was cancelled."
                e.message?.contains("10", ignoreCase = true) == true || e.message?.contains("DEVELOPER_ERROR", ignoreCase = true) == true ->
                    "Google Sign-In configuration error (Developer Error). Please verify SHA-1 / SHA-256 certificate in Firebase Console."
                e.message?.contains("7", ignoreCase = true) == true || e.message?.contains("NETWORK_ERROR", ignoreCase = true) == true ->
                    "Network error during sign-in. Please check your internet connection."
                else -> "Google sign-in error: ${e.localizedMessage ?: "Please try again."}"
            }
            return GoogleSignInResult(firebaseUser = null, errorMessage = userFriendlyMsg)
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In failed with exception: ${e.message}", e)
            val msg = e.localizedMessage ?: e.message ?: "Authentication failed."
            val userFriendlyMsg = when {
                msg.contains("network", ignoreCase = true) -> "Network error during Google sign-in. Please check your internet connection."
                msg.contains("API key", ignoreCase = true) -> "Firebase API key error. Please verify google-services.json configuration."
                else -> "Authentication failed: $msg"
            }
            return GoogleSignInResult(firebaseUser = null, errorMessage = userFriendlyMsg)
        }
    }

    suspend fun signOut() {
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
    }
}
