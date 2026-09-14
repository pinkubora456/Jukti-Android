import re

path = "app/src/main/java/com/example/data/local/Entities.kt"
with open(path, "r") as f:
    content = f.read()

pattern = r"(\s+val duplicateKey: String = \"\",\n\s+@ColumnInfo\(defaultValue = \"\"\) val pyqExams: String = \"\")"
replacement = r"\1,\n    @ColumnInfo(defaultValue = \"normal\") val contentType: String = \"normal\",\n    @ColumnInfo(defaultValue = \"\") val passageId: String = \"\""

if re.search(pattern, content):
    content = re.sub(pattern, replacement, content)
    with open(path, "w") as f:
        f.write(content)
    print("Patched Entities.kt successfully")
else:
    print("Could not find pattern in Entities.kt")

