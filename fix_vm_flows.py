import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    vm = f.read()

# For questions
vm = vm.replace(
"""    val questions: StateFlow<List<QuestionEntity>> = combine(kotlinx.coroutines.flow.combine(repository.allQuestions, repository.premiumQuestions) { f: List<QuestionEntity>, p: List<QuestionEntity> -> f + p }, effectiveEntitlement, isAdminOrOwner) { list: List<QuestionEntity>, effective: com.example.data.util.EffectiveUserEntitlement?, isAdmin: Boolean ->
        val eff = effective ?: com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(userEntitlements.value, plans.value, getTrustedTime())
        list.filter { !it.isReported }.map { q ->
            if (!q.isPremium || com.example.data.util.PlanValidityEngine.isQuestionAccessible(q, eff, isAdmin)) q
            else q.copy(""",
"""    val questions: StateFlow<List<QuestionEntity>> = combine(kotlinx.coroutines.flow.combine(repository.allQuestions, repository.premiumQuestions) { f: List<QuestionEntity>, p: List<QuestionEntity> -> f + p }, effectiveEntitlement, isAdminOrOwner, networkMonitor.isConnected) { list: List<QuestionEntity>, effective: com.example.data.util.EffectiveUserEntitlement?, isAdmin: Boolean, isConnected: Boolean ->
        val eff = effective ?: com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(userEntitlements.value, plans.value, getTrustedTime())
        list.filter { !it.isReported }.map { q ->
            if (!q.isPremium || (isConnected && com.example.data.util.PlanValidityEngine.isQuestionAccessible(q, eff, isAdmin))) q
            else q.copy("""
)

# For mockTests
vm = vm.replace(
"""    val mockTests: StateFlow<List<MockTestEntity>> = combine(kotlinx.coroutines.flow.combine(repository.allMockTests, repository.premiumMockTests) { f: List<MockTestEntity>, p: List<MockTestEntity> -> f + p }, effectiveEntitlement, isAdminOrOwner) { list: List<MockTestEntity>, effective: com.example.data.util.EffectiveUserEntitlement?, isAdmin: Boolean ->
        val eff = effective ?: com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(userEntitlements.value, plans.value, getTrustedTime())
        list.map { m ->
            if (!m.isPremium || com.example.data.util.PlanValidityEngine.isMockTestAccessible(m, eff, isAdmin)) m
            else m.copy(""",
"""    val mockTests: StateFlow<List<MockTestEntity>> = combine(kotlinx.coroutines.flow.combine(repository.allMockTests, repository.premiumMockTests) { f: List<MockTestEntity>, p: List<MockTestEntity> -> f + p }, effectiveEntitlement, isAdminOrOwner, networkMonitor.isConnected) { list: List<MockTestEntity>, effective: com.example.data.util.EffectiveUserEntitlement?, isAdmin: Boolean, isConnected: Boolean ->
        val eff = effective ?: com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(userEntitlements.value, plans.value, getTrustedTime())
        list.map { m ->
            if (!m.isPremium || (isConnected && com.example.data.util.PlanValidityEngine.isMockTestAccessible(m, eff, isAdmin))) m
            else m.copy("""
)

# For studyNotes
vm = vm.replace(
"""    val studyNotes: StateFlow<List<StudyNoteEntity>> = combine(kotlinx.coroutines.flow.combine(repository.allNotes, repository.premiumStudyNotes) { f: List<StudyNoteEntity>, p: List<StudyNoteEntity> -> f + p }, effectiveEntitlement, isAdminOrOwner) { list: List<StudyNoteEntity>, effective: com.example.data.util.EffectiveUserEntitlement?, isAdmin: Boolean ->
        val eff = effective ?: com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(userEntitlements.value, plans.value, getTrustedTime())
        list.map { n ->
            if (!n.isPremium || com.example.data.util.PlanValidityEngine.isStudyNoteAccessible(n, eff, isAdmin)) n
            else n.copy(""",
"""    val studyNotes: StateFlow<List<StudyNoteEntity>> = combine(kotlinx.coroutines.flow.combine(repository.allNotes, repository.premiumStudyNotes) { f: List<StudyNoteEntity>, p: List<StudyNoteEntity> -> f + p }, effectiveEntitlement, isAdminOrOwner, networkMonitor.isConnected) { list: List<StudyNoteEntity>, effective: com.example.data.util.EffectiveUserEntitlement?, isAdmin: Boolean, isConnected: Boolean ->
        val eff = effective ?: com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(userEntitlements.value, plans.value, getTrustedTime())
        list.map { n ->
            if (!n.isPremium || (isConnected && com.example.data.util.PlanValidityEngine.isStudyNoteAccessible(n, eff, isAdmin))) n
            else n.copy("""
)

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(vm)
