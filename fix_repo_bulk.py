import re

def fix_file(filepath):
    with open(filepath, "r") as f:
        content = f.read()

    new_func = """    suspend fun updateQuestion(question: QuestionEntity): Pair<Boolean, String> {
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
    }

    suspend fun bulkMoveQuestions(
        questionIds: List<Long>,
        targetExam: String,
        targetSubject: String,
        targetChapter: String
    ): Pair<Boolean, String> {
        val localQs = questionDao.getAllQuestions().firstOrNull() ?: emptyList()
        val premQs = _premiumQuestions.value
        val allQs = localQs + premQs
        
        val toUpdate = allQs.filter { it.id in questionIds }
        if (toUpdate.isEmpty()) return false to "No questions found to move"

        val normSubject = normalizeSubjectName(targetSubject)
        val normTopic = normalizeChapterName(targetChapter, normSubject)

        val updatedQs = toUpdate.map { 
            normalizeQuestionEntity(it.copy(
                examCategory = targetExam,
                subject = normSubject,
                topic = normTopic,
                updatedAt = System.currentTimeMillis()
            ))
        }

        val localToUpdate = updatedQs.filter { !it.isPremium }
        val premToUpdate = updatedQs.filter { it.isPremium }

        if (localToUpdate.isNotEmpty()) {
            questionDao.updateQuestions(localToUpdate)
        }
        
        if (premToUpdate.isNotEmpty()) {
            val current = _premiumQuestions.value.toMutableList()
            premToUpdate.forEach { upd ->
                val index = current.indexOfFirst { it.id == upd.id }
                if (index != -1) current[index] = upd
            }
            _premiumQuestions.value = current
        }

        updatedQs.forEach { q ->
            val fbId = q.firebaseId.ifEmpty { q.id.toString() }
            syncManager.enqueueAndSync("QUESTION", fbId, "UPDATE", syncManager.questionToMap(q))
        }
        
        return true to "Successfully moved ${updatedQs.size} questions."
    }"""
    
    # We replace updateQuestion with updateQuestion + bulkMoveQuestions
    old_update = """    suspend fun updateQuestion(question: QuestionEntity): Pair<Boolean, String> {
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
        content = content.replace(old_update, new_func)
        with open(filepath, "w") as f:
            f.write(content)
        print("Fixed JuktiRepository.kt")
    else:
        print("old_update not found")

fix_file("app/src/main/java/com/example/data/repository/JuktiRepository.kt")
