import re

def fix(filepath):
    with open(filepath, "r") as f:
        content = f.read()

    # We can replace the broken Button block:
    broken = """                                    Button(
                                    onClick = {
                                        if (currentQuestionIndex < displayQuestions.size - 1) {
                                            currentQuestionIndex++
             } else {
                                            viewModel.awardChapterCompletionXp()
                                            showSummary = true
                                                    ) {"""
                                                    
    fixed = """                                    Button(
                                        onClick = {
                                            if (currentQuestionIndex < displayQuestions.size - 1) {
                                                currentQuestionIndex++
                                            } else {
                                                viewModel.awardChapterCompletionXp()
                                                showSummary = true
                                            }
                                        }
                                    ) {"""

    content = content.replace(broken, fixed)
    
    # Also fix the Text block:
    broken_text = """                                    Text(
                                        if (currentQuestionIndex == displayQuestions.size - 1) {
                                            "Completed"
             } else {
                                            "Next"
                                                )"""
    fixed_text = """                                    Text(
                                        if (currentQuestionIndex == displayQuestions.size - 1) {
                                            "Completed"
                                        } else {
                                            "Next"
                                        }
                                    )"""
                                    
    content = content.replace(broken_text, fixed_text)
    
    with open(filepath, "w") as f:
        f.write(content)

fix("app/src/main/java/com/example/ui/screens/PracticeScreen.kt")
fix("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt")
