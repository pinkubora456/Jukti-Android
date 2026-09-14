import re

path = "app/src/main/java/com/example/ui/components/BatchImportMockQuestionsDialog.kt"
with open(path, "r") as f:
    content = f.read()

# Fix the backslashes
content = content.replace(r'\"', '"')

with open(path, "w") as f:
    f.write(content)

