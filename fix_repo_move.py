import re

with open('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace('''    fun bulkMoveQuestions(
        questionsToUpdate: List<QuestionEntity>,
        targetExam: String,
        targetSubject: String,
        targetChapter: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val (success, msg) = repository.bulkMoveQuestions(questionsToUpdate, targetExam, targetSubject, targetChapter)
            onComplete(success, msg)
            if (success) {
                _syncToastMessage.value = msg
            }
        }
    }''', '''    fun bulkMoveQuestions(
        questionsToUpdate: List<QuestionEntity>,
        targetSubject: String,
        targetChapter: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val (success, msg) = repository.bulkMoveQuestions(questionsToUpdate, targetSubject, targetChapter)
            onComplete(success, msg)
            if (success) {
                _syncToastMessage.value = msg
            }
        }
    }''')

with open('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/data/repository/JuktiRepository.kt', 'r') as f:
    content = f.read()

content = content.replace('''    suspend fun bulkMoveQuestions(
        questionsToUpdate: List<com.example.data.local.QuestionEntity>,
        targetExam: String,
        targetSubject: String,
        targetChapter: String
    ): Pair<Boolean, String> {
        if (questionsToUpdate.isEmpty()) return false to "No questions found to move"

        val normSubject = normalizeSubjectName(targetSubject)
        val normTopic = normalizeChapterName(targetChapter, normSubject)

        val updatedQs = questionsToUpdate.map { 
            normalizeQuestionEntity(it.copy(
                examCategory = targetExam,
                subject = normSubject,
                topic = normTopic,
                updatedAt = System.currentTimeMillis()
            ))
        }''', '''    suspend fun bulkMoveQuestions(
        questionsToUpdate: List<com.example.data.local.QuestionEntity>,
        targetSubject: String,
        targetChapter: String
    ): Pair<Boolean, String> {
        if (questionsToUpdate.isEmpty()) return false to "No questions found to move"

        val normSubject = normalizeSubjectName(targetSubject)
        val normTopic = normalizeChapterName(targetChapter, normSubject)

        val updatedQs = questionsToUpdate.map { 
            normalizeQuestionEntity(it.copy(
                subject = normSubject,
                topic = normTopic,
                updatedAt = System.currentTimeMillis()
            ))
        }''')

with open('app/src/main/java/com/example/data/repository/JuktiRepository.kt', 'w') as f:
    f.write(content)

