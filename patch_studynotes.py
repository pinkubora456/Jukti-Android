import re

filepath = 'app/src/main/java/com/example/ui/screens/StudyNotesScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

target = 'val subjects = listOf("All", "Assam History", "Assam Geography", "Assam Culture", "Polity")'
replacement = '''val allSubjectsChapters by viewModel.allSubjectsChapters.collectAsState()
    val subjects = listOf("All") + allSubjectsChapters.map { it.subject }.distinct().ifEmpty { listOf("Assam History", "Assam Geography", "Assam Culture", "Polity") }'''
content = content.replace(target, replacement)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
