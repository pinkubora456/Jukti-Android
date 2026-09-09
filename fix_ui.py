import re

# Fix AllQuestionsScreen
with open('app/src/main/java/com/example/ui/screens/AllQuestionsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('Icon(androidx.compose.ui.res.vectorResource(id = android.R.drawable.ic_menu_revert),', 'Icon(Icons.Default.ArrowForward,')

with open('app/src/main/java/com/example/ui/screens/AllQuestionsScreen.kt', 'w') as f:
    f.write(content)


# Fix SingleQuestionUploadScreen
with open('app/src/main/java/com/example/ui/screens/SingleQuestionUploadScreen.kt', 'r') as f:
    content = f.read()

old_code = """                                val newQuestion = QuestionEntity(
                                    id = editingQuestion?.id ?: 0L,
                                    createdAt = editingQuestion?.createdAt ?: System.currentTimeMillis(),
                                    subject = normSubject,
                                    topic = normChapter,
                                    difficulty = difficulty,
                                    questionEn = questionEnglish.trim(),
                                    questionAs = questionAssamese.trim(),
                                    optionAEn = optionAEnglish.trim(),
                                    optionBEn = optionBEnglish.trim(),
                                    optionCEn = optionCEnglish.trim(),
                                    optionDEn = optionDEnglish.trim(),
                                    optionAAs = optionAAssamese.trim(),
                                    optionBAs = optionBAssamese.trim(),
                                    optionCAs = optionCAssamese.trim(),
                                    optionDAs = optionDAssamese.trim(),
                                    correctOptionIndex = correctOptionIndex,
                                    explanationEn = explanationEnglish.trim(),
                                    explanationAs = explanationAssamese.trim(),
                                    examCategory = examCategory,
                                    isPremium = isPremium,
                                    accessType = if (isPremium) "PREMIUM" else "FREE",
                                    questionType = qTypeFinal,
                                    duplicateKey = editingQuestion?.duplicateKey ?: ""
                                )"""

new_code = """                                val newQuestion = if (editingQuestion != null) {
                                    editingQuestion.copy(
                                        subject = normSubject,
                                        topic = normChapter,
                                        difficulty = difficulty,
                                        questionEn = questionEnglish.trim(),
                                        questionAs = questionAssamese.trim(),
                                        optionAEn = optionAEnglish.trim(),
                                        optionBEn = optionBEnglish.trim(),
                                        optionCEn = optionCEnglish.trim(),
                                        optionDEn = optionDEnglish.trim(),
                                        optionAAs = optionAAssamese.trim(),
                                        optionBAs = optionBAssamese.trim(),
                                        optionCAs = optionCAssamese.trim(),
                                        optionDAs = optionDAssamese.trim(),
                                        correctOptionIndex = correctOptionIndex,
                                        explanationEn = explanationEnglish.trim(),
                                        explanationAs = explanationAssamese.trim(),
                                        examCategory = examCategory,
                                        isPremium = isPremium,
                                        accessType = if (isPremium) "PREMIUM" else "FREE",
                                        questionType = qTypeFinal,
                                        updatedAt = System.currentTimeMillis()
                                    )
                                } else {
                                    QuestionEntity(
                                        subject = normSubject,
                                        topic = normChapter,
                                        difficulty = difficulty,
                                        questionEn = questionEnglish.trim(),
                                        questionAs = questionAssamese.trim(),
                                        optionAEn = optionAEnglish.trim(),
                                        optionBEn = optionBEnglish.trim(),
                                        optionCEn = optionCEnglish.trim(),
                                        optionDEn = optionDEnglish.trim(),
                                        optionAAs = optionAAssamese.trim(),
                                        optionBAs = optionBAssamese.trim(),
                                        optionCAs = optionCAssamese.trim(),
                                        optionDAs = optionDAssamese.trim(),
                                        correctOptionIndex = correctOptionIndex,
                                        explanationEn = explanationEnglish.trim(),
                                        explanationAs = explanationAssamese.trim(),
                                        examCategory = examCategory,
                                        isPremium = isPremium,
                                        accessType = if (isPremium) "PREMIUM" else "FREE",
                                        questionType = qTypeFinal,
                                        updatedAt = System.currentTimeMillis()
                                    )
                                }"""

content = content.replace(old_code, new_code)

with open('app/src/main/java/com/example/ui/screens/SingleQuestionUploadScreen.kt', 'w') as f:
    f.write(content)

