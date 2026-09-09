import re

with open('app/src/main/java/com/example/data/repository/JuktiRepository.kt', 'r') as f:
    content = f.read()

# Remove the specific block for "Reading Comprehension" subject chapters
content = re.sub(
    r'"Reading Comprehension" -> when \{.*?\n        \}\n',
    '',
    content,
    flags=re.DOTALL
)

with open('app/src/main/java/com/example/data/repository/JuktiRepository.kt', 'w') as f:
    f.write(content)
