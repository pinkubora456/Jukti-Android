import re

with open('app/src/main/java/com/example/data/repository/JuktiRepository.kt', 'r') as f:
    content = f.read()

# Fix normalizeChapterName for General English
gen_eng_chapter = """        "General English" -> when {
            lower.contains("vocab") -> "Vocabulary"
            lower.contains("synonym") || lower.contains("antonym") -> "Synonyms & Antonyms"
            lower.contains("one-word") || lower.contains("one word") || lower.contains("idiom") || lower.contains("substitution") -> "One-Word & Idioms"
            lower.contains("phrasal") -> "Phrasal Verbs"
            lower.contains("spotting") || lower.contains("error") -> "Spotting Errors"
            lower.contains("improvement") -> "Sentence Improvement"
            lower.contains("fill") || lower.contains("blank") -> "Fill in the Blanks"
            lower.contains("cloze") -> "Cloze Test"
            lower.contains("jumble") || lower.contains("rearrang") -> "Para Jumbles"
            lower.contains("voice") || lower.contains("passive") -> "Active & Passive Voice"
            lower.contains("speech") || lower.contains("direct") -> "Direct & Indirect Speech"
            lower.contains("article") -> "Articles"
            lower.contains("preposition") -> "Prepositions"
            lower.contains("conjunction") -> "Conjunctions"
            lower.contains("tense") -> "Tenses"
            lower.contains("sub") && lower.contains("verb") -> "Sub–Verb Agreement"
            lower.contains("narration") -> "Narration"
            lower.contains("correction") || lower.contains("grammar") -> "Sentence Correction"
            lower.contains("comprehension") || lower.contains("passage") || lower.contains("reading") -> "Reading Comprehension"
            else -> trimmed
        }
"""
content = re.sub(
    r'"General English" -> when \{.*?(?="Transport & Motor Vehicle"|"Reading Comprehension"|"Transport Rule")',
    gen_eng_chapter,
    content,
    flags=re.DOTALL
)

# Remove the independent "Reading Comprehension" block from normalizeChapterName
content = re.sub(
    r'        "Reading Comprehension" -> when \{.*?\n        \}\n',
    '',
    content,
    flags=re.DOTALL
)

# Fix normalizeQuestionEntity
old_normalize_q = """    val normSubject = if (isComprehension) {
        "Reading Comprehension"
    } else {
        normalizeSubjectName(q.subject)
    }

    val normTopic = normalizeChapterName(q.topic, normSubject)"""

new_normalize_q = """    val normSubject = normalizeSubjectName(q.subject)
    val normTopic = if (isComprehension) "Reading Comprehension" else normalizeChapterName(q.topic, normSubject)"""

content = content.replace(old_normalize_q, new_normalize_q)

# Also fix the `isComprehension` check itself to ensure it applies Reading Comprehension chapter if it was the Subject before.
old_is_comp = """    // Detect Reading Comprehension questions first
    val isComprehension = qTypeLower.contains("comprehension") || qTypeLower.contains("passage") ||
            topicLower.contains("comprehension") || topicLower.contains("passage") ||
            subLower.contains("comprehension") || subLower.contains("passage") ||
            qEnLower.contains("read the passage") || qEnLower.contains("following passage")"""

new_is_comp = """    // Detect Reading Comprehension questions first
    val isComprehension = qTypeLower.contains("comprehension") || qTypeLower.contains("passage") ||
            topicLower.contains("comprehension") || topicLower.contains("passage") ||
            subLower.contains("comprehension") || subLower.contains("passage") ||
            qEnLower.contains("read the passage") || qEnLower.contains("following passage") || q.subject.equals("Reading Comprehension", ignoreCase = true)"""

content = content.replace(old_is_comp, new_is_comp)


with open('app/src/main/java/com/example/data/repository/JuktiRepository.kt', 'w') as f:
    f.write(content)
