import re

with open('app/src/main/java/com/example/ui/screens/AllQuestionsScreen.kt', 'r') as f:
    content = f.read()

old_edit_btn = """                        OutlinedButton(
                            onClick = { showBulkEditDialog = true },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        }"""

new_edit_btn = """                        OutlinedButton(
                            onClick = { 
                                if (selectedQuestionIds.size == 1) {
                                    val q = questions.find { it.id == selectedQuestionIds.first() }
                                    viewModel.startEditingQuestion(q)
                                    selectedQuestionIds = emptySet()
                                } else {
                                    showBulkEditDialog = true 
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        }"""

content = content.replace(old_edit_btn, new_edit_btn)

with open('app/src/main/java/com/example/ui/screens/AllQuestionsScreen.kt', 'w') as f:
    f.write(content)
