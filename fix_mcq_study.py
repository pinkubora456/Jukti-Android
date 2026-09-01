with open("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "r") as f:
    content = f.read()

old_block = """    var activeStudySessionQuestions by remember { mutableStateOf<List<QuestionEntity>>(emptyList()) }
    var lastStudyStartingQuestionId by rememberSaveable { mutableLongStateOf(-1L) }

    LaunchedEffect(isStudySessionStarted, selectedSubjectTab, selectedChapters, hiddenIds) {
        if (isStudySessionStarted) {
            val filtered = questions.filter { q ->
                try {
                    if (q.id in hiddenIds) return@filter false
                    val matchSubject = when (selectedSubjectTab) {
                        "All Subject", "All Subjects" -> true
                        "General Knowledge" -> q.subject in listOf("General Knowledge", "Assam History", "Assam Geography", "Assamese Literature & Culture", "Current Affairs")
                        "General English" -> q.subject == "General English"
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
                        if (selectedSubjectTab == "Reasoning") {
                            selectedChapters.any { ch ->
                                topicStr.contains(ch, ignoreCase = true) ||
                                        (ch == "Coding & Decoding" && (normTopic == "Coding-Decoding" || topicStr.contains("Coding", ignoreCase = true))) ||
                                        (ch == "Blood Relations" && (normTopic == "Blood Relation" || topicStr.contains("Blood", ignoreCase = true))) ||
                                        (ch == "Number & Alphabet Series" && (normTopic.contains("Series") || topicStr.contains("Series", ignoreCase = true))) ||
                                        (ch == "Analogy & Classification" && (normTopic.contains("Analogy") || normTopic.contains("Classification") || topicStr.contains("Analogy", ignoreCase = true))) ||
                                        (ch == "Syllogism" && (normTopic == "Syllogism" || topicStr.contains("Syllogism", ignoreCase = true))) ||
                                        (ch == "Venn Diagrams" && (normTopic == "Venn Diagram" || topicStr.contains("Venn", ignoreCase = true))) ||
                                        (ch == "Direction Sense" && (normTopic == "Direction Sense Test" || topicStr.contains("Direction", ignoreCase = true)))
                            }
                        } else if (selectedSubjectTab == "General English") {
                            selectedChapters.any { ch ->
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
                        } else if (selectedSubjectTab == "General Knowledge") {
                            selectedChapters.any { ch ->
                                val nCh = com.example.data.repository.normalizeChapterName(ch, q.subject)
                                normTopic == nCh || topicStr.contains(ch, ignoreCase = true) || ch.contains(topicStr, ignoreCase = true)
                            }
                        } else {
                            // For any other subject, direct check
                            selectedChapters.any { ch ->
                                val nCh = com.example.data.repository.normalizeChapterName(ch, q.subject)
                                normTopic == nCh || topicStr.contains(ch, ignoreCase = true) || ch.contains(topicStr, ignoreCase = true)
                            }
                        }
                    }
                    matchSubject && matchChapter
                } catch (e: Exception) {
                    false
                }
            }.shuffled().toMutableList()

            if (filtered.size > 1 && filtered[0].id == lastStudyStartingQuestionId) {
                val temp = filtered[0]
                filtered[0] = filtered[1]
                filtered[1] = temp
            }
            if (filtered.isNotEmpty()) {
                lastStudyStartingQuestionId = filtered[0].id
            }
            activeStudySessionQuestions = filtered
            currentQuestionIndex = 0
            selectedOptionIndex = null
        } else {
            activeStudySessionQuestions = emptyList()
        }
    }

    val studyQuestionsList = if (isStudySessionStarted && activeStudySessionQuestions.isNotEmpty()) activeStudySessionQuestions else questions.filter { !hiddenIds.contains(it.id) }"""

new_block = """    var activeStudySessionQuestions by remember { mutableStateOf<List<QuestionEntity>>(emptyList()) }
    var lastStudyStartingQuestionId by rememberSaveable { mutableLongStateOf(-1L) }

    val filteredStudyQuestions = remember(questions, selectedSubjectTab, selectedChapters, hiddenIds) {
        questions.filter { q ->
            try {
                if (q.id in hiddenIds) return@filter false
                val matchSubject = when (selectedSubjectTab) {
                    "All Subject", "All Subjects" -> true
                    "General Knowledge" -> q.subject in listOf("General Knowledge", "Assam History", "Assam Geography", "Assamese Literature & Culture", "Current Affairs")
                    "General English" -> q.subject == "General English"
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
                    if (selectedSubjectTab == "Reasoning") {
                        selectedChapters.any { ch ->
                            topicStr.contains(ch, ignoreCase = true) ||
                                    (ch == "Coding & Decoding" && (normTopic == "Coding-Decoding" || topicStr.contains("Coding", ignoreCase = true))) ||
                                    (ch == "Blood Relations" && (normTopic == "Blood Relation" || topicStr.contains("Blood", ignoreCase = true))) ||
                                    (ch == "Number & Alphabet Series" && (normTopic.contains("Series") || topicStr.contains("Series", ignoreCase = true))) ||
                                    (ch == "Analogy & Classification" && (normTopic.contains("Analogy") || normTopic.contains("Classification") || topicStr.contains("Analogy", ignoreCase = true))) ||
                                    (ch == "Syllogism" && (normTopic == "Syllogism" || topicStr.contains("Syllogism", ignoreCase = true))) ||
                                    (ch == "Venn Diagrams" && (normTopic == "Venn Diagram" || topicStr.contains("Venn", ignoreCase = true))) ||
                                    (ch == "Direction Sense" && (normTopic == "Direction Sense Test" || topicStr.contains("Direction", ignoreCase = true)))
                        }
                    } else if (selectedSubjectTab == "General English") {
                        selectedChapters.any { ch ->
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
                    } else if (selectedSubjectTab == "General Knowledge") {
                        selectedChapters.any { ch ->
                            val nCh = com.example.data.repository.normalizeChapterName(ch, q.subject)
                            normTopic == nCh || topicStr.contains(ch, ignoreCase = true) || ch.contains(topicStr, ignoreCase = true)
                        }
                    } else {
                        // For any other subject, direct check
                        selectedChapters.any { ch ->
                            val nCh = com.example.data.repository.normalizeChapterName(ch, q.subject)
                            normTopic == nCh || topicStr.contains(ch, ignoreCase = true) || ch.contains(topicStr, ignoreCase = true)
                        }
                    }
                }
                matchSubject && matchChapter
            } catch (e: Exception) {
                false
            }
        }
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

if old_block in content:
    content = content.replace(old_block, new_block, 1)
    with open("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "w") as f:
        f.write(content)
    print("Success: McqStudyScreen rewritten")
else:
    print("Failed: old_block not found in McqStudyScreen")
