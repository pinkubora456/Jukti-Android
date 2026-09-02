import re

def fix(filepath):
    with open(filepath, "r") as f:
        content = f.read()

    # We want to replace the `else -> "..."` inside normalizeChapterName.
    # It's probably easier to just replace specific lines, but regex is more robust.
    # The block is inside `fun normalizeChapterName`.
    
    start_idx = content.find("fun normalizeChapterName(")
    end_idx = content.find("fun normalizeSubjectName(")
    
    if start_idx == -1 or end_idx == -1:
        return
        
    block = content[start_idx:end_idx]
    
    # We replace:
    block = re.sub(r'else -> "Static GK"', 'else -> trimmed', block)
    block = re.sub(r'else -> "Simplification"', 'else -> trimmed', block)
    block = re.sub(r'else -> "Analogy"', 'else -> trimmed', block)
    block = re.sub(r'else -> "Vocabulary"', 'else -> trimmed', block)
    block = re.sub(r'else -> "Reading Comprehension & Passages"', 'else -> trimmed', block)
    block = re.sub(r'else -> "Computer Fundamentals & Architecture"', 'else -> trimmed', block)
    block = re.sub(r'else -> "Traffic Signs, Signals & Road Safety"', 'else -> trimmed', block)
    
    content = content[:start_idx] + block + content[end_idx:]
    
    with open(filepath, "w") as f:
        f.write(content)

fix("app/src/main/java/com/example/data/repository/JuktiRepository.kt")
