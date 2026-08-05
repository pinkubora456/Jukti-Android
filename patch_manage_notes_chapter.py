import re

filepath = 'app/src/main/java/com/example/ui/screens/ManageStudyNotesScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

# Add chapterExpanded state
target_state = 'var chapter by remember { mutableStateOf("Ahom Dynasty") }'
replacement_state = '''var chapter by remember { mutableStateOf("Ahom Dynasty") }
    var chapterDropdownExpanded by remember { mutableStateOf(false) }
    val chaptersList = allSubjectsChapters.filter { it.subject == subject }.map { it.chapter }.distinct().ifEmpty { listOf("General") }'''
content = content.replace(target_state, replacement_state)

chapter_tf = """                    item {
                        OutlinedTextField(
                            value = chapter,
                            onValueChange = { chapter = it },
                            label = { Text("Chapter / Topic (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }"""
chapter_dropdown = """                    item {
                        ExposedDropdownMenuBox(
                            expanded = chapterDropdownExpanded,
                            onExpandedChange = { chapterDropdownExpanded = !chapterDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = chapter,
                                onValueChange = { chapter = it },
                                label = { Text("Chapter / Topic (Optional)") },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                singleLine = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = chapterDropdownExpanded) }
                            )
                            ExposedDropdownMenu(
                                expanded = chapterDropdownExpanded,
                                onDismissRequest = { chapterDropdownExpanded = false }
                            ) {
                                chaptersList.forEach { chap ->
                                    DropdownMenuItem(
                                        text = { Text(chap) },
                                        onClick = {
                                            chapter = chap
                                            chapterDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }"""
content = content.replace(chapter_tf, chapter_dropdown)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
