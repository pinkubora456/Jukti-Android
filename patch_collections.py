import re

def fix_collections(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # CreateMockScreen
    content = re.sub(r'val qChaptersList: List<String> = allSubjectsChapters\.filter \{ it\.subject == qSubject \}\.map \{ it\.chapter \}\.distinct\(\)\.ifEmpty<String> \{ listOf\("General"\) \}', 
                     r'val rawChapters = allSubjectsChapters.filter { it.subject == qSubject }.map { it.chapter }.distinct()\n    val qChaptersList: List<String> = if (rawChapters.isEmpty()) listOf("General") else rawChapters',
                     content)

    # ManageStudyNotesScreen
    content = re.sub(r'val chaptersList: List<String> = allSubjectsChapters\.filter \{ it\.subject == subject \}\.map \{ it\.chapter \}\.distinct\(\)\.ifEmpty<String> \{ listOf\("General"\) \}',
                     r'val rawChapters = allSubjectsChapters.filter { it.subject == subject }.map { it.chapter }.distinct()\n    val chaptersList: List<String> = if (rawChapters.isEmpty()) listOf("General") else rawChapters',
                     content)

    # SingleQuestionUploadScreen subjects
    content = re.sub(r'val subjectsList: List<String> = allSubjectsChapters\.map \{ it\.subject \}\.distinct\(\)\.ifEmpty<String> \{ listOf\("Assam History", "General Knowledge"\) \}',
                     r'val rawSubjects = allSubjectsChapters.map { it.subject }.distinct()\n    val subjectsList: List<String> = if (rawSubjects.isEmpty()) listOf("Assam History", "General Knowledge") else rawSubjects',
                     content)
    
    # Also without List<String> (in case I missed it)
    content = re.sub(r'val subjectsList = allSubjectsChapters\.map \{ it\.subject \}\.distinct\(\)\.ifEmpty<String> \{ listOf\("Assam History", "General Knowledge"\) \}',
                     r'val rawSubjects = allSubjectsChapters.map { it.subject }.distinct()\n    val subjectsList: List<String> = if (rawSubjects.isEmpty()) listOf("Assam History", "General Knowledge") else rawSubjects',
                     content)

    # SingleQuestionUploadScreen chapters
    content = re.sub(r'val chaptersList: List<String> = allSubjectsChapters\.filter \{ it\.subject == subject \}\.map \{ it\.chapter \}\.distinct\(\)\.ifEmpty<String> \{ listOf\("General"\) \}',
                     r'val rawChapters = allSubjectsChapters.filter { it.subject == subject }.map { it.chapter }.distinct()\n    val chaptersList: List<String> = if (rawChapters.isEmpty()) listOf("General") else rawChapters',
                     content)
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

fix_collections('app/src/main/java/com/example/ui/screens/CreateMockScreen.kt')
fix_collections('app/src/main/java/com/example/ui/screens/ManageStudyNotesScreen.kt')
fix_collections('app/src/main/java/com/example/ui/screens/SingleQuestionUploadScreen.kt')
