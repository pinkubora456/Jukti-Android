import re

def fix(filepath):
    with open(filepath, 'r') as f:
        content = f.read()
        
    start_str = "// Options List"
    end_str = "// Explanation Card"
    
    start_idx = content.find(start_str)
    end_idx = content.find(end_str, start_idx)
    
    if start_idx != -1 and end_idx != -1:
        new_block = """// Options List
                            val options = listOf(
                                currentQuestion.optionAEn to currentQuestion.optionAAs,
                                currentQuestion.optionBEn to currentQuestion.optionBAs,
                                currentQuestion.optionCEn to currentQuestion.optionCAs,
                                currentQuestion.optionDEn to currentQuestion.optionDAs
                            )

                            options.forEachIndexed { index, pair ->
                                val isSelected = (selectedOptionIndex == index)
                                val isCorrect = (index == currentQuestion.correctOptionIndex)
                                val optionLetter = ('A' + index).toString()

                                val backgroundColor = when {
                                    isSubmitted && isCorrect -> MaterialTheme.colorScheme.successContainer
                                    isSubmitted && isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }

                                val borderColor = when {
                                    isSubmitted && isCorrect -> MaterialTheme.colorScheme.success
                                    isSubmitted && isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                }

                                Surface(
                                    onClick = {
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
                                    shape = RoundedCornerShape(12.dp),
                                    color = backgroundColor,
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = borderColor
                                    ),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = when {
                                                isSubmitted && isCorrect -> MaterialTheme.colorScheme.success
                                                isSubmitted && isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                                                else -> MaterialTheme.colorScheme.primaryContainer
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                if (isSubmitted && isCorrect) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                } else if (isSubmitted && isSelected && !isCorrect) {
                                                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                } else {
                                                    Text(
                                                        text = optionLetter,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))

                                        BilingualText(
                                            textEn = pair.first,
                                            textAs = pair.second,
                                            language = questionLanguage,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSubmitted && isCorrect) FontWeight.Bold else FontWeight.Normal,
                                            color = when {
                                                isSubmitted && isCorrect -> MaterialTheme.colorScheme.onSuccessContainer
                                                isSubmitted && isSelected && !isCorrect -> MaterialTheme.colorScheme.onErrorContainer
                                                else -> MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                    }
                                }
                            }

                            """
        content = content[:start_idx] + new_block + content[end_idx:]
        with open(filepath, 'w') as f:
            f.write(content)
        print("Replaced options list")

fix("app/src/main/java/com/example/ui/screens/PracticeScreen.kt")
