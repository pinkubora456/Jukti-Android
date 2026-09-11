with open('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', 'r') as f:
    content = f.read()

import re

# Add getChapterStatsByExamMultiSubject
add_method = """    fun getChapterStatsByExamMultiSubject(
        subjects: Set<String>,
        exam: String,
        qType: String = "All Types",
        qTag: String = "All Tags"
    ): Flow<List<com.example.data.local.ChapterStatResult>> {
        return allResolvedQuestions.map { list ->
            val isAllSubjects = subjects.contains("All Subjects")
            val isAllExams = exam == "All Exams"
            val isAllTypes = qType == "All Types"
            val isAllTags = qTag == "All Tags"

            val filtered = list.filter { q ->
                (isAllSubjects || subjects.contains(com.example.data.repository.normalizeSubjectName(q.subject))) &&
                (isAllExams || q.examCategory.contains(exam, ignoreCase = true)) &&
                (isAllTypes || (qType == "Premium" && q.isPremium) || (qType == "Free" && !q.isPremium)) &&
                (isAllTags || q.questionType.equals(qTag, ignoreCase = true))
            }
            
            filtered.groupBy { it.topic }
                .map { (chap, qs) ->
                    com.example.data.local.ChapterStatResult(
                        chapter = chap.ifBlank { "Uncategorized" },
                        total = qs.size,
                        easy = qs.count { it.difficulty.equals("Easy", ignoreCase = true) },
                        medium = qs.count { it.difficulty.equals("Medium", ignoreCase = true) },
                        hard = qs.count { it.difficulty.equals("Hard", ignoreCase = true) }
                    )
                }
        }.flowOn(kotlinx.coroutines.Dispatchers.Default)
    }
"""

if "getChapterStatsByExamMultiSubject" not in content:
    content = content.replace("fun getChapterStatsByExam(", add_method + "\n    fun getChapterStatsByExam(")

with open('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', 'w') as f:
    f.write(content)
