import re

with open("app/src/main/java/com/example/ui/screens/AllQuestionsScreen.kt", "r") as f:
    content = f.read()

new_state = """    var selectedSubject by remember { mutableStateOf("All Subjects") }
    var subjectExpanded by remember { mutableStateOf(false) }
    
    var selectedChapter by remember { mutableStateOf("All Chapters") }
    var chapterExpanded by remember { mutableStateOf(false) }

    val subjectsList = listOf("All Subjects") + com.example.data.repository.SampleData.CANONICAL_SUBJECTS
    
    val chaptersList = remember(questions, selectedSubject) {
        val chapters = if (selectedSubject == "All Subjects") {
            questions.map { it.topic }.filter { it.isNotBlank() }.distinct().sorted()
        } else {
            questions.filter { q ->
                val normSubj = com.example.data.repository.normalizeSubjectName(q.subject)
                normSubj.equals(selectedSubject, ignoreCase = true) || q.subject.equals(selectedSubject, ignoreCase = true)
            }.map { it.topic }.filter { it.isNotBlank() }.distinct().sorted()
        }
        listOf("All Chapters") + chapters
    }

    LaunchedEffect(selectedSubject) {
        selectedChapter = "All Chapters"
    }"""

content = re.sub(
    r'    var selectedSubject by remember \{ mutableStateOf\("All Subjects"\) \}\n    var subjectExpanded by remember \{ mutableStateOf\(false\) \}\n\n    val subjectsList = listOf\("All Subjects"\) \+ com\.example\.data\.repository\.SampleData\.CANONICAL_SUBJECTS',
    new_state,
    content
)

old_filter = """    val filteredQuestions = remember(questions, searchQuery, selectedSubject) {
        questions.filter { q ->
            val normSubj = com.example.data.repository.normalizeSubjectName(q.subject)
            val matchesSubject = selectedSubject == "All Subjects" || 
                 normSubj.equals(selectedSubject, ignoreCase = true) ||
                q.subject.equals(selectedSubject, ignoreCase = true)

            val matchesSearch = searchQuery.isBlank() ||
                    q.questionEn.contains(searchQuery, ignoreCase = true) ||
                    q.questionAs.contains(searchQuery, ignoreCase = true) ||
                    q.subject.contains(searchQuery, ignoreCase = true) ||
                    normSubj.contains(searchQuery, ignoreCase = true) ||
                    q.topic.contains(searchQuery, ignoreCase = true)

            matchesSubject && matchesSearch
        }
    }"""

new_filter = """    val filteredQuestions = remember(questions, searchQuery, selectedSubject, selectedChapter) {
        questions.filter { q ->
            val normSubj = com.example.data.repository.normalizeSubjectName(q.subject)
            val matchesSubject = selectedSubject == "All Subjects" || 
                 normSubj.equals(selectedSubject, ignoreCase = true) ||
                q.subject.equals(selectedSubject, ignoreCase = true)

            val normChapter = com.example.data.repository.normalizeChapterName(q.topic)
            val matchesChapter = selectedChapter == "All Chapters" ||
                 normChapter.equals(selectedChapter, ignoreCase = true) ||
                q.topic.equals(selectedChapter, ignoreCase = true)

            val matchesSearch = searchQuery.isBlank() ||
                    q.questionEn.contains(searchQuery, ignoreCase = true) ||
                    q.questionAs.contains(searchQuery, ignoreCase = true) ||
                    q.subject.contains(searchQuery, ignoreCase = true) ||
                    normSubj.contains(searchQuery, ignoreCase = true) ||
                    q.topic.contains(searchQuery, ignoreCase = true)

            matchesSubject && matchesChapter && matchesSearch
        }
    }"""

content = content.replace(old_filter, new_filter)

old_dropdown = """            // Subject Filter Dropdown
            ExposedDropdownMenuBox(
                expanded = subjectExpanded,
                onExpandedChange = { subjectExpanded = !subjectExpanded }
            ) {
                OutlinedTextField(
                    value = selectedSubject,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Filter by Subject") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = subjectExpanded,
                    onDismissRequest = { subjectExpanded = false }
                ) {
                    subjectsList.forEach { subj ->
                        DropdownMenuItem(
                            text = { Text(subj) },
                            onClick = {
                                selectedSubject = subj
                                subjectExpanded = false
                            }
                        )
                    }
                }
            }"""

new_dropdown = """            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Subject Filter Dropdown
                ExposedDropdownMenuBox(
                    expanded = subjectExpanded,
                    onExpandedChange = { subjectExpanded = !subjectExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedSubject,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Subject") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = subjectExpanded,
                        onDismissRequest = { subjectExpanded = false }
                    ) {
                        subjectsList.forEach { subj ->
                            DropdownMenuItem(
                                text = { Text(subj) },
                                onClick = {
                                    selectedSubject = subj
                                    subjectExpanded = false
                                }
                            )
                        }
                    }
                }

                // Chapter Filter Dropdown
                ExposedDropdownMenuBox(
                    expanded = chapterExpanded,
                    onExpandedChange = { chapterExpanded = !chapterExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedChapter,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Chapter") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = chapterExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = chapterExpanded,
                        onDismissRequest = { chapterExpanded = false }
                    ) {
                        chaptersList.forEach { chap ->
                            DropdownMenuItem(
                                text = { Text(chap) },
                                onClick = {
                                    selectedChapter = chap
                                    chapterExpanded = false
                                }
                            )
                        }
                    }
                }
            }"""

content = content.replace(old_dropdown, new_dropdown)

with open("app/src/main/java/com/example/ui/screens/AllQuestionsScreen.kt", "w") as f:
    f.write(content)
