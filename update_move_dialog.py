import re

def update_file():
    with open("app/src/main/java/com/example/ui/screens/AllQuestionsScreen.kt", "r") as f:
        content = f.read()

    # We need to change:
    # 1. destExam to be destExam string, but dropdown item uses checkboxes
    # 2. subjOptions to split destExam and match any
    # 3. chapOptions to split destExam and match any
    
    old_destExam = """    var destExam by remember { mutableStateOf(examOptions.firstOrNull() ?: "") }
    var examExpanded by remember { mutableStateOf(false) }

    val subjOptions = remember(allSubjectsChapters, questions, destExam) {
        val filtered = questions.filter { it.examCategory.contains(destExam, ignoreCase = true) }
        filtered.map { com.example.data.repository.normalizeSubjectName(it.subject) }.filter { it.isNotBlank() }.distinct().sorted()
    }
    
    var destSubj by remember { mutableStateOf("") }
    var subjExpanded by remember { mutableStateOf(false) }

    val chapOptions = remember(questions, destExam, destSubj) {
        val filtered = questions.filter { 
            it.examCategory.contains(destExam, ignoreCase = true) && 
            (com.example.data.repository.normalizeSubjectName(it.subject).equals(destSubj, ignoreCase = true) || it.subject.equals(destSubj, ignoreCase = true)) 
        }
        filtered.map { com.example.data.repository.normalizeChapterName(it.topic, it.subject) }.filter { it.isNotBlank() }.distinct().sorted()
    }"""

    new_destExam = """    var destExam by remember { mutableStateOf(examOptions.firstOrNull() ?: "") }
    var examExpanded by remember { mutableStateOf(false) }

    val subjOptions = remember(allSubjectsChapters, questions, destExam) {
        val targetExams = destExam.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val filtered = if (targetExams.isEmpty()) questions else questions.filter { q -> 
            targetExams.any { q.examCategory.contains(it, ignoreCase = true) } 
        }
        filtered.map { com.example.data.repository.normalizeSubjectName(it.subject) }.filter { it.isNotBlank() }.distinct().sorted()
    }
    
    var destSubj by remember { mutableStateOf("") }
    var subjExpanded by remember { mutableStateOf(false) }

    val chapOptions = remember(questions, destExam, destSubj) {
        val targetExams = destExam.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val filtered = questions.filter { q -> 
            (targetExams.isEmpty() || targetExams.any { q.examCategory.contains(it, ignoreCase = true) }) && 
            (com.example.data.repository.normalizeSubjectName(q.subject).equals(destSubj, ignoreCase = true) || q.subject.equals(destSubj, ignoreCase = true)) 
        }
        filtered.map { com.example.data.repository.normalizeChapterName(it.topic, it.subject) }.filter { it.isNotBlank() }.distinct().sorted()
    }"""
    
    old_dropdown = """                    // Destination Exam
                    ExposedDropdownMenuBox(expanded = examExpanded, onExpandedChange = { examExpanded = it }) {
                        OutlinedTextField(
                            value = destExam, onValueChange = { destExam = it },
                            label = { Text("Destination Exam") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = examExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(), colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(expanded = examExpanded, onDismissRequest = { examExpanded = false }) {
                            examOptions.forEach { e ->
                                DropdownMenuItem(text = { Text(e) }, onClick = { destExam = e; examExpanded = false })
                            }
                        }
                    }"""

    new_dropdown = """                    // Destination Exam
                    ExposedDropdownMenuBox(expanded = examExpanded, onExpandedChange = { examExpanded = it }) {
                        OutlinedTextField(
                            value = destExam, onValueChange = { destExam = it },
                            label = { Text("Destination Exam") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = examExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(), colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(expanded = examExpanded, onDismissRequest = { examExpanded = false }) {
                            val currentSelected = destExam.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
                            examOptions.forEach { e ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                            androidx.compose.material3.Checkbox(
                                                checked = currentSelected.contains(e),
                                                onCheckedChange = null
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(e)
                                        }
                                    },
                                    onClick = {
                                        val newSelected = if (currentSelected.contains(e)) currentSelected - e else currentSelected + e
                                        destExam = newSelected.joinToString(", ")
                                    }
                                )
                            }
                        }
                    }"""
                    
    if old_destExam in content and old_dropdown in content:
        content = content.replace(old_destExam, new_destExam)
        content = content.replace(old_dropdown, new_dropdown)
        with open("app/src/main/java/com/example/ui/screens/AllQuestionsScreen.kt", "w") as f:
            f.write(content)
        print("Successfully updated AllQuestionsScreen.kt")
    else:
        print("Could not find the target code in AllQuestionsScreen.kt")

update_file()
