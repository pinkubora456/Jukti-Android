import re

with open('app/src/main/java/com/example/ui/screens/AllQuestionsScreen.kt', 'r') as f:
    content = f.read()

# Fix the end of the AllQuestionsScreen function
# We will match from where questionToDelete is handled, to the end of the function, and replace it completely.
start_idx = content.find('    if (questionToDelete != null) {')
end_idx = content.find('fun BulkMoveQuestionsDialog(')

if start_idx != -1 and end_idx != -1:
    correct_bottom = """    if (questionToDelete != null) {
        AlertDialog(
            onDismissRequest = { questionToDelete = null },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete this question?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteQuestion(questionToDelete!!)
                    questionToDelete = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { questionToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showBulkDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showBulkDeleteConfirm = false },
            title = { Text("Confirm Bulk Delete") },
            text = { Text("Are you sure you want to delete ${selectedQuestionIds.size} selected questions? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        val toDelete = questions.filter { it.id in selectedQuestionIds }
                        viewModel.deleteQuestions(toDelete)
                        selectedQuestionIds = emptySet()
                        showBulkDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBulkDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    questionToToggleAccess?.let { targetQuestion ->
        val targetToPremium = !targetQuestion.isPremium
        AlertDialog(
            onDismissRequest = { questionToToggleAccess = null },
            title = {
                Text(if (targetToPremium) "Change Question to Premium?" else "Make Question Free?")
            },
            text = {
                Text(
                    if (targetToPremium)
                        "This question will become available only to Premium users."
                    else
                        "This question will become available to all users."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setQuestionAccessType(targetQuestion, targetToPremium) { _, msg ->
                            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                        }
                        questionToToggleAccess = null
                    }
                ) {
                    Text(if (targetToPremium) "Make Premium" else "Make Free")
                }
            },
            dismissButton = {
                TextButton(onClick = { questionToToggleAccess = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

"""
    # Find the last @OptIn that comes before BulkMoveQuestionsDialog
    opt_in_idx = content.rfind('@OptIn', start_idx, end_idx)
    if opt_in_idx != -1:
        end_idx = opt_in_idx

    content = content[:start_idx] + correct_bottom + content[end_idx:]

# Also fix the text in BulkMoveQuestionsDialog which caused string literal error
content = content.replace('Text("Update $selectedCount Questions?   652', 'Text("Update $selectedCount Questions?\\n\\nSubject: $destSubj\\nChapter: $destChap\\n\\nThese changes will be applied to all $selectedCount selected questions.")')
# Actually, let's just replace the whole text block in BulkMoveQuestionsDialog that is broken
broken_text = """                Text("Update $selectedCount Questions?   652	   653	Subject: $destSubj   654	Chapter: $destChap   655	   656	These changes will be applied to all $selectedCount selected questions.")"""
fixed_text = """                Text("Update $selectedCount Questions?\\n\\nSubject: $destSubj\\nChapter: $destChap\\n\\nThese changes will be applied to all $selectedCount selected questions.")"""
content = content.replace(broken_text, fixed_text)

with open('app/src/main/java/com/example/ui/screens/AllQuestionsScreen.kt', 'w') as f:
    f.write(content)
