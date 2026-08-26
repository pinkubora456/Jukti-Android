import re

with open("app/src/main/java/com/example/data/util/PlanValidityEngine.kt", "r") as f:
    content = f.read()

target = """    fun filterAccessibleMockTests(
        userProfile: UserProfileEntity?,
        entitlements: List<EntitlementEntity>?,
        plans: List<PlanEntity>,
        mockTests: List<MockTestEntity>,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): List<MockTestEntity> {
        return mockTests
    }"""

replacement = """    fun filterAccessibleMockTests(
        userProfile: UserProfileEntity?,
        entitlements: List<EntitlementEntity>?,
        plans: List<PlanEntity>,
        mockTests: List<MockTestEntity>,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): List<MockTestEntity> {
        val effective = resolveEffectiveEntitlement(entitlements, plans, currentTime)
        return mockTests.filter { isMockTestAccessible(it, effective, isAdminOrOwner) }
    }"""

if target in content:
    content = content.replace(target, replacement)
    print("Patched filterAccessibleMockTests")
else:
    print("Target mock tests not found")


target = """    fun filterAccessibleStudyNotes(
        userProfile: UserProfileEntity?,
        entitlements: List<EntitlementEntity>?,
        plans: List<PlanEntity>,
        studyNotes: List<StudyNoteEntity>,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): List<StudyNoteEntity> {
        return studyNotes
    }"""

replacement = """    fun filterAccessibleStudyNotes(
        userProfile: UserProfileEntity?,
        entitlements: List<EntitlementEntity>?,
        plans: List<PlanEntity>,
        studyNotes: List<StudyNoteEntity>,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): List<StudyNoteEntity> {
        val effective = resolveEffectiveEntitlement(entitlements, plans, currentTime)
        return studyNotes.filter { isStudyNoteAccessible(it, effective, isAdminOrOwner) }
    }"""

if target in content:
    content = content.replace(target, replacement)
    print("Patched filterAccessibleStudyNotes")
else:
    print("Target study notes not found")


target = """    fun filterAccessibleQuestions(
        userProfile: UserProfileEntity?,
        entitlements: List<EntitlementEntity>?,
        plans: List<PlanEntity>,
        questions: List<QuestionEntity>,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): List<QuestionEntity> {
        val validQuestions = questions.filter { !it.isReported }
        return validQuestions
    }"""

replacement = """    fun filterAccessibleQuestions(
        userProfile: UserProfileEntity?,
        entitlements: List<EntitlementEntity>?,
        plans: List<PlanEntity>,
        questions: List<QuestionEntity>,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): List<QuestionEntity> {
        val effective = resolveEffectiveEntitlement(entitlements, plans, currentTime)
        val validQuestions = questions.filter { !it.isReported }
        return validQuestions.filter { isQuestionAccessible(it, effective, isAdminOrOwner) }
    }"""

if target in content:
    content = content.replace(target, replacement)
    print("Patched filterAccessibleQuestions")
else:
    print("Target questions not found")


with open("app/src/main/java/com/example/data/util/PlanValidityEngine.kt", "w") as f:
    f.write(content)

