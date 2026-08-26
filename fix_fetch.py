import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

old_str = """    suspend fun fetchUserEntitlementDirect(email: String): com.example.data.local.EntitlementEntity? {
        return try {
            val sanitizedDocId = com.example.data.repository.FirebaseRepository().getSanitizedUserDocId(email)
            val trimmedEmail = email.trim().lowercase()
            // 1. Check local Room DB first
            val local = repository.getUserEntitlementDirect(sanitizedDocId, trimmedEmail)
            if (local != null && validateEntitlements(local)) {
                return local
            }
            // 2. Fetch from Firebase
            val remote = repository.fetchUserEntitlementFromFirebase(email)
            if (remote != null) {
                repository.insertEntitlement(remote)
                if (sanitizedDocId.isNotBlank() && sanitizedDocId != remote.userId) {
                    repository.insertEntitlement(remote.copy(userId = sanitizedDocId))
                }
                return remote
            }
            local
        } catch (e: Exception) {
            null
        }
    }"""

new_str = """    suspend fun fetchUserEntitlementDirect(email: String): com.example.data.local.EntitlementEntity? {
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
    }"""

content = content.replace(old_str, new_str)

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(content)
