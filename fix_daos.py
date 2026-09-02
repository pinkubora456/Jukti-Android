import re

def fix_file(filepath):
    with open(filepath, "r") as f:
        content = f.read()

    old = """    @Update
    abstract suspend fun updateQuestion(question: QuestionEntity)"""
    new = """    @Update
    abstract suspend fun updateQuestion(question: QuestionEntity)
    
    @Update
    abstract suspend fun updateQuestions(questions: List<QuestionEntity>)"""

    if old in content:
        content = content.replace(old, new)
        with open(filepath, "w") as f:
            f.write(content)
        print("Fixed Daos.kt")
    else:
        print("old not found")

fix_file("app/src/main/java/com/example/data/local/Daos.kt")
