import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

target = """    val savedNotes = repository.savedNotes.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )"""

replacement = """    val savedNotes = studyNotes.map { list -> list.filter { it.isBookmarked || it.isDownloaded } }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )"""

content = content.replace(target, replacement)
with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(content)
