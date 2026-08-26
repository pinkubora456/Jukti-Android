import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

match = re.search(r"    val accessibleContentCounts: StateFlow.*?PlanAccessibleContentCounts\(\)\n    \)", content, flags=re.DOTALL)
if match:
    block = match.group(0)
    content = content[:match.start()] + content[match.end():]
    
    target = "    private val _isGuestMode"
    idx = content.find(target)
    content = content[:idx] + block + "\n\n" + content[idx:]
    with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
        f.write(content)
    print("Fixed accessibleContentCounts")
else:
    print("Could not find accessibleContentCounts")
