import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

funcs = """
    fun refreshGuidanceData() {
        viewModelScope.launch {
            repository.refreshGuidanceData()
        }
    }

    fun migrateGuidanceToFirestore() {
        viewModelScope.launch {
            repository.migrateGuidanceToFirestore()
        }
    }
"""

if "fun refreshGuidanceData()" not in content:
    # insert before companion object
    idx = content.rfind("companion object {")
    if idx != -1:
        content = content[:idx] + funcs + "\n    " + content[idx:]
    else:
        # maybe before final }
        idx = content.rfind("}")
        if idx != -1:
            content = content[:idx] + funcs + "\n}"

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(content)

