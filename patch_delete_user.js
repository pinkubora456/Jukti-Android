const fs = require('fs');
let code = fs.readFileSync('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', 'utf8');

const regex = /suspend fun deleteUserAccount\(userEmail: String, explicitUserId: String\? = null\): Boolean \{[\s\S]*?repository\.deleteUserAccount\(uid, userEmail\)\n\s*true\n\s*\} catch \(e: Exception\) \{\n\s*false\n\s*\}\n\s*\}/;

const replacement = `suspend fun deleteUserAccount(userEmail: String, explicitUserId: String? = null): Boolean {
        return try {
            var uid = explicitUserId ?: ""
            if (uid.isBlank()) {
                val users = repository.fetchAllUsersDirect()
                val targetUser = users.find { it.email.equals(userEmail, ignoreCase = true) }
                uid = targetUser?.uid ?: ""
            }
            if (uid.isBlank()) {
                uid = userEmail.trim().lowercase().replace("@", "_at_").replace(".", "_dot_")
            }
            
            val data = mapOf("targetUid" to uid)
            val functions = com.google.firebase.functions.FirebaseFunctions.getInstance()
            functions.getHttpsCallable("deleteUserCompletely").call(data).await()
            
            repository.deleteUserAccount(uid, userEmail)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }`;

code = code.replace(regex, replacement);
fs.writeFileSync('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', code);
