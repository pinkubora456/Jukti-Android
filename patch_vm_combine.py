import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

# For accessibleMockTests
content = re.sub(
    r'val accessibleMockTests: StateFlow<List<MockTestEntity>> = combine\(\s*userProfile,\s*userEntitlements,\s*plans,\s*mockTests,\s*isAdminOrOwner\s*\) \{ args: Array<Any\?> ->\s*val profile = args\[0\] as\? UserProfileEntity\s*@Suppress\("UNCHECKED_CAST"\)\s*val entitlements = args\[1\] as\? List<EntitlementEntity> \?: emptyList\(\)\s*@Suppress\("UNCHECKED_CAST"\)\s*val allPlans = args\[2\] as\? List<PlanEntity> \?: emptyList\(\)\s*@Suppress\("UNCHECKED_CAST"\)\s*val mocks = args\[3\] as\? List<MockTestEntity> \?: emptyList\(\)\s*val admin = args\[4\] as\? Boolean \?: false\s*com.example.data.util.PlanValidityEngine.filterAccessibleMockTests\(\s*userProfile = profile,',
    '''val accessibleMockTests: StateFlow<List<MockTestEntity>> = combine(
        userEntitlements,
        plans,
        mockTests,
        isAdminOrOwner
    ) { ents, allPlans, mocks, admin ->
        com.example.data.util.PlanValidityEngine.filterAccessibleMockTests(
            userProfile = null,''', content)

# For accessibleContentCounts
content = re.sub(
    r'val accessibleContentCounts: StateFlow<com\.example\.data\.util\.PlanAccessibleContentCounts> = combine\(\s*userProfile,\s*userEntitlements,\s*plans,\s*mockTests,\s*studyNotes,\s*questions,\s*isAdminOrOwner\s*\) \{ args: Array<Any\?> ->\s*val profile = args\[0\] as\? UserProfileEntity\s*@Suppress\("UNCHECKED_CAST"\)\s*val entitlements = args\[1\] as\? List<EntitlementEntity> \?: emptyList\(\)\s*@Suppress\("UNCHECKED_CAST"\)\s*val allPlans = args\[2\] as\? List<PlanEntity> \?: emptyList\(\)\s*@Suppress\("UNCHECKED_CAST"\)\s*val mocks = args\[3\] as\? List<MockTestEntity> \?: emptyList\(\)\s*@Suppress\("UNCHECKED_CAST"\)\s*val notes = args\[4\] as\? List<StudyNoteEntity> \?: emptyList\(\)\s*@Suppress\("UNCHECKED_CAST"\)\s*val qs = args\[5\] as\? List<QuestionEntity> \?: emptyList\(\)\s*val admin = args\[6\] as\? Boolean \?: false\s*com\.example\.data\.util\.PlanValidityEngine\.calculateAccessibleCounts\(\s*userProfile = profile,',
    '''val accessibleContentCounts: StateFlow<com.example.data.util.PlanAccessibleContentCounts> = combine(
        userEntitlements,
        plans,
        mockTests,
        studyNotes,
        questions,
        isAdminOrOwner
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val entitlements = args[0] as? List<EntitlementEntity> ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val allPlans = args[1] as? List<PlanEntity> ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val mocks = args[2] as? List<MockTestEntity> ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val notes = args[3] as? List<StudyNoteEntity> ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val qs = args[4] as? List<QuestionEntity> ?: emptyList()
        val admin = args[5] as? Boolean ?: false
        com.example.data.util.PlanValidityEngine.calculateAccessibleCounts(
            userProfile = null,''', content)

# For accessibleStudyNotes
content = re.sub(
    r'val accessibleStudyNotes: StateFlow<List<StudyNoteEntity>> = combine\(\s*userProfile,\s*userEntitlements,\s*plans,\s*studyNotes,\s*isAdminOrOwner\s*\) \{ args: Array<Any\?> ->\s*val profile = args\[0\] as\? UserProfileEntity\s*@Suppress\("UNCHECKED_CAST"\)\s*val entitlements = args\[1\] as\? List<EntitlementEntity> \?: emptyList\(\)\s*@Suppress\("UNCHECKED_CAST"\)\s*val allPlans = args\[2\] as\? List<PlanEntity> \?: emptyList\(\)\s*@Suppress\("UNCHECKED_CAST"\)\s*val notes = args\[3\] as\? List<StudyNoteEntity> \?: emptyList\(\)\s*val admin = args\[4\] as\? Boolean \?: false\s*com\.example\.data\.util\.PlanValidityEngine\.filterAccessibleStudyNotes\(\s*userProfile = profile,',
    '''val accessibleStudyNotes: StateFlow<List<StudyNoteEntity>> = combine(
        userEntitlements,
        plans,
        studyNotes,
        isAdminOrOwner
    ) { ents, allPlans, notes, admin ->
        com.example.data.util.PlanValidityEngine.filterAccessibleStudyNotes(
            userProfile = null,''', content)


# For accessibleQuestions
content = re.sub(
    r'val accessibleQuestions: StateFlow<List<QuestionEntity>> = combine\(\s*userProfile,\s*userEntitlements,\s*plans,\s*questions,\s*isAdminOrOwner\s*\) \{ args: Array<Any\?> ->\s*val profile = args\[0\] as\? UserProfileEntity\s*@Suppress\("UNCHECKED_CAST"\)\s*val entitlements = args\[1\] as\? List<EntitlementEntity> \?: emptyList\(\)\s*@Suppress\("UNCHECKED_CAST"\)\s*val allPlans = args\[2\] as\? List<PlanEntity> \?: emptyList\(\)\s*@Suppress\("UNCHECKED_CAST"\)\s*val qs = args\[3\] as\? List<QuestionEntity> \?: emptyList\(\)\s*val admin = args\[4\] as\? Boolean \?: false\s*com\.example\.data\.util\.PlanValidityEngine\.filterAccessibleQuestions\(\s*userProfile = profile,',
    '''val accessibleQuestions: StateFlow<List<QuestionEntity>> = combine(
        userEntitlements,
        plans,
        questions,
        isAdminOrOwner
    ) { ents, allPlans, qs, admin ->
        com.example.data.util.PlanValidityEngine.filterAccessibleQuestions(
            userProfile = null,''', content)


with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(content)

