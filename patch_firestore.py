import re

with open("app/src/main/java/com/example/JuktiApplication.kt", "r") as f:
    content = f.read()

target = """                if (defaultApp != null) {
                    Log.i("JuktiApplication", "FirebaseApp initialized automatically from resources.")
                    return defaultApp
                }"""

replacement = """                if (defaultApp != null) {
                    Log.i("JuktiApplication", "FirebaseApp initialized automatically from resources.")
                    disableFirestorePersistence()
                    return defaultApp
                }"""
content = content.replace(target, replacement)

target2 = """                val app = FirebaseApp.initializeApp(context, options, "JuktiApp")
                Log.i("JuktiApplication", "FirebaseApp initialized manually.")
                return app"""

replacement2 = """                val app = FirebaseApp.initializeApp(context, options, "JuktiApp")
                Log.i("JuktiApplication", "FirebaseApp initialized manually.")
                disableFirestorePersistence()
                return app"""
content = content.replace(target2, replacement2)

target3 = """    companion object {"""

replacement3 = """    companion object {
        private var persistenceDisabled = false
        private fun disableFirestorePersistence() {
            if (persistenceDisabled) return
            persistenceDisabled = true
            try {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val settings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(com.google.firebase.firestore.MemoryCacheSettings.newBuilder().build())
                    .build()
                db.firestoreSettings = settings
                android.util.Log.i("JuktiApplication", "Firestore persistence disabled for secure premium content.")
            } catch (e: Exception) {
                android.util.Log.e("JuktiApplication", "Failed to disable Firestore persistence", e)
            }
        }"""
content = content.replace(target3, replacement3)

with open("app/src/main/java/com/example/JuktiApplication.kt", "w") as f:
    f.write(content)
print("Patched JuktiApplication.kt")
