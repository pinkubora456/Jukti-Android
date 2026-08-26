import re

with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "r") as f:
    content = f.read()

props = """
    private val _premiumQuestions = kotlinx.coroutines.flow.MutableStateFlow<List<QuestionEntity>>(emptyList())
    val premiumQuestions: kotlinx.coroutines.flow.StateFlow<List<QuestionEntity>> = _premiumQuestions.asStateFlow()

    private val _premiumMockTests = kotlinx.coroutines.flow.MutableStateFlow<List<MockTestEntity>>(emptyList())
    val premiumMockTests: kotlinx.coroutines.flow.StateFlow<List<MockTestEntity>> = _premiumMockTests.asStateFlow()

    private val _premiumStudyNotes = kotlinx.coroutines.flow.MutableStateFlow<List<StudyNoteEntity>>(emptyList())
    val premiumStudyNotes: kotlinx.coroutines.flow.StateFlow<List<StudyNoteEntity>> = _premiumStudyNotes.asStateFlow()

    private val _premiumExamUpdates = kotlinx.coroutines.flow.MutableStateFlow<List<ExamUpdateEntity>>(emptyList())
    val premiumExamUpdates: kotlinx.coroutines.flow.StateFlow<List<ExamUpdateEntity>> = _premiumExamUpdates.asStateFlow()
"""

# Insert before `init {` or at a safe place
idx = content.find("    val allExams: Flow<List<ExamEntity>>")
if idx != -1:
    content = content[:idx] + props + "\n" + content[idx:]
    with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "w") as f:
        f.write(content)
    print("Added properties")
