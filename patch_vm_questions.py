import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

content = content.replace(
"""    val questions = repository.allQuestions.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )""",
"""    val questions = repository.allQuestions.map { list -> list.filter { !it.isReported } }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    
    val reportedQuestions = repository.allQuestions.map { list -> list.filter { it.isReported } }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )""")

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(content)
