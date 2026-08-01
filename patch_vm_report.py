import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

methods = """
    fun reportQuestion(question: QuestionEntity) {
        viewModelScope.launch {
            repository.updateQuestion(question.copy(isReported = true))
        }
    }
    
    fun resolveReportedQuestion(question: QuestionEntity) {
        viewModelScope.launch {
            repository.updateQuestion(question.copy(isReported = false))
        }
    }
    
    fun deleteQuestion(question: QuestionEntity) {
        viewModelScope.launch {
            repository.deleteQuestion(question)
        }
    }
"""
content = content.rstrip()
if content.endswith("}"):
    content = content[:-1] + methods + "\n}"

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(content)
