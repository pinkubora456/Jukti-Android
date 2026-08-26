import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    text = f.read()

# Fix questions
text = text.replace(
    "kotlinx.coroutines.flow.combine(repository.allQuestions, repository.premiumQuestions) { f: List<QuestionEntity>, p: List<QuestionEntity> -> f + p }",
    "repository.allQuestions"
)

# Fix studyNotes
text = text.replace(
    "kotlinx.coroutines.flow.combine(repository.allNotes, repository.premiumStudyNotes) { f: List<StudyNoteEntity>, p: List<StudyNoteEntity> -> f + p }",
    "repository.allNotes"
)

# Fix mockTests to be locked when offline
mockTests_decl = """    val mockTests = repository.allMockTests.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )"""

mockTests_new = """    val mockTests: StateFlow<List<MockTestEntity>> = combine(
        repository.allMockTests, effectiveEntitlement, isAdminOrOwner, networkMonitor.isConnected
    ) { list, effective, isAdmin, isConnected ->
        val eff = effective ?: com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(userEntitlements.value, plans.value, getTrustedTime())
        list.map { m ->
            if (!m.isPremium || (isConnected && com.example.data.util.PlanValidityEngine.isMockTestAccessible(m, eff, isAdmin))) m
            else m.copy(
                titleEn = "Premium Content 🔒", titleAs = "প্ৰিমিয়াম সমল 🔒"
            )
        }
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )"""

text = text.replace(mockTests_decl, mockTests_new)

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(text)

