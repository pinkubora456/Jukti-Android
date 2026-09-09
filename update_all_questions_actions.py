import re

with open('app/src/main/java/com/example/ui/screens/AllQuestionsScreen.kt', 'r') as f:
    content = f.read()

# Remove showBulkMakeFreeConfirm, showBulkMakePremiumConfirm
content = re.sub(r'    var showBulkMakeFreeConfirm by remember \{ mutableStateOf\(false\) \}\n', '', content)
content = re.sub(r'    var showBulkMakePremiumConfirm by remember \{ mutableStateOf\(false\) \}\n', '', content)

# Add showBulkEditDialog
content = re.sub(r'    var showBulkDeleteConfirm by remember \{ mutableStateOf\(false\) \}\n',
                 '    var showBulkDeleteConfirm by remember { mutableStateOf(false) }\n    var showBulkEditDialog by remember { mutableStateOf(false) }\n', content)

# Update action row buttons
action_buttons = """                if (selectedQuestionIds.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = {
                                showBulkDeleteConfirm = true
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        OutlinedButton(
                            onClick = { showMoveDialog = true },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(androidx.compose.ui.res.vectorResource(id = android.R.drawable.ic_menu_revert), contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        OutlinedButton(
                            onClick = { showBulkEditDialog = true },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }"""

old_action_buttons = r'                if \(selectedQuestionIds\.isNotEmpty\(\)\) \{.*?Row\(horizontalArrangement = Arrangement\.spacedBy\(6\.dp\).*?Button\(\s*onClick = \{ showMoveDialog = true \}.*?\n\s*\}\n\s*\}\n\s*\}'
content = re.sub(old_action_buttons, action_buttons, content, flags=re.MULTILINE | re.DOTALL)

# Add import for BulkEditQuestionsDialog
if "import com.example.ui.components.BulkEditQuestionsDialog" not in content:
    content = content.replace("import com.example.ui.components.EditQuestionDialog", "import com.example.ui.components.BulkEditQuestionsDialog\nimport androidx.compose.ui.res.vectorResource")

# Replace EditQuestionDialog block with startEditingQuestion call in the icon click, not below it
content = re.sub(r'    editingQuestion\?\.let \{ question ->.*?    \}', '', content, flags=re.MULTILINE | re.DOTALL)

content = content.replace('onClick = { editingQuestion = question }', 'onClick = { viewModel.startEditingQuestion(question) }')

# Add BulkEditQuestionsDialog instantiation
bulk_edit_call = """
    if (showBulkEditDialog) {
        val examsList by viewModel.examsList.collectAsState()
        BulkEditQuestionsDialog(
            selectedCount = selectedQuestionIds.size,
            examsList = examsList.map { it.title }.distinct(),
            onDismiss = { showBulkEditDialog = false },
            onConfirm = { exam, access, questionType, pyqExamName, tags, difficulty ->
                val selectedQs = questions.filter { it.id in selectedQuestionIds }
                viewModel.bulkEditQuestions(
                    questionsToUpdate = selectedQs,
                    targetExam = exam,
                    targetAccess = access,
                    targetQuestionType = questionType,
                    targetPyqExamName = pyqExamName,
                    targetTags = tags,
                    targetDifficulty = difficulty
                ) { success, _ ->
                    if (success) {
                        selectedQuestionIds = emptySet()
                        showBulkEditDialog = false
                    }
                }
            }
        )
    }
"""

content = content.replace('    if (questionToDelete != null) {', bulk_edit_call + '\n    if (questionToDelete != null) {')

# Remove the old Bulk Make Free/Premium confirm dialogs
content = re.sub(r'    if \(showBulkMakeFreeConfirm\) \{.*?    \}\n', '', content, flags=re.MULTILINE | re.DOTALL)
content = re.sub(r'    if \(showBulkMakePremiumConfirm\) \{.*?    \}\n', '', content, flags=re.MULTILINE | re.DOTALL)


with open('app/src/main/java/com/example/ui/screens/AllQuestionsScreen.kt', 'w') as f:
    f.write(content)
