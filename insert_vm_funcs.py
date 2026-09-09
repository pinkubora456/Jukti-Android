import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

# We need to insert the two functions inside JuktiViewModel
funcs = """
    fun migrateGuidanceToFirestore() {
        viewModelScope.launch {
            repository.migrateGuidanceToFirestore()
        }
    }

    fun refreshGuidanceData() {
        viewModelScope.launch {
            repository.refreshGuidanceData()
        }
    }
"""

if "migrateGuidanceToFirestore" not in content:
    # Let's insert before `companion object {` which is near the bottom of JuktiViewModel
    idx = content.rfind("companion object {")
    if idx != -1:
        content = content[:idx] + funcs + "\n    " + content[idx:]
    else:
        # maybe before `class PlanPurchaseSummaryItem`
        idx2 = content.rfind("data class PlanPurchaseSummaryItem")
        if idx2 != -1:
            # find the end of JuktiViewModel before it
            content = content[:idx2] + funcs + "}\n\n" + content[idx2:]

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(content)
