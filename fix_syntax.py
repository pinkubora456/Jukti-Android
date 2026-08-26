import re

with open("app/src/main/java/com/example/data/repository/FirebaseRepository.kt", "r") as f:
    text = f.read()

# Restore commas for those that are not followed by closing parenthesis or brackets
# Wait, let's just make sure we only remove the comma if it's right before a closing parenthesis.
text = re.sub(r'else "FREE"\)\s*questionType', r'else "FREE"),\n                        questionType', text)
text = re.sub(r'else "FREE"\)\s*inProgress', r'else "FREE"),\n                        inProgress', text)
text = re.sub(r'else "FREE"\)\s*role', r'else "FREE"),\n                    role', text)

with open("app/src/main/java/com/example/data/repository/FirebaseRepository.kt", "w") as f:
    f.write(text)

