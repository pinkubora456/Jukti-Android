import re

with open('app/src/main/java/com/example/ui/screens/AllQuestionsScreen.kt', 'r') as f:
    content = f.read()

# Replace BulkMoveQuestionsDialog entirely
new_bulk_move_dialog = """
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkMoveQuestionsDialog(
    viewModel: JuktiViewModel,
    selectedCount: Int,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    val allSubjectsChapters by viewModel.allSubjectsChapters.collectAsState()
    
    val subjOptions = remember(allSubjectsChapters) {
        allSubjectsChapters.map { com.example.data.repository.normalizeSubjectName(it.subject) }.filter { it.isNotBlank() }.distinct().sorted()
    }
    
    var destSubj by remember { mutableStateOf("") }
    var subjExpanded by remember { mutableStateOf(false) }

    val chapOptions = remember(allSubjectsChapters, destSubj) {
        if (destSubj.isBlank()) emptyList()
        else {
            val normDestSubj = com.example.data.repository.normalizeSubjectName(destSubj)
            allSubjectsChapters
                .filter { com.example.data.repository.normalizeSubjectName(it.subject).equals(normDestSubj, ignoreCase = true) }
                .map { com.example.data.repository.normalizeChapterName(it.chapter, it.subject) }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()
        }
    }
    
    var destChap by remember { mutableStateOf("") }
    var chapExpanded by remember { mutableStateOf(false) }

    var showConfirm by remember { mutableStateOf(false) }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Confirm Move") },
            text = {
                Text("Update $selectedCount Questions?\\n\\nSubject: $destSubj\\nChapter: $destChap\\n\\nThese changes will be applied to all $selectedCount selected questions.")
            },
            confirmButton = {
                Button(onClick = { 
                    showConfirm = false
                    onConfirm(destSubj, destChap)
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
            title = { Text("Move Questions") },
            text = {
                Column {
                    Text("Selected Questions: $selectedCount", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ExposedDropdownMenuBox(
                        expanded = subjExpanded,
                        onExpandedChange = { subjExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = destSubj,
                            onValueChange = { destSubj = it; destChap = "" },
                            label = { Text("Select Subject") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = subjExpanded,
                            onDismissRequest = { subjExpanded = false }
                        ) {
                            subjOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        destSubj = option
                                        destChap = ""
                                        subjExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    ExposedDropdownMenuBox(
                        expanded = chapExpanded,
                        onExpandedChange = { chapExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = destChap,
                            onValueChange = { destChap = it },
                            label = { Text("Select Chapter") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = chapExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = chapExpanded,
                            onDismissRequest = { chapExpanded = false }
                        ) {
                            chapOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        destChap = option
                                        chapExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showConfirm = true },
                    enabled = destSubj.isNotBlank() && destChap.isNotBlank()
                ) {
                    Text("Move Questions")
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
"""

content = re.sub(r'fun BulkMoveQuestionsDialog.*?^}$', new_bulk_move_dialog, content, flags=re.MULTILINE | re.DOTALL)

with open('app/src/main/java/com/example/ui/screens/AllQuestionsScreen.kt', 'w') as f:
    f.write(content)
