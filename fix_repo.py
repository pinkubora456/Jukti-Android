import re

with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "r") as f:
    content = f.read()

# 1. Add premium StateFlows
flow_target = "    val allExams: Flow<List<ExamEntity>> = examDao.getAllExams()"
if flow_target in content:
    content = content.replace(flow_target, """    val allExams: Flow<List<ExamEntity>> = examDao.getAllExams()

    private val _premiumQuestions = kotlinx.coroutines.flow.MutableStateFlow<List<QuestionEntity>>(emptyList())
    val premiumQuestions: kotlinx.coroutines.flow.StateFlow<List<QuestionEntity>> = _premiumQuestions.asStateFlow()

    private val _premiumMockTests = kotlinx.coroutines.flow.MutableStateFlow<List<MockTestEntity>>(emptyList())
    val premiumMockTests: kotlinx.coroutines.flow.StateFlow<List<MockTestEntity>> = _premiumMockTests.asStateFlow()

    private val _premiumStudyNotes = kotlinx.coroutines.flow.MutableStateFlow<List<StudyNoteEntity>>(emptyList())
    val premiumStudyNotes: kotlinx.coroutines.flow.StateFlow<List<StudyNoteEntity>> = _premiumStudyNotes.asStateFlow()

    private val _premiumExamUpdates = kotlinx.coroutines.flow.MutableStateFlow<List<ExamUpdateEntity>>(emptyList())
    val premiumExamUpdates: kotlinx.coroutines.flow.StateFlow<List<ExamUpdateEntity>> = _premiumExamUpdates.asStateFlow()
""")

# 2. Rewrite refreshDataFromFirebase
# Find start of fetchAllQuestions
start_idx = content.find("            val questions = firebaseRepository.fetchAllQuestions()")
# Find end of exam updates
end_idx = content.find("            if (updates.isNotEmpty()) {\n                examUpdateDao.insertAll(updates)\n            }")
if start_idx != -1 and end_idx != -1:
    end_idx += len("            if (updates.isNotEmpty()) {\n                examUpdateDao.insertAll(updates)\n            }")
    
    new_block = """            val questions = firebaseRepository.fetchAllQuestions()
            questionDao.deletePremiumQuestions()
            val freeQuestions = questions.filter { !it.isPremium }
            val premiumQs = questions.filter { it.isPremium }
            if (freeQuestions.isNotEmpty()) {
                questionDao.insertAll(freeQuestions)
            }
            _premiumQuestions.value = premiumQs

            val mocks = firebaseRepository.fetchAllMockTests()
            mockTestDao.deletePremiumMockTests()
            val freeMocks = mocks.filter { !it.isPremium }
            val premiumMs = mocks.filter { it.isPremium }
            if (freeMocks.isNotEmpty()) {
                mockTestDao.insertAll(freeMocks)
            }
            _premiumMockTests.value = premiumMs

            val notes = firebaseRepository.fetchAllStudyNotes()
            studyNoteDao.deletePremiumStudyNotes()
            val freeNotes = notes.filter { !it.isPremium }
            val premiumNs = notes.filter { it.isPremium }
            if (freeNotes.isNotEmpty()) {
                studyNoteDao.insertAll(freeNotes)
            }
            _premiumStudyNotes.value = premiumNs

            val updates = firebaseRepository.fetchAllExamUpdates()
            examUpdateDao.deletePremiumExamUpdates()
            val freeUpdates = updates.filter { !it.isPremium }
            val premiumUs = updates.filter { it.isPremium }
            if (freeUpdates.isNotEmpty()) {
                examUpdateDao.insertAll(freeUpdates)
            }
            _premiumExamUpdates.value = premiumUs"""
            
    content = content[:start_idx] + new_block + content[end_idx:]

with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "w") as f:
    f.write(content)
print("Updated Repository")
