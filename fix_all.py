import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

def move_down(start_str, end_regex, target_str):
    global content
    idx1 = content.find(start_str)
    if idx1 == -1:
        print(f"Could not find start_str: {start_str[:40]}")
        return
    
    match = re.search(end_regex, content[idx1:])
    if not match:
        print(f"Could not find end_regex for {start_str[:40]}")
        return
        
    idx2 = idx1 + match.end()
    block = content[idx1:idx2]
    content = content[:idx1] + content[idx2:]
    
    idx3 = content.find(target_str)
    if idx3 != -1:
        content = content[:idx3] + block + "\n\n" + content[idx3:]
        print(f"Moved {start_str[:40]} successfully")
    else:
        print(f"Could not find target_str: {target_str}")

# Move accessibleStudyNotes
move_down("    val accessibleStudyNotes: StateFlow", r"\)\.stateIn\(viewModelScope, SharingStarted\.WhileSubscribed\(5000\), emptyList\(\)\)", "    private val _isGuestMode")

# Move accessibleQuestions
move_down("    val accessibleQuestions: StateFlow", r"\)\.stateIn\(\n        viewModelScope, SharingStarted\.WhileSubscribed\(5000\), emptyList\(\)\n    \)", "    private val _isGuestMode")

# Move bookmarkedQuestions
move_down("    val bookmarkedQuestions: StateFlow", r"\)\.stateIn\(viewModelScope, SharingStarted\.WhileSubscribed\(5000\), emptyList\(\)\)", "    private val _isGuestMode")

# Move hiddenQuestions
move_down("    val hiddenQuestions: StateFlow", r"\)\.stateIn\(viewModelScope, SharingStarted\.WhileSubscribed\(5000\), emptyList\(\)\)", "    private val _isGuestMode")


with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(content)
