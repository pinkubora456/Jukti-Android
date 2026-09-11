import re
with open('app/src/main/java/com/example/ui/screens/ContentQuestionsOverviewScreen.kt', 'r') as f:
    content = f.read()

regex = r'OutlinedTextField\(\s*value = selectedSubject,\s*onValueChange = \{\},\s*readOnly = true,\s*label = \{ Text\("Subject", maxLines = 1, overflow = TextOverflow\.Ellipsis\) \},\s*trailingIcon = \{ ExposedDropdownMenuDefaults\.TrailingIcon\(expanded = expanded\) \},\s*colors = ExposedDropdownMenuDefaults\.outlinedTextFieldColors\(\),\s*singleLine = true,\s*textStyle = MaterialTheme\.typography\.bodyMedium,\s*shape = RoundedCornerShape\(10\.dp\),\s*modifier = Modifier\.menuAnchor\(\)\.fillMaxWidth\(\)\s*\)\s*ExposedDropdownMenu\(\s*expanded = expanded,\s*onDismissRequest = \{ expanded = false \}\s*\) \{\s*subjectsList\.forEach \{ subj ->\s*DropdownMenuItem\(\s*text = \{ Text\(subj, style = MaterialTheme\.typography\.bodyMedium\) \},\s*onClick = \{\s*viewModel\.setSubjectFilter\(subj\)\s*expanded = false\s*\}\s*\)\s*\}\s*\}'

replacement = """val displayValue = if (selectedOverviewSubjects.contains("All Subjects")) "All Subjects" else "${selectedOverviewSubjects.size} Selected"
                    OutlinedTextField(
                        value = displayValue,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Subject", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
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
                                    Text("All Subjects", style = MaterialTheme.typography.bodyMedium)
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
                                        Text(subj, style = MaterialTheme.typography.bodyMedium)
                                    }
                                },
                                onClick = { viewModel.toggleOverviewSubject(subj, !selectedOverviewSubjects.contains(subj)) }
                            )
                        }
                    }"""

content = re.sub(regex, replacement, content)

with open('app/src/main/java/com/example/ui/screens/ContentQuestionsOverviewScreen.kt', 'w') as f:
    f.write(content)
