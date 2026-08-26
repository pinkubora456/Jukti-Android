import re

with open("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "r") as f:
    content = f.read()

target = """                if (showSavedQuestionsDialog) {
                    SavedQuestionsDialog(
                        questions = bookmarkedQuestions,
                        language = language,
                        onDismiss = { showSavedQuestionsDialog = false },
                        onToggleBookmark = { q -> viewModel.toggleBookmarkQuestion(q) }
                    )
                }"""

replacement = """                if (showSavedQuestionsDialog) {
                    val isPremium by viewModel.isUserPremium.collectAsState()
                    val isAdmin by viewModel.isAdminOrOwner.collectAsState()
                    SavedQuestionsDialog(
                        questions = bookmarkedQuestions,
                        language = language,
                        isUserPremium = isPremium,
                        isAdminOrOwner = isAdmin,
                        onDismiss = { showSavedQuestionsDialog = false },
                        onToggleBookmark = { q -> viewModel.toggleBookmarkQuestion(q) }
                    )
                }"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "w") as f:
        f.write(content)
    print("Patched McqStudyScreen.kt")
else:
    print("Could not find target in McqStudyScreen.kt")
