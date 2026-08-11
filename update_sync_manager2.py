import sys

with open('app/src/main/java/com/example/data/repository/FirebaseSyncManager.kt', 'r') as f:
    content = f.read()

old_error = """        } catch (e: com.google.firebase.firestore.FirebaseFirestoreException) {
            val codeName = e.code.name // e.g., PERMISSION_DENIED, UNAUTHENTICATED, UNAVAILABLE
            val errorMsg = "[$codeName] ${e.message ?: "Firestore Exception"} | Path: $path | AuthUID: $authUid"
            Log.e("FirebaseSyncManager", "Firestore error on $path: $errorMsg", e)
            updateItemFailure(item, errorMsg)
            Pair(false, errorMsg)
        }"""

new_error = """        } catch (e: com.google.firebase.firestore.FirebaseFirestoreException) {
            val codeName = e.code.name // e.g., PERMISSION_DENIED, UNAUTHENTICATED, UNAVAILABLE
            var errorMsg = "[$codeName] ${e.message ?: "Firestore Exception"} | Path: $path | AuthUID: $authUid"
            if (codeName.contains("PERMISSION_DENIED")) {
                errorMsg = "Permission Denied: Please update your Firestore Security Rules in the Firebase Console."
            }
            Log.e("FirebaseSyncManager", "Firestore error on $path: $errorMsg", e)
            updateItemFailure(item, errorMsg)
            Pair(false, errorMsg)
        }"""

content = content.replace(old_error, new_error)

with open('app/src/main/java/com/example/data/repository/FirebaseSyncManager.kt', 'w') as f:
    f.write(content)

