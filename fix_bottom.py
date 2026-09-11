with open('app/src/main/java/com/example/ui/screens/SingleQuestionUploadScreen.kt', 'r') as f:
    content = f.read()

bad_chunk = """                                if (editingQuestion != null) {
                                    viewModel.updateQuestion(newQuestion)
                                    isDeploying = false
                                    android.widget.Toast.makeText(context, "Question updated successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                    viewModel.startEditingQuestion(null)
                                    viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_QBANK)
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
                            android.widget.Toast.makeText(context, "Please fill in all required fields", android.widget.Toast.LENGTH_SHORT).show()
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    enabled = !isDeploying
                ) {
                    if (isDeploying) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (editingQuestion != null) "Updating..." else "Deploying...")
                        Text(if (editingQuestion != null) "Update Question" else "Deploy Question")"""

good_chunk = """                                if (editingQuestion != null) {
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
                                }
                            } else {
                                android.widget.Toast.makeText(context, "Please fill in all required fields", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    enabled = !isDeploying
                ) {
                    if (isDeploying) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (editingQuestion != null) "Updating..." else "Deploying...")
                    } else {
                        Text(if (editingQuestion != null) "Update Question" else "Deploy Question")
                    }
                }
            }
        }
    }"""

if bad_chunk in content:
    content = content.replace(bad_chunk, good_chunk)
    print("Replaced!")
else:
    print("Could not find chunk")

with open('app/src/main/java/com/example/ui/screens/SingleQuestionUploadScreen.kt', 'w') as f:
    f.write(content)
