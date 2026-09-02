import re

def fix_file(filepath):
    with open(filepath, "r") as f:
        content = f.read()

    # We need to add reportQuestion to JuktiRepository.kt
    # Let's insert it after updateQuestion
    if "suspend fun reportQuestion" not in content:
        old = "suspend fun updateQuestion(question: QuestionEntity): Pair<Boolean, String> {"
        new = """suspend fun reportQuestion(question: QuestionEntity): Pair<Boolean, String> {
        val updated = question.copy(isReported = true)
        questionDao.updateQuestion(updated)
        val fbId = updated.firebaseId.ifEmpty { updated.id.toString() }
        val payload = mapOf("isReported" to true, "updatedAt" to System.currentTimeMillis())
        return syncManager.enqueueAndSync("REPORT_QUESTION", fbId, "UPDATE", payload)
    }

    suspend fun updateQuestion(question: QuestionEntity): Pair<Boolean, String> {"""
        content = content.replace(old, new)
        with open(filepath, "w") as f:
            f.write(content)
        print("Added reportQuestion to repository")

fix_file("app/src/main/java/com/example/data/repository/JuktiRepository.kt")
