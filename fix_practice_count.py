import re

with open("app/src/main/java/com/example/ui/screens/PracticeScreen.kt", "r") as f:
    content = f.read()

old_block = r"""                    val questionCount = remember\(visibleQuestions, banner\.subjectKey\) \{
                        when \(banner\.subjectKey\) \{
                            "All Subjects" -> visibleQuestions\.size
                            "General Knowledge" -> visibleQuestions\.count \{ it\.subject in listOf\("General Knowledge", "Assam History", "Assam Geography", "Assamese Literature & Culture", "Current Affairs"\) \}
                            "General English" -> visibleQuestions\.count \{ it\.subject == "General English" \}
                            "General Mathematics" -> visibleQuestions\.count \{ it\.subject in listOf\("General Mathematics", "Mathematics", "Quantitative Aptitude"\) \}
                            "Reasoning" -> visibleQuestions\.count \{ it\.subject in listOf\("Reasoning", "Logical Reasoning", "Logical Reasoning & Mental Ability", "Mental Ability", "Logical Aptitude", "Reasoning & Mental Ability"\) \}
                            "Basic Computer", "Computer Knowledge", "Computer" -> visibleQuestions\.count \{ it\.subject in listOf\("Basic Computer", "Computer Knowledge", "Computer", "Computer Awareness", "Computer Science", "Information Technology", "IT"\) || it\.subject\.contains\("Computer", ignoreCase = true\) || it\.topic\.contains\("Computer", ignoreCase = true\) || it\.topic\.contains\("MS Office", ignoreCase = true\) || it\.topic\.contains\("Operating System", ignoreCase = true\) || it\.topic\.contains\("Internet", ignoreCase = true\) || it\.topic\.contains\("Hardware", ignoreCase = true\) \}
                            "Transport Rule" -> visibleQuestions\.count \{ it\.subject\.equals\("Transport Rule", ignoreCase = true\) || it\.subject\.equals\("Transport Rules", ignoreCase = true\) || it\.subject\.equals\("Manual Entry", ignoreCase = true\) || it\.subject\.contains\("Manual", ignoreCase = true\) || it\.topic\.contains\("Transport Rule", ignoreCase = true\) || it\.topic\.contains\("Traffic Sign", ignoreCase = true\) || it\.topic\.contains\("Motor Vehicle", ignoreCase = true\) || it\.topic\.contains\("Driving Regulation", ignoreCase = true\) || it\.topic\.contains\("Vehicle Safety", ignoreCase = true\) \}
                            else -> visibleQuestions\.count \{ it\.subject\.equals\(banner\.subjectKey, ignoreCase = true\) \}
                        \}
                    \}

                    val currentSelectedChapters = chaptersMap\[banner\.subjectKey\] \?: emptySet\(\)"""

new_block = """                    val currentSelectedChapters = chaptersMap[banner.subjectKey] ?: emptySet()
                    
                    val questionCount = remember(visibleQuestions, banner.subjectKey, currentSelectedChapters) {
                        visibleQuestions.count { q ->
                            val matchSubject = when (banner.subjectKey) {
                                "All Subjects" -> true
                                "General Knowledge" -> q.subject in listOf("General Knowledge", "Assam History", "Assam Geography", "Assamese Literature & Culture", "Current Affairs")
                                "General English" -> q.subject == "General English"
                                "General Mathematics" -> q.subject in listOf("General Mathematics", "Mathematics", "Quantitative Aptitude")
                                "Reasoning" -> q.subject in listOf("Reasoning", "Logical Reasoning", "Logical Reasoning & Mental Ability", "Mental Ability", "Logical Aptitude", "Reasoning & Mental Ability")
                                "Basic Computer", "Computer Knowledge", "Computer" -> q.subject in listOf("Basic Computer", "Computer Knowledge", "Computer", "Computer Awareness", "Computer Science", "Information Technology", "IT") || q.subject.contains("Computer", ignoreCase = true) || q.topic.contains("Computer", ignoreCase = true) || q.topic.contains("MS Office", ignoreCase = true) || q.topic.contains("Operating System", ignoreCase = true) || q.topic.contains("Internet", ignoreCase = true) || q.topic.contains("Hardware", ignoreCase = true)
                                "Transport Rule" -> q.subject.equals("Transport Rule", ignoreCase = true) || q.subject.equals("Transport Rules", ignoreCase = true) || q.subject.equals("Manual Entry", ignoreCase = true) || q.subject.contains("Manual", ignoreCase = true) || q.topic.contains("Transport Rule", ignoreCase = true) || q.topic.contains("Traffic Sign", ignoreCase = true) || q.topic.contains("Motor Vehicle", ignoreCase = true) || q.topic.contains("Driving Regulation", ignoreCase = true) || q.topic.contains("Vehicle Safety", ignoreCase = true)
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

content, count = re.subn(old_block, new_block, content, count=1)
if count == 1:
    with open("app/src/main/java/com/example/ui/screens/PracticeScreen.kt", "w") as f:
        f.write(content)
    print("Success: questionCount in PracticeScreen rewritten")
else:
    print("Failed to replace questionCount in PracticeScreen")
