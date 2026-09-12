import re

with open('app/src/main/java/com/example/ui/screens/GuidanceScreen.kt', 'r') as f:
    content = f.read()

# Fix the formatting issue introduced in the previous regex replacement
content = content.replace("topics.take(10).forEachIndexed { index, topic ->                    val (statusColor, statusIcon) = when (topic.priorityLabel) {", 
"topics.take(10).forEachIndexed { index, topic ->\n                    val (statusColor, statusIcon) = when (topic.priorityLabel) {")

content = content.replace("items.take(10).forEachIndexed { index, item ->                    val (statusColor, statusIcon) = when {",
"items.take(10).forEachIndexed { index, item ->\n                    val (statusColor, statusIcon) = when {")

with open('app/src/main/java/com/example/ui/screens/GuidanceScreen.kt', 'w') as f:
    f.write(content)
