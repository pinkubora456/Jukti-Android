import re

with open('app/src/main/java/com/example/ui/screens/ManageGuidanceScreen.kt', 'r') as f:
    content = f.read()

old_dialog_content = """                    SafeOutlinedTextField(
                        value = addSubject,
                        onValueChange = { addSubject = it },
                        label = { Text("Subject") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    SafeOutlinedTextField(
                        value = addChapter,
                        onValueChange = { addChapter = it },
                        label = { Text("Chapter") },
                        modifier = Modifier.fillMaxWidth()
                    )"""

new_dialog_content = """                    var subjectExpanded by remember { mutableStateOf(false) }
                    var chapterExpanded by remember { mutableStateOf(false) }

                    val allRawSubjects = remember(allSubjectsChapters) {
                        allSubjectsChapters.map { it.subject }.distinct().sorted()
                    }
                    val chaptersForSubj = remember(addSubject, allSubjectsChapters) {
                        allSubjectsChapters.filter { it.subject.equals(addSubject, ignoreCase = true) }.map { it.chapter }.distinct().sorted()
                    }

                    ExposedDropdownMenuBox(
                        expanded = subjectExpanded,
                        onExpandedChange = { subjectExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = addSubject,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Subject") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = subjectExpanded,
                            onDismissRequest = { subjectExpanded = false }
                        ) {
                            allRawSubjects.forEach { subj ->
                                DropdownMenuItem(
                                    text = { Text(subj) },
                                    onClick = { 
                                        if (addSubject != subj) {
                                            addSubject = subj
                                            addChapter = ""
                                        }
                                        subjectExpanded = false 
                                    }
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = chapterExpanded,
                        onExpandedChange = { chapterExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = addChapter,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Chapter") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = chapterExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = chapterExpanded,
                            onDismissRequest = { chapterExpanded = false }
                        ) {
                            chaptersForSubj.forEach { chap ->
                                DropdownMenuItem(
                                    text = { Text(chap) },
                                    onClick = { addChapter = chap; chapterExpanded = false }
                                )
                            }
                        }
                    }"""

content = content.replace(old_dialog_content, new_dialog_content)

with open('app/src/main/java/com/example/ui/screens/ManageGuidanceScreen.kt', 'w') as f:
    f.write(content)
