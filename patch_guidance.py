import re

with open('app/src/main/java/com/example/data/util/GuidanceEngine.kt', 'r') as f:
    content = f.read()

replacement = """        // Combine chapters from PYQs and Questions
        val allExamChapters = mutableSetOf<Pair<String, String>>()
        examPyqs.forEach { pyq ->
            if (pyq.subject.isNotBlank() && pyq.chapter.isNotBlank()) {
                allExamChapters.add(Pair(pyq.subject, pyq.chapter))
            }
        }
        allQuestions.forEach { q ->
            if (q.examCategory.contains(exam, ignoreCase = true) || q.examCategory.isEmpty() || exam.contains(q.examCategory, ignoreCase = true)) {
                if (q.subject.isNotBlank() && q.topic.isNotBlank()) {
                    allExamChapters.add(Pair(q.subject, q.topic))
                }
            }
        }
        
        val pyqAvgMap = examPyqs.associate { Pair(it.subject, it.chapter) to (if (it.examsCovered > 0) it.pyqCount.toFloat() / it.examsCovered else 0f) }

        // Build ChapterPerformance
        val chapterPerformances = allExamChapters.map { key ->
            val perf = performanceMap[key]
            val totalAtt = perf?.first ?: 0
            val correctAtt = perf?.second ?: 0
            val acc = if (totalAtt >= minAttemptsRequired) (correctAtt.toFloat() / totalAtt) * 100f else null
            val avg = pyqAvgMap[key] ?: 0f
            
            ChapterPerformance(
                subject = key.first,
                chapter = key.second,
                totalAttempts = totalAtt,
                correctAttempts = correctAtt,
                accuracy = acc,
                pyqAvg = avg
            )
        }"""

pattern = r'\s*// Build ChapterPerformance\s*val chapterPerformances = examPyqs\.map \{ pyq ->\s*val key = Pair\(pyq\.subject, pyq\.chapter\)\s*val perf = performanceMap\[key\]\s*val totalAtt = perf\?\.first \?: 0\s*val correctAtt = perf\?\.second \?: 0\s*val acc = if \(totalAtt >= minAttemptsRequired\) \(correctAtt\.toFloat\(\) / totalAtt\) \* 100f else null\s*val avg = if \(pyq\.examsCovered > 0\) pyq\.pyqCount\.toFloat\(\) / pyq\.examsCovered else 0f\s*ChapterPerformance\(\s*subject = pyq\.subject,\s*chapter = pyq\.chapter,\s*totalAttempts = totalAtt,\s*correctAttempts = correctAtt,\s*accuracy = acc,\s*pyqAvg = avg\s*\)\s*\}'

new_content = re.sub(pattern, replacement, content, flags=re.MULTILINE)

with open('app/src/main/java/com/example/data/util/GuidanceEngine.kt', 'w') as f:
    f.write(new_content)
