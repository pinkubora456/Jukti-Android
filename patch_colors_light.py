import re

with open('app/src/main/java/com/example/ui/screens/GuidanceScreen.kt', 'r') as f:
    content = f.read()

replacement1 = """                    val (statusColor, containerColor) = when (topic.priorityLabel) {
                        "High Priority" -> Pair(Color(0xFFC62828), Color(0xFFFFEBEE)) // Light Red bg, Dark Red text
                        "Medium Priority" -> Pair(Color(0xFFE65100), Color(0xFFFFF3E0)) // Light Orange bg, Dark Orange text
                        else -> Pair(Color(0xFF424242), Color(0xFFF5F5F5)) // Light Gray bg, Dark Gray text
                    }"""

pattern1 = r'\s*val \(statusColor, containerColor\) = when \(topic\.priorityLabel\) \{\s*"High Priority" -> Pair\(Color\.White, Color\(0xFFE53935\)\) // Red bg\s*"Medium Priority" -> Pair\(Color\.Black, Color\(0xFFFB8C00\)\) // Orange bg\s*else -> Pair\(Color\.Black, Color\(0xFFE0E0E0\)\) // Gray bg\s*\}'
content = re.sub(pattern1, replacement1, content, flags=re.MULTILINE)

replacement2 = """                    val (statusColor, containerColor) = when {
                        item.statusLabel == "Not Enough Data" -> Pair(Color(0xFF424242), Color(0xFFF5F5F5)) // Light Gray bg
                        item.isImportant && item.isWeak -> Pair(Color(0xFFC62828), Color(0xFFFFEBEE)) // Light Red bg
                        !item.isImportant && item.isWeak -> Pair(Color(0xFFE65100), Color(0xFFFFF3E0)) // Light Orange bg
                        else -> Pair(Color(0xFF2E7D32), Color(0xFFE8F5E9)) // Light Green bg
                    }"""

pattern2 = r'\s*val \(statusColor, containerColor\) = when \{\s*item\.statusLabel == "Not Enough Data" -> Pair\(Color\.Black, Color\(0xFFE0E0E0\)\) // Gray bg\s*item\.isImportant && item\.isWeak -> Pair\(Color\.White, Color\(0xFFE53935\)\) // Red bg\s*!item\.isImportant && item\.isWeak -> Pair\(Color\.Black, Color\(0xFFFB8C00\)\) // Orange bg\s*else -> Pair\(Color\.White, Color\(0xFF43A047\)\) // Green bg\s*\}'
content = re.sub(pattern2, replacement2, content, flags=re.MULTILINE)

# Also update the icon button background to match the new lighter theme
content = content.replace(
    "Modifier.background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(50))",
    "Modifier.background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(50))"
)

with open('app/src/main/java/com/example/ui/screens/GuidanceScreen.kt', 'w') as f:
    f.write(content)
