import re

with open('app/src/main/java/com/example/ui/screens/GuidanceScreen.kt', 'r') as f:
    content = f.read()

replacement = """                    val (statusColor, statusIcon) = when (topic.priorityLabel) {
                        "High Priority" -> Pair(Color(0xFFE53935), Icons.Default.Warning) // Red
                        "Medium Priority" -> Pair(Color(0xFFFB8C00), Icons.Default.TrendingFlat) // Orange
                        else -> Pair(Color(0xFF757575), Icons.Default.Check) // Gray
                    }"""

pattern = r'\s*val \(statusColor, statusIcon\) = when \(topic\.priorityLabel\) \{\s*"High Priority" -> Pair\(Color\(0xFFE53935\), Icons\.Default\.Warning\)\s*"Medium Priority" -> Pair\(Color\(0xFFFDD835\), Icons\.Default\.TrendingDown\)\s*"Maintain" -> Pair\(Color\(0xFF43A047\), Icons\.Default\.Verified\)\s*else -> Pair\(Color\(0xFF81C784\), Icons\.Default\.Check\)\s*\}'

new_content = re.sub(pattern, replacement, content, flags=re.MULTILINE)

replacement2 = """                    val (statusColor, statusIcon) = when {
                        item.statusLabel == "Not Enough Data" -> Pair(Color(0xFF757575), Icons.Default.HelpOutline) // Gray
                        item.isImportant && item.isWeak -> Pair(Color(0xFFE53935), Icons.Default.Warning) // Red (High Priority)
                        !item.isImportant && item.isWeak -> Pair(Color(0xFFFB8C00), Icons.Default.TrendingDown) // Orange (Medium Priority)
                        item.isImportant && !item.isWeak -> Pair(Color(0xFF43A047), Icons.Default.Verified) // Green
                        else -> Pair(Color(0xFF43A047), Icons.Default.TrendingUp) // Green
                    }"""

pattern2 = r'\s*val \(statusColor, statusIcon\) = when \{\s*item\.statusLabel == "Not Enough Data" -> Pair\(MaterialTheme\.colorScheme\.onSurfaceVariant, Icons\.Default\.HelpOutline\)\s*item\.isImportant && item\.isWeak -> Pair\(Color\(0xFFE53935\), Icons\.Default\.Warning\) // Red\s*item\.isImportant && !item\.isWeak -> Pair\(Color\(0xFF43A047\), Icons\.Default\.Verified\) // Green\s*!item\.isImportant && item\.isWeak -> Pair\(Color\(0xFFFDD835\), Icons\.Default\.TrendingDown\) // Yellow\s*else -> Pair\(Color\(0xFF43A047\), Icons\.Default\.TrendingUp\) // Green\s*\}'

new_content = re.sub(pattern2, replacement2, new_content, flags=re.MULTILINE)

with open('app/src/main/java/com/example/ui/screens/GuidanceScreen.kt', 'w') as f:
    f.write(new_content)
