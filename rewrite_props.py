import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

# We will just capture the entire class body from `{` down to `init {` and sort it? No, too hard.
# I will use Python to find the properties that are causing compilation errors and move them below the ones they depend on.

def move_block_down(start_marker, end_marker, target_marker):
    global content
    idx1 = content.find(start_marker)
    idx2 = content.find(end_marker)
    if idx1 == -1 or idx2 == -1: return
    idx2 += len(end_marker)
    
    block = content[idx1:idx2]
    content = content[:idx1] + content[idx2:]
    
    idx3 = content.find(target_marker)
    if idx3 != -1:
        content = content[:idx3] + block + "\n\n" + content[idx3:]

# Move accessibleContentCounts
move_block_down(
    "    val accessibleContentCounts: StateFlow",
    "    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.data.util.PlanAccessibleContentCounts(0, 0, 0))",
    "    private val _isGuestMode"
)

# Move accessibleMockTests
move_block_down(
    "    val accessibleMockTests: StateFlow",
    "    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())",
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

# Write back
with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(content)

