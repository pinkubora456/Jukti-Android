import re

with open("app/src/main/java/com/example/data/util/PlanValidityEngine.kt", "r") as f:
    content = f.read()

# Fix filterAccessibleMockTests
filter_mock_regex = r"fun filterAccessibleMockTests\([\s\S]*?fun filterAccessibleMockTests"
new_filter_mock = """fun filterAccessibleMockTests(
        userProfile: UserProfileEntity?,
        entitlements: List<EntitlementEntity>?,
        plans: List<PlanEntity>,
        mockTests: List<MockTestEntity>,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): List<MockTestEntity> {
        return mockTests
    }
    
    fun filterAccessibleMockTests"""
content = re.sub(filter_mock_regex, new_filter_mock, content, count=1)


# Fix filterAccessibleStudyNotes
filter_notes_regex = r"fun filterAccessibleStudyNotes\([\s\S]*?fun filterAccessibleStudyNotes"
new_filter_notes = """fun filterAccessibleStudyNotes(
        userProfile: UserProfileEntity?,
        entitlements: List<EntitlementEntity>?,
        plans: List<PlanEntity>,
        studyNotes: List<StudyNoteEntity>,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): List<StudyNoteEntity> {
        return studyNotes
    }

    fun filterAccessibleStudyNotes"""
content = re.sub(filter_notes_regex, new_filter_notes, content, count=1)


# Fix filterAccessibleQuestions
filter_q_regex = r"fun filterAccessibleQuestions\([\s\S]*?fun filterAccessibleQuestions"
new_filter_q = """fun filterAccessibleQuestions(
        userProfile: UserProfileEntity?,
        entitlements: List<EntitlementEntity>?,
        plans: List<PlanEntity>,
        questions: List<QuestionEntity>,
        isAdminOrOwner: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): List<QuestionEntity> {
        val validQuestions = questions.filter { !it.isReported }
        return validQuestions
    }

    fun filterAccessibleQuestions"""
content = re.sub(filter_q_regex, new_filter_q, content, count=1)

with open("app/src/main/java/com/example/data/util/PlanValidityEngine.kt", "w") as f:
    f.write(content)
