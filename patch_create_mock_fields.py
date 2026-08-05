import re

filepath = 'app/src/main/java/com/example/ui/screens/CreateMockScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

# Add states inside CreateMockScreen
target_state = 'var qSubject by remember { mutableStateOf("") }'
replacement_state = '''var qSubject by remember { mutableStateOf("") }
    var qSubjectExpanded by remember { mutableStateOf(false) }
    var qChapterExpanded by remember { mutableStateOf(false) }
    val qChaptersList = allSubjectsChapters.filter { it.subject == qSubject }.map { it.chapter }.distinct().ifEmpty { listOf("General") }'''
content = content.replace(target_state, replacement_state)

subject_tf = """                    item {
                        OutlinedTextField(
                            value = qSubject,
                            onValueChange = { qSubject = it },
                            label = { Text("Subject *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }"""
subject_dropdown = """                    item {
                        ExposedDropdownMenuBox(
                            expanded = qSubjectExpanded,
                            onExpandedChange = { qSubjectExpanded = !qSubjectExpanded }
                        ) {
                            OutlinedTextField(
                                value = qSubject,
                                onValueChange = { qSubject = it },
                                label = { Text("Subject *") },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                singleLine = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = qSubjectExpanded) }
                            )
                            ExposedDropdownMenu(
                                expanded = qSubjectExpanded,
                                onDismissRequest = { qSubjectExpanded = false }
                            ) {
                                subjectsList.filter { it != "All Subjects" }.forEach { subj ->
                                    DropdownMenuItem(
                                        text = { Text(subj) },
                                        onClick = {
                                            qSubject = subj
                                            qSubjectExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }"""
content = content.replace(subject_tf, subject_dropdown)

chapter_tf = """                    item {
                        OutlinedTextField(
                            value = qChapter,
                            onValueChange = { qChapter = it },
                            label = { Text("Chapter/Topic *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }"""
chapter_dropdown = """                    item {
                        ExposedDropdownMenuBox(
                            expanded = qChapterExpanded,
                            onExpandedChange = { qChapterExpanded = !qChapterExpanded }
                        ) {
                            OutlinedTextField(
                                value = qChapter,
                                onValueChange = { qChapter = it },
                                label = { Text("Chapter/Topic *") },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                singleLine = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = qChapterExpanded) }
                            )
                            ExposedDropdownMenu(
                                expanded = qChapterExpanded,
                                onDismissRequest = { qChapterExpanded = false }
                            ) {
                                qChaptersList.forEach { chap ->
                                    DropdownMenuItem(
                                        text = { Text(chap) },
                                        onClick = {
                                            qChapter = chap
                                            qChapterExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }"""
content = content.replace(chapter_tf, chapter_dropdown)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
