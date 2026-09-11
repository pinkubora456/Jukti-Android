import re

with open('app/src/main/java/com/example/ui/screens/PracticeScreen.kt', 'r') as f:
    content = f.read()

replacement = """                                        onClick = {
                                            if (userAnswers[currentQuestion.id] == null) {
                                                val isAnsCorrect = (index == currentQuestion.correctOptionIndex)
                                                userAnswers[currentQuestion.id] = index
                                                viewModel.submitQuestionAnswer(currentQuestion.id, isAnsCorrect, 15)
                                            }
                                        },"""
content = re.sub(r'                                        onClick = \{\n                                            if \(\!isSubmitted\) \{\n                                                val isAnsCorrect = \(index == currentQuestion\.correctOptionIndex\)\n                                                userAnswers\[currentQuestion\.id\] = index\n                                                viewModel\.submitQuestionAnswer\(currentQuestion\.id, isAnsCorrect, 15\)\n                                            \}\n                                        \},', replacement, content)

with open('app/src/main/java/com/example/ui/screens/PracticeScreen.kt', 'w') as f:
    f.write(content)

