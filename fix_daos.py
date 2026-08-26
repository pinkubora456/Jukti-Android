import re

with open("app/src/main/java/com/example/data/local/Daos.kt", "r") as f:
    text = f.read()

text = text.replace(
    "if (question.isPremium) return -1L // Prevent inserting Premium questions into Room",
    'if (question.accessType == "PREMIUM" || question.isPremium) return -1L // Prevent inserting Premium questions into Room'
)
text = text.replace(
    "val freeQuestions = questions.filter { !it.isPremium }",
    'val freeQuestions = questions.filter { it.accessType != "PREMIUM" && !it.isPremium }'
)

text = text.replace(
    "if (test.isPremium) return -1L",
    'if (test.accessType == "PREMIUM" || test.isPremium) return -1L'
)
text = text.replace(
    "val freeTests = tests.filter { !it.isPremium }",
    'val freeTests = tests.filter { it.accessType != "PREMIUM" && !it.isPremium }'
)

text = text.replace(
    "if (note.isPremium) return -1L",
    'if (note.accessType == "PREMIUM" || note.isPremium) return -1L'
)
text = text.replace(
    "val freeNotes = notes.filter { !it.isPremium }",
    'val freeNotes = notes.filter { it.accessType != "PREMIUM" && !it.isPremium }'
)

with open("app/src/main/java/com/example/data/local/Daos.kt", "w") as f:
    f.write(text)
