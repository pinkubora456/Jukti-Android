with open("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "r") as f:
    content = f.read()

import re

# We want to find the broken Surface call and replace it.
# The broken block is:
broken_str = """                                    Surface(
                                        shape = CircleShape,
                                        color = when {
                                            isCorrect -> MaterialTheme.colorScheme.primary
                                            isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                                            else -> androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
                                        }
                            }
        ,
                                    modifier = Modifier.size(30.dp)
                                ) {"""

fixed_str = """                                    Surface(
                                        shape = CircleShape,
                                        color = when {
                                            isCorrect -> MaterialTheme.colorScheme.primary
                                            isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                                            else -> androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
                                        },
                                        modifier = Modifier.size(30.dp)
                                    ) {"""

content = content.replace(broken_str, fixed_str)

with open("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "w") as f:
    f.write(content)

