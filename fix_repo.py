import re

def fix(filepath):
    with open(filepath, "r") as f:
        content = f.read()

    # Find the first duplicate normalizeSubjectName and remove it.
    idx1 = content.find("fun normalizeSubjectName")
    idx2 = content.find("fun normalizeSubjectName", idx1 + 10)
    
    if idx2 != -1:
        # It's a duplicate, delete from idx1 to idx2
        content = content[:idx1] + content[idx2:]

    # Replace `else -> "General Knowledge"` with `else -> trimmed`
    # BUT only inside normalizeSubjectName and normalizeChapterName
    # Wait, in normalizeChapterName there are multiple `else -> "General Knowledge"`?
    # Let's just globally replace it, wait, what if there's one that shouldn't be?
    # If the default branch is just `trimmed`, it's safe for everything because if it falls through,
    # it just returns what the user typed.
    content = content.replace('else -> "General Knowledge"', 'else -> trimmed')
    
    with open(filepath, "w") as f:
        f.write(content)

fix("app/src/main/java/com/example/data/repository/JuktiRepository.kt")
