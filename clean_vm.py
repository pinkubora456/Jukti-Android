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

def remove_stray(text, func_name):
    pattern = r'    fun ' + func_name + r'\(\).*?\{.*?\n    \}'
    return re.sub(pattern, '', text, flags=re.DOTALL)

content = remove_stray(content, "migrateGuidanceToFirestore")
content = remove_stray(content, "refreshGuidanceData")

# Let's insert it back safely inside JuktiViewModel
idx = content.rfind("companion object {")
if idx != -1:
    content = content[:idx] + funcs + "\n    " + content[idx:]

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(content)
