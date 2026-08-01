import os

screens = [
    "app/src/main/java/com/example/ui/screens/McqStudyScreen.kt",
    "app/src/main/java/com/example/ui/screens/PracticeScreen.kt",
    "app/src/main/java/com/example/ui/screens/MockTestPlayerScreen.kt",
    "app/src/main/java/com/example/ui/screens/GlobalSearchScreen.kt"
]

for screen in screens:
    with open(screen, "r") as f:
        content = f.read()
    
    # In McqStudyScreen, PracticeScreen, MockTestPlayerScreen
    content = content.replace(
        "onSubmitReport = { reason, details ->\n                    showReportDialog = false\n                    android.widget.Toast.makeText(context, \"Report submitted successfully\", android.widget.Toast.LENGTH_SHORT).show()\n                }",
        "onSubmitReport = { reason, details ->\n                    showReportDialog = false\n                    viewModel.reportQuestion(currentQuestion!!)\n                    android.widget.Toast.makeText(context, \"Report submitted successfully\", android.widget.Toast.LENGTH_SHORT).show()\n                }"
    )

    # GlobalSearchScreen
    content = content.replace(
        """onReportClick = { android.widget.Toast.makeText(context, "Question reported successfully", android.widget.Toast.LENGTH_SHORT).show() }""",
        """onReportClick = { viewModel.reportQuestion(result); android.widget.Toast.makeText(context, "Question reported successfully", android.widget.Toast.LENGTH_SHORT).show() }"""
    )
    
    with open(screen, "w") as f:
        f.write(content)
