import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

def extract_and_remove(pattern):
    global content
    match = re.search(pattern, content, flags=re.DOTALL)
    if not match:
        print(f"Could not match pattern: {pattern[:50]}...")
        return ""
    
    block = match.group(0)
    content = content[:match.start()] + content[match.end():]
    return block

# The regex for stateIn can be a bit tricky because of newlines, so we'll match up to emptyList()) or similar.
blocks = []

# accessibleContentCounts
b = extract_and_remove(r"    val accessibleContentCounts: StateFlow.*?PlanAccessibleContentCounts\(0, 0, 0\)\n    \)")
if b: blocks.append(b)

# accessibleStudyNotes
b = extract_and_remove(r"    val accessibleStudyNotes: StateFlow.*?emptyList\(\)\n    \)")
if b: blocks.append(b)

# accessibleMockTests (in case it is there)
b = extract_and_remove(r"    val accessibleMockTests: StateFlow.*?emptyList\(\)\n    \)")
if b: blocks.append(b)

# accessibleQuestions
b = extract_and_remove(r"    val accessibleQuestions: StateFlow.*?emptyList\(\)\n    \)")
if b: blocks.append(b)

# hiddenQuestions
b = extract_and_remove(r"    val hiddenQuestions: StateFlow.*?emptyList\(\)\n    \)")
if b: blocks.append(b)

# bookmarkedQuestions
b = extract_and_remove(r"    val bookmarkedQuestions: StateFlow.*?emptyList\(\)\n    \)")
if b: blocks.append(b)


target = "    private val _isGuestMode"
idx = content.find(target)
if idx != -1:
    content = content[:idx] + "\n\n".join(blocks) + "\n\n" + content[idx:]
    with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
        f.write(content)
    print("Moved blocks successfully.")
else:
    print("Could not find _isGuestMode")
