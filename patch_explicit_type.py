import re

filepath = 'app/src/main/java/com/example/ui/screens/ManageStudyNotesScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('val chaptersList =', 'val chaptersList: List<String> =')

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
