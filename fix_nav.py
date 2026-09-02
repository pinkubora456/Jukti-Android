import re

def fix(filepath):
    with open(filepath, "r") as f:
        content = f.read()

    # The broken row starts around "Navigation Buttons"
    start_idx = content.find("// Navigation Buttons")
    end_idx = content.find("if (showReportDialog && currentQuestion != null) {", start_idx)
    
    if start_idx == -1 or end_idx == -1:
        print("not found")
        return

    replacement = """// Navigation Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        if (currentQuestionIndex > 0) {
                                            currentQuestionIndex--
                                        }
                                    },
                                    enabled = currentQuestionIndex > 0
                                ) {
                                    Icon(Icons.Default.ChevronLeft, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Previous")
                                }

                                Button(
                                    onClick = {
                                        if (currentQuestionIndex < displayQuestions.size - 1) {
                                            currentQuestionIndex++
                                        } else {
                                            viewModel.awardChapterCompletionXp()
                                            showSummary = true
                                        }
                                    }
                                ) {
                                    Text(
                                        if (currentQuestionIndex == displayQuestions.size - 1) {
                                            "Completed"
                                        } else {
                                            "Next"
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            TextButton(
                                onClick = { showSummary = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("End Practice", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
        
        """
        
    new_content = content[:start_idx] + replacement + content[end_idx:]

    with open(filepath, "w") as f:
        f.write(new_content)

fix("app/src/main/java/com/example/ui/screens/PracticeScreen.kt")
