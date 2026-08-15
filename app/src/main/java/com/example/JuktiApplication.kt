package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class JuktiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ensureFirebaseInitialized(this)
        createNotificationChannel()
        com.example.data.worker.FirestoreSyncWorker.scheduleSync(this)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Jukti Notifications"
            val descriptionText = "Notifications for Jukti app"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("jukti_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        fun ensureFirebaseInitialized(context: Context): FirebaseApp? {
            val existingApps = FirebaseApp.getApps(context)
            if (existingApps.isNotEmpty()) {
                return FirebaseApp.getInstance()
            }

            try {
                val defaultApp = FirebaseApp.initializeApp(context)
                if (defaultApp != null) {
                    Log.i("JuktiApplication", "FirebaseApp initialized automatically from resources.")
                    return defaultApp
                }
            } catch (e: Throwable) {
                Log.w("JuktiApplication", "Automatic FirebaseApp initialization failed: ${e.message}")
            }

            return try {
                val appId = getResString(context, "google_app_id", "1:738578156874:android:13873ee9dd611227bec5b3")
                val apiKey = getResString(context, "google_api_key", "AIzaSyCJmcTFzebtCOrlgJG6TJiiEb2suuxojOk")
                val projectId = getResString(context, "project_id", "jukti-26035")
                val storageBucket = getResString(context, "google_storage_bucket", "jukti-26035.firebasestorage.app")
                val senderId = getResString(context, "gcm_defaultSenderId", "738578156874")

                val options = FirebaseOptions.Builder()
                    .setApplicationId(appId)
                    .setApiKey(apiKey)
                    .setProjectId(projectId)
                    .setStorageBucket(storageBucket)
                    .setGcmSenderId(senderId)
                    .build()

                val app = FirebaseApp.initializeApp(context, options)
                Log.i("JuktiApplication", "FirebaseApp initialized explicitly with fallback options.")
                app
            } catch (e: Throwable) {
                Log.e("JuktiApplication", "Critical failure initializing FirebaseApp", e)
                null
            }
        }

        private fun getResString(context: Context, key: String, fallback: String): String {
            return try {
                val id = context.resources.getIdentifier(key, "string", context.packageName)
                if (id != 0) context.getString(id) else fallback
            } catch (e: Throwable) {
                fallback
            }
        }

        fun getAuth(context: Context): FirebaseAuth? {
            ensureFirebaseInitialized(context)
            return try {
                FirebaseAuth.getInstance()
            } catch (e: Throwable) {
                Log.e("JuktiApplication", "FirebaseAuth not available", e)
                null
            }
        }

        fun getFirestore(context: Context): FirebaseFirestore? {
            ensureFirebaseInitialized(context)
            return try {
                FirebaseFirestore.getInstance()
            } catch (e: Throwable) {
                Log.e("JuktiApplication", "FirebaseFirestore not available", e)
                null
            }
        }

        fun getStorage(context: Context): FirebaseStorage? {
            ensureFirebaseInitialized(context)
            return try {
                FirebaseStorage.getInstance()
            } catch (e: Throwable) {
                Log.e("JuktiApplication", "FirebaseStorage not available", e)
                null
            }
        }
    }
}
