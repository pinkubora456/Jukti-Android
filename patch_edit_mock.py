import re

filepath = 'app/src/main/java/com/example/ui/screens/EditMockScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

target1 = 'val subjectsList = listOf("All Subjects") + allQuestions.map { it.subject }.distinct()'
replacement1 = '''val allSubjectsChapters by viewModel.allSubjectsChapters.collectAsState()
    val subjectsList = listOf("All Subjects") + allSubjectsChapters.map { it.subject }.distinct()'''
content = content.replace(target1, replacement1)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
