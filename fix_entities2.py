import re
with open("app/src/main/java/com/example/data/local/Entities.kt", "r") as f:
    text = f.read()

text = text.replace('val accessType: String = "FREE"\\)', 'val accessType: String = "FREE"\n)')

with open("app/src/main/java/com/example/data/local/Entities.kt", "w") as f:
    f.write(text)

