import re

with open("app/src/main/java/com/example/data/local/JuktiDatabase.kt", "r") as f:
    content = f.read()

content = content.replace("ALTER TABLE prep_strategies", "ALTER TABLE prep_strategy")

with open("app/src/main/java/com/example/data/local/JuktiDatabase.kt", "w") as f:
    f.write(content)

