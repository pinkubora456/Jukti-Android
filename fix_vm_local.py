with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

old_block = """            val local = repository.getUserEntitlementDirect(sanitizedDocId, trimmedEmail)
            if (local != null && validateEntitlements(local)) {
                return local
            }"""

new_block = """            val local = repository.getUserEntitlementsDirect(sanitizedDocId, trimmedEmail)
            if (local.isNotEmpty() && validateEntitlements(local)) {
                return local.firstOrNull { validateEntitlements(listOf(it)) } ?: local.first()
            }"""
content = content.replace(old_block, new_block)

old_fetch = """    suspend fun fetchUserEntitlementDirect(email: String): com.example.data.local.EntitlementEntity? {"""
new_fetch = """    suspend fun fetchUserEntitlementsDirect(email: String): List<com.example.data.local.EntitlementEntity> {
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
    suspend fun fetchUserEntitlementDirect(email: String): com.example.data.local.EntitlementEntity? {"""

content = content.replace(old_fetch, new_fetch)

# Need to update the rest of fetchUserEntitlementDirect? Actually, let's just replace the body of fetchUserEntitlementDirect to return the first item from fetchUserEntitlementsDirect.
