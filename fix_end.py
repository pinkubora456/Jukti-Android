with open('app/src/main/java/com/example/ui/screens/SingleQuestionUploadScreen.kt', 'r') as f:
    content = f.read()

bad_chunk = """    if (targetExamDialogVisible) {
        AlertDialog(
            text = {
                if (exams.isEmpty()) {
                    Text("No exams available. Please add exams in Manage Exams first.", color = MaterialTheme.colorScheme.error)
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(exams) { exam ->
                            val isSelected = selectedExams.contains(exam.title)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isSelected) {
                                            selectedExams.remove(exam.title)
                                            selectedExams.add(exam.title)
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            if (!selectedExams.contains(exam.title)) selectedExams.add(exam.title)
                                            selectedExams.remove(exam.title)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(exam.title, style = MaterialTheme.typography.bodyLarge)
            confirmButton = {
                    Text("Done")
        )
"""

good_chunk = """    if (targetExamDialogVisible) {
        AlertDialog(
            onDismissRequest = { targetExamDialogVisible = false },
            title = { Text("Select Target Exams") },
            text = {
                if (exams.isEmpty()) {
                    Text("No exams available. Please add exams in Manage Exams first.", color = MaterialTheme.colorScheme.error)
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(exams) { exam ->
                            val isSelected = selectedExams.contains(exam.title)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isSelected) {
                                            selectedExams.remove(exam.title)
                                        } else {
                                            selectedExams.add(exam.title)
                                        }
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            if (!selectedExams.contains(exam.title)) selectedExams.add(exam.title)
                                        } else {
                                            selectedExams.remove(exam.title)
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(exam.title, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { targetExamDialogVisible = false }) {
                    Text("Done")
                }
            }
        )
    }
}
"""

if bad_chunk in content:
    content = content.replace(bad_chunk, good_chunk)
    print("Replaced!")
else:
    print("Could not find chunk")

with open('app/src/main/java/com/example/ui/screens/SingleQuestionUploadScreen.kt', 'w') as f:
    f.write(content)
