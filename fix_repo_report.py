import re

def fix_file(filepath):
    with open(filepath, "r") as f:
        content = f.read()

    # 1. Fix reportQuestion to handle premium questions
    old_report = """suspend fun reportQuestion(question: QuestionEntity): Pair<Boolean, String> {
        val updated = question.copy(isReported = true)
        questionDao.updateQuestion(updated)
        val fbId = updated.firebaseId.ifEmpty { updated.id.toString() }
        val payload = mapOf("isReported" to true, "updatedAt" to System.currentTimeMillis())
        return syncManager.enqueueAndSync("REPORT_QUESTION", fbId, "UPDATE", payload)
    }"""
    
    new_report = """suspend fun reportQuestion(question: QuestionEntity): Pair<Boolean, String> {
        val updated = question.copy(isReported = true)
        if (question.isPremium) {
            val current = _premiumQuestions.value.toMutableList()
            val index = current.indexOfFirst { it.id == question.id }
            if (index != -1) {
                current[index] = updated
                _premiumQuestions.value = current
            }
        } else {
            questionDao.updateQuestion(updated)
        }
        val fbId = updated.firebaseId.ifEmpty { updated.id.toString() }
        val payload = mapOf("isReported" to true, "updatedAt" to System.currentTimeMillis())
        return syncManager.enqueueAndSync("REPORT_QUESTION", fbId, "UPDATE", payload)
    }"""

    if old_report in content:
        content = content.replace(old_report, new_report)
    
    # 2. Fix resolveReportedQuestion if it exists
    old_resolve = """fun resolveReportedQuestion(question: QuestionEntity) {
        viewModelScope.launch {
            val res = repository.updateQuestion(question.copy(isReported = false))
            _syncToastMessage.value = res.second
        }
    }"""
    # Wait, resolve is in ViewModel! But what if updateQuestion has the same issue?
    # Let's fix the combine logic in JuktiRepository to prefer local isReported state
    
    old_combine = """    val allQuestions: Flow<List<QuestionEntity>> = combine(
        firebaseRepository.observeQuestions(),
        questionDao.getAllQuestions()
    ) { remoteQuestions, localQuestions ->
        val combined = if (remoteQuestions.isEmpty()) {
            localQuestions
        } else {
            val remoteIds = remoteQuestions.map { it.id }.toSet()
            val localOnly = localQuestions.filter { it.id !in remoteIds }
            remoteQuestions + localOnly
        }
        combined.map { normalizeQuestionEntity(it) }
    }"""

    new_combine = """    val allQuestions: Flow<List<QuestionEntity>> = combine(
        firebaseRepository.observeQuestions(),
        questionDao.getAllQuestions()
    ) { remoteQuestions, localQuestions ->
        val combined = if (remoteQuestions.isEmpty()) {
            localQuestions
        } else {
            val localMap = localQuestions.associateBy { it.id }
            val remoteIds = remoteQuestions.map { it.id }.toSet()
            val merged = remoteQuestions.map { remote ->
                val local = localMap[remote.id]
                // Prefer local if its isReported state differs from remote (optimistic update)
                if (local != null && local.isReported != remote.isReported) local else remote
            }
            val localOnly = localQuestions.filter { it.id !in remoteIds }
            merged + localOnly
        }
        combined.map { normalizeQuestionEntity(it) }
    }"""
    
    if old_combine in content:
        content = content.replace(old_combine, new_combine)
    else:
        print("Could not find old_combine")
        
    with open(filepath, "w") as f:
        f.write(content)
    print("Fixed repository")

fix_file("app/src/main/java/com/example/data/repository/JuktiRepository.kt")
