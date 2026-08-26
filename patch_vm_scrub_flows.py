import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

target = """    val questions = repository.allQuestions.map { list ->
        list.filter { !it.isReported }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())"""

replacement = """    val questions = combine(repository.allQuestions, effectiveEntitlement, isAdminOrOwner) { list, effective, isAdmin ->
        val eff = effective ?: com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(userEntitlements.value, plans.value, getTrustedTime())
        list.filter { !it.isReported }.map { q ->
            if (!q.isPremium || com.example.data.util.PlanValidityEngine.isQuestionAccessible(q, eff, isAdmin)) q
            else q.copy(
                questionEn = "Premium Content 🔒", questionAs = "প্ৰিমিয়াম সমল 🔒",
                optionAEn = "Unlock to view", optionAAs = "Unlock to view",
                optionBEn = "Unlock to view", optionBAs = "Unlock to view",
                optionCEn = "Unlock to view", optionCAs = "Unlock to view",
                optionDEn = "Unlock to view", optionDAs = "Unlock to view",
                correctOptionIndex = -1, explanationEn = "Locked", explanationAs = "Locked"
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())"""

content = content.replace(target, replacement)

target2 = """    val studyNotes = repository.allNotes.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )"""

replacement2 = """    val studyNotes = combine(repository.allNotes, effectiveEntitlement, isAdminOrOwner) { list, effective, isAdmin ->
        val eff = effective ?: com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(userEntitlements.value, plans.value, getTrustedTime())
        list.map { n ->
            if (!n.isPremium || com.example.data.util.PlanValidityEngine.isStudyNoteAccessible(n, eff, isAdmin)) n
            else n.copy(contentEn = "Premium Content 🔒", contentAs = "Premium Content 🔒")
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())"""

content = content.replace(target2, replacement2)

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(content)
print("Patched VM flows")
