import re

with open("app/src/main/java/com/example/ui/screens/AllQuestionsScreen.kt", "r") as f:
    content = f.read()

old_filter = """    val filteredQuestions = remember(questions, searchQuery, selectedSubject) {
        questions.filter { q ->
            val normSubj = com.example.data.repository.normalizeSubjectName(q.subject)
            val matchesSubject = selectedSubject == "All Subjects" || 
                 normSubj.equals(selectedSubject, ignoreCase = true) ||
                q.subject.equals(selectedSubject, ignoreCase = true)

            val matchesSearch = searchQuery.isBlank() ||
                    q.questionEn.contains(searchQuery, ignoreCase = true) ||
                    q.questionAs.contains(searchQuery, ignoreCase = true) ||
                    q.subject.contains(searchQuery, ignoreCase = true) ||
                    normSubj.contains(searchQuery, ignoreCase = true) ||
                    q.topic.contains(searchQuery, ignoreCase = true)

            matchesSubject && matchesSearch
        }
    }"""

new_filter = """    val filteredQuestions = remember(questions, searchQuery, selectedSubject, selectedChapter) {
        questions.filter { q ->
            val normSubj = com.example.data.repository.normalizeSubjectName(q.subject)
            val matchesSubject = selectedSubject == "All Subjects" || 
                 normSubj.equals(selectedSubject, ignoreCase = true) ||
                q.subject.equals(selectedSubject, ignoreCase = true)

            val normChapter = com.example.data.repository.normalizeChapterName(q.topic)
            val matchesChapter = selectedChapter == "All Chapters" ||
                 normChapter.equals(selectedChapter, ignoreCase = true) ||
                q.topic.equals(selectedChapter, ignoreCase = true)

            val matchesSearch = searchQuery.isBlank() ||
                    q.questionEn.contains(searchQuery, ignoreCase = true) ||
                    q.questionAs.contains(searchQuery, ignoreCase = true) ||
                    q.subject.contains(searchQuery, ignoreCase = true) ||
                    normSubj.contains(searchQuery, ignoreCase = true) ||
                    q.topic.contains(searchQuery, ignoreCase = true)

            matchesSubject && matchesChapter && matchesSearch
        }
    }"""

if old_filter in content:
    content = content.replace(old_filter, new_filter)
    with open("app/src/main/java/com/example/ui/screens/AllQuestionsScreen.kt", "w") as f:
        f.write(content)
    print("Replaced successfully")
else:
    print("Not found, trying regex...")
    
    # Just replace from val filteredQuestions to Scaffold(
    regex = r'val filteredQuestions = remember\(questions, searchQuery, selectedSubject\).*?matchesSubject && matchesSearch\n        \}\n    \}'
    if re.search(regex, content, re.DOTALL):
        content = re.sub(regex, new_filter.strip(), content, flags=re.DOTALL)
        with open("app/src/main/java/com/example/ui/screens/AllQuestionsScreen.kt", "w") as f:
            f.write(content)
        print("Replaced via regex")
    else:
        print("Regex failed too")
