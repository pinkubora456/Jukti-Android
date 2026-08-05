import re

filepath = 'app/src/main/java/com/example/ui/screens/CreateMockScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

# Remove the one at line 100ish
content = content.replace("val allSubjectsChapters by viewModel.allSubjectsChapters.collectAsState()\n", "")

# Add it at the top of the function
target = 'val exams by viewModel.examsList.collectAsState()'
replacement = '''val exams by viewModel.examsList.collectAsState()
    val allSubjectsChapters by viewModel.allSubjectsChapters.collectAsState()'''
content = content.replace(target, replacement)

# Fix ifEmpty
content = content.replace('.distinct().ifEmpty { listOf("General") }', '.distinct().ifEmpty { listOf("General") }')
content = content.replace('.distinct().ifEmpty', '.distinct().ifEmpty<String>')
content = content.replace('ifEmpty<String><String>', 'ifEmpty<String>') # in case of double replace

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
