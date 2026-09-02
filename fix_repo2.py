import re

def fix():
    with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "r") as f:
        content = f.read()
        
    old = """        val updatedQs = toUpdate.map { 
            normalizeQuestionEntity(it.copy(
                examCategory = targetExam,
                subject = normSubject,
                topic = normTopic,
                updatedAt = System.currentTimeMillis()
            ))
        }"""
        
    new = """        val updatedQs = questionsToUpdate.map { 
            normalizeQuestionEntity(it.copy(
                examCategory = targetExam,
                subject = normSubject,
                topic = normTopic,
                updatedAt = System.currentTimeMillis()
            ))
        }"""
        
    if old in content:
        content = content.replace(old, new)
        with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "w") as f:
            f.write(content)
        print("Fixed Repo toUpdate")
    else:
        print("Old not found in repo")

fix()
