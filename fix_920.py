import re

def fix(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # The broken block:
    broken = """                                        Surface(
                                            shape = CircleShape,
                                            color = when {
                                                isSubmitted && isCorrect -> MaterialTheme.colorScheme.success
                                                isSubmitted && isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                                                else -> androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
                                            }
                                        },
                                            modifier = Modifier.size(28.dp)
                                        ) {"""
                                        
    fixed = """                                        Surface(
                                            shape = CircleShape,
                                            color = when {
                                                isSubmitted && isCorrect -> MaterialTheme.colorScheme.success
                                                isSubmitted && isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                                                else -> androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {"""
                                        
    content = content.replace(broken, fixed)
    with open(filepath, 'w') as f:
        f.write(content)
        
fix("app/src/main/java/com/example/ui/screens/PracticeScreen.kt")
