import re

with open('app/src/main/java/com/example/ui/screens/SingleQuestionUploadScreen.kt', 'r') as f:
    content = f.read()

# Replace the specific lines
content = content.replace(
    '                                    id = editingQuestion?.id ?: 0L,\n                                    createdAt = editingQuestion?.createdAt ?: System.currentTimeMillis(),',
    '                                    id = editingQuestion?.id ?: 0L,\n                                    updatedAt = System.currentTimeMillis(),'
)

with open('app/src/main/java/com/example/ui/screens/SingleQuestionUploadScreen.kt', 'w') as f:
    f.write(content)
