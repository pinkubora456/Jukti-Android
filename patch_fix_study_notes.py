import re

filepath = 'app/src/main/java/com/example/ui/screens/ManageStudyNotesScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

# Fix subjectsList definition
bad_block = """    val rawSubj = allSubjectsChapters.map { it.subject }.distinct()
    val subjectsList = if (rawSubj.isEmpty())  
        listOf("Assam History", "Assam Geography", "Assam Culture", "Polity", "General Studies", "Quantitative Aptitude", "Logical Reasoning", "English")
     else rawSubj"""
good_block = """    val rawSubj = allSubjectsChapters.map { it.subject }.distinct()
    val subjectsList: List<String> = if (rawSubj.isEmpty()) listOf("Assam History", "Assam Geography", "Assam Culture", "Polity", "General Studies", "Quantitative Aptitude", "Logical Reasoning", "English") else rawSubj"""
content = content.replace(bad_block, good_block)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
