import re

def fix():
    with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "r") as f:
        content = f.read()
        
    old = """    suspend fun bulkMoveQuestions(
        questionsToUpdate: List<QuestionEntity>,
        targetExam: String,
        targetSubject: String,
        targetChapter: String
    ): Pair<Boolean, String> {"""
        
    new = """    suspend fun bulkMoveQuestions(
        questionsToUpdate: List<com.example.data.local.QuestionEntity>,
        targetExam: String,
        targetSubject: String,
        targetChapter: String
    ): Pair<Boolean, String> {"""
        
    if old in content:
        content = content.replace(old, new)
        with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "w") as f:
            f.write(content)
        print("Fixed Repo import")
    else:
        print("Old not found in repo")

fix()
