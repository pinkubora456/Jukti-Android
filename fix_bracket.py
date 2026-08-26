import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    lines = f.readlines()

def move_block(start_marker, target_marker):
    start_idx = -1
    for i, line in enumerate(lines):
        if line.startswith(start_marker):
            start_idx = i
            break
    
    if start_idx == -1:
        print(f"Start marker not found: {start_marker}")
        return
        
    brackets = 0
    end_idx = start_idx
    in_block = False
    
    for i in range(start_idx, len(lines)):
        line = lines[i]
        brackets += line.count('{') - line.count('}')
        if '{' in line:
            in_block = True
            
        if in_block and brackets <= 0:
            end_idx = i
            if i + 1 < len(lines) and ".stateIn" in lines[i+1]:
                end_idx = i + 1
            if i + 2 < len(lines) and ".stateIn" in lines[i+2]:
                end_idx = i + 2
            if i + 3 < len(lines) and "emptyList())" in lines[i+3]:
                end_idx = i + 3
            break
            
    if end_idx == start_idx and not in_block:
        end_idx = start_idx
        while end_idx < len(lines) and not lines[end_idx].strip().endswith(")"):
            end_idx += 1
            
    # let's just make sure we capture up to emptyList())
    while end_idx < len(lines) and "emptyList()" not in lines[end_idx]:
        end_idx += 1
        
    block = lines[start_idx:end_idx+1]
    
    target_idx = -1
    for i, line in enumerate(lines):
        if line.startswith(target_marker):
            target_idx = i
            break
            
    if target_idx != -1:
        # remove block
        del lines[start_idx:end_idx+1]
        
        # update target idx
        target_idx = -1
        for i, line in enumerate(lines):
            if line.startswith(target_marker):
                target_idx = i
                break
                
        lines.insert(target_idx, "".join(block) + "\n")
        print(f"Moved {start_marker}")

move_block("    val accessibleStudyNotes: StateFlow<List<StudyNoteEntity>> = combine(", "    private val _isGuestMode")
move_block("    val accessibleQuestions: StateFlow<List<QuestionEntity>> = combine(", "    private val _isGuestMode")
move_block("    val bookmarkedQuestions: StateFlow<List<QuestionEntity>> = combine(", "    private val _isGuestMode")
move_block("    val hiddenQuestions: StateFlow<List<QuestionEntity>> = combine(", "    private val _isGuestMode")

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.writelines(lines)
