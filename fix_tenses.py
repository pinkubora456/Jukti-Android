def add_tenses(filepath):
    with open(filepath, "r") as f:
        content = f.read()
    
    # For McqStudyScreen and PracticeScreen
    content = content.replace(
        'set.add("Cloze Test")',
        'set.add("Cloze Test")\n                                set.add("Active & Passive Voice")\n                                set.add("Tenses")'
    )
    
    # For SingleQuestionUploadScreen
    content = content.replace(
        '"One-Word & Idioms", "Reading Comprehension & Para Jumbles", "Cloze Test"',
        '"One-Word & Idioms", "Reading Comprehension & Para Jumbles", "Cloze Test", "Active & Passive Voice", "Tenses"'
    )
    
    with open(filepath, "w") as f:
        f.write(content)

add_tenses("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt")
add_tenses("app/src/main/java/com/example/ui/screens/PracticeScreen.kt")
add_tenses("app/src/main/java/com/example/ui/screens/SingleQuestionUploadScreen.kt")
