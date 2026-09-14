import re
path = "app/src/main/java/com/example/data/repository/JuktiRepository.kt"
with open(path, "r") as f:
    content = f.read()

pattern = r"(private val entitlementHistoryDao: EntitlementHistoryDao,\n\s*val syncManager: FirebaseSyncManager)"
if re.search(pattern, content):
    content = re.sub(pattern, r"private val entitlementHistoryDao: EntitlementHistoryDao,\n    private val rcPassageDao: com.example.data.local.ReadingComprehensionPassageDao,\n    val syncManager: FirebaseSyncManager", content)
    with open(path, "w") as f:
        f.write(content)
    print("Fixed JuktiRepository constructor")
