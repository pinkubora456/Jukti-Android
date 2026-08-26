import re

with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "r") as f:
    text = f.read()

# Add _premium states back
premium_states = """
    private val _premiumQuestions = kotlinx.coroutines.flow.MutableStateFlow<List<QuestionEntity>>(emptyList())
    val premiumQuestions: kotlinx.coroutines.flow.StateFlow<List<QuestionEntity>> = _premiumQuestions.asStateFlow()

    private val _premiumMockTests = kotlinx.coroutines.flow.MutableStateFlow<List<MockTestEntity>>(emptyList())
    val premiumMockTests: kotlinx.coroutines.flow.StateFlow<List<MockTestEntity>> = _premiumMockTests.asStateFlow()

    private val _premiumStudyNotes = kotlinx.coroutines.flow.MutableStateFlow<List<StudyNoteEntity>>(emptyList())
    val premiumStudyNotes: kotlinx.coroutines.flow.StateFlow<List<StudyNoteEntity>> = _premiumStudyNotes.asStateFlow()

    suspend fun refreshPremiumContent() {
        try {
            _premiumQuestions.value = firebaseRepository.fetchPremiumQuestions()
            _premiumMockTests.value = firebaseRepository.fetchPremiumMockTests()
            _premiumStudyNotes.value = firebaseRepository.fetchPremiumStudyNotes()
        } catch (e: Exception) {
            clearPremiumCache()
        }
    }

    fun clearPremiumCache() {
        _premiumQuestions.value = emptyList()
        _premiumMockTests.value = emptyList()
        _premiumStudyNotes.value = emptyList()
    }
"""

text = re.sub(r'    fun clearPremiumCache\(\) \{.*?\n    \}', '', text, flags=re.DOTALL)
text = re.sub(r'    private val _premiumQuestions = kotlinx.coroutines.flow.MutableStateFlow.*?emptyList\(\)\)\n    val premiumStudyNotes.*?asStateFlow\(\)', '', text, flags=re.DOTALL)

text = text.replace("    val allPlans: Flow<List<PlanEntity>>", premium_states + "\n    val allPlans: Flow<List<PlanEntity>>")


with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "w") as f:
    f.write(text)

