import re

with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "r") as f:
    content = f.read()

target = """            val entitlements = entitlementDao.getEntitlementsDirect()"""

replacement = """            val sanitizedDocId = if (userProfile != null) firebaseRepository.getSanitizedUserDocId(userProfile.email) else ""
            val entitlements = if (sanitizedDocId.isNotBlank()) entitlementDao.getEntitlementsDirectMulti(sanitizedDocId, userProfile?.uid ?: "", userProfile?.email ?: "") else emptyList()"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "w") as f:
        f.write(content)
    print("Patched JuktiRepository.kt")
else:
    print("Could not find target in JuktiRepository.kt")
