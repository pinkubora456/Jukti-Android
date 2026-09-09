import re

with open("app/src/main/java/com/example/data/repository/FirebaseRepository.kt", "r") as f:
    content = f.read()

repo_up = """
    suspend fun uploadGuidanceData(pyqs: List<PyqFocusEntity>, strats: List<PrepStrategyEntity>) {
        val db = firestore ?: return
        
        pyqs.forEach { pyq ->
            val data = hashMapOf(
                "exam" to pyq.exam,
                "subject" to pyq.subject,
                "chapter" to pyq.chapter,
                "pyqCount" to pyq.pyqCount,
                "examsCovered" to pyq.examsCovered,
                "published" to true,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("guidance_pyq_focus").add(data)
        }
        
        strats.forEach { strat ->
            val data = hashMapOf(
                "exam" to strat.exam,
                "subject" to strat.subject,
                "content" to strat.content,
                "published" to true,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("guidance_prep_strategies").add(data)
        }
    }
"""

if "uploadGuidanceData" not in content:
    idx = content.rfind("}")
    content = content[:idx] + repo_up + content[idx:]

with open("app/src/main/java/com/example/data/repository/FirebaseRepository.kt", "w") as f:
    f.write(content)

