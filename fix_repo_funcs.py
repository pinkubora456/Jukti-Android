import re

with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "r") as f:
    content = f.read()

# Make sure migrateGuidanceToFirestore is inside the class JuktiRepository.
funcs = """
    suspend fun migrateGuidanceToFirestore() {
        val pyqs = guidanceDao.getAllPyqFocus().firstOrNull() ?: emptyList()
        val strats = guidanceDao.getAllPrepStrategies().firstOrNull() ?: emptyList()
        firebaseRepository.uploadGuidanceData(pyqs, strats)
    }

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

# Let's remove the stray implementations:
def remove_stray(text, func_name):
    pattern = r'suspend fun ' + func_name + r'\(\).*?\{.*?\n    \}'
    return re.sub(pattern, '', text, flags=re.DOTALL)

content = remove_stray(content, "migrateGuidanceToFirestore")
content = remove_stray(content, "refreshGuidanceData")

# Insert before closing brace of JuktiRepository
# To find it reliably, let's search for "companion object {"
idx = content.rfind("companion object {")
if idx != -1:
    content = content[:idx] + funcs + "\n    " + content[idx:]
else:
    # insert before last }
    idx2 = content.rfind("}")
    if idx2 != -1:
        content = content[:idx2] + funcs + "\n}"

with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "w") as f:
    f.write(content)

