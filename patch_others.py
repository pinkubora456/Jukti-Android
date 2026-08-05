import re

def fix_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # if allSubjectsChapters is declared late, move it up
    content = content.replace('.distinct().ifEmpty {', '.distinct().ifEmpty<String> {')
    content = content.replace('ifEmpty<String><String>', 'ifEmpty<String>')
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

fix_file('app/src/main/java/com/example/ui/screens/ManageStudyNotesScreen.kt')
fix_file('app/src/main/java/com/example/ui/screens/SingleQuestionUploadScreen.kt')
