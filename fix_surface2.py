with open("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "r") as f:
    content = f.read()

broken_str = """                                    color = when {
                                        isCorrect -> MaterialTheme.colorScheme.success
                                        isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                                        else -> androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
                                    }
                            }
        ,
                                    modifier = Modifier.size(30.dp)
                                ) {"""

fixed_str = """                                    color = when {
                                        isCorrect -> MaterialTheme.colorScheme.success
                                        isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                                        else -> androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
                                    },
                                    modifier = Modifier.size(30.dp)
                                ) {"""

if broken_str in content:
    content = content.replace(broken_str, fixed_str)
    with open("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "w") as f:
        f.write(content)
    print("Fixed surface 1")
else:
    print("Not found 1")

broken_str2 = """                                        color = when {
                                            isCorrect -> MaterialTheme.colorScheme.successContainer
                                            isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer
                                            else -> MaterialTheme.colorScheme.surface
                                        }
                                ,
                                        border = BorderStroke(1.dp, when {
                                            isCorrect -> MaterialTheme.colorScheme.success
                                            isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                                            else -> MaterialTheme.colorScheme.outlineVariant
                                        }
                                ),"""
fixed_str2 = """                                        color = when {
                                            isCorrect -> MaterialTheme.colorScheme.successContainer
                                            isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer
                                            else -> MaterialTheme.colorScheme.surface
                                        },
                                        border = BorderStroke(1.dp, when {
                                            isCorrect -> MaterialTheme.colorScheme.success
                                            isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                                            else -> MaterialTheme.colorScheme.outlineVariant
                                        }),"""
if broken_str2 in content:
    content = content.replace(broken_str2, fixed_str2)
    with open("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "w") as f:
        f.write(content)
    print("Fixed surface 2")
else:
    print("Not found 2")
