import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

# Replace allQuestions
content = content.replace(
    "combine(repository.allQuestions, effectiveEntitlement, isAdminOrOwner)", 
    "combine(kotlinx.coroutines.flow.combine(repository.allQuestions, repository.premiumQuestions) { f, p -> f + p }, effectiveEntitlement, isAdminOrOwner)"
)

# Replace allMockTests
content = content.replace(
    "combine(repository.allMockTests, effectiveEntitlement, isAdminOrOwner)", 
    "combine(kotlinx.coroutines.flow.combine(repository.allMockTests, repository.premiumMockTests) { f, p -> f + p }, effectiveEntitlement, isAdminOrOwner)"
)

# Replace allNotes
content = content.replace(
    "combine(repository.allNotes, effectiveEntitlement, isAdminOrOwner)", 
    "combine(kotlinx.coroutines.flow.combine(repository.allNotes, repository.premiumStudyNotes) { f, p -> f + p }, effectiveEntitlement, isAdminOrOwner)"
)

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(content)
print("Updated ViewModel")
