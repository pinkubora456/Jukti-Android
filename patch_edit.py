import re

with open("app/src/main/java/com/example/ui/components/EditQuestionDialog.kt", "r") as f:
    content = f.read()

target = """                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {"""
                
replacement = """                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/src/main/java/com/example/ui/components/EditQuestionDialog.kt", "w") as f:
        f.write(content)
    print("Patched successfully")
else:
    print("Target not found")
