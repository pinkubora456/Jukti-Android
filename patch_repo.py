import re
path = "app/src/main/java/com/example/data/repository/JuktiRepository.kt"
with open(path, "r") as f:
    content = f.read()

# Add rcPassageDao to JuktiRepository if missing
if "rcPassageDao: ReadingComprehensionPassageDao" not in content:
    pattern_dao = r"(private val examDao: ExamDao,\n\s*private val syncManager: FirebaseSyncManager)"
    if re.search(pattern_dao, content):
        content = re.sub(pattern_dao, r"private val examDao: ExamDao,\n    private val rcPassageDao: com.example.data.local.ReadingComprehensionPassageDao,\n    private val syncManager: FirebaseSyncManager", content)
    else:
        # try another pattern
        pattern_dao2 = r"(private val examDao: ExamDao,[\s\S]*?private val syncManager: FirebaseSyncManager)"
        content = re.sub(pattern_dao2, r"\1,\n    private val rcPassageDao: com.example.data.local.ReadingComprehensionPassageDao", content)
        
# Add bulkInsertPassages
bulk_passages = """
    suspend fun bulkInsertPassages(passages: List<com.example.data.local.ReadingComprehensionPassageEntity>): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (passages.isEmpty()) return@withContext Pair(true, "No passages to insert.")
        val now = System.currentTimeMillis()
        val updatedList = passages.map { p ->
            p.copy(updatedAt = now, firebaseId = p.passageId)
        }
        rcPassageDao.insertPassages(updatedList)

        val syncItems = updatedList.map { p ->
            com.example.data.local.SyncQueueEntity(
                entityId = p.passageId,
                dataType = "RC_PASSAGE",
                operation = "CREATE",
                payloadJson = syncManager.mapToJson(syncManager.passageToMap(p)),
                createdAt = now,
                updatedAt = now,
                syncStatus = "PENDING"
            )
        }
        syncManager.enqueueBatch(syncItems)
        Pair(true, "Success")
    }
"""

if "bulkInsertPassages" not in content:
    pattern_bulk = r"(suspend fun bulkInsertQuestions[\s\S]*?\n    \})"
    if re.search(pattern_bulk, content):
        content = re.sub(pattern_bulk, r"\1\n" + bulk_passages, content)

with open(path, "w") as f:
    f.write(content)
print("Patched JuktiRepository.kt")
