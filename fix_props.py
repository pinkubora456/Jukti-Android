import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

def move_block_down(start_marker, end_marker, target_marker):
    global content
    idx1 = content.find(start_marker)
    if idx1 == -1:
        print(f"Failed to find {start_marker}")
        return
    idx2 = content.find(end_marker, idx1)
    if idx2 == -1:
        print(f"Failed to find {end_marker}")
        return
    idx2 += len(end_marker)
    
    block = content[idx1:idx2]
    content = content[:idx1] + content[idx2:]
    
    idx3 = content.find(target_marker)
    if idx3 != -1:
        content = content[:idx3] + block + "\n\n" + content[idx3:]
    else:
        print(f"Failed to find target {target_marker}")

# Move accessibleContentCounts
move_block_down(
    "    val accessibleContentCounts: StateFlow",
    "    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.data.util.PlanAccessibleContentCounts(0, 0, 0))",
    "    private val _isGuestMode"
)

# Move accessibleStudyNotes
move_block_down(
    "    val accessibleStudyNotes: StateFlow",
    "    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())",
    "    private val _isGuestMode"
)

# Move accessibleQuestions
move_block_down(
    "    val accessibleQuestions: StateFlow",
    "    ).stateIn(\n        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()\n    )",
    "    private val _isGuestMode"
)

# Move hiddenQuestions
move_block_down(
    "    val hiddenQuestions: StateFlow",
    "    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())",
    "    private val _isGuestMode"
)

# Move bookmarkedQuestions
move_block_down(
    "    val bookmarkedQuestions: StateFlow",
    "    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())",
    "    private val _isGuestMode"
)

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(content)
print("Moved blocks")
