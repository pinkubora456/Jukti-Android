with open('app/src/main/java/com/example/ui/screens/SingleQuestionUploadScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    "onNavigationClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_QBANK) },",
    "onBackClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_QBANK) },"
)

with open('app/src/main/java/com/example/ui/screens/SingleQuestionUploadScreen.kt', 'w') as f:
    f.write(content)
