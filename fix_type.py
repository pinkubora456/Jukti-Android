import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

content = content.replace(
    "val questions = combine(kotlinx.coroutines.flow.combine(repository.allQuestions, repository.premiumQuestions) { f, p -> f + p }, effectiveEntitlement, isAdminOrOwner) { list, effective, isAdmin ->",
    "val questions: StateFlow<List<QuestionEntity>> = combine(kotlinx.coroutines.flow.combine(repository.allQuestions, repository.premiumQuestions) { f: List<QuestionEntity>, p: List<QuestionEntity> -> f + p }, effectiveEntitlement, isAdminOrOwner) { list: List<QuestionEntity>, effective: com.example.data.util.EffectiveUserEntitlement?, isAdmin: Boolean ->"
)

content = content.replace(
    "val mockTests = combine(kotlinx.coroutines.flow.combine(repository.allMockTests, repository.premiumMockTests) { f, p -> f + p }, effectiveEntitlement, isAdminOrOwner) { list, effective, isAdmin ->",
    "val mockTests: StateFlow<List<MockTestEntity>> = combine(kotlinx.coroutines.flow.combine(repository.allMockTests, repository.premiumMockTests) { f: List<MockTestEntity>, p: List<MockTestEntity> -> f + p }, effectiveEntitlement, isAdminOrOwner) { list: List<MockTestEntity>, effective: com.example.data.util.EffectiveUserEntitlement?, isAdmin: Boolean ->"
)

content = content.replace(
    "val studyNotes = combine(kotlinx.coroutines.flow.combine(repository.allNotes, repository.premiumStudyNotes) { f, p -> f + p }, effectiveEntitlement, isAdminOrOwner) { list, effective, isAdmin ->",
    "val studyNotes: StateFlow<List<StudyNoteEntity>> = combine(kotlinx.coroutines.flow.combine(repository.allNotes, repository.premiumStudyNotes) { f: List<StudyNoteEntity>, p: List<StudyNoteEntity> -> f + p }, effectiveEntitlement, isAdminOrOwner) { list: List<StudyNoteEntity>, effective: com.example.data.util.EffectiveUserEntitlement?, isAdmin: Boolean ->"
)

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(content)
print("Updated ViewModel explicit types")
