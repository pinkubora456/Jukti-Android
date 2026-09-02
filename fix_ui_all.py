import re

def fix():
    with open("app/src/main/java/com/example/ui/screens/AllQuestionsScreen.kt", "r") as f:
        content = f.read()
        
    old = """        BulkMoveQuestionsDialog(
            viewModel = viewModel,
            selectedCount = selectedQuestionIds.size,
            onDismiss = { showMoveDialog = false },
            onConfirm = { destExam, destSubj, destChap ->
                viewModel.bulkMoveQuestions(
                    questionIds = selectedQuestionIds.toList(),
                    targetExam = destExam,
                    targetSubject = destSubj,
                    targetChapter = destChap
                ) { success, _ ->"""
                
    new = """        BulkMoveQuestionsDialog(
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
                ) { success, _ ->"""
                
    if old in content:
        content = content.replace(old, new)
        with open("app/src/main/java/com/example/ui/screens/AllQuestionsScreen.kt", "w") as f:
            f.write(content)
        print("Fixed UI")
    else:
        print("Old not found in UI")

fix()
