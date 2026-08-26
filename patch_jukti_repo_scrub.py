import re

with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "r") as f:
    content = f.read()

target = """            val scrubbedQuestions = questions.map { q ->
                if (com.example.data.util.PlanValidityEngine.isQuestionAccessible(q, effectiveEntitlement, isAdminOrOwner)) {
                    q
                } else {
                    q.copy(
                        questionEn = "Premium Content 🔒",
                        questionAs = "প্ৰিমিয়াম সমল 🔒",
                        optionAEn = "Unlock to view options", optionAAs = "",
                        optionBEn = "Unlock to view options", optionBAs = "",
                        optionCEn = "Unlock to view options", optionCAs = "",
                        optionDEn = "Unlock to view options", optionDAs = "",
                        correctOptionIndex = -1,
                        explanationEn = "This explanation is locked. Please upgrade to a Premium plan to view the full answer and explanation.",
                        explanationAs = "এই ব্যাখ্যাটো তলা লগোৱা আছে। সম্পূৰ্ণ উত্তৰ আৰু ব্যাখ্যা চাবলৈ অনুগ্ৰহ কৰি এটা প্ৰিমিয়াম প্লেনলৈ আপগ্ৰেড কৰক।"
                    )
                }
            }"""

replacement = """            val scrubbedQuestions = questions.map { q ->
                if (!q.isPremium) {
                    q
                } else {
                    q.copy(
                        questionEn = "Premium Content 🔒",
                        questionAs = "প্ৰিমিয়াম সমল 🔒",
                        optionAEn = "Unlock to view options", optionAAs = "",
                        optionBEn = "Unlock to view options", optionBAs = "",
                        optionCEn = "Unlock to view options", optionCAs = "",
                        optionDEn = "Unlock to view options", optionDAs = "",
                        correctOptionIndex = -1,
                        explanationEn = "This explanation is locked. Please upgrade to a Premium plan to view the full answer and explanation.",
                        explanationAs = "এই ব্যাখ্যাটো তলা লগোৱা আছে। সম্পূৰ্ণ উত্তৰ আৰু ব্যাখ্যা চাবলৈ অনুগ্ৰহ কৰি এটা প্ৰিমিয়াম প্লেনলৈ আপগ্ৰেড কৰক।"
                    )
                }
            }"""
content = content.replace(target, replacement)

target2 = """            val scrubbedNotes = notes.map { n ->
                if (com.example.data.util.PlanValidityEngine.isStudyNoteAccessible(n, effectiveEntitlement, isAdminOrOwner)) {
                    n
                } else {
                    n.copy(
                        contentEn = "This content is locked. Please upgrade to a Premium plan to view the full content.",
                        contentAs = "এই সমলটো তলা লগোৱা আছে। সম্পূৰ্ণ সমলটো চাবলৈ অনুগ্ৰহ কৰি এটা প্ৰিমিয়াম প্লেনলৈ আপগ্ৰেড কৰক।"
                    )
                }
            }"""

replacement2 = """            val scrubbedNotes = notes.map { n ->
                if (!n.isPremium) {
                    n
                } else {
                    n.copy(
                        contentEn = "This content is locked. Please upgrade to a Premium plan to view the full content.",
                        contentAs = "এই সমলটো তলা লগোৱা আছে। সম্পূৰ্ণ সমলটো চাবলৈ অনুগ্ৰহ কৰি এটা প্ৰিমিয়াম প্লেনলৈ আপগ্ৰেড কৰক।"
                    )
                }
            }"""
content = content.replace(target2, replacement2)

with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "w") as f:
    f.write(content)
print("Patched JuktiRepository scrub logic")
