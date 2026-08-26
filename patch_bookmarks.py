import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

target = """    val bookmarkedQuestions: StateFlow<List<QuestionEntity>> = combine(
        repository.allQuestions,
        bookmarkedIds
    ) { questions, ids ->
        questions.filter { it.id in ids }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())"""

replacement = """    val bookmarkedQuestions: StateFlow<List<QuestionEntity>> = combine(
        questions,
        bookmarkedIds
    ) { qs, ids ->
        qs.filter { it.id in ids }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
        f.write(content)
    print("Patched bookmarkedQuestions")
else:
    print("Could not find bookmarkedQuestions")
