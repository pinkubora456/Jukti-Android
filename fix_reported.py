import re

with open('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', 'r') as f:
    content = f.read()

old_reported = """    val reportedQuestions = questions.map { list -> list.filter { it.isReported } }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )"""

new_reported = """    val reportedQuestions = kotlinx.coroutines.flow.combine(repository.allQuestions, repository.premiumQuestions) { f, p -> f + p }.map { list -> list.filter { it.isReported } }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )"""

if old_reported in content:
    content = content.replace(old_reported, new_reported)
    with open('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', 'w') as f:
        f.write(content)
    print("Replaced reportedQuestions")
else:
    print("Could not find old_reported")
