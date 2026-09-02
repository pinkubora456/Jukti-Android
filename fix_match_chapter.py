import re

def fix(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    start_str = "val matchChapter = if (selectedChapters.isEmpty()) {"
    end_str = "matchSubject && matchChapter"
    
    start_idx = content.find(start_str)
    end_idx = content.find(end_str, start_idx)
    
    if start_idx != -1 and end_idx != -1:
        new_block = """val matchChapter = if (selectedChapters.isEmpty()) {
                    true
                } else {
                    val topicStr = q.topic ?: ""
                    val normTopic = com.example.data.repository.normalizeChapterName(topicStr, q.subject)
                    
                    if (selectedSubjectKey == "Reasoning") {
                        selectedChapters.any { ch ->
                            topicStr.contains(ch, ignoreCase = true) || 
                            ch.contains(topicStr, ignoreCase = true) ||
                            q.subject.contains(ch, ignoreCase = true) ||
                            (ch.contains("Coding", ignoreCase = true) && (topicStr.isBlank() || topicStr.contains("Code", ignoreCase = true) || topicStr.contains("Series", ignoreCase = true) || topicStr.contains("Analogy", ignoreCase = true))) ||
                            (ch.contains("Blood", ignoreCase = true) && (topicStr.contains("Blood", ignoreCase = true) || topicStr.contains("Direction", ignoreCase = true) || topicStr.contains("Relation", ignoreCase = true))) ||
                            (ch.contains("Seating", ignoreCase = true) && (topicStr.contains("Seat", ignoreCase = true) || topicStr.contains("Puzzle", ignoreCase = true) || topicStr.contains("Venn", ignoreCase = true))) ||
                            (ch.contains("Syllogism", ignoreCase = true) && (topicStr.contains("Syllogism", ignoreCase = true) || topicStr.contains("Statement", ignoreCase = true) || topicStr.contains("Assumption", ignoreCase = true)))
                        }
                    } else if (selectedSubjectKey == "General English") {
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
                    } else {
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
                }
                
                """
        content = content[:start_idx] + new_block + content[end_idx:]
        with open(filepath, 'w') as f:
            f.write(content)
        print("Replaced matchChapter")

fix("app/src/main/java/com/example/ui/screens/PracticeScreen.kt")
