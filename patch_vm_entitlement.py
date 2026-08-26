import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

target = """    val isUserPremium: StateFlow<Boolean> = combine(userProfile, isAdminOrOwner, userEntitlements, plans) { profile, admin, entitlements, allPlans ->
        val email = profile?.email?.trim()?.lowercase() ?: ""
        val isOwner = email == "juktieducation@gmail.com"
        if (isOwner || admin) {
            true
        } else {
            com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(entitlements, allPlans, getTrustedTime()).isPremium
        }
    }.stateIn(
        viewModelScope, SharingStarted.Eagerly, false
    )"""

replacement = """    val isUserPremium: StateFlow<Boolean> = combine(userProfile, isAdminOrOwner, userEntitlements, plans) { profile, admin, entitlements, allPlans ->
        val email = profile?.email?.trim()?.lowercase() ?: ""
        val isOwner = email == "juktieducation@gmail.com"
        if (isOwner || admin) {
            true
        } else {
            com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(entitlements, allPlans, getTrustedTime()).isPremium
        }
    }.stateIn(
        viewModelScope, SharingStarted.Eagerly, false
    )

    val effectiveEntitlement: StateFlow<com.example.data.util.EffectiveUserEntitlement?> = combine(
        userProfile, isAdminOrOwner, userEntitlements, plans
    ) { profile, admin, entitlements, allPlans ->
        val email = profile?.email?.trim()?.lowercase() ?: ""
        val isOwner = email == "juktieducation@gmail.com"
        val effective = com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(entitlements, allPlans, getTrustedTime())
        if (isOwner || admin) {
            effective.copy(isPremium = true, hasAllExamsAccess = true)
        } else {
            effective
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    
    fun canAccessQuestion(question: com.example.data.local.QuestionEntity): Boolean {
        return com.example.data.util.PlanValidityEngine.isQuestionAccessible(question, effectiveEntitlement.value, isAdminOrOwner.value)
    }
    
    fun canAccessMockTest(mock: com.example.data.local.MockTestEntity): Boolean {
        return com.example.data.util.PlanValidityEngine.isMockTestAccessible(mock, effectiveEntitlement.value, isAdminOrOwner.value)
    }
    
    fun canAccessStudyNote(note: com.example.data.local.StudyNoteEntity): Boolean {
        return com.example.data.util.PlanValidityEngine.isStudyNoteAccessible(note, effectiveEntitlement.value, isAdminOrOwner.value)
    }"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
        f.write(content)
    print("Patched JuktiViewModel effectiveEntitlement")
else:
    print("Could not find target in JuktiViewModel")
