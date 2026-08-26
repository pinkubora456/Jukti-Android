import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

target = """    val hiddenQuestions: StateFlow<List<QuestionEntity>> = combine(
        repository.allQuestions,
        hiddenIds
    ) { questions, ids ->
        questions.filter { it.id in ids }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())"""

replacement = """    val hiddenQuestions: StateFlow<List<QuestionEntity>> = combine(
        questions,
        hiddenIds
    ) { qs, ids ->
        qs.filter { it.id in ids }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())"""

content = content.replace(target, replacement)
with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(content)
