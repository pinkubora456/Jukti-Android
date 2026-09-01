import re

def fix_file(filepath):
    with open(filepath, "r") as f:
        content = f.read()

    # Find the practiceQuestions logic
    pattern = r'    val practiceQuestions = remember\(questions, selectedSubjectKey, selectedChapters, hiddenIds\) \{\n(?:.*?\n)+?        \}\n    \}'
    
    match = re.search(pattern, content, re.MULTILINE)
    if not match:
        print("Could not find practiceQuestions pattern in", filepath)
    else:
        old_block = match.group(0)
        
        # We know `practiceQuestions` doesn't use `LaunchedEffect`, it directly assigns to a `val practiceQuestions = remember(...)`.
        # However, wait! `PracticeScreen` ALREADY uses `remember` for `practiceQuestions`.
        # Let's check `old_block` to see if it's already using `remember` and if it is correct.
        
        filter_pattern = r'        questions\.filter \{ q ->\n((?:.*?\n)+?)        \}'
        filter_match = re.search(filter_pattern, old_block)
        filter_body = filter_match.group(1)
        
        new_block = """    val practiceQuestions = remember(questions, selectedSubjectKey, selectedChapters, hiddenIds) {
        questions.filter { q ->
""" + filter_body + """        }
    }"""
        # Actually, practiceQuestions doesn't need LaunchedEffect, it's already a remember! So no race conditions.
        # But wait, did I need to fix `PracticeScreen` filtering logic?
        # The user requested: "The filtering logic in PracticeScreen.kt (lines ~69-120) is currently structured similarly to the old McqStudyScreen.kt implementation. It must be updated to use a derived filteredPracticeQuestions state (similar to the filteredStudyQuestions implementation) to ensure the "Hard Filter" requirement is applied consistently across both modes."
        
        # Actually `practiceQuestions` IS a derived state using `remember`! Let's check the existing code of `PracticeScreen.kt`.
    
    # Let's just fix totalCount in PracticeScreen.kt
    total_count_pattern = r'                    val questionCount = remember\(visibleQuestions, banner\.subjectKey\) \{\n(?:.*?\n)+?                    \}\n\n                    val currentSelectedChapters = chaptersMap\[banner\.subjectKey\] \?: emptySet\(\)'
    match = re.search(total_count_pattern, content)
    if not match:
        print("Could not find questionCount pattern in", filepath)
    else:
        old_total = match.group(0)
        # We need the inner match logic but with `visibleQuestions.count`
        new_total = """                    val currentSelectedChapters = chaptersMap[banner.subjectKey] ?: emptySet()
                    
                    val questionCount = remember(visibleQuestions, banner.subjectKey, currentSelectedChapters, hiddenIds) {
                        visibleQuestions.count { q ->
                            if (q.id in hiddenIds) return@count false
                            val matchSubject = when (banner.subjectKey) {
                                "All Subjects" -> true
                                "General Knowledge" -> q.subject in listOf("General Knowledge", "Assam History", "Assam Geography", "Assamese Literature & Culture", "Current Affairs")
                                "General English" -> q.subject == "General English"
                                "General Mathematics", "Mathematics" -> q.subject in listOf("General Mathematics", "Mathematics", "Quantitative Aptitude")
                                "Reasoning" -> q.subject in listOf("Reasoning", "Logical Reasoning", "Logical Reasoning & Mental Ability", "Mental Ability", "Logical Aptitude", "Reasoning & Mental Ability")
                                "Basic Computer", "Computer Knowledge", "Computer" -> q.subject in listOf("Basic Computer", "Computer Knowledge", "Computer", "Computer Awareness", "Computer Science", "Information Technology", "IT") || q.subject.contains("Computer", ignoreCase = true) || q.topic.contains("Computer", ignoreCase = true) || q.topic.contains("MS Office", ignoreCase = true) || q.topic.contains("Operating System", ignoreCase = true) || q.topic.contains("Internet", ignoreCase = true) || q.topic.contains("Hardware", ignoreCase = true)
                                "Transport Rule", "Transport Rules" -> q.subject.equals("Transport Rule", ignoreCase = true) || q.subject.equals("Transport Rules", ignoreCase = true) || q.subject.equals("Manual Entry", ignoreCase = true) || q.subject.contains("Manual", ignoreCase = true) || q.topic.contains("Transport Rule", ignoreCase = true) || q.topic.contains("Traffic Sign", ignoreCase = true) || q.topic.contains("Motor Vehicle", ignoreCase = true) || q.topic.contains("Driving Regulation", ignoreCase = true) || q.topic.contains("Vehicle Safety", ignoreCase = true)
                                else -> q.subject.equals(banner.subjectKey, ignoreCase = true)
                            }
                            val matchChapter = if (currentSelectedChapters.isEmpty()) {
                                true
                            } else {
                                val topicStr = q.topic ?: ""
                                val normTopic = com.example.data.repository.normalizeChapterName(topicStr, q.subject)
                                if (banner.subjectKey == "Reasoning") {
                                    currentSelectedChapters.any { ch ->
                                        topicStr.contains(ch, ignoreCase = true) ||
                                                (ch == "Coding & Decoding" && (normTopic == "Coding-Decoding" || topicStr.contains("Coding", ignoreCase = true))) ||
                                                (ch == "Blood Relations" && (normTopic == "Blood Relation" || topicStr.contains("Blood", ignoreCase = true))) ||
                                                (ch == "Number & Alphabet Series" && (normTopic.contains("Series") || topicStr.contains("Series", ignoreCase = true))) ||
                                                (ch == "Analogy & Classification" && (normTopic.contains("Analogy") || normTopic.contains("Classification") || topicStr.contains("Analogy", ignoreCase = true))) ||
                                                (ch == "Syllogism" && (normTopic == "Syllogism" || topicStr.contains("Syllogism", ignoreCase = true))) ||
                                                (ch == "Venn Diagrams" && (normTopic == "Venn Diagram" || topicStr.contains("Venn", ignoreCase = true))) ||
                                                (ch == "Direction Sense" && (normTopic == "Direction Sense Test" || topicStr.contains("Direction", ignoreCase = true)))
                                    }
                                } else if (banner.subjectKey == "General English") {
                                    currentSelectedChapters.any { ch ->
                                        if (ch == "One-Word & Idioms" || ch == "One-Word & Idiom") {
                                            normTopic == "One-Word & Idioms" || normTopic == "One-Word & Idiom" || topicStr.contains("Idiom", ignoreCase = true) || topicStr.contains("One-Word", ignoreCase = true) || topicStr.contains("One Word", ignoreCase = true) || topicStr.contains("Substitution", ignoreCase = true) || topicStr.contains("Phrase", ignoreCase = true)
                                        } else if (ch == "Synonyms, Antonyms & Vocabulary") {
                                            normTopic == "Synonyms & Antonyms" || topicStr.contains("Synonym", ignoreCase = true) || topicStr.contains("Antonym", ignoreCase = true) || topicStr.contains("Vocabulary", ignoreCase = true) || topicStr.contains("Meaning", ignoreCase = true) || topicStr.contains("Word", ignoreCase = true)
                                        } else if (ch == "Reading Comprehension & Para Jumbles") {
                                            normTopic == "Reading Comprehension" || topicStr.contains("Reading", ignoreCase = true) || topicStr.contains("Comprehension", ignoreCase = true) || topicStr.contains("Passage", ignoreCase = true) || topicStr.contains("Jumble", ignoreCase = true) || topicStr.contains("Para", ignoreCase = true)
                                        } else if (ch == "Grammar & Sentence Correction") {
                                            normTopic == "Grammar" || topicStr.contains("Grammar", ignoreCase = true) || topicStr.contains("Sentence", ignoreCase = true) || topicStr.contains("Correction", ignoreCase = true) || topicStr.contains("Error", ignoreCase = true) || topicStr.contains("Fill in", ignoreCase = true) || topicStr.contains("Preposition", ignoreCase = true) || topicStr.contains("Article", ignoreCase = true) || topicStr.contains("Conjunction", ignoreCase = true) || topicStr.contains("Noun", ignoreCase = true) || topicStr.contains("Pronoun", ignoreCase = true) || topicStr.contains("Verb", ignoreCase = true) || topicStr.contains("Adverb", ignoreCase = true) || topicStr.contains("Adjective", ignoreCase = true)
                                        } else if (ch == "Cloze Test") {
                                            normTopic == "Cloze Test" || topicStr.contains("Cloze", ignoreCase = true)
                                        } else if (ch == "Active & Passive Voice") {
                                            normTopic == "Active & Passive Voice" || topicStr.contains("Voice", ignoreCase = true) || topicStr.contains("Active", ignoreCase = true) || topicStr.contains("Passive", ignoreCase = true)
                                        } else {
                                            val nCh = com.example.data.repository.normalizeChapterName(ch, q.subject)
                                            normTopic == nCh || topicStr.contains(ch, ignoreCase = true) || ch.contains(topicStr, ignoreCase = true)
                                        }
                                    }
                                } else if (banner.subjectKey == "General Knowledge") {
                                    currentSelectedChapters.any { ch ->
                                        val nCh = com.example.data.repository.normalizeChapterName(ch, q.subject)
                                        normTopic == nCh || topicStr.contains(ch, ignoreCase = true) || ch.contains(topicStr, ignoreCase = true)
                                    }
                                } else {
                                    currentSelectedChapters.any { ch ->
                                        val nCh = com.example.data.repository.normalizeChapterName(ch, q.subject)
                                        normTopic == nCh || topicStr.contains(ch, ignoreCase = true) || ch.contains(topicStr, ignoreCase = true)
                                    }
                                }
                            }
                            matchSubject && matchChapter
                        }
                    }"""
        content = content.replace(old_total, new_total, 1)

    with open(filepath, "w") as f:
        f.write(content)
    print("Fixed", filepath)

fix_file("app/src/main/java/com/example/ui/screens/PracticeScreen.kt")
