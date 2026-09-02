import re

with open("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "r") as f:
    content = f.read()

start_marker = "val filteredStudyQuestions = remember(questions, selectedSubjectTab, selectedChapters, hiddenIds) {"
end_marker = "// Learning Summary Dialog"

start_idx = content.find(start_marker)
end_idx = content.find(end_marker)

if start_idx == -1 or end_idx == -1:
    print("Markers not found")
    exit(1)

old_block = content[start_idx:end_idx]

# We need to construct the new block
new_block = """val filteredStudyQuestions = remember(questions, selectedSubjectTab, selectedChapters, hiddenIds) {
        questions.filter { q ->
            if (q.id in hiddenIds) return@filter false
            val matchSubject = when (selectedSubjectTab) {
                "All Subject", "All Subjects" -> true
                "General Knowledge" -> q.subject in listOf("General Knowledge", "Assam History", "Assam Geography", "Assamese Literature & Culture", "Current Affairs")
                "General English" -> q.subject.equals("General English", ignoreCase = true) || q.subject.equals("English", ignoreCase = true) || q.subject.contains("English", ignoreCase = true)
                "General Mathematics", "Mathematics" -> q.subject in listOf("General Mathematics", "Mathematics", "Quantitative Aptitude")
                "Reasoning" -> q.subject in listOf("Reasoning", "Logical Reasoning", "Logical Reasoning & Mental Ability", "Mental Ability", "Logical Aptitude", "Reasoning & Mental Ability")
                "Basic Computer", "Computer Knowledge", "Computer" -> q.subject in listOf("Basic Computer", "Computer Knowledge", "Computer", "Computer Awareness", "Computer Science", "Information Technology", "IT") || q.subject.contains("Computer", ignoreCase = true) || q.topic.contains("Computer", ignoreCase = true) || q.topic.contains("MS Office", ignoreCase = true) || q.topic.contains("Operating System", ignoreCase = true) || q.topic.contains("Internet", ignoreCase = true) || q.topic.contains("Hardware", ignoreCase = true)
                "Transport Rule", "Transport Rules" -> q.subject.equals("Transport Rule", ignoreCase = true) || q.subject.equals("Transport Rules", ignoreCase = true) || q.subject.equals("Manual Entry", ignoreCase = true) || q.subject.contains("Manual", ignoreCase = true) || q.topic.contains("Transport Rule", ignoreCase = true) || q.topic.contains("Traffic Sign", ignoreCase = true) || q.topic.contains("Motor Vehicle", ignoreCase = true) || q.topic.contains("Driving Regulation", ignoreCase = true) || q.topic.contains("Vehicle Safety", ignoreCase = true)
                else -> q.subject.equals(selectedSubjectTab, ignoreCase = true)
            }
            val matchChapter = if (selectedChapters.isEmpty()) {
                true
            } else {
                val topicStr = q.topic ?: ""
                val normTopic = com.example.data.repository.normalizeChapterName(topicStr, q.subject)
                selectedChapters.any { ch ->
                    val nCh = com.example.data.repository.normalizeChapterName(ch, q.subject)
                    normTopic.equals(nCh, ignoreCase = true) || 
                    topicStr.equals(ch, ignoreCase = true) || 
                    topicStr.contains(ch, ignoreCase = true) || 
                    ch.contains(topicStr, ignoreCase = true) || 
                    normTopic.contains(ch, ignoreCase = true) || 
                    ch.contains(normTopic, ignoreCase = true)
                }
            }
            matchSubject && matchChapter
        }
    }

    val studyBanners = listOf(
        StudyBannerConfig("General Knowledge", "সাধাৰণ জ্ঞান", "Assam history, geography, and more", "অসমৰ ইতিহাস, ভূগোল আৰু অন্যান্য", "General Knowledge", androidx.compose.material.icons.Icons.Default.Public, androidx.compose.ui.graphics.Color(0xFFE8F5E9), androidx.compose.ui.graphics.Color(0xFF2E7D32)),
        StudyBannerConfig("General English", "সাধাৰণ ইংৰাজী", "Grammar, vocabulary, and comprehension", "ব্যাকৰণ, শব্দভাণ্ডাৰ আৰু বুজাপৰা", "General English", androidx.compose.material.icons.Icons.Default.MenuBook, androidx.compose.ui.graphics.Color(0xFFE3F2FD), androidx.compose.ui.graphics.Color(0xFF1565C0)),
        StudyBannerConfig("General Mathematics", "সাধাৰণ গণিত", "Arithmetic, algebra, and geometry", "পাটিগণিত, বীজগণিত আৰু জ্যামিতি", "General Mathematics", androidx.compose.material.icons.Icons.Default.Calculate, androidx.compose.ui.graphics.Color(0xFFFFF3E0), androidx.compose.ui.graphics.Color(0xFFEF6C00)),
        StudyBannerConfig("Reasoning", "যুক্তি", "Logical and analytical reasoning", "যৌক্তিক আৰু বিশ্লেষণাত্মক যুক্তি", "Reasoning", androidx.compose.material.icons.Icons.Default.Psychology, androidx.compose.ui.graphics.Color(0xFFF3E5F5), androidx.compose.ui.graphics.Color(0xFF6A1B9A)),
        StudyBannerConfig("Basic Computer", "কম্পিউটাৰৰ সাধাৰণ জ্ঞান", "Computer fundamentals and internet", "কম্পিউটাৰৰ মূল কথা আৰু ইণ্টাৰনেট", "Basic Computer", androidx.compose.material.icons.Icons.Default.Computer, androidx.compose.ui.graphics.Color(0xFFE0F7FA), androidx.compose.ui.graphics.Color(0xFF00838F)),
        StudyBannerConfig("Transport Rule", "পৰিবহন নিয়ম", "Motor vehicle act and traffic signs", "মটৰ বাহন আইন আৰু যান-বাহনৰ সংকেত", "Transport Rule", androidx.compose.material.icons.Icons.Default.Traffic, androidx.compose.ui.graphics.Color(0xFFFBE9E7), androidx.compose.ui.graphics.Color(0xFFD84315)),
        StudyBannerConfig("All Subjects", "সকলো বিষয়", "Mixed questions from all subjects", "সকলো বিষয়ৰ পৰা মিশ্ৰিত প্ৰশ্ন", "All Subjects", androidx.compose.material.icons.Icons.Default.AllInclusive, androidx.compose.ui.graphics.Color(0xFFF5F5F5), androidx.compose.ui.graphics.Color(0xFF424242))
    )

    if (!isStudySessionStarted) {
        androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            items(studyBanners) { banner ->
                val totalCount = questions.count { q ->
                    if (banner.subjectKey == "All Subjects") true
                    else {
                        val match = when (banner.subjectKey) {
                            "General Knowledge" -> q.subject in listOf("General Knowledge", "Assam History", "Assam Geography", "Assamese Literature & Culture", "Current Affairs")
                            "General English" -> q.subject.contains("English", ignoreCase = true)
                            "General Mathematics" -> q.subject in listOf("General Mathematics", "Mathematics", "Quantitative Aptitude")
                            "Reasoning" -> q.subject in listOf("Reasoning", "Logical Reasoning", "Logical Reasoning & Mental Ability", "Mental Ability", "Logical Aptitude", "Reasoning & Mental Ability")
                            "Basic Computer" -> q.subject.contains("Computer", ignoreCase = true) || q.topic.contains("Computer", ignoreCase = true) || q.topic.contains("MS Office", ignoreCase = true) || q.topic.contains("Operating System", ignoreCase = true) || q.topic.contains("Internet", ignoreCase = true) || q.topic.contains("Hardware", ignoreCase = true)
                            "Transport Rule" -> q.subject.contains("Transport", ignoreCase = true) || q.subject.contains("Manual", ignoreCase = true) || q.topic.contains("Transport Rule", ignoreCase = true) || q.topic.contains("Traffic Sign", ignoreCase = true) || q.topic.contains("Motor Vehicle", ignoreCase = true) || q.topic.contains("Driving Regulation", ignoreCase = true) || q.topic.contains("Vehicle Safety", ignoreCase = true)
                            else -> q.subject.equals(banner.subjectKey, ignoreCase = true)
                        }
                        match && !hiddenIds.contains(q.id)
                    }
                }
                
                val availableChapters = allSubjectsChapters[banner.subjectKey] ?: emptyList()
                val currentSelectedChapters = chaptersMap[banner.subjectKey] ?: emptySet()
                
                StudySubjectBannerCard(
                    banner = banner,
                    availableChapters = availableChapters,
                    selectedChapters = currentSelectedChapters,
                    onChaptersChanged = { newSet ->
                        chaptersMap = chaptersMap + (banner.subjectKey to newSet)
                    },
                    totalQuestionsCount = totalCount,
                    actionButtonTextEn = "Start Study",
                    actionButtonTextAs = "অধ্যয়ন আৰম্ভ কৰক",
                    onStartClick = {
                        selectedSubjectTab = banner.subjectKey
                        selectedChapters = chaptersMap[banner.subjectKey] ?: emptySet()
                        currentQuestionIndex = 0
                        selectedOptionIndex = null
                        isStudySessionStarted = true
                    },
                    isAssamese = isAssamese
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    } else {
"""

# Now we need to append the active session code
# I will find the active session code from the original block.
# The active session starts with `val currentQuestion = filteredStudyQuestions.getOrNull(currentQuestionIndex)`
active_session_start = old_block.find("val currentQuestion = filteredStudyQuestions.getOrNull(currentQuestionIndex)")
if active_session_start == -1:
    print("Could not find active session start!")
    exit(1)

active_session_code = old_block[active_session_start:]

# But wait, active_session_code might have too many closing braces at the end.
# Let's clean up the trailing braces of active_session_code.
# We want active_session_code to end exactly where `// Learning Summary Dialog` begins, minus any extra braces that were closing functions we didn't open.
# Wait, let's just append active_session_code and see if it compiles.
# We are replacing from `val filteredStudyQuestions` to `// Learning Summary Dialog`.
# Since we opened `if (!isStudySessionStarted) { ... } else {` we MUST ensure there is ONE closing brace for the `else {` block at the end of active_session_code!
# The easiest way is to let active_session_code remain, but make sure it has exactly one `}` at its end, just before `// Learning Summary Dialog`.

# Wait! The original code had a closing `}` for the `else` block at the very end of active_session_code!
# Let's count the braces in our new active_session_code.
# Actually, let's just write active_session_code into the string.
new_content = content[:start_idx] + new_block + active_session_code + content[end_idx:]

with open("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "w") as f:
    f.write(new_content)

print("Replaced!")
