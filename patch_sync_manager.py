import re
path = "app/src/main/java/com/example/data/repository/FirebaseSyncManager.kt"
with open(path, "r") as f:
    content = f.read()

# Add to questionToMap
pattern_q = r'("firebaseId" to q.firebaseId)'
replacement_q = r'\1,\n        "contentType" to q.contentType,\n        "passageId" to q.passageId,\n        "pyqExams" to q.pyqExams'
if re.search(pattern_q, content):
    content = re.sub(pattern_q, replacement_q, content)

# Add passageToMap
pattern_p = r'(fun questionToMap\(q: QuestionEntity\): Map<String, Any\?> = mapOf\([\s\S]*?\n    \))'
replacement_p = r'\1\n\n    fun passageToMap(p: com.example.data.local.ReadingComprehensionPassageEntity): Map<String, Any?> = mapOf(\n        "passageId" to p.passageId,\n        "passage" to p.passage,\n        "subject" to p.subject,\n        "chapter" to p.chapter,\n        "topic" to p.topic,\n        "difficulty" to p.difficulty,\n        "updatedAt" to p.updatedAt,\n        "firebaseId" to p.firebaseId\n    )'
if re.search(pattern_p, content):
    content = re.sub(pattern_p, replacement_p, content)

# Update executeSingleSync
pattern_exec = r'("QUESTION" -> db\.collection\("questions"\)\.document\(entityId\)\.set\(payloadMap\)\.await\(\))'
replacement_exec = r'\1\n                    "RC_PASSAGE" -> db.collection("rc_passages").document(entityId).set(payloadMap).await()'
if re.search(pattern_exec, content):
    content = re.sub(pattern_exec, replacement_exec, content)
    
pattern_exec_del = r'("QUESTION" -> db\.collection\("questions"\)\.document\(entityId\)\.delete\(\)\.await\(\))'
replacement_exec_del = r'\1\n                    "RC_PASSAGE" -> db.collection("rc_passages").document(entityId).delete().await()'
if re.search(pattern_exec_del, content):
    content = re.sub(pattern_exec_del, replacement_exec_del, content)

with open(path, "w") as f:
    f.write(content)
print("Patched FirebaseSyncManager.kt")
