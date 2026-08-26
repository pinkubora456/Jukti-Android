import re

with open("app/src/main/java/com/example/data/local/Daos.kt", "r") as f:
    daos = f.read()

daos = daos.replace(
    "@Query(\"SELECT * FROM mock_tests ORDER BY id DESC\")\n    fun getAllMockTests()",
    "@Query(\"DELETE FROM mock_tests WHERE isPremium = 1\")\n    suspend fun deletePremiumMockTests()\n\n    @Query(\"SELECT * FROM mock_tests ORDER BY id DESC\")\n    fun getAllMockTests()"
)

daos = daos.replace(
    "@Query(\"SELECT * FROM study_notes ORDER BY id DESC\")\n    fun getAllStudyNotes()",
    "@Query(\"DELETE FROM study_notes WHERE isPremium = 1\")\n    suspend fun deletePremiumStudyNotes()\n\n    @Query(\"SELECT * FROM study_notes ORDER BY id DESC\")\n    fun getAllStudyNotes()"
)

with open("app/src/main/java/com/example/data/local/Daos.kt", "w") as f:
    f.write(daos)


with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "r") as f:
    repo = f.read()

# Fix asStateFlow
repo = repo.replace(
    "val premiumQuestions: kotlinx.coroutines.flow.StateFlow<List<QuestionEntity>> = _premiumQuestions.asStateFlow()",
    "val premiumQuestions: kotlinx.coroutines.flow.StateFlow<List<QuestionEntity>> = _premiumQuestions.kotlinx_coroutines_flow_asStateFlow()"
).replace("kotlinx_coroutines_flow_asStateFlow", "asStateFlow")

# wait, I can just add import kotlinx.coroutines.flow.asStateFlow
if "import kotlinx.coroutines.flow.asStateFlow" not in repo:
    repo = repo.replace("import kotlinx.coroutines.flow.Flow", "import kotlinx.coroutines.flow.Flow\nimport kotlinx.coroutines.flow.asStateFlow")


# Fix exam updates
repo = repo.replace(
"""            val updates = firebaseRepository.fetchAllExamUpdates()
            examUpdateDao.deletePremiumExamUpdates()
            val freeUpdates = updates.filter { !it.isPremium }
            val premiumUs = updates.filter { it.isPremium }
            if (freeUpdates.isNotEmpty()) {
                examUpdateDao.insertAll(freeUpdates)
            }
            _premiumExamUpdates.value = premiumUs""",
"""            val updates = firebaseRepository.fetchAllExamUpdates()
            if (updates.isNotEmpty()) {
                examUpdateDao.insertAll(updates)
            }"""
)

with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "w") as f:
    f.write(repo)
