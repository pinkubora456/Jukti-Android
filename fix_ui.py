import re

# Fix PracticeScreen.kt
with open('app/src/main/java/com/example/ui/screens/PracticeScreen.kt', 'r') as f:
    content = f.read()

content = re.sub(r'                                                if \(isAnsCorrect\) \{\n                                                    viewModel\.awardCorrectAnswerXp\(\)\n                                                \}', '', content)

with open('app/src/main/java/com/example/ui/screens/PracticeScreen.kt', 'w') as f:
    f.write(content)

# Fix McqStudyScreen.kt
with open('app/src/main/java/com/example/ui/screens/McqStudyScreen.kt', 'r') as f:
    content = f.read()

content = re.sub(r'                                        if \(isCorrect\) \{\n                                        viewModel\.awardCorrectAnswerXp\(\)\n                                        \}', '', content)
content = re.sub(r'                                        if \(isCorrect\) \{\n                                            viewModel\.awardCorrectAnswerXp\(\)\n                                        \}', '', content)

with open('app/src/main/java/com/example/ui/screens/McqStudyScreen.kt', 'w') as f:
    f.write(content)

