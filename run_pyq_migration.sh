#!/bin/bash
sed -i 's/if (questionType == "PYQ" && pyqExamName != "Don'\''t Change" && pyqExamName?.isNotBlank() == true) Text("PYQ Exam: $pyqExamName")//g' app/src/main/java/com/example/ui/components/BulkEditQuestionsDialog.kt

sed -i '/if (questionType == "PYQ") {/,/Spacer(modifier = Modifier.height(8.dp))/d' app/src/main/java/com/example/ui/components/BulkEditQuestionsDialog.kt

sed -i '/if (questionTag == "PYQ") {/,/}/d' app/src/main/java/com/example/ui/screens/SingleQuestionUploadScreen.kt
