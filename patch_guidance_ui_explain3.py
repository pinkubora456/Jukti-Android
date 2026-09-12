import re

with open('app/src/main/java/com/example/ui/screens/GuidanceScreen.kt', 'r') as f:
    content = f.read()

replacement3 = """                    val (baseColor, bgColor) = when {
                        item.statusLabel == "Not Enough Data" -> Pair(Color(0xFF757575), Color(0xFF757575).copy(alpha = 0.1f))
                        item.isImportant && item.isWeak -> Pair(Color(0xFFE53935), Color(0xFFE53935).copy(alpha = 0.1f))
                        !item.isImportant && item.isWeak -> Pair(Color(0xFFFB8C00), Color(0xFFFB8C00).copy(alpha = 0.1f))
                        else -> Pair(Color(0xFF43A047), Color(0xFF43A047).copy(alpha = 0.1f))
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onImprove(item.subject, item.chapter) },
                        colors = CardDefaults.cardColors(containerColor = bgColor),
                        border = BorderStroke(1.dp, baseColor.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.statusLabel.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = baseColor)
                                Text(item.subject + " - " + item.chapter, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                Text("Avg PYQ: ${String.format("%.1f", item.pyqAvg)} | Accuracy: ${item.accuracy?.let { String.format("%.0f%%", it) } ?: "N/A"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            
                            Icon(Icons.Default.ArrowForwardIos, contentDescription = "Improve", tint = baseColor, modifier = Modifier.size(16.dp))
                        }
                    }"""

pattern3 = r'\s*val \(statusColor, containerColor\) = when \{.*?"Not Enough Data".*?Icon\(Icons\.Default\.ArrowForwardIos, contentDescription = "Improve", tint = statusColor, modifier = Modifier\.size\(16\.dp\)\)\s*\}\s*\}'

content = re.sub(pattern3, replacement3, content, flags=re.MULTILINE | re.DOTALL)

if "androidx.compose.foundation.BorderStroke" not in content:
    content = content.replace("import androidx.compose.ui.Modifier", "import androidx.compose.ui.Modifier\nimport androidx.compose.foundation.BorderStroke")

with open('app/src/main/java/com/example/ui/screens/GuidanceScreen.kt', 'w') as f:
    f.write(content)
