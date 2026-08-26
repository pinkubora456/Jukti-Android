import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

content = content.replace("""    fun canAccessQuestion(question: com.example.data.local.QuestionEntity): Boolean {
        return com.example.data.util.PlanValidityEngine.isQuestionAccessible(question, effectiveEntitlement.value, isAdminOrOwner.value)
    }""", """    fun canAccessQuestion(question: com.example.data.local.QuestionEntity): Boolean {
        val effective = effectiveEntitlement.value ?: com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(userEntitlements.value, plans.value, getTrustedTime())
        return com.example.data.util.PlanValidityEngine.isQuestionAccessible(question, effective, isAdminOrOwner.value)
    }""")

content = content.replace("""    fun canAccessMockTest(mock: com.example.data.local.MockTestEntity): Boolean {
        return com.example.data.util.PlanValidityEngine.isMockTestAccessible(mock, effectiveEntitlement.value, isAdminOrOwner.value)
    }""", """    fun canAccessMockTest(mock: com.example.data.local.MockTestEntity): Boolean {
        val effective = effectiveEntitlement.value ?: com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(userEntitlements.value, plans.value, getTrustedTime())
        return com.example.data.util.PlanValidityEngine.isMockTestAccessible(mock, effective, isAdminOrOwner.value)
    }""")

content = content.replace("""    fun canAccessStudyNote(note: com.example.data.local.StudyNoteEntity): Boolean {
        return com.example.data.util.PlanValidityEngine.isStudyNoteAccessible(note, effectiveEntitlement.value, isAdminOrOwner.value)
    }""", """    fun canAccessStudyNote(note: com.example.data.local.StudyNoteEntity): Boolean {
        val effective = effectiveEntitlement.value ?: com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(userEntitlements.value, plans.value, getTrustedTime())
        return com.example.data.util.PlanValidityEngine.isStudyNoteAccessible(note, effective, isAdminOrOwner.value)
    }""")

content = content.replace("""    ) { questions, effective, isAdmin ->
        questions.filter { q ->
            com.example.data.util.PlanValidityEngine.isQuestionAccessible(q, effective, isAdmin)
        }""", """    ) { questions, effective, isAdmin ->
        val eff = effective ?: com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(userEntitlements.value, plans.value, getTrustedTime())
        questions.filter { q ->
            com.example.data.util.PlanValidityEngine.isQuestionAccessible(q, eff, isAdmin)
        }""")

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(content)
print("Patched JuktiViewModel nullability")
