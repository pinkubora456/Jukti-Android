import re

path = "app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt"
with open(path, "r") as f:
    content = f.read()

# We need to insert the new block after accessibleQuestions.
# accessibleQuestions ends with `.stateIn( viewModelScope, SharingStarted.Lazily, emptyList() )` (or similar)

new_block = """
    private fun isQuestionSubjectMatch(qSubject: String, subjectKey: String): Boolean {
        val key = subjectKey.trim()
        if (key.equals("All Subject", ignoreCase = true) || key.equals("All Subjects", ignoreCase = true) || key.isBlank()) {
            return true
        }
        val qNorm = com.example.data.repository.normalizeSubjectName(qSubject)
        val keyNorm = com.example.data.repository.normalizeSubjectName(key)
        return qNorm.equals(keyNorm, ignoreCase = true)
    }

    val precomputedBannerData: StateFlow<Map<String, Triple<List<String>, List<QuestionEntity>, Map<String, Int>>>> = combine(
        accessibleQuestions,
        selectedExam,
        allSubjectsChapters
    ) { questions, targetExam, chapters ->
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            val map = mutableMapOf<String, Triple<List<String>, List<QuestionEntity>, Map<String, Int>>>()
            val subjectKeys = listOf(
                "General Knowledge",
                "General English",
                "General Mathematics",
                "Reasoning & Mental Ability",
                "Transport & Motor Vehicle",
                "All Subjects"
            )

            val qIdToNormalizedTopic = questions.associate { q ->
                q.id to com.example.data.repository.normalizeChapterName(q.topic ?: "", q.subject)
            }

            for (subjectKey in subjectKeys) {
                val bannerQuestions = questions.filter { q -> 
                    com.example.data.util.QuestionFilterUtils.isEligible(q, targetExam, subjectKey, emptySet()) 
                }

                val set = mutableSetOf<String>()
                if (subjectKey == "All Subjects" || subjectKey == "All Subject") {
                    chapters.forEach { sc ->
                        val norm = com.example.data.repository.normalizeChapterName(sc.chapter, sc.subject).ifBlank { sc.chapter.trim() }
                        if (norm.isNotBlank()) set.add(norm)
                    }
                    bannerQuestions.forEach { q ->
                        val norm = qIdToNormalizedTopic[q.id]?.ifBlank { q.topic?.trim() ?: "" } ?: (q.topic?.trim() ?: "")
                        if (norm.isNotBlank()) set.add(norm)
                    }
                } else {
                    chapters.filter { isQuestionSubjectMatch(it.subject, subjectKey) }
                        .forEach { sc ->
                            val norm = com.example.data.repository.normalizeChapterName(sc.chapter, sc.subject).ifBlank { sc.chapter.trim() }
                            if (norm.isNotBlank()) set.add(norm)
                        }
                    bannerQuestions.forEach { q ->
                        val norm = qIdToNormalizedTopic[q.id]?.ifBlank { q.topic?.trim() ?: "" } ?: (q.topic?.trim() ?: "")
                        if (norm.isNotBlank()) set.add(norm)
                    }
                }

                val availableChaptersList = set.toList().sortedWith(String.CASE_INSENSITIVE_ORDER)

                val chapterCountsMap = availableChaptersList.associateWith { rawCh ->
                    val selSubj = if (rawCh.contains(": ")) rawCh.substringBefore(": ").trim() else ""
                    val ch = if (rawCh.contains(": ")) rawCh.substringAfter(": ").trim() else rawCh.trim()

                    bannerQuestions.count { q ->
                        val qSubj = q.subject ?: ""
                        val topicStr = q.topic ?: ""
                        val normTopic = qIdToNormalizedTopic[q.id] ?: ""

                        val subjectMatches = if (selSubj.isNotBlank()) {
                            when (selSubj) {
                                "General Knowledge" -> qSubj in listOf("General Knowledge", "Assam History", "Assam Geography", "Assamese Literature & Culture", "Current Affairs")
                                "General English" -> qSubj.equals("General English", ignoreCase = true) || qSubj.equals("English", ignoreCase = true) || qSubj.contains("English", ignoreCase = true)
                                "General Mathematics", "Mathematics" -> qSubj in listOf("General Mathematics", "Mathematics", "Quantitative Aptitude")
                                "Reasoning", "Reasoning & Mental Ability" -> qSubj in listOf("Reasoning", "Logical Reasoning", "Logical Reasoning & Mental Ability", "Mental Ability", "Logical Aptitude", "Reasoning & Mental Ability")
                                "Transport & Motor Vehicle" -> qSubj.equals("Transport & Motor Vehicle", ignoreCase = true) || qSubj.contains("Transport", ignoreCase = true) || qSubj.contains("Motor Vehicle", ignoreCase = true)
                                else -> qSubj.equals(selSubj, ignoreCase = true) || qSubj.contains(selSubj, ignoreCase = true) || selSubj.contains(qSubj, ignoreCase = true)
                            }
                        } else true

                        if (!subjectMatches) false
                        else {
                            val normCh = com.example.data.repository.normalizeChapterName(ch, qSubj).ifBlank { ch }
                            normTopic.equals(normCh, ignoreCase = true) ||
                            normTopic.equals(ch, ignoreCase = true) ||
                            topicStr.equals(ch, ignoreCase = true) ||
                            (topicStr.isNotBlank() && ch.isNotBlank() && (
                                topicStr.contains(ch, ignoreCase = true) ||
                                ch.contains(topicStr, ignoreCase = true) ||
                                normTopic.contains(normCh, ignoreCase = true) ||
                                normCh.contains(normTopic, ignoreCase = true)
                            ))
                        }
                    }
                }
                map[subjectKey] = Triple(availableChaptersList, bannerQuestions, chapterCountsMap)
            }
            map
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyMap()
    )
"""

# Find the end of accessibleQuestions block
# It usually ends with `emptyList()\n    )`
search_pattern = r"emptyList\(\)\n    \)"
match = re.search(search_pattern, content)
if match:
    insert_pos = match.end()
    new_content = content[:insert_pos] + "\n" + new_block + content[insert_pos:]
    with open(path, "w") as f:
        f.write(new_content)
    print("Success")
else:
    print("Pattern not found")

