import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

content = re.sub(
    r'val accessibleMockTests: StateFlow<List<MockTestEntity>> = combine\(\s*userEntitlements,\s*plans,\s*mockTests,\s*isAdminOrOwner\s*\) \{ ents, allPlans, mocks, admin ->\s*com\.example\.data\.util\.PlanValidityEngine\.filterAccessibleMockTests\(\s*userProfile = null,\s*entitlements = entitlements,',
    '''val accessibleMockTests: StateFlow<List<MockTestEntity>> = combine(
        userEntitlements,
        plans,
        mockTests,
        isAdminOrOwner
    ) { ents, allPlans, mocks, admin ->
        com.example.data.util.PlanValidityEngine.filterAccessibleMockTests(
            userProfile = null,
            entitlements = ents,''', content)

content = re.sub(
    r'val accessibleStudyNotes: StateFlow<List<StudyNoteEntity>> = combine\(\s*userEntitlements,\s*plans,\s*studyNotes,\s*isAdminOrOwner\s*\) \{ ents, allPlans, notes, admin ->\s*com\.example\.data\.util\.PlanValidityEngine\.filterAccessibleStudyNotes\(\s*userProfile = null,\s*entitlements = entitlements,',
    '''val accessibleStudyNotes: StateFlow<List<StudyNoteEntity>> = combine(
        userEntitlements,
        plans,
        studyNotes,
        isAdminOrOwner
    ) { ents, allPlans, notes, admin ->
        com.example.data.util.PlanValidityEngine.filterAccessibleStudyNotes(
            userProfile = null,
            entitlements = ents,''', content)

content = re.sub(
    r'val accessibleQuestions: StateFlow<List<QuestionEntity>> = combine\(\s*userEntitlements,\s*plans,\s*questions,\s*isAdminOrOwner\s*\) \{ ents, allPlans, qs, admin ->\s*com\.example\.data\.util\.PlanValidityEngine\.filterAccessibleQuestions\(\s*userProfile = null,\s*entitlements = entitlements,',
    '''val accessibleQuestions: StateFlow<List<QuestionEntity>> = combine(
        userEntitlements,
        plans,
        questions,
        isAdminOrOwner
    ) { ents, allPlans, qs, admin ->
        com.example.data.util.PlanValidityEngine.filterAccessibleQuestions(
            userProfile = null,
            entitlements = ents,''', content)

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(content)

