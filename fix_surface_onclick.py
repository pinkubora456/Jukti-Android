import re

def fix(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    bad = """                                    onClick = {
                                        if (!isSubmitted) {
                                            selectedOptionIndex = index
                                            isSubmitted = true
                                            val isAnsCorrect = (index == currentQuestion.correctOptionIndex)
                                            userAnswers = userAnswers + (currentQuestion.id to index)
                                            if (isAnsCorrect) {
                                                viewModel.awardCorrectAnswerXp()
                                            }
                                    },
                                    shape = RoundedCornerShape(12.dp),"""
                                    
    good = """                                    onClick = {
                                        if (!isSubmitted) {
                                            val isAnsCorrect = (index == currentQuestion.correctOptionIndex)
                                            userAnswers[currentQuestion.id] = index
                                            if (isAnsCorrect) {
                                                viewModel.awardCorrectAnswerXp()
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),"""
                                    
    content = content.replace(bad, good)
    
    # Let me also try another pattern if the above is slightly different
    bad2 = """                                    onClick = {
                                        if (!isSubmitted) {
                                            selectedOptionIndex = index
                                            isSubmitted = true
                                            val isAnsCorrect = (index == currentQuestion.correctOptionIndex)
                                            userAnswers = userAnswers + (currentQuestion.id to index)
                                            if (isAnsCorrect) {
                                                viewModel.awardCorrectAnswerXp()
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),"""
    content = content.replace(bad2, good)

    with open(filepath, 'w') as f:
        f.write(content)

fix("app/src/main/java/com/example/ui/screens/PracticeScreen.kt")
