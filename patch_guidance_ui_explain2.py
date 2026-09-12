import re

with open('app/src/main/java/com/example/ui/screens/GuidanceScreen.kt', 'r') as f:
    content = f.read()

replacement2 = """                    val (baseColor, bgColor) = when (topic.priorityLabel) {
                        "High Priority" -> Pair(Color(0xFFE53935), Color(0xFFE53935).copy(alpha = 0.1f))
                        "Medium Priority" -> Pair(Color(0xFFFB8C00), Color(0xFFFB8C00).copy(alpha = 0.1f))
                        else -> Pair(Color(0xFF757575), Color(0xFF757575).copy(alpha = 0.1f))
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = bgColor),
                        border = BorderStroke(1.dp, baseColor.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(topic.priorityLabel.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = baseColor)
                                Text(topic.subject + " - " + topic.chapter, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                Text("PYQ Avg: ${String.format("%.1f", topic.pyqAvg)} | Your Accuracy: ${topic.accuracy?.let { String.format("%.0f%%", it) } ?: "N/A"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(topic.reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            
                            IconButton(onClick = { onPractice(topic.subject, topic.chapter) }, modifier = Modifier.background(baseColor.copy(alpha = 0.1f), RoundedCornerShape(50))) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Practice", tint = baseColor)
                            }
                        }
                    }"""

pattern2 = r'\s*val \(statusColor, containerColor\) = when \(topic\.priorityLabel\) \{\s*"High Priority".*?Icon\(Icons\.Default\.PlayArrow, contentDescription = "Practice", tint = statusColor\)\s*\}\s*\}\s*\}'

content = re.sub(pattern2, replacement2, content, flags=re.MULTILINE | re.DOTALL)

with open('app/src/main/java/com/example/ui/screens/GuidanceScreen.kt', 'w') as f:
    f.write(content)
