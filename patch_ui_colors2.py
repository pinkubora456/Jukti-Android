import re

with open('app/src/main/java/com/example/ui/screens/GuidanceScreen.kt', 'r') as f:
    content = f.read()

# Fix the formatting issue introduced in the previous regex replacement
content = content.replace("topics.take(10).forEachIndexed { index, topic ->                    val (statusColor, containerColor) = when (topic.priorityLabel) {", 
"topics.take(10).forEachIndexed { index, topic ->\n                    val (statusColor, containerColor) = when (topic.priorityLabel) {")

content = content.replace("items.take(10).forEachIndexed { index, item ->                    val (statusColor, containerColor) = when {",
"items.take(10).forEachIndexed { index, item ->\n                    val (statusColor, containerColor) = when {")

with open('app/src/main/java/com/example/ui/screens/GuidanceScreen.kt', 'w') as f:
    f.write(content)
