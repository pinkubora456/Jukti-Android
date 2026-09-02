import re

with open("app/src/main/java/com/example/ui/screens/ManageSubjectsChaptersScreen.kt", "r") as f:
    content = f.read()

# Add new states
new_states = """
    var showRenameSubjectDialog by remember { mutableStateOf<String?>(null) }
    var showRenameChapterDialog by remember { mutableStateOf<String?>(null) }
    var showAddChapterDialog by remember { mutableStateOf<String?>(null) }
"""
content = content.replace("    var showMergeDialog by remember { mutableStateOf(false) }", "    var showMergeDialog by remember { mutableStateOf(false) }" + new_states)

# Add UI actions for Subjects
subject_actions = """
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(onClick = { showRenameSubjectDialog = subject.name }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Rename Subject")
                                    }
                                    Button(onClick = { 
                                        selectedSubjectForChapters = subject.name
                                        searchQuery = ""
                                    }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp), modifier = Modifier.height(36.dp)) {
                                        Text("Manage")
                                    }
                                }
"""
content = re.sub(r'Button\(onClick = \{\s*selectedSubjectForChapters = subject\.name\s*searchQuery = ""\s*\}, contentPadding = PaddingValues\(horizontal = 12\.dp, vertical = 6\.dp\), modifier = Modifier\.height\(36\.dp\)\) \{\s*Text\("Manage"\)\s*\}', subject_actions.strip(), content)

# Add "Add Chapter" button
chapter_header = """
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Chapters", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { showAddChapterDialog = subjectName }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp), modifier = Modifier.height(36.dp)) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add")
                                }
                                Button(onClick = { showMergeDialog = true }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp), modifier = Modifier.height(36.dp)) {
                                    Icon(Icons.Default.Merge, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Merge")
                                }
                            }
                        }
"""
content = re.sub(r'Row\(modifier = Modifier.fillMaxWidth\(\), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically\) \{.*?Text\("Merge Chapters"\)\s*\}\s*\}', chapter_header.strip(), content, flags=re.DOTALL)

# Add Rename Chapter button
chapter_actions = """
                                IconButton(onClick = { showRenameChapterDialog = chapter.name }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Rename Chapter")
                                }
"""
# insert before the end of Row in chapters list
content = content.replace("                                } // End of Column", "                                }\n" + chapter_actions)
content = re.sub(r'(viewModel.navigateTo\(Screen.ALL_QUESTIONS\)\s*\})', r'\1\n                                    )\n                                }\n' + chapter_actions, content)

# Add dialogs at the end
dialogs = """
    if (showRenameSubjectDialog != null) {
        var newName by remember { mutableStateOf(showRenameSubjectDialog!!) }
        AlertDialog(
            onDismissRequest = { showRenameSubjectDialog = null },
            title = { Text("Rename Subject") },
            text = {
                SafeOutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("New Subject Name") }, modifier = Modifier.fillMaxWidth())
            },
            confirmButton = {
                Button(onClick = {
                    if (newName.isNotBlank() && newName != showRenameSubjectDialog) {
                        viewModel.renameSubject(showRenameSubjectDialog!!, newName)
                        if (selectedSubjectForChapters == showRenameSubjectDialog) {
                            selectedSubjectForChapters = newName
                        }
                        Toast.makeText(context, "Subject renamed", Toast.LENGTH_SHORT).show()
                    }
                    showRenameSubjectDialog = null
                }) { Text("Rename") }
            },
            dismissButton = { TextButton(onClick = { showRenameSubjectDialog = null }) { Text("Cancel") } }
        )
    }

    if (showRenameChapterDialog != null) {
        val subjectName = selectedSubjectForChapters!!
        var newName by remember { mutableStateOf(showRenameChapterDialog!!) }
        AlertDialog(
            onDismissRequest = { showRenameChapterDialog = null },
            title = { Text("Rename Chapter") },
            text = {
                SafeOutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("New Chapter Name") }, modifier = Modifier.fillMaxWidth())
            },
            confirmButton = {
                Button(onClick = {
                    if (newName.isNotBlank() && newName != showRenameChapterDialog) {
                        viewModel.renameChapter(subjectName, showRenameChapterDialog!!, newName)
                        Toast.makeText(context, "Chapter renamed", Toast.LENGTH_SHORT).show()
                    }
                    showRenameChapterDialog = null
                }) { Text("Rename") }
            },
            dismissButton = { TextButton(onClick = { showRenameChapterDialog = null }) { Text("Cancel") } }
        )
    }

    if (showAddChapterDialog != null) {
        val subjectName = showAddChapterDialog!!
        var newName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddChapterDialog = null },
            title = { Text("Add Chapter") },
            text = {
                SafeOutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Chapter Name") }, modifier = Modifier.fillMaxWidth())
            },
            confirmButton = {
                Button(onClick = {
                    if (newName.isNotBlank()) {
                        viewModel.addSubjectChapter(subjectName, newName)
                        Toast.makeText(context, "Chapter added", Toast.LENGTH_SHORT).show()
                    }
                    showAddChapterDialog = null
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAddChapterDialog = null }) { Text("Cancel") } }
        )
    }
}
"""
content = content.replace("    if (showMergeDialog && selectedSubjectForChapters != null) {", dialogs + "\n    if (showMergeDialog && selectedSubjectForChapters != null) {")


with open("app/src/main/java/com/example/ui/screens/ManageSubjectsChaptersScreen.kt", "w") as f:
    f.write(content)
