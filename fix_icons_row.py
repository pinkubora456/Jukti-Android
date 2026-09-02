import re

def fix(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    bad = """                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Report Button
                                    IconButton(
                                        onClick = { showReportDialog = true },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Report,
                                            contentDescription = "Report Question",
                                            tint = MaterialTheme.colorScheme.error
                                        )
           
                                    // Save / Bookmark Button
                                    }
                                    IconButton(
                                        onClick = { viewModel.toggleBookmarkQuestion(currentQuestion) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (currentQuestion.id in bookmarkedIds) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                            contentDescription = "Save Question",
                                            tint = if (currentQuestion.id in bookmarkedIds) androidx.compose.material3.MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
           

                                    // Hide Question Button
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.toggleHideQuestion(currentQuestion)
                                            activeSessionQuestions = activeSessionQuestions.filter { it.id != currentQuestion.id }
                                            if (currentQuestionIndex >= displayQuestions.size - 1 && currentQuestionIndex > 0) {
                                                currentQuestionIndex--
                                            }
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VisibilityOff,
                                            contentDescription = "Hide Question",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
           
   
                                    }
                            }"""
                            
    good = """                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Report Button
                                    IconButton(
                                        onClick = { showReportDialog = true },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Report,
                                            contentDescription = "Report Question",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                    // Save / Bookmark Button
                                    IconButton(
                                        onClick = { viewModel.toggleBookmarkQuestion(currentQuestion) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (currentQuestion.id in bookmarkedIds) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                            contentDescription = "Save Question",
                                            tint = if (currentQuestion.id in bookmarkedIds) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    // Hide Question Button
                                    IconButton(
                                        onClick = {
                                            viewModel.toggleHideQuestion(currentQuestion)
                                            activeSessionQuestions = activeSessionQuestions.filter { it.id != currentQuestion.id }
                                            if (currentQuestionIndex >= displayQuestions.size - 1 && currentQuestionIndex > 0) {
                                                currentQuestionIndex--
                                            }
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VisibilityOff,
                                            contentDescription = "Hide Question",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }"""
    content = content.replace(bad, good)
    
    with open(filepath, 'w') as f:
        f.write(content)
        
fix("app/src/main/java/com/example/ui/screens/PracticeScreen.kt")
