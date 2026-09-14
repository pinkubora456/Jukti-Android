import re
path = "app/src/main/java/com/example/ui/screens/BatchImportQuestionScreen.kt"
with open(path, "r") as f:
    content = f.read()

pattern = r"(        Column\(modifier = Modifier\.padding\(14\.dp\)\) \{\n)"
replacement = r"""\1            if (q.passageId.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "Passage ID: ${q.passageId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
"""
if re.search(pattern, content):
    content = re.sub(pattern, replacement, content, count=1)
    print("Replaced card")
else:
    print("Pattern not found in card")

with open(path, "w") as f:
    f.write(content)
