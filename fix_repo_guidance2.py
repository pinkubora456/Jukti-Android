import re

with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "r") as f:
    content = f.read()

new_func = """
    suspend fun refreshGuidanceData() {
        try {
            val pyqs = firebaseRepository.fetchPyqFocus()
            if (pyqs.isNotEmpty()) {
                guidanceDao.deleteAllPyqFocus()
                pyqs.forEach { guidanceDao.insertPyqFocus(it) }
            }
            
            val strats = firebaseRepository.fetchPrepStrategies()
            if (strats.isNotEmpty()) {
                guidanceDao.deleteAllPrepStrategies()
                strats.forEach { guidanceDao.insertPrepStrategy(it) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
"""

content = content.replace(new_func.strip(), "") # Just in case

new_func_safe = """
    suspend fun refreshGuidanceData() {
        try {
            val pyqs = firebaseRepository.fetchPyqFocus()
            if (pyqs.isNotEmpty()) {
                guidanceDao.deleteAllPyqFocus()
                pyqs.forEach { guidanceDao.insertPyqFocus(it) }
            } else {
                // Do not delete local if firestore empty to avoid data loss during migration
            }
            
            val strats = firebaseRepository.fetchPrepStrategies()
            if (strats.isNotEmpty()) {
                guidanceDao.deleteAllPrepStrategies()
                strats.forEach { guidanceDao.insertPrepStrategy(it) }
            } else {
                // Keep local
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
"""

idx = content.find("suspend fun refreshGuidanceData()")
if idx != -1:
    end_idx = content.find("suspend fun savePyqFocus", idx)
    content = content[:idx] + new_func_safe + content[end_idx:]

with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "w") as f:
    f.write(content)
