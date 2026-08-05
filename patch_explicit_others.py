import re

def fix_explicit(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    content = content.replace('val qChaptersList =', 'val qChaptersList: List<String> =')
    content = content.replace('val chaptersList =', 'val chaptersList: List<String> =')
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

fix_explicit('app/src/main/java/com/example/ui/screens/CreateMockScreen.kt')
fix_explicit('app/src/main/java/com/example/ui/screens/SingleQuestionUploadScreen.kt')
