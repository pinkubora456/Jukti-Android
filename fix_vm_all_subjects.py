import re

def fix_file(filepath):
    with open(filepath, "r") as f:
        content = f.read()

    old = """            premiumQs.filter { it.subject == subject && !it.isReported && (exam == "All Exams" || it.examCategory.contains(exam)) }"""
    new = """            premiumQs.filter { (it.subject == subject || subject == "All Subjects") && !it.isReported && (exam == "All Exams" || it.examCategory.contains(exam)) }"""

    if old in content:
        content = content.replace(old, new)
        with open(filepath, "w") as f:
            f.write(content)
        print("Fixed ViewModel")
    else:
        print("old not found")

fix_file("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt")
