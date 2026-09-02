import re

def fix_file(filepath):
    with open(filepath, "r") as f:
        content = f.read()

    # updateQuestion
    old_update = 'return syncManager.enqueueAndSync("QUESTION", norm.id.toString(), "UPDATE", syncManager.questionToMap(norm))'
    new_update = 'val fbId = norm.firebaseId.ifEmpty { norm.id.toString() }\n        return syncManager.enqueueAndSync("QUESTION", fbId, "UPDATE", syncManager.questionToMap(norm))'
    content = content.replace(old_update, new_update)
    
    # deleteQuestion
    old_delete = 'return syncManager.enqueueAndSync("QUESTION", question.id.toString(), "DELETE")'
    new_delete = 'val fbId = question.firebaseId.ifEmpty { question.id.toString() }\n        return syncManager.enqueueAndSync("QUESTION", fbId, "DELETE")'
    content = content.replace(old_delete, new_delete)

    with open(filepath, "w") as f:
        f.write(content)
    print("Fixed repository")

fix_file("app/src/main/java/com/example/data/repository/JuktiRepository.kt")
