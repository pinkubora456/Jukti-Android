import re

with open('app/src/main/java/com/example/data/repository/JuktiRepository.kt', 'r') as f:
    content = f.read()

bulk_edit_repo_fixed = """    suspend fun bulkEditQuestions(
        questionsToUpdate: List<com.example.data.local.QuestionEntity>,
        targetExam: String?,
        targetAccess: String?,
        targetQuestionType: String?,
        targetPyqExamName: String?,
        targetTags: String?,
        targetDifficulty: String?
    ): Pair<Boolean, String> {
        if (questionsToUpdate.isEmpty()) return false to "No questions found to edit"

        val updatedQs = questionsToUpdate.map { q ->
            var finalQuestionType = q.questionType
            
            if (targetQuestionType == "PYQ") {
                val baseExam = if (targetPyqExamName.isNullOrBlank()) "" else targetPyqExamName
                finalQuestionType = if (baseExam.isNotBlank()) "PYQ - $baseExam" else "PYQ"
            } else if (targetQuestionType == "Expected") {
                finalQuestionType = "Expected"
            }
            
            if (targetTags != null) {
                if (targetTags == "Expected" && targetQuestionType == null && !q.questionType.startsWith("PYQ", ignoreCase = true)) {
                    finalQuestionType = "Expected"
                }
            }

            normalizeQuestionEntity(q.copy(
                examCategory = targetExam ?: q.examCategory,
                isPremium = when (targetAccess) {
                    "Free" -> false
                    "Premium" -> true
                    else -> q.isPremium
                },
                questionType = finalQuestionType,
                difficulty = targetDifficulty ?: q.difficulty,
                updatedAt = System.currentTimeMillis()
            ))
        }

        val localToUpdate = updatedQs.filter { !it.isPremium }
        val premToUpdate = updatedQs.filter { it.isPremium }
        val localToRemove = questionsToUpdate.filter { !it.isPremium && updatedQs.find { u -> u.id == it.id }?.isPremium == true }
        val premToRemove = questionsToUpdate.filter { it.isPremium && updatedQs.find { u -> u.id == it.id }?.isPremium == false }

        if (localToRemove.isNotEmpty()) {
            questionDao.deleteQuestions(localToRemove)
        }
        
        if (localToUpdate.isNotEmpty()) {
            val existingLocal = questionDao.getAllQuestions().firstOrNull() ?: emptyList()
            val existingIds = existingLocal.map { it.id }.toSet()
            val toInsert = localToUpdate.filter { !existingIds.contains(it.id) }
            val toUpdate = localToUpdate.filter { existingIds.contains(it.id) }
            
            if (toInsert.isNotEmpty()) questionDao.insertAll(toInsert)
            if (toUpdate.isNotEmpty()) questionDao.updateQuestions(toUpdate)
        }
        
        val currentPrem = _premiumQuestions.value.toMutableList()
        if (premToRemove.isNotEmpty()) {
            val idsToRemove = premToRemove.map { it.id }.toSet()
            currentPrem.removeAll { it.id in idsToRemove }
            _premiumQuestions.value = currentPrem
        }
        
        if (premToUpdate.isNotEmpty()) {
            premToUpdate.forEach { upd ->
                val index = currentPrem.indexOfFirst { it.id == upd.id }
                if (index >= 0) currentPrem[index] = upd
                else currentPrem.add(upd)
            }
            _premiumQuestions.value = currentPrem
        }
        
        updatedQs.forEach { q ->
            val fbId = q.firebaseId.ifEmpty { q.id.toString() }
            syncManager.enqueueAndSync("QUESTION", fbId, "UPDATE", syncManager.questionToMap(q))
        }
        
        return true to "Successfully updated ${questionsToUpdate.size} questions"
    }"""

# Replace the broken bulkEditQuestions function
content = re.sub(r'    suspend fun bulkEditQuestions\(.*?\): Pair<Boolean, String> \{.*?return true to "Successfully updated \$\{questionsToUpdate\.size\} questions"\n    \}', bulk_edit_repo_fixed, content, flags=re.MULTILINE | re.DOTALL)

with open('app/src/main/java/com/example/data/repository/JuktiRepository.kt', 'w') as f:
    f.write(content)
