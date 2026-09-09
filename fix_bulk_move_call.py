import re

with open('app/src/main/java/com/example/ui/screens/AllQuestionsScreen.kt', 'r') as f:
    content = f.read()

old_call = """if (showMoveDialog) {
        BulkMoveQuestionsDialog(
            viewModel = viewModel,
            selectedCount = selectedQuestionIds.size,
            onDismiss = { showMoveDialog = false },
            onConfirm = { destExam, destSubj, destChap ->
                val selectedQs = questions.filter { it.id in selectedQuestionIds }
                viewModel.bulkMoveQuestions(
                    questionsToUpdate = selectedQs,
                    targetExam = destExam,
                    targetSubject = destSubj,
                    targetChapter = destChap
                ) { success, _ ->
                    if (success) {
                        selectedQuestionIds = emptySet()
                        showMoveDialog = false
                    }
                }
            }
        )
    }"""

new_call = """if (showMoveDialog) {
        BulkMoveQuestionsDialog(
            viewModel = viewModel,
            selectedCount = selectedQuestionIds.size,
            onDismiss = { showMoveDialog = false },
            onConfirm = { destSubj, destChap ->
                val selectedQs = questions.filter { it.id in selectedQuestionIds }
                viewModel.bulkMoveQuestions(
                    questionsToUpdate = selectedQs,
                    targetSubject = destSubj,
                    targetChapter = destChap
                ) { success, _ ->
                    if (success) {
                        selectedQuestionIds = emptySet()
                        showMoveDialog = false
                    }
                }
            }
        )
    }"""

content = content.replace(old_call, new_call)

with open('app/src/main/java/com/example/ui/screens/AllQuestionsScreen.kt', 'w') as f:
    f.write(content)
