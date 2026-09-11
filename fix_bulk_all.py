import re

# 1. BulkEditQuestionsDialog.kt
with open('app/src/main/java/com/example/ui/components/BulkEditQuestionsDialog.kt', 'r') as f:
    content = f.read()

content = content.replace("onConfirm: (String?, String?, String?, String?, String?, String?) -> Unit", "onConfirm: (String?, String?, String?, String?, String?) -> Unit")
content = re.sub(r'onConfirm\(\n\s*if \(exam == "Don\'t Change"\) null else exam,\n\s*if \(access == "Don\'t Change"\) null else access,\n\s*if \(questionType == "Don\'t Change"\) null else questionType,\n\s*null,\n\s*if \(difficulty == "Don\'t Change"\) null else difficulty\n\s*\)', 'onConfirm(\n                        if (exam == "Don\'t Change") null else exam,\n                        if (access == "Don\'t Change") null else access,\n                        if (questionType == "Don\'t Change") null else questionType,\n                        null,\n                        if (difficulty == "Don\'t Change") null else difficulty\n                    )', content)

with open('app/src/main/java/com/example/ui/components/BulkEditQuestionsDialog.kt', 'w') as f:
    f.write(content)

# 2. AllQuestionsScreen.kt
with open('app/src/main/java/com/example/ui/screens/AllQuestionsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("onConfirm = { exam, access, questionType, pyqExamName, tags, difficulty ->", "onConfirm = { exam, access, questionType, tags, difficulty ->")
content = content.replace("targetPyqExamName = pyqExamName,", "")

with open('app/src/main/java/com/example/ui/screens/AllQuestionsScreen.kt', 'w') as f:
    f.write(content)

# 3. JuktiViewModel.kt
with open('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace("targetPyqExamName: String?,", "")
content = content.replace("questionsToUpdate, targetExam, targetAccess, targetQuestionType, targetPyqExamName, targetTags, targetDifficulty", "questionsToUpdate, targetExam, targetAccess, targetQuestionType, targetTags, targetDifficulty")

with open('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', 'w') as f:
    f.write(content)

# 4. JuktiRepository.kt
with open('app/src/main/java/com/example/data/repository/JuktiRepository.kt', 'r') as f:
    content = f.read()

content = content.replace("targetPyqExamName: String?,", "")

# Fix logic inside JuktiRepository.kt if needed
def repl_repo(m):
    return """            if (targetQuestionType != null) {
                updatedQuestionType = targetQuestionType
            }
"""
content = re.sub(r'if \(targetQuestionType != null\) \{[\s\S]*?\} else if \(targetPyqExamName != null\) \{[\s\S]*?\}', repl_repo, content)
content = re.sub(r'if \(targetQuestionType != null\) \{[\s\S]*?\}', repl_repo, content)

with open('app/src/main/java/com/example/data/repository/JuktiRepository.kt', 'w') as f:
    f.write(content)

