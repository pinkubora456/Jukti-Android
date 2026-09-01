import re

def fix_file(filepath):
    with open(filepath, "r") as f:
        content = f.read()

    # Find the LaunchedEffect
    pattern = r'(    LaunchedEffect\(isStudySessionStarted[^\n]*\n        if \(isStudySessionStarted\) \{\n            val filtered = questions\.filter \{ q ->\n(?:.*?\n)+?                \}\n            \}\.shuffled\(\)\.toMutableList\(\)\n(?:.*?\n)+?        \} else \{\n            activeStudySessionQuestions = emptyList\(\)\n        \}\n    \}\n\n    val studyQuestionsList = if \(isStudySessionStarted && activeStudySessionQuestions\.isNotEmpty\(\)\) activeStudySessionQuestions else questions\.filter \{ !hiddenIds\.contains\(it\.id\) \})'
    
    match = re.search(pattern, content, re.MULTILINE)
    if not match:
        print("Could not find LaunchedEffect pattern in", filepath)
    else:
        old_block = match.group(1)
        
        # Extract the inside of questions.filter { q -> ... }
        filter_pattern = r'            val filtered = questions\.filter \{ q ->\n((?:.*?\n)+?)            \}\.shuffled\(\)\.toMutableList\(\)'
        filter_match = re.search(filter_pattern, old_block)
        filter_body = filter_match.group(1)
        
        new_block = """    val filteredStudyQuestions = remember(questions, selectedSubjectTab, selectedChapters, hiddenIds) {
        questions.filter { q ->
""" + filter_body + """        }
    }

    LaunchedEffect(isStudySessionStarted, filteredStudyQuestions) {
        if (isStudySessionStarted) {
            val eligible = filteredStudyQuestions.shuffled().toMutableList()

            if (eligible.size > 1 && eligible[0].id == lastStudyStartingQuestionId) {
                val temp = eligible[0]
                eligible[0] = eligible[1]
                eligible[1] = temp
            }
            if (eligible.isNotEmpty()) {
                lastStudyStartingQuestionId = eligible[0].id
            }
            activeStudySessionQuestions = eligible
            currentQuestionIndex = 0
            selectedOptionIndex = null
        } else {
            activeStudySessionQuestions = emptyList()
        }
    }

    val studyQuestionsList = if (isStudySessionStarted) {
        if (activeStudySessionQuestions.isNotEmpty()) activeStudySessionQuestions else filteredStudyQuestions
    } else emptyList()"""

        content = content.replace(old_block, new_block)
        
    # Also fix totalCount
    total_count_pattern = r'                    val totalCount = remember\(questions, banner\.subjectKey\) \{\n(?:.*?\n)+?                    \}\n\n                    val currentSelectedChapters = chaptersMap\[banner\.subjectKey\] \?: emptySet\(\)'
    match = re.search(total_count_pattern, content)
    if not match:
        print("Could not find totalCount pattern in", filepath)
    else:
        old_total = match.group(0)
        # We know filter_body has the filtering logic. But for totalCount, `q.subject` is checked against `banner.subjectKey` and `selectedChapters` is `currentSelectedChapters`.
        # Instead of replacing totalCount logic entirely with filter_body, we can just replace the whole `totalCount` block with one that uses `filter_body` but adapts it.
        # However, it's easier to just use `filteredStudyQuestions.size`? No, totalCount is for BANNERS! Each banner has its own subjectKey!
        # So totalCount must be calculated per banner.
        pass
        
    with open(filepath, "w") as f:
        f.write(content)
    print("Fixed", filepath)

fix_file("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt")
