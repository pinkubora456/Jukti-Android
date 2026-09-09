import re

with open('app/src/main/java/com/example/data/repository/JuktiRepository.kt', 'r') as f:
    content = f.read()

new_migration = """    suspend fun normalizeGuidanceData() {
        try {
            val pyqs = guidanceDao.getAllPyqFocus().firstOrNull() ?: emptyList()
            pyqs.forEach { pyq ->
                if (pyq.subject.equals("Reading Comprehension", ignoreCase = true)) {
                    val normalized = pyq.copy(subject = "General English", chapter = "Reading Comprehension")
                    guidanceDao.deletePyqFocus(pyq)
                    guidanceDao.insertPyqFocus(normalized)
                }
            }
            val topics = guidanceDao.getAllFocusTopics().firstOrNull() ?: emptyList()
            topics.forEach { topic ->
                if (topic.subject.equals("Reading Comprehension", ignoreCase = true)) {
                    val normalized = topic.copy(subject = "General English", chapter = "Reading Comprehension")
                    guidanceDao.deleteFocusTopic(topic)
                    guidanceDao.insertFocusTopic(normalized)
                }
            }
            val strats = guidanceDao.getAllPrepStrategies().firstOrNull() ?: emptyList()
            strats.forEach { strat ->
                if (strat.subject?.equals("Reading Comprehension", ignoreCase = true) == true) {
                    val normalized = strat.copy(subject = "General English")
                    guidanceDao.deletePrepStrategy(strat)
                    guidanceDao.insertPrepStrategy(normalized)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun migrateGuidanceToFirestore() {
        normalizeGuidanceData()
        val pyqs = guidanceDao.getAllPyqFocus().firstOrNull() ?: emptyList()
        val strats = guidanceDao.getAllPrepStrategies().firstOrNull() ?: emptyList()
        firebaseRepository.uploadGuidanceData(pyqs, strats)
    }"""

content = re.sub(
    r'    suspend fun migrateGuidanceToFirestore\(\) \{.*?\n    \}',
    new_migration,
    content,
    flags=re.DOTALL
)

with open('app/src/main/java/com/example/data/repository/JuktiRepository.kt', 'w') as f:
    f.write(content)
