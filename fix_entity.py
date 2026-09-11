with open('app/src/main/java/com/example/data/local/Entities.kt', 'r') as f:
    content = f.read()

import re

# Add pyqExams
regex = r'val duplicateKey: String = ""\n\)'
replacement = r'val duplicateKey: String = "",\n    @ColumnInfo(defaultValue = "") val pyqExams: String = ""\n)'

content = re.sub(regex, replacement, content)

with open('app/src/main/java/com/example/data/local/Entities.kt', 'w') as f:
    f.write(content)
