import re

with open("app/src/main/java/com/example/ui/screens/GuidanceScreen.kt", "r") as f:
    content = f.read()

content = content.replace("Divider()", "HorizontalDivider()")
content = content.replace("Divider(modifier = Modifier.padding(vertical = 4.dp))", "HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))")

with open("app/src/main/java/com/example/ui/screens/GuidanceScreen.kt", "w") as f:
    f.write(content)
