import re

with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "r") as f:
    content = f.read()

target = """            val scrubbedNotes = notes.map { n ->
                if (com.example.data.util.PlanValidityEngine.isStudyNoteAccessible(n, effectiveEntitlement, isAdminOrOwner)) {
                    n
                } else {
                    n.copy(
                        contentEn = "This content is locked. Please upgrade to a Premium plan to view the full content.",
                        contentAs = "এই সমলটো তলা লগোৱা আছে। সম্পূৰ্ণ বিষয়বস্তু চাবলৈ অনুগ্ৰহ কৰি এটা প্ৰিমিয়াম প্লেনলৈ আপগ্ৰেড কৰক।"
                    )
                }
            }"""

replacement = """            val scrubbedNotes = notes.map { n ->
                if (!n.isPremium) {
                    n
                } else {
                    n.copy(
                        contentEn = "This content is locked. Please upgrade to a Premium plan to view the full content.",
                        contentAs = "এই সমলটো তলা লগোৱা আছে। সম্পূৰ্ণ বিষয়বস্তু চাবলৈ অনুগ্ৰহ কৰি এটা প্ৰিমিয়াম প্লেনলৈ আপগ্ৰেড কৰক।"
                    )
                }
            }"""

content = content.replace(target, replacement)

with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "w") as f:
    f.write(content)
print("Patched notes scrubbing")
