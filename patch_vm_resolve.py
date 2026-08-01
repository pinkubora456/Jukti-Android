import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

methods = """
    fun updateQuestionAndResolve(question: QuestionEntity) {
        viewModelScope.launch {
            repository.updateQuestion(question.copy(isReported = false))
        }
    }
}
"""

content = content.rstrip()
if content.endswith("}"):
    content = content[:-1] + methods

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(content)
