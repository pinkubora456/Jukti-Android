import re

def fix_file(filepath):
    with open(filepath, "r") as f:
        content = f.read()

    old = """        WHERE subject = :subject 
          AND isReported = 0 
          AND (examCategory LIKE '%' || :exam || '%' OR :exam = 'All Exams')"""
          
    new = """        WHERE (subject = :subject OR :subject = 'All Subjects')
          AND isReported = 0 
          AND (examCategory LIKE '%' || :exam || '%' OR :exam = 'All Exams')"""

    if old in content:
        content = content.replace(old, new)
        with open(filepath, "w") as f:
            f.write(content)
        print("Fixed query")
    else:
        print("old not found")

fix_file("app/src/main/java/com/example/data/local/Daos.kt")
