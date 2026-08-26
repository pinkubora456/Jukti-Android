import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    text = f.read()

# Merge back premium content into the master flows
text = text.replace(
    'val questions: StateFlow<List<QuestionEntity>> = combine(repository.allQuestions,',
    'val questions: StateFlow<List<QuestionEntity>> = combine(kotlinx.coroutines.flow.combine(repository.allQuestions, repository.premiumQuestions) { f, p -> f + p },'
)

text = text.replace(
    'val mockTests: StateFlow<List<MockTestEntity>> = combine(\n        repository.allMockTests,',
    'val mockTests: StateFlow<List<MockTestEntity>> = combine(\n        kotlinx.coroutines.flow.combine(repository.allMockTests, repository.premiumMockTests) { f, p -> f + p },'
)

text = text.replace(
    'val studyNotes: StateFlow<List<StudyNoteEntity>> = combine(repository.allNotes,',
    'val studyNotes: StateFlow<List<StudyNoteEntity>> = combine(kotlinx.coroutines.flow.combine(repository.allNotes, repository.premiumStudyNotes) { f, p -> f + p },'
)

# And add the refreshPremiumContent trigger when entitlement changes and is connected
refresh_logic = """
    init {
        viewModelScope.launch {
            networkMonitor.isConnected.collect { online ->
                if (!online) {
                    repository.clearPremiumCache()
                    
                    if (_activeMockQuestions.value.any { it.isPremium }) {
                        _activeMockQuestions.value = emptyList()
                        _currentMockAttempt.value = null
                        _sessionMessage.value = "Premium Content is unavailable offline."
                        if (_currentScreen.value == Screen.MOCK_PLAYER || _currentScreen.value == Screen.MOCK_RESULT) {
                            navigateTo(Screen.HOME)
                        }
                    }
                }
            }
        }
        
        viewModelScope.launch {
            combine(effectiveEntitlement, networkMonitor.isConnected) { eff, online -> Pair(eff, online) }
                .collect { (eff, online) ->
                    if (online && eff != null && eff.isValid) {
                        repository.refreshPremiumContent()
                    } else {
                        repository.clearPremiumCache()
                    }
                }
        }
"""

text = re.sub(r'    init \{\n        viewModelScope\.launch \{\n            networkMonitor\.isConnected\.collect \{ online ->.*?\}\n                \}\n            \}\n        \}', refresh_logic, text, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(text)

