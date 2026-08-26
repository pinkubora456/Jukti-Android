with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

import re

# Find the start of fetchUserEntitlementDirect
start_idx = content.find("suspend fun fetchUserEntitlementDirect(email: String): com.example.data.local.EntitlementEntity? {")
if start_idx != -1:
    # Find the end of it (next function is probably 'fun showToast')
    end_idx = content.find("    fun submitMockTest(", start_idx)
    if end_idx != -1:
        old_func = content[start_idx:end_idx]
        
        new_func = """suspend fun fetchUserEntitlementDirect(email: String): com.example.data.local.EntitlementEntity? {
        return fetchUserEntitlementsDirect(email).firstOrNull()
    }

    suspend fun fetchUserEntitlementsDirect(email: String): List<com.example.data.local.EntitlementEntity> {
        return try {
            val sanitizedDocId = com.example.data.repository.FirebaseRepository().getSanitizedUserDocId(email)
            val trimmedEmail = email.trim().lowercase()
            
            val local = repository.getUserEntitlementsDirect(sanitizedDocId, trimmedEmail)
            if (local.isNotEmpty() && validateEntitlements(local)) {
                return local
            }
            
            val remote = repository.fetchUserEntitlementsFromFirebase(email)
            if (remote.isNotEmpty()) {
                repository.insertEntitlements(remote)
                val mapped = if (sanitizedDocId.isNotBlank()) remote.map { it.copy(userId = sanitizedDocId) } else emptyList()
                if (mapped.isNotEmpty()) {
                    repository.insertEntitlements(mapped)
                }
                return remote
            }
            local
        } catch (e: Exception) {
            emptyList()
        }
    }

"""
        content = content[:start_idx] + new_func + content[end_idx:]

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(content)
