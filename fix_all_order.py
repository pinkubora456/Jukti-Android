import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

# I will find the block that starts with `    val bookmarkedQuestions:` and ends at `    val savedNotes = ...`
start_marker = "    val bookmarkedQuestions: StateFlow<List<QuestionEntity>> = combine("
end_marker = "    val savedNotes = studyNotes.map { list -> list.filter { it.isBookmarked || it.isDownloaded } }.stateIn(\n        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()\n    )"

idx1 = content.find(start_marker)
idx2 = content.find(end_marker) + len(end_marker)
if idx1 != -1 and idx2 != -1:
    block_to_move = content[idx1:idx2]
    content = content[:idx1] + content[idx2:]
    
    # insert it right before `private val _isGuestMode` which is down at the bottom of flows
    target = "    private val _isGuestMode"
    idx3 = content.find(target)
    content = content[:idx3] + block_to_move + "\n\n" + content[idx3:]
    
    with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
        f.write(content)
    print("Moved block to bottom of state flows.")
else:
    print("Could not find block.")
