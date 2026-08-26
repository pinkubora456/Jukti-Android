import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

# We need to extract bookmarkedQuestions through savedNotes
target_start_1 = "    val bookmarkedQuestions: StateFlow<List<QuestionEntity>> = combine("
target_end_1 = "    val savedNotes = studyNotes.map { list -> list.filter { it.isBookmarked || it.isDownloaded } }.stateIn(\n        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()\n    )"

# Find start
idx1 = content.find(target_start_1)
idx2 = content.find(target_end_1) + len(target_end_1)

block_to_move = content[idx1:idx2]

# Remove it from current location
content = content[:idx1] + content[idx2:]

# Insert it before init {
init_str = "    init {\n        viewModelScope.launch {"
idx3 = content.find(init_str)

content = content[:idx3] + block_to_move + "\n\n" + content[idx3:]

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(content)
