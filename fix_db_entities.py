import re
path = "app/src/main/java/com/example/data/local/JuktiDatabase.kt"
with open(path, "r") as f:
    content = f.read()

pattern = r"EntitlementHistoryEntity::class\n    \],"
replacement = "EntitlementHistoryEntity::class,\n        ReadingComprehensionPassageEntity::class\n    ],"

if re.search(pattern, content):
    content = re.sub(pattern, replacement, content)
    with open(path, "w") as f:
        f.write(content)
    print("Fixed entities list")
else:
    print("Pattern not found")
