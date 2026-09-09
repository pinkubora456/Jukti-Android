import re

with open('app/src/main/java/com/example/ui/screens/MockQuestionsScreen.kt', 'r') as f:
    content = f.read()

old_call = """onConfirm = { destExam, destSubj, destChap ->
                val selectedQs = questions.filter { it.id in selectedQuestionIds }
                viewModel.bulkMoveQuestions(
                    questionsToUpdate = selectedQs,
                    targetExam = destExam,
                    targetSubject = destSubj,
                    targetChapter = destChap
                )"""

new_call = """onConfirm = { destSubj, destChap ->
                val selectedQs = questions.filter { it.id in selectedQuestionIds }
                viewModel.bulkMoveQuestions(
                    questionsToUpdate = selectedQs,
                    targetSubject = destSubj,
                    targetChapter = destChap
                )"""

content = content.replace(old_call, new_call)

with open('app/src/main/java/com/example/ui/screens/MockQuestionsScreen.kt', 'w') as f:
    f.write(content)
