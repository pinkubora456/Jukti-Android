import re

def fix_file(filepath):
    with open(filepath, "r") as f:
        content = f.read()

    # Fix matchSubject for General English everywhere in the file
    old_subj = r'"General English" -> q\.subject == "General English"'
    new_subj = r'"General English" -> q.subject.equals("General English", ignoreCase = true) || q.subject.equals("English", ignoreCase = true) || q.subject.contains("English", ignoreCase = true)'
    
    content = re.sub(old_subj, new_subj, content)

    with open(filepath, "w") as f:
        f.write(content)
    print("Fixed totalCount subject in", filepath)

fix_file("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt")
fix_file("app/src/main/java/com/example/ui/screens/PracticeScreen.kt")
