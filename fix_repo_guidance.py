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

if "refreshGuidanceData" not in content:
    idx = content.rfind("}")
    if idx != -1:
        content = content[:idx] + new_func + content[idx:]

with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "w") as f:
    f.write(content)
