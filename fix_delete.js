const fs = require('fs');
let code = fs.readFileSync('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', 'utf8');

const regex = /val db = com\.example\.JuktiApplication\.getFirestore\(getApplication\(\)\) \?\: return false[\s\S]*?repository\.deleteUserAccount\(uid, userEmail\)/;

const replacement = `val data = mapOf("targetEmail" to userEmail.trim())
            val functions = com.google.firebase.functions.FirebaseFunctions.getInstance()
            functions.getHttpsCallable("deleteUserCompletely").call(data).await()
            
            repository.deleteUserAccount(uid, userEmail)`;

code = code.replace(regex, replacement);
fs.writeFileSync('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', code);
