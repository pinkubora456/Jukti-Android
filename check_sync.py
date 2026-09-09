import re

with open("app/src/main/java/com/example/data/repository/FirebaseSyncManager.kt", "r") as f:
    content = f.read()

print("Functions in FirebaseSyncManager:")
for match in re.finditer(r"fun \w+\(", content):
    print(match.group())

