import re

def fix_file(filepath):
    with open(filepath, "r") as f:
        content = f.read()

    new_func = """    fun resolveReportedQuestion(question: QuestionEntity) {
        viewModelScope.launch {
            val res = repository.updateQuestion(question.copy(isReported = false))
            _syncToastMessage.value = res.second
        }
    }

    fun bulkMoveQuestions(
        questionIds: List<Long>,
        targetExam: String,
        targetSubject: String,
        targetChapter: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val (success, msg) = repository.bulkMoveQuestions(questionIds, targetExam, targetSubject, targetChapter)
            onComplete(success, msg)
            if (success) {
                _syncToastMessage.value = msg
            }
        }
    }"""
    
    old_func = """    fun resolveReportedQuestion(question: QuestionEntity) {
        viewModelScope.launch {
            val res = repository.updateQuestion(question.copy(isReported = false))
            _syncToastMessage.value = res.second
        }
    }"""
    
    if old_func in content:
        content = content.replace(old_func, new_func)
        with open(filepath, "w") as f:
            f.write(content)
        print("Fixed JuktiViewModel.kt")
    else:
        print("old_func not found")

fix_file("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt")
