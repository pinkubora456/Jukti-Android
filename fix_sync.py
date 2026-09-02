import re

def fix_file(filepath):
    with open(filepath, "r") as f:
        content = f.read()

    # getCollectionName
    old_col = '"QUESTION" -> "questions"'
    new_col = '"QUESTION", "REPORT_QUESTION" -> "questions"'
    if old_col in content:
        content = content.replace(old_col, new_col)

    with open(filepath, "w") as f:
        f.write(content)
    print("Fixed FirebaseSyncManager")

fix_file("app/src/main/java/com/example/data/repository/FirebaseSyncManager.kt")
