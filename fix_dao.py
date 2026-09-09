import re

with open('app/src/main/java/com/example/data/local/Daos.kt', 'r') as f:
    content = f.read()

content = content.replace('    abstract suspend fun deleteQuestion(question: QuestionEntity)', '''    abstract suspend fun deleteQuestion(question: QuestionEntity)
    
    @Delete
    abstract suspend fun deleteQuestions(questions: List<QuestionEntity>)''')

with open('app/src/main/java/com/example/data/local/Daos.kt', 'w') as f:
    f.write(content)
