with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "r") as f:
    repo = f.read()

repo = repo.replace(
    "    val premiumStudyNotes: kotlinx.coroutines.flow.StateFlow<List<StudyNoteEntity>> = _premiumStudyNotes.asStateFlow()",
    "    val premiumStudyNotes: kotlinx.coroutines.flow.StateFlow<List<StudyNoteEntity>> = _premiumStudyNotes.asStateFlow()\n\n    fun clearPremiumCache() {\n        _premiumQuestions.value = emptyList()\n        _premiumMockTests.value = emptyList()\n        _premiumStudyNotes.value = emptyList()\n    }"
)

with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "w") as f:
    f.write(repo)
