import re

filepath = 'app/src/main/java/com/example/ui/screens/ManageStudyNotesScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

target = 'val subjectsList = listOf("Assam History", "Assam Geography", "Assam Culture", "Polity", "General Studies", "Quantitative Aptitude", "Logical Reasoning", "English")'
replacement = '''val allSubjectsChapters by viewModel.allSubjectsChapters.collectAsState()
    val subjectsList = allSubjectsChapters.map { it.subject }.distinct().ifEmpty { 
        listOf("Assam History", "Assam Geography", "Assam Culture", "Polity", "General Studies", "Quantitative Aptitude", "Logical Reasoning", "English")
    }'''
content = content.replace(target, replacement)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
