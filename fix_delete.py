import re

with open('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', 'r') as f:
    content = f.read()

delete_questions = """    fun deleteQuestions(questions: List<QuestionEntity>) {
        logActivity("Deleted ${questions.size} questions")
        viewModelScope.launch {
            questions.forEach { q ->
                repository.deleteQuestion(q)
            }
            _syncToastMessage.value = "Successfully deleted ${questions.size} questions."
        }
    }"""

content = content.replace('    fun deleteQuestion(question: QuestionEntity) {', delete_questions + '\n\n    fun deleteQuestion(question: QuestionEntity) {')

with open('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', 'w') as f:
    f.write(content)
