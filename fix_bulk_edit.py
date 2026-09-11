import re
with open('app/src/main/java/com/example/ui/components/BulkEditQuestionsDialog.kt', 'r') as f:
    content = f.read()

# Remove pyqExamName state
content = re.sub(r'var pyqExamName by remember \{ mutableStateOf<String\?>\("Don\'t Change"\) \}\n\s*', '', content)

# Remove it from onConfirm parameters
content = content.replace("pyqExamName: String?,", "")
content = content.replace("if (pyqExamName == \"Don't Change\") null else pyqExamName,", "")

# Remove it from option click logic
content = re.sub(r'if \(option != "PYQ"\) pyqExamName = "Don\'t Change"\n\s*else if \(pyqExamName == "Don\'t Change"\) pyqExamName = ""', '', content)

with open('app/src/main/java/com/example/ui/components/BulkEditQuestionsDialog.kt', 'w') as f:
    f.write(content)
