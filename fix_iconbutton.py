import re

def fix(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    bad = """                                    IconButton(
                                        onClick = {
                                            viewModel.toggleHideQuestion(currentQuestion)
                                            activeSessionQuestions = activeSessionQuestions.filter { it.id != currentQuestion.id }
                                            if (currentQuestionIndex >= displayQuestions.size - 1 && currentQuestionIndex > 0) {
                                                currentQuestionIndex--
                   
                                            }
                                        }
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {"""
                                    
    good = """                                    IconButton(
                                        onClick = {
                                            viewModel.toggleHideQuestion(currentQuestion)
                                            activeSessionQuestions = activeSessionQuestions.filter { it.id != currentQuestion.id }
                                            if (currentQuestionIndex >= displayQuestions.size - 1 && currentQuestionIndex > 0) {
                                                currentQuestionIndex--
                                            }
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {"""
                                    
    content = content.replace(bad, good)
    
    with open(filepath, 'w') as f:
        f.write(content)
        
fix("app/src/main/java/com/example/ui/screens/PracticeScreen.kt")
