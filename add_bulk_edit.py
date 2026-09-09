import re

with open('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', 'r') as f:
    content = f.read()

bulk_edit_vm = """    fun bulkEditQuestions(
        questionsToUpdate: List<QuestionEntity>,
        targetExam: String?,
        targetAccess: String?,
        targetQuestionType: String?,
        targetPyqExamName: String?,
        targetTags: String?,
        targetDifficulty: String?,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val (success, msg) = repository.bulkEditQuestions(
                questionsToUpdate, targetExam, targetAccess, targetQuestionType, targetPyqExamName, targetTags, targetDifficulty
            )
            onComplete(success, msg)
            if (success) {
                _syncToastMessage.value = msg
            }
        }
    }"""

content = content.replace('    fun bulkMoveQuestions(', bulk_edit_vm + '\n\n    fun bulkMoveQuestions(')

with open('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/data/repository/JuktiRepository.kt', 'r') as f:
    content = f.read()

bulk_edit_repo = """    suspend fun bulkEditQuestions(
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
            questionDao.updateQuestions(localToUpdate)
        }
        
        val currentPrem = _premiumQuestions.value.toMutableList()
        if (premToRemove.isNotEmpty()) {
            val idsToRemove = premToRemove.map { it.id }.toSet()
            currentPrem.removeAll { it.id in idsToRemove }
            _premiumQuestions.value = currentPrem
            publishPremiumQuestionsToFirestore(premToRemove, isDelete = true)
        }
        
        if (premToUpdate.isNotEmpty()) {
            premToUpdate.forEach { upd ->
                val index = currentPrem.indexOfFirst { it.id == upd.id }
                if (index >= 0) currentPrem[index] = upd
                else currentPrem.add(upd)
            }
            _premiumQuestions.value = currentPrem
            publishPremiumQuestionsToFirestore(premToUpdate, isDelete = false)
        }
        
        refreshData()
        return true to "Successfully updated ${questionsToUpdate.size} questions"
    }"""

content = content.replace('    suspend fun bulkMoveQuestions(', bulk_edit_repo + '\n\n    suspend fun bulkMoveQuestions(')

with open('app/src/main/java/com/example/data/repository/JuktiRepository.kt', 'w') as f:
    f.write(content)
