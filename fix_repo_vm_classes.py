import re

# Fix JuktiRepository
with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "r") as f:
    content = f.read()

# Remove the appended function at the very end
malformed_func = """    suspend fun migrateGuidanceToFirestore() {
        val pyqs = guidanceDao.getAllPyqFocus().firstOrNull() ?: emptyList()
        val strats = guidanceDao.getAllPrepStrategies().firstOrNull() ?: emptyList()
        firebaseRepository.uploadGuidanceData(pyqs, strats)
    }"""
content = content.replace(malformed_func, "")
content = content.strip()
if content.endswith("}"):
    content = content[:-1] + malformed_func + "\n}"

with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "w") as f:
    f.write(content)

# Fix JuktiViewModel
with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

malformed_vm_func = """    fun migrateGuidanceToFirestore() {
        viewModelScope.launch {
            repository.migrateGuidanceToFirestore()
        }
    }"""
content = content.replace(malformed_vm_func, "")
malformed_vm_func2 = """    fun refreshGuidanceData() {
        viewModelScope.launch {
            repository.refreshGuidanceData()
        }
    }"""
content = content.replace(malformed_vm_func2, "")

# Find where JuktiViewModel ends.
# We can search for `PlanPurchaseSummaryState` or similar to know where the class ends.
# JuktiViewModel starts around line 90 and goes up to ~ 4000.
# Let's just find the last function `checkAndUpdateAllEntitlements` or similar.

