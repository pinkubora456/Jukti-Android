import re

with open('app/src/main/java/com/example/ui/screens/SingleQuestionUploadScreen.kt', 'r') as f:
    content = f.read()

# Add editingQuestion state
content = content.replace('    var subject by remember { mutableStateOf("") }', '''    val editingQuestion by viewModel.editingQuestionForUpload.collectAsState()
    
    var subject by remember { mutableStateOf("") }''')

# Modify Back navigation to clear state
content = content.replace('onBackClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_QBANK) }', 
    'onBackClick = { viewModel.startEditingQuestion(null); viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_QBANK) }')

# Change top app bar title based on edit mode
content = content.replace('title = "Manual Entry",', 'title = if (editingQuestion != null) "Edit Question" else "Manual Entry",')

# Modify "Deploy Question" button
content = content.replace('Text("Deploying...")', 'Text(if (editingQuestion != null) "Updating..." else "Deploying...")')
content = content.replace('Text("Deploy Question")', 'Text(if (editingQuestion != null) "Update Question" else "Deploy Question")')

# Add LaunchedEffect to populate values
launched_effect = """
    LaunchedEffect(editingQuestion) {
        editingQuestion?.let { q ->
            subject = q.subject
            chapter = q.topic
            difficulty = q.difficulty
            questionFor = if (q.isPremium) "Premium" else "Free"
            
            if (q.questionType.startsWith("PYQ", ignoreCase = true)) {
                questionTag = "PYQ"
                val pyqParts = q.questionType.replace("PYQ - ", "").split(" ")
                if (pyqParts.size >= 2 && pyqParts.last().toIntOrNull() != null) {
                    pyqYear = pyqParts.last()
                    pyqExamName = pyqParts.dropLast(1).joinToString(" ")
                } else {
                    pyqExamName = q.questionType.replace("PYQ - ", "")
                    pyqYear = ""
                }
            } else {
                questionTag = q.questionType.ifBlank { "Expected" }
            }
            
            questionEnglish = q.questionEn
            questionAssamese = q.questionAs
            
            optionAEnglish = q.optionAEn
            optionBEnglish = q.optionBEn
            optionCEnglish = q.optionCEn
            optionDEnglish = q.optionDEn
            
            optionAAssamese = q.optionAAs
            optionBAssamese = q.optionBAs
            optionCAssamese = q.optionCAs
            optionDAssamese = q.optionDAs
            
            correctOption = when(q.correctOptionIndex) {
                0 -> "A"
                1 -> "B"
                2 -> "C"
                3 -> "D"
                else -> "A"
            }
            
            explanationEnglish = q.explanationEn
            explanationAssamese = q.explanationAs
            
            selectedExams.clear()
            if (q.examCategory.isNotBlank()) {
                selectedExams.addAll(q.examCategory.split(",").map { it.trim() })
            }
        }
    }
"""
content = content.replace('    var duplicateError by remember { mutableStateOf<com.example.data.local.QuestionEntity?>(null) }', 
    '    var duplicateError by remember { mutableStateOf<com.example.data.local.QuestionEntity?>(null) }\n' + launched_effect)

# Modify Save logic
save_logic_old = '''                                val newQuestion = QuestionEntity(
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
                                    examCategory = selectedExams.joinToString(", "),
                                    isPremium = questionFor.equals("Premium", ignoreCase = true),
                                    questionType = finalQuestionTag,
                                    duplicateKey = duplicateKey
                                )
                                
                                viewModel.addQuestion(newQuestion) {
                                    isDeploying = false
                                    android.widget.Toast.makeText(context, "Question deployed successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                    
                                    // Reset fields
                                    questionEnglish = ""
                                    questionAssamese = ""
                                    optionAEnglish = ""
                                    optionBEnglish = ""
                                    optionCEnglish = ""
                                    optionDEnglish = ""
                                    optionAAssamese = ""
                                    optionBAssamese = ""
                                    optionCAssamese = ""
                                    optionDAssamese = ""
                                    explanationEnglish = ""
                                    explanationAssamese = ""
                                    // Keep context like subject, chapter, exam intact for fast subsequent inserts
                                }'''

save_logic_new = '''                                val newQuestion = QuestionEntity(
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
                                    examCategory = selectedExams.joinToString(", "),
                                    isPremium = questionFor.equals("Premium", ignoreCase = true),
                                    questionType = finalQuestionTag,
                                    duplicateKey = if (editingQuestion == null) duplicateKey else editingQuestion!!.duplicateKey,
                                    status = editingQuestion?.status ?: "ACTIVE"
                                )
                                
                                if (editingQuestion != null) {
                                    viewModel.updateQuestion(newQuestion)
                                    isDeploying = false
                                    android.widget.Toast.makeText(context, "Question updated successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                    viewModel.startEditingQuestion(null)
                                    viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_QBANK)
                                } else {
                                    viewModel.addQuestion(newQuestion) {
                                        isDeploying = false
                                        android.widget.Toast.makeText(context, "Question deployed successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                        
                                        // Reset fields
                                        questionEnglish = ""
                                        questionAssamese = ""
                                        optionAEnglish = ""
                                        optionBEnglish = ""
                                        optionCEnglish = ""
                                        optionDEnglish = ""
                                        optionAAssamese = ""
                                        optionBAssamese = ""
                                        optionCAssamese = ""
                                        optionDAssamese = ""
                                        explanationEnglish = ""
                                        explanationAssamese = ""
                                        // Keep context like subject, chapter, exam intact for fast subsequent inserts
                                    }
                                }'''

content = content.replace(save_logic_old, save_logic_new)

with open('app/src/main/java/com/example/ui/screens/SingleQuestionUploadScreen.kt', 'w') as f:
    f.write(content)
