import re

with open("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "r") as f:
    content = f.read()

target1 = """                    if (currentQuestion.isPremium && !isUserPremium && !isAdminOrOwner) {"""
replacement1 = """                    if (!viewModel.canAccessQuestion(currentQuestion)) {"""

content = content.replace(target1, replacement1)

target2 = """                    if (!currentQuestion.isPremium || isUserPremium || isAdminOrOwner) {"""
replacement2 = """                    if (viewModel.canAccessQuestion(currentQuestion)) {"""

content = content.replace(target2, replacement2)

target3 = """                                if (!isUserPremium && !isAdminOrOwner && currentQuestionIndex >= 24) {
                                    viewModel.showPaywall()
                                } else if (currentQuestionIndex < studyQuestionsList.size - 1) {"""
replacement3 = """                                if (currentQuestionIndex < studyQuestionsList.size - 1) {"""

content = content.replace(target3, replacement3)

with open("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "w") as f:
    f.write(content)
print("Patched McqStudyScreen.kt ui logic")
