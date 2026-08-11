import sys

with open('app/src/main/java/com/example/data/repository/FirebaseSyncManager.kt', 'r') as f:
    content = f.read()

old_error = """            val codeName = if (e is com.google.firebase.firestore.FirebaseFirestoreException) e.code.name else e.javaClass.simpleName
            val errorMsg = "❌ Minimal write test FAILED on $path: [$codeName] ${e.message}"
            Log.e("FirebaseSyncManager", errorMsg, e)
            Pair(false, errorMsg)"""

new_error = """            val codeName = if (e is com.google.firebase.firestore.FirebaseFirestoreException) e.code.name else e.javaClass.simpleName
            var errorMsg = "❌ Minimal write test FAILED on $path: [$codeName] ${e.message}"
            if (codeName.contains("PERMISSION_DENIED")) {
                errorMsg = "Permission Denied: Please update your Firestore Security Rules in the Firebase Console."
            }
            Log.e("FirebaseSyncManager", errorMsg, e)
            Pair(false, errorMsg)"""

content = content.replace(old_error, new_error)

with open('app/src/main/java/com/example/data/repository/FirebaseSyncManager.kt', 'w') as f:
    f.write(content)

