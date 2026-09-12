import re

with open('app/src/main/java/com/example/ui/screens/GuidanceScreen.kt', 'r') as f:
    content = f.read()

# Fix the compile error (using Int instead of Dp for padding or wrong arguments)
# We need to make sure we use 12.dp instead of just 12, etc.
content = content.replace("Modifier.fillMaxWidth().padding(horizontal = 12.dp, bottom = 8.dp)", "Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)")

with open('app/src/main/java/com/example/ui/screens/GuidanceScreen.kt', 'w') as f:
    f.write(content)
