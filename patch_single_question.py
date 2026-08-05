import re

filepath = 'app/src/main/java/com/example/ui/screens/SingleQuestionUploadScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

# Add allSubjectsChapters state
target_state = 'val exams by viewModel.examsList.collectAsState()'
replacement_state = '''val exams by viewModel.examsList.collectAsState()
    val allSubjectsChapters by viewModel.allSubjectsChapters.collectAsState()
    val subjectsList = allSubjectsChapters.map { it.subject }.distinct().ifEmpty { listOf("Assam History", "General Knowledge") }
    var subjectExpanded by remember { mutableStateOf(false) }
    var chapterExpanded by remember { mutableStateOf(false) }
    val chaptersList = allSubjectsChapters.filter { it.subject == subject }.map { it.chapter }.distinct().ifEmpty { listOf("General") }'''
content = content.replace(target_state, replacement_state)

# Replace Subject OutlinedTextField
subject_tf = """            item {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }"""
subject_dropdown = """            item {
                ExposedDropdownMenuBox(
                    expanded = subjectExpanded,
                    onExpandedChange = { subjectExpanded = !subjectExpanded }
                ) {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject *") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        singleLine = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = subjectExpanded,
                        onDismissRequest = { subjectExpanded = false }
                    ) {
                        subjectsList.forEach { subj ->
                            DropdownMenuItem(
                                text = { Text(subj) },
                                onClick = {
                                    subject = subj
                                    subjectExpanded = false
                                }
                            )
                        }
                    }
                }
            }"""
content = content.replace(subject_tf, subject_dropdown)

# Replace Chapter OutlinedTextField
chapter_tf = """            item {
                OutlinedTextField(
                    value = chapter,
                    onValueChange = { chapter = it },
                    label = { Text("Chapter/Topic *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }"""
chapter_dropdown = """            item {
                ExposedDropdownMenuBox(
                    expanded = chapterExpanded,
                    onExpandedChange = { chapterExpanded = !chapterExpanded }
                ) {
                    OutlinedTextField(
                        value = chapter,
                        onValueChange = { chapter = it },
                        label = { Text("Chapter/Topic *") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        singleLine = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = chapterExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = chapterExpanded,
                        onDismissRequest = { chapterExpanded = false }
                    ) {
                        chaptersList.forEach { chap ->
                            DropdownMenuItem(
                                text = { Text(chap) },
                                onClick = {
                                    chapter = chap
                                    chapterExpanded = false
                                }
                            )
                        }
                    }
                }
            }"""
content = content.replace(chapter_tf, chapter_dropdown)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
