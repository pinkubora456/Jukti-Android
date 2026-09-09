import re

with open("app/src/main/java/com/example/data/repository/FirebaseRepository.kt", "r") as f:
    content = f.read()

new_functions = """
    suspend fun fetchPyqFocus(): List<PyqFocusEntity> = suspendCancellableCoroutine { continuation ->
        val db = firestore ?: run {
            continuation.resume(emptyList())
            return@suspendCancellableCoroutine
        }
        
        db.collection("guidance_pyq_focus")
            .whereEqualTo("published", true)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { doc ->
                    try {
                        PyqFocusEntity(
                            id = 0L,
                            exam = doc.getString("exam") ?: "",
                            subject = doc.getString("subject") ?: "",
                            chapter = doc.getString("chapter") ?: "",
                            pyqCount = doc.getLong("pyqCount")?.toInt() ?: 0,
                            examsCovered = doc.getLong("examsCovered")?.toInt() ?: 0
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                continuation.resume(list)
            }
            .addOnFailureListener {
                continuation.resume(emptyList())
            }
    }

    suspend fun fetchPrepStrategies(): List<PrepStrategyEntity> = suspendCancellableCoroutine { continuation ->
        val db = firestore ?: run {
            continuation.resume(emptyList())
            return@suspendCancellableCoroutine
        }
        
        db.collection("guidance_prep_strategies")
            .whereEqualTo("published", true)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { doc ->
                    try {
                        PrepStrategyEntity(
                            id = 0L,
                            exam = doc.getString("exam") ?: "",
                            content = doc.getString("content") ?: ""
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                continuation.resume(list)
            }
            .addOnFailureListener {
                continuation.resume(emptyList())
            }
    }
"""

if "fetchPyqFocus" not in content:
    idx = content.rfind("}")
    if idx != -1:
        content = content[:idx] + new_functions + content[idx:]

with open("app/src/main/java/com/example/data/repository/FirebaseRepository.kt", "w") as f:
    f.write(content)
