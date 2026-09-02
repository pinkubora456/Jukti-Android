import re

def fix_file(filepath):
    with open(filepath, "r") as f:
        content = f.read()

    # 1. Fix updateQuestion
    old_update = """suspend fun updateQuestion(question: QuestionEntity): Pair<Boolean, String> {
        val norm = normalizeQuestionEntity(question)
        questionDao.updateQuestion(norm)
        val fbId = norm.firebaseId.ifEmpty { norm.id.toString() }
        return syncManager.enqueueAndSync("QUESTION", fbId, "UPDATE", syncManager.questionToMap(norm))
    }"""

    new_update = """suspend fun updateQuestion(question: QuestionEntity): Pair<Boolean, String> {
        val norm = normalizeQuestionEntity(question.copy(updatedAt = System.currentTimeMillis()))
        if (norm.isPremium) {
            val current = _premiumQuestions.value.toMutableList()
            val index = current.indexOfFirst { it.id == norm.id }
            if (index != -1) {
                current[index] = norm
                _premiumQuestions.value = current
            }
        } else {
            questionDao.updateQuestion(norm)
        }
        val fbId = norm.firebaseId.ifEmpty { norm.id.toString() }
        return syncManager.enqueueAndSync("QUESTION", fbId, "UPDATE", syncManager.questionToMap(norm))
    }"""
    
    if old_update in content:
        content = content.replace(old_update, new_update)

    # 2. Fix reportQuestion to update updatedAt
    old_report = """suspend fun reportQuestion(question: QuestionEntity): Pair<Boolean, String> {
        val updated = question.copy(isReported = true)"""
        
    new_report = """suspend fun reportQuestion(question: QuestionEntity): Pair<Boolean, String> {
        val updated = question.copy(isReported = true, updatedAt = System.currentTimeMillis())"""
        
    if old_report in content:
        content = content.replace(old_report, new_report)

    # 3. Update combine block
    old_combine = """                if (local != null && local.isReported != remote.isReported) local else remote"""
    new_combine = """                if (local != null && (local.isReported != remote.isReported || local.updatedAt > remote.updatedAt)) local else remote"""
    
    if old_combine in content:
        content = content.replace(old_combine, new_combine)
        
    with open(filepath, "w") as f:
        f.write(content)
    print("Fixed updateQuestion and combine block")

fix_file("app/src/main/java/com/example/data/repository/JuktiRepository.kt")
