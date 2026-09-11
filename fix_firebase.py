import re

with open('app/src/main/java/com/example/data/repository/FirebaseRepository.kt', 'r') as f:
    content = f.read()

replacement = """    suspend fun saveUserProfile(profile: UserProfileEntity, merge: Boolean = true) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
"""
content = re.sub(r'    suspend fun saveUserProfile\(profile: UserProfileEntity, merge: Boolean = true\) \{\n        try \{', replacement, content)

content = re.sub(r'        \} catch \(e: kotlinx.coroutines.CancellationException\) \{ throw e \}\n        catch \(e: Throwable\) \{\n            logOperationError\("Error saving user profile to Firebase", e\)\n        \}\n    \}', 
r'        } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Throwable) { logOperationError("Error saving user profile to Firebase", e) }\n    }', content)

with open('app/src/main/java/com/example/data/repository/FirebaseRepository.kt', 'w') as f:
    f.write(content)
