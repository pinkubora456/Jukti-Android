import re

with open('app/src/main/java/com/example/ui/screens/ContentQuestionsOverviewScreen.kt', 'r') as f:
    content = f.read()

# Replace selectedSubject with selectedOverviewSubjects
content = content.replace("val selectedSubject by viewModel.selectedSubject.collectAsState()", "val selectedOverviewSubjects by viewModel.selectedOverviewSubjects.collectAsState()")

# Remove the LaunchedEffect
content = re.sub(r'LaunchedEffect\(subjectsList\) \{\s*if \(selectedSubject !in subjectsList && subjectsList\.isNotEmpty\(\)\) \{\s*viewModel\.setSubjectFilter\(subjectsList\.first\(\)\)\s*\}\s*\}', '', content)

# Update chapterStatsResults
content = content.replace('val chapterStatsResults by remember(selectedSubject, selectedTargetExam, selectedQuestionType, selectedQuestionTag) {', 'val chapterStatsResults by remember(selectedOverviewSubjects, selectedTargetExam, selectedQuestionType, selectedQuestionTag) {')
content = content.replace('viewModel.getChapterStatsByExam(selectedSubject, selectedTargetExam, selectedQuestionType, selectedQuestionTag)', 'viewModel.getChapterStatsByExamMultiSubject(selectedOverviewSubjects, selectedTargetExam, selectedQuestionType, selectedQuestionTag)')

# Update isFilterActive
content = content.replace('(subjectsList.isNotEmpty() && selectedSubject != subjectsList.first())', '(!selectedOverviewSubjects.contains("All Subjects"))')

# Now for the dropdown:
# Find ExposedDropdownMenuBox for Subject
subject_dropdown_regex = r'ExposedDropdownMenuBox\(\s*expanded = expanded,\s*onExpandedChange = \{ expanded = it \},\s*modifier = Modifier.weight\(1f\)\s*\) \{\s*OutlinedTextField\(\s*value = selectedSubject,\s*onValueChange = \{\},\s*readOnly = true,\s*label = \{ Text\("Subject", maxLines = 1, overflow = TextOverflow\.Ellipsis\) \},\s*trailingIcon = \{\s*ExposedDropdownMenuDefaults\.TrailingIcon\(expanded = expanded\)\s*\},\s*colors = ExposedDropdownMenuDefaults\.outlinedTextFieldColors\(\),\s*modifier = Modifier\.menuAnchor\(\)\s*\)\s*ExposedDropdownMenu\(\s*expanded = expanded,\s*onDismissRequest = \{ expanded = false \}\s*\) \{\s*subjectsList\.forEach \{ subj ->\s*DropdownMenuItem\(\s*text = \{ Text\(subj\) \},\s*onClick = \{\s*viewModel\.setSubjectFilter\(subj\)\s*expanded = false\s*\}\s*\)\s*\}\s*\}\s*\}'

new_dropdown = """ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    val displayValue = if (selectedOverviewSubjects.contains("All Subjects")) "All Subjects" else "${selectedOverviewSubjects.size} Selected"
                    OutlinedTextField(
                        value = displayValue,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Subject", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = selectedOverviewSubjects.contains("All Subjects"), onCheckedChange = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("All Subjects")
                                }
                            },
                            onClick = { viewModel.toggleOverviewSubject("All Subjects", !selectedOverviewSubjects.contains("All Subjects")) }
                        )
                        subjectsList.filter { it != "All Subjects" }.forEach { subj ->
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = selectedOverviewSubjects.contains(subj), onCheckedChange = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(subj)
                                    }
                                },
                                onClick = { viewModel.toggleOverviewSubject(subj, !selectedOverviewSubjects.contains(subj)) }
                            )
                        }
                    }
                }"""

content = re.sub(subject_dropdown_regex, new_dropdown, content)

with open('app/src/main/java/com/example/ui/screens/ContentQuestionsOverviewScreen.kt', 'w') as f:
    f.write(content)
