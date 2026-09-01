import re

def normalizeChapterName(raw, subject):
    trimmed = (raw or "").strip()
    if not trimmed: return "General Knowledge"
    lower = trimmed.lower()
    
    normSubject = subject
    
    if normSubject == "General English":
        if "vocab" in lower: return "Vocabulary"
        if "synonym" in lower or "antonym" in lower: return "Synonyms & Antonyms"
        if "one-word" in lower or "one word" in lower or "idiom" in lower or "substitution" in lower: return "One-Word & Idioms"
        if "phrasal" in lower: return "Phrasal Verbs"
        if "spotting" in lower or "error" in lower: return "Spotting Errors"
        if "improvement" in lower: return "Sentence Improvement"
        if "fill" in lower or "blank" in lower: return "Fill in the Blanks"
        if "cloze" in lower: return "Cloze Test"
        if "jumble" in lower or "rearrang" in lower: return "Para Jumbles"
        if "voice" in lower or "passive" in lower: return "Active & Passive Voice"
        if "speech" in lower or "direct" in lower: return "Direct & Indirect Speech"
        if "article" in lower: return "Articles"
        if "preposition" in lower: return "Prepositions"
        if "conjunction" in lower: return "Conjunctions"
        if "tense" in lower: return "Tenses"
        if "sub" in lower and "verb" in lower: return "Sub–Verb Agreement"
        if "narration" in lower: return "Narration"
        if "correction" in lower or "grammar" in lower: return "Sentence Correction"
        return "Vocabulary"

print(normalizeChapterName("cloze test", "General English"))
print(normalizeChapterName("cloze test", ""))

