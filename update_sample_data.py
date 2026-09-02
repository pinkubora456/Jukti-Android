import re

with open("app/src/main/java/com/example/data/repository/SampleData.kt", "r") as f:
    content = f.read()

replacement = """
    val sampleSubjectsChapters = listOf(
        SubjectChapterEntity(subject = "General English", chapter = "Grammar & Sentence Correction"),
        SubjectChapterEntity(subject = "General English", chapter = "Synonyms, Antonyms & Vocabulary"),
        SubjectChapterEntity(subject = "General English", chapter = "One-Word & Idioms"),
        SubjectChapterEntity(subject = "General English", chapter = "Reading Comprehension & Para Jumbles"),
        SubjectChapterEntity(subject = "General English", chapter = "Cloze Test"),
        SubjectChapterEntity(subject = "General English", chapter = "Active & Passive Voice"),
        SubjectChapterEntity(subject = "General English", chapter = "Tenses"),

        SubjectChapterEntity(subject = "General Mathematics", chapter = "Number System, LCM & HCF"),
        SubjectChapterEntity(subject = "General Mathematics", chapter = "Percentage, Ratio & Proportion"),
        SubjectChapterEntity(subject = "General Mathematics", chapter = "Profit, Loss, Discount & Simple/Compound Interest"),
        SubjectChapterEntity(subject = "General Mathematics", chapter = "Time, Work, Speed, Distance & Mensuration"),

        SubjectChapterEntity(subject = "Reasoning & Mental Ability", chapter = "Coding-Decoding, Series & Analogy"),
        SubjectChapterEntity(subject = "Reasoning & Mental Ability", chapter = "Blood Relations & Direction Sense Test"),
        SubjectChapterEntity(subject = "Reasoning & Mental Ability", chapter = "Seating Arrangement, Puzzles & Venn Diagrams"),
        SubjectChapterEntity(subject = "Reasoning & Mental Ability", chapter = "Syllogism, Statements & Assumptions"),

        SubjectChapterEntity(subject = "Basic Computer", chapter = "Computer Fundamentals & Architecture"),
        SubjectChapterEntity(subject = "Basic Computer", chapter = "Operating Systems & MS Office (Word, Excel, PowerPoint)"),
        SubjectChapterEntity(subject = "Basic Computer", chapter = "Internet, Networking & Cyber Security"),
        SubjectChapterEntity(subject = "Basic Computer", chapter = "Hardware, Software & Input/Output Devices"),
        SubjectChapterEntity(subject = "Basic Computer", chapter = "Database, Shortcuts & Computer Abbreviations"),

        SubjectChapterEntity(subject = "Transport Rule", chapter = "Traffic Signs, Signals & Road Safety"),
        SubjectChapterEntity(subject = "Transport Rule", chapter = "Motor Vehicles Act & Traffic Rules"),
        SubjectChapterEntity(subject = "Transport Rule", chapter = "Driving Regulations, Licences & Permits"),
        SubjectChapterEntity(subject = "Transport Rule", chapter = "Vehicle Safety, Violations & Penalties")
    )
"""

content = content.replace("val sampleSubjectsChapters = emptyList<SubjectChapterEntity>()", replacement.strip())

with open("app/src/main/java/com/example/data/repository/SampleData.kt", "w") as f:
    f.write(content)

