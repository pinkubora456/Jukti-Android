import re

# Fix MockTestsScreen
with open("app/src/main/java/com/example/ui/screens/MockTestsScreen.kt", "r") as f:
    content = f.read()

content = content.replace(
    "val mockTests by viewModel.accessibleMockTests.collectAsState()",
    "val mockTests by viewModel.mockTests.collectAsState()"
)

with open("app/src/main/java/com/example/ui/screens/MockTestsScreen.kt", "w") as f:
    f.write(content)

# Fix StudyNotesScreen
with open("app/src/main/java/com/example/ui/screens/StudyNotesScreen.kt", "r") as f:
    content = f.read()

content = content.replace(
    "val notes by viewModel.accessibleStudyNotes.collectAsState()",
    "val notes by viewModel.studyNotes.collectAsState()"
)

with open("app/src/main/java/com/example/ui/screens/StudyNotesScreen.kt", "w") as f:
    f.write(content)

