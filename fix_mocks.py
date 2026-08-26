import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

target = """    val mockTests = repository.allMockTests.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )"""

idx1 = content.find(target)
if idx1 != -1:
    content = content[:idx1] + content[idx1 + len(target):]
    idx2 = content.find("    val examsList: StateFlow")
    content = content[:idx2] + target + "\n\n" + content[idx2:]
    with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
        f.write(content)
    print("Fixed mocks")
