import os
import re

def fix_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # ManageStudyNotesScreen
    content = re.sub(
        r'val subjectsList = allSubjectsChapters\.map \{ it\.subject \}\.distinct\(\)\.ifEmpty<String> \{(.*?)\}',
        r'val rawSubj = allSubjectsChapters.map { it.subject }.distinct()\n    val subjectsList = if (rawSubj.isEmpty()) \1 else rawSubj',
        content,
        flags=re.DOTALL
    )

    # StudyNotesScreen
    content = re.sub(
        r'val subjects = listOf\("All"\) \+ allSubjectsChapters\.map \{ it\.subject \}\.distinct\(\)\.ifEmpty \{ listOf\("Assam History", "Assam Geography", "Assam Culture", "Polity"\) \}',
        r'val rawSubj = allSubjectsChapters.map { it.subject }.distinct()\n    val subjects = listOf("All") + if (rawSubj.isEmpty()) listOf("Assam History", "Assam Geography", "Assam Culture", "Polity") else rawSubj',
        content
    )

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

fix_file('app/src/main/java/com/example/ui/screens/ManageStudyNotesScreen.kt')
fix_file('app/src/main/java/com/example/ui/screens/StudyNotesScreen.kt')
