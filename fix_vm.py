import re

def fix_file(filepath):
    with open(filepath, "r") as f:
        content = f.read()

    old = "val res = repository.updateQuestion(question.copy(isReported = true))"
    new = "val res = repository.reportQuestion(question)"
    
    if old in content:
        content = content.replace(old, new)
        with open(filepath, "w") as f:
            f.write(content)
        print("Fixed JuktiViewModel")

fix_file("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt")
