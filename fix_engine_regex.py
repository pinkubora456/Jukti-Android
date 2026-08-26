import re

with open("app/src/main/java/com/example/data/util/PlanValidityEngine.kt", "r") as f:
    content = f.read()

# Mock Tests
mock_pattern = r'    fun filterAccessibleMockTests\(\n\s+userProfile: UserProfileEntity\?,\n\s+entitlements: List<EntitlementEntity>\?,\n\s+plans: List<PlanEntity>,\n\s+mockTests: List<MockTestEntity>,\n\s+isAdminOrOwner: Boolean,\n\s+currentTime: Long = System\.currentTimeMillis\(\)\n\s+\): List<MockTestEntity> \{.*?(?=\n    fun filterAccessibleMockTests\()'
mock_repl = """    fun filterAccessibleMockTests(
        userProfile: UserProfileEntity?,
        entitlements: List<EntitlementEntity>?,
        plans: List<PlanEntity>,
        mockTests: List<MockTestEntity>,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): List<MockTestEntity> {
        val isUserAdmin = isAdminOrOwner || (userProfile?.email?.trim()?.lowercase() == "juktieducation@gmail.com")
        if (isUserAdmin) return mockTests

        val effective = resolveEffectiveEntitlement(entitlements, plans, currentTime)

        if (!effective.isPremium) {
            return mockTests.filter { !it.isPremium }
        }

        return if (effective.hasAllExamsAccess || effective.combinedTargetExams.isEmpty()) {
            mockTests
        } else {
            mockTests.filter { mock ->
                !mock.isPremium || matchesExamTarget(mock.category, mock.titleEn, effective.combinedTargetExams.toList())
            }
        }
    }"""
content = re.sub(mock_pattern, mock_repl, content, flags=re.DOTALL)

# Study Notes
notes_pattern = r'    fun filterAccessibleStudyNotes\(\n\s+userProfile: UserProfileEntity\?,\n\s+entitlements: List<EntitlementEntity>\?,\n\s+plans: List<PlanEntity>,\n\s+studyNotes: List<StudyNoteEntity>,\n\s+isAdminOrOwner: Boolean,\n\s+currentTime: Long = System\.currentTimeMillis\(\)\n\s+\): List<StudyNoteEntity> \{.*?(?=\n    fun filterAccessibleStudyNotes\()'
notes_repl = """    fun filterAccessibleStudyNotes(
        userProfile: UserProfileEntity?,
        entitlements: List<EntitlementEntity>?,
        plans: List<PlanEntity>,
        studyNotes: List<StudyNoteEntity>,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): List<StudyNoteEntity> {
        val isUserAdmin = isAdminOrOwner || (userProfile?.email?.trim()?.lowercase() == "juktieducation@gmail.com")
        if (isUserAdmin) return studyNotes

        val effective = resolveEffectiveEntitlement(entitlements, plans, currentTime)

        if (!effective.isPremium) {
            return studyNotes.filter { !it.isPremium }
        }

        return if (effective.hasAllExamsAccess || effective.combinedTargetExams.isEmpty()) {
            studyNotes
        } else {
            studyNotes.filter { note ->
                !note.isPremium || matchesExamTarget(note.subject, "${note.topic} ${note.titleEn}", effective.combinedTargetExams.toList())
            }
        }
    }"""
content = re.sub(notes_pattern, notes_repl, content, flags=re.DOTALL)

# Questions
quest_pattern = r'    fun filterAccessibleQuestions\(\n\s+userProfile: UserProfileEntity\?,\n\s+entitlements: List<EntitlementEntity>\?,\n\s+plans: List<PlanEntity>,\n\s+questions: List<QuestionEntity>,\n\s+isAdminOrOwner: Boolean,\n\s+currentTime: Long = System\.currentTimeMillis\(\)\n\s+\): List<QuestionEntity> \{.*?(?=\n    fun filterAccessibleQuestions\()'
quest_repl = """    fun filterAccessibleQuestions(
        userProfile: UserProfileEntity?,
        entitlements: List<EntitlementEntity>?,
        plans: List<PlanEntity>,
        questions: List<QuestionEntity>,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): List<QuestionEntity> {
        val validQuestions = questions.filter { !it.isReported }
        val isUserAdmin = isAdminOrOwner || (userProfile?.email?.trim()?.lowercase() == "juktieducation@gmail.com")
        if (isUserAdmin) return validQuestions

        val effective = resolveEffectiveEntitlement(entitlements, plans, currentTime)

        if (!effective.isPremium) {
            return validQuestions.filter { !it.isPremium }
        }

        return if (effective.hasAllExamsAccess || effective.combinedTargetExams.isEmpty()) {
            validQuestions
        } else {
            validQuestions.filter { q ->
                !q.isPremium || matchesExamTarget(q.examCategory, "${q.subject} ${q.topic}", effective.combinedTargetExams.toList())
            }
        }
    }"""
content = re.sub(quest_pattern, quest_repl, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/data/util/PlanValidityEngine.kt", "w") as f:
    f.write(content)
