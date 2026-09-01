with open("app/src/main/java/com/example/ui/screens/AllQuestionsScreen.kt", "r") as f:
    content = f.read()
content = content.replace(
    "val normChapter = com.example.data.repository.normalizeChapterName(q.topic)",
    "val normChapter = com.example.data.repository.normalizeChapterName(q.topic, q.subject)"
)
with open("app/src/main/java/com/example/ui/screens/AllQuestionsScreen.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/data/repository/FirebaseRepository.kt", "r") as f:
    content = f.read()
content = content.replace(
    """                        topic = normalizeChapterName(rawTopic),
                        difficulty = doc.getString("difficulty") ?: "Medium",""",
    """                        topic = normalizeChapterName(rawTopic, normalizeSubjectName(doc.getString("subject"))),
                        difficulty = doc.getString("difficulty") ?: "Medium","""
)
with open("app/src/main/java/com/example/data/repository/FirebaseRepository.kt", "w") as f:
    f.write(content)
