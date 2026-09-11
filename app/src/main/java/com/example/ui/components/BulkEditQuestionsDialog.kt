package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkEditQuestionsDialog(
    selectedCount: Int,
    examsList: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (
        exam: String?,
        access: String?,
        questionType: String?,
        
        tags: String?,
        difficulty: String?
    ) -> Unit
) {
    var exam by remember { mutableStateOf<String?>("Don't Change") }
    var examExpanded by remember { mutableStateOf(false) }

    var access by remember { mutableStateOf<String?>("Don't Change") }
    var accessExpanded by remember { mutableStateOf(false) }

    var questionType by remember { mutableStateOf<String?>("Don't Change") }
    var questionTypeExpanded by remember { mutableStateOf(false) }

    var difficulty by remember { mutableStateOf<String?>("Don't Change") }
    var difficultyExpanded by remember { mutableStateOf(false) }

    var showConfirm by remember { mutableStateOf(false) }

    val accessOptions = listOf("Don't Change", "Free", "Premium")
    val questionTypeOptions = listOf("Don't Change", "Expected", "PYQ")
    val difficultyOptions = listOf("Don't Change", "Easy", "Medium", "Hard")

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Update $selectedCount Questions?") },
            text = {
                Column {
                    if (exam != "Don't Change") Text("Exam: $exam")
                    if (access != "Don't Change") Text("Access: $access")
                    if (questionType != "Don't Change") Text("Question Type: $questionType")
                    
                    if (difficulty != "Don't Change") Text("Difficulty: $difficulty")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("These changes will be applied to all $selectedCount selected questions.")
                }
            },
            confirmButton = {
                Button(onClick = {
                    showConfirm = false
                    onConfirm(
                        if (exam == "Don't Change") null else exam,
                        if (access == "Don't Change") null else access,
                        if (questionType == "Don't Change") null else questionType,
                        null,
                        if (difficulty == "Don't Change") null else difficulty
                    )
                }) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Edit Selected Questions") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Selected Questions: $selectedCount", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Exam
                    ExposedDropdownMenuBox(
                        expanded = examExpanded,
                        onExpandedChange = { examExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = exam ?: "Don't Change",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Exam") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = examExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = examExpanded,
                            onDismissRequest = { examExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Don't Change") },
                                onClick = { exam = "Don't Change"; examExpanded = false }
                            )
                            examsList.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = { exam = option; examExpanded = false }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Access
                    ExposedDropdownMenuBox(
                        expanded = accessExpanded,
                        onExpandedChange = { accessExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = access ?: "Don't Change",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Access") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accessExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = accessExpanded,
                            onDismissRequest = { accessExpanded = false }
                        ) {
                            accessOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = { access = option; accessExpanded = false }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Question Type
                    ExposedDropdownMenuBox(
                        expanded = questionTypeExpanded,
                        onExpandedChange = { questionTypeExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = questionType ?: "Don't Change",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Question Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = questionTypeExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = questionTypeExpanded,
                            onDismissRequest = { questionTypeExpanded = false }
                        ) {
                            questionTypeOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = { 
                                        questionType = option
                                        
                                        questionTypeExpanded = false 
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                     // Difficulty
                     ExposedDropdownMenuBox(
                         expanded = difficultyExpanded,
                         onExpandedChange = { difficultyExpanded = it }
                     ) {
                         OutlinedTextField(
                             value = difficulty ?: "Don't Change",
                             onValueChange = { },
                             readOnly = true,
                             label = { Text("Difficulty") },
                             trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = difficultyExpanded) },
                             modifier = Modifier.fillMaxWidth().menuAnchor(),
                             colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                         )
                         ExposedDropdownMenu(
                             expanded = difficultyExpanded,
                             onDismissRequest = { difficultyExpanded = false }
                         ) {
                             difficultyOptions.forEach { option ->
                                 DropdownMenuItem(
                                     text = { Text(option) },
                                     onClick = { difficulty = option; difficultyExpanded = false }
                                 )
                             }
                         }
                     }
                 }
            },
            confirmButton = {
                Button(
                    onClick = { showConfirm = true },
                    enabled = exam != "Don't Change" || access != "Don't Change" || questionType != "Don't Change" || difficulty != "Don't Change"
                ) {
                    Text("Update Questions")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}
