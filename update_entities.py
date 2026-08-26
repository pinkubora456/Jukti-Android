import re

with open("app/src/main/java/com/example/data/local/Entities.kt", "r") as f:
    text = f.read()

text = re.sub(r'val isPremium: Boolean = false,', r'val isPremium: Boolean = false,\n    val accessType: String = "FREE",', text)
text = re.sub(r'val isPremium: Boolean = false\n\)', r'val isPremium: Boolean = false,\n    val accessType: String = "FREE"\n\)', text)

with open("app/src/main/java/com/example/data/local/Entities.kt", "w") as f:
    f.write(text)

