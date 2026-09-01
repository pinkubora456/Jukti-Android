import re

def fix_file(filepath):
    with open(filepath, "r") as f:
        content = f.read()

    # Fix where it populates available chapters for General English
    old_subj = r'it\.subject == "General English"'
    new_subj = r'(it.subject.equals("General English", ignoreCase = true) || it.subject.equals("English", ignoreCase = true) || it.subject.contains("English", ignoreCase = true))'
    
    content = re.sub(old_subj, new_subj, content)

    with open(filepath, "w") as f:
        f.write(content)
    print("Fixed populate chapters subject in", filepath)

fix_file("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt")
fix_file("app/src/main/java/com/example/ui/screens/PracticeScreen.kt")
