import re

def fix(filepath):
    with open(filepath, "r") as f:
        content = f.read()

    content = content.replace('else -> "Static GK"', 'else -> trimmed')
    content = content.replace('else -> "Simplification"', 'else -> trimmed')
    content = content.replace('else -> "Analogy"', 'else -> trimmed')
    content = content.replace('else -> "Vocabulary"', 'else -> trimmed')
    content = content.replace('else -> "Reading Comprehension & Passages"', 'else -> trimmed')
    content = content.replace('else -> "Computer Fundamentals & Architecture"', 'else -> trimmed')
    content = content.replace('else -> "Traffic Signs, Signals & Road Safety"', 'else -> trimmed')
    
    with open(filepath, "w") as f:
        f.write(content)

fix("app/src/main/java/com/example/data/repository/JuktiRepository.kt")
