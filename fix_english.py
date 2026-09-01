import re

def fix_file(filepath, var_name, subject_var):
    with open(filepath, "r") as f:
        content = f.read()

    # Fix 1: matchSubject for General English in filteredStudyQuestions/practiceQuestions
    old_subj = r'"General English" -> q\.subject == "General English"'
    new_subj = r'"General English" -> q.subject.equals("General English", ignoreCase = true) || q.subject.equals("English", ignoreCase = true) || q.subject.contains("English", ignoreCase = true)'
    content = re.sub(old_subj, new_subj, content)

    # Fix 2: matchChapter for General English in filteredStudyQuestions/practiceQuestions
    # It currently looks like:
    # } else if (selectedSubjectTab == "General English") {
    #     selectedChapters.any { ch ->
    #         if (ch == "One-Word & Idioms" || ch == "One-Word & Idiom") {
    #             normTopic == "One-Word & Idioms" || normTopic == "One-Word & Idiom" || topicStr.contains("Idiom", ignoreCase = true) || topicStr.contains("One-Word", ignoreCase = true) || topicStr.contains("One Word", ignoreCase = true) || topicStr.contains("Substitution", ignoreCase = true) || topicStr.contains("Phrase", ignoreCase = true)
    #         } else {
    #             normTopic.equals(ch, ignoreCase = true) || topicStr.contains(ch, ignoreCase = true) || ch.contains(topicStr, ignoreCase = true)
    #         }
    #     }
    # }
    
    pattern_chapter = r'\} else if \(' + subject_var + r' == "General English"\) \{\n\s+selectedChapters\.any \{ ch ->\n(?:.*?\n)+?\s+\}\n\s+\} else \{'
    
    new_chapter = """} else if (""" + subject_var + """ == "General English") {
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
                        } else {"""
    
    content = re.sub(pattern_chapter, new_chapter, content)

    with open(filepath, "w") as f:
        f.write(content)
    print("Fixed", filepath)

fix_file("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "filteredStudyQuestions", "selectedSubjectTab")
fix_file("app/src/main/java/com/example/ui/screens/PracticeScreen.kt", "practiceQuestions", "selectedSubjectKey")
