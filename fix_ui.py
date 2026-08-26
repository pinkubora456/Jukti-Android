import re

with open("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "r") as f:
    content = f.read()

# Replace onClick in Current Affairs
content = content.replace("onClick = { selectedNote = note }", "onClick = { viewModel.selectStudyNote(note) }")

with open("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "w") as f:
    f.write(content)
