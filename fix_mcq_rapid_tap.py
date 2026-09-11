import re

with open('app/src/main/java/com/example/ui/screens/McqStudyScreen.kt', 'r') as f:
    content = f.read()

replacement = """                                onClick = {
                                    if (userAnswers[currentQuestion.id] == null) {
                                        userAnswers[currentQuestion.id] = index
                                        val isCorrect = (index == currentQuestion.correctOptionIndex)
                                        viewModel.submitQuestionAnswer(currentQuestion.id, isCorrect)
                                    }
                                }"""
content = re.sub(r'                                onClick = \{\n                                    if \(!isSubmitted\) \{\n                                        userAnswers\[currentQuestion\.id\] = index\n                                        val isCorrect = \(index == currentQuestion\.correctOptionIndex\)\n                                        viewModel\.submitQuestionAnswer\(currentQuestion\.id, isCorrect\)\n                                    \}\n                                \}', replacement, content)

with open('app/src/main/java/com/example/ui/screens/McqStudyScreen.kt', 'w') as f:
    f.write(content)

