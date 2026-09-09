import re

with open("app/src/main/java/com/example/data/local/JuktiDatabase.kt", "r") as f:
    content = f.read()

content = content.replace("version = 42,", "version = 43,")

with open("app/src/main/java/com/example/data/local/JuktiDatabase.kt", "w") as f:
    f.write(content)
