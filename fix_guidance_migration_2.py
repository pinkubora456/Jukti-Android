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
                    // We don't delete from Firestore here directly because uploadGuidanceData uses merge, 
                    // but wait, we need to delete the old one. We'll let pyqs sync handle the new ones, 
                    // but we should delete the old one from firestore.
                    try { firebaseRepository.deletePyqFocus(pyq) } catch (e: Exception) {}
                }
            }
            val topics = guidanceDao.getAllFocusTopics().firstOrNull() ?: emptyList()
            topics.forEach { topic ->
                if (topic.subject.equals("Reading Comprehension", ignoreCase = true)) {
                    val normalized = topic.copy(subject = "General English", chapter = "Reading Comprehension")
                    guidanceDao.deleteFocusTopic(topic)
                    guidanceDao.insertFocusTopic(normalized)
                    try { firebaseRepository.deleteFocusTopic(topic) } catch (e: Exception) {}
                    try { firebaseRepository.saveFocusTopic(normalized) } catch (e: Exception) {}
                }
            }
            val strats = guidanceDao.getAllPrepStrategies().firstOrNull() ?: emptyList()
            strats.forEach { strat ->
                if (strat.subject?.equals("Reading Comprehension", ignoreCase = true) == true) {
                    val normalized = strat.copy(subject = "General English")
                    guidanceDao.deletePrepStrategy(strat)
                    guidanceDao.insertPrepStrategy(normalized)
                    try { firebaseRepository.deletePrepStrategy(strat) } catch (e: Exception) {}
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }"""

content = re.sub(
    r'    suspend fun normalizeGuidanceData\(\) \{.*?\n    \}',
    new_migration,
    content,
    flags=re.DOTALL
)

with open('app/src/main/java/com/example/data/repository/JuktiRepository.kt', 'w') as f:
    f.write(content)
