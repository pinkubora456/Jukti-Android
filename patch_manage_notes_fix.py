import re

filepath = 'app/src/main/java/com/example/ui/screens/ManageStudyNotesScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('val allSubjectsChapters by viewModel.allSubjectsChapters.collectAsState()\n', '')
target = 'val exams by viewModel.examsList.collectAsState()'
replacement = '''val exams by viewModel.examsList.collectAsState()
    val allSubjectsChapters by viewModel.allSubjectsChapters.collectAsState()'''
content = content.replace(target, replacement)

# and we have an issue:
# e: file:///app/applet/app/src/main/java/com/example/ui/screens/ManageStudyNotesScreen.kt:315:46 Cannot infer type for this parameter. Specify it explicitly.
# Wait, let's see what is at line 315 in ManageStudyNotesScreen.kt

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
