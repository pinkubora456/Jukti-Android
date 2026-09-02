import re

def fix(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    good_block = """if (!isSubmitted) {
                        Button(
                            onClick = {
                                if (selectedOptionIndex != null) {
                                    isSubmitted = true
                                    val isAnsCorrect = (selectedOptionIndex == currentQuestion?.correctOptionIndex)
                                    if (isAnsCorrect) {
                                        scoreCount += 10
                                    }
                                    if (currentQuestion != null) {
                                        viewModel.submitQuestionAnswer(currentQuestion.id, isAnsCorrect, 10)
                                    }
                                }
                            },
                            enabled = selectedOptionIndex != null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Submit Answer")
                        }
                    } else {
                        Button(
                            onClick = {
                                if (currentQuestionIndex < activeQuestions.size - 1) {
                                    currentQuestionIndex++
                                    selectedOptionIndex = null
                                    isSubmitted = false
                                } else {
                                    viewModel.awardChapterCompletionXp()
                                    viewModel.navigateTo(Screen.HOME)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (currentQuestionIndex == activeQuestions.size - 1) "Finish Practice" else "Next Question")
                        }
                    }"""

    start_str = "if (!isSubmitted) {"
    end_str = "Text(if (currentQuestionIndex == activeQuestions.size - 1) (\"Finish Practice\") else (\"Next Question\"))\n                        }\n                    }"
    
    start_idx = content.find(start_str)
    end_idx = content.find(end_str, start_idx)
    
    if start_idx != -1 and end_idx != -1:
        content = content[:start_idx] + good_block + content[end_idx + len(end_str):]
        with open(filepath, 'w') as f:
            f.write(content)
        print("Replaced bad block in McqStudyScreen")
        
fix("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt")
