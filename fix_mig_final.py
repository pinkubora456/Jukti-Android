import re

with open("app/src/main/java/com/example/ui/screens/ManageGuidanceScreen.kt", "r") as f:
    content = f.read()

content = content.replace('fun ManageGuidanceScreen(viewModel: JuktiViewModel) {', 'fun ManageGuidanceScreen(viewModel: JuktiViewModel, onBackClick: () -> Unit = {}) {')

with open("app/src/main/java/com/example/ui/screens/ManageGuidanceScreen.kt", "w") as f:
    f.write(content)
