import re

def add_cloze_test(filepath):
    with open(filepath, "r") as f:
        content = f.read()
    
    # For McqStudyScreen and PracticeScreen
    content = content.replace(
        'set.add("Reading Comprehension & Para Jumbles")',
        'set.add("Reading Comprehension & Para Jumbles")\n                                set.add("Cloze Test")'
    )
    
    # For SingleQuestionUploadScreen
    content = content.replace(
        '"One-Word & Idioms", "Reading Comprehension & Para Jumbles"',
        '"One-Word & Idioms", "Reading Comprehension & Para Jumbles", "Cloze Test"'
    )
    
    with open(filepath, "w") as f:
        f.write(content)

add_cloze_test("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt")
add_cloze_test("app/src/main/java/com/example/ui/screens/PracticeScreen.kt")
add_cloze_test("app/src/main/java/com/example/ui/screens/SingleQuestionUploadScreen.kt")
