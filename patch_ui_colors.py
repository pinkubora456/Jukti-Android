import re

with open('app/src/main/java/com/example/ui/screens/GuidanceScreen.kt', 'r') as f:
    content = f.read()

replacement1 = """                    val (statusColor, containerColor) = when (topic.priorityLabel) {
                        "High Priority" -> Pair(Color.White, Color(0xFFE53935)) // Red bg
                        "Medium Priority" -> Pair(Color.Black, Color(0xFFFB8C00)) // Orange bg
                        else -> Pair(Color.Black, Color(0xFFE0E0E0)) // Gray bg
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = containerColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(topic.priorityLabel.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = statusColor.copy(alpha = 0.8f))
                                Text(topic.subject + " - " + topic.chapter, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = statusColor)
                                Text("PYQ Avg: ${String.format("%.1f", topic.pyqAvg)} | Your Accuracy: ${topic.accuracy?.let { String.format("%.0f%%", it) } ?: "N/A"}", style = MaterialTheme.typography.bodySmall, color = statusColor.copy(alpha = 0.8f))
                                Text(topic.reason, style = MaterialTheme.typography.bodySmall, color = statusColor.copy(alpha = 0.8f))
                            }
                            
                            IconButton(onClick = { onPractice(topic.subject, topic.chapter) }, modifier = Modifier.background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(50))) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Practice", tint = statusColor)
                            }
                        }
                    }"""

pattern1 = r'\s*val \(statusColor, statusIcon\) = when \(topic\.priorityLabel\) \{.*?(?=if \(index < topics\.size - 1\) HorizontalDivider)if \(index < topics\.size - 1\) HorizontalDivider\(modifier = Modifier\.padding\(vertical = 4\.dp\)\)'

new_content = re.sub(pattern1, replacement1, content, flags=re.MULTILINE | re.DOTALL)

replacement2 = """                    val (statusColor, containerColor) = when {
                        item.statusLabel == "Not Enough Data" -> Pair(Color.Black, Color(0xFFE0E0E0)) // Gray bg
                        item.isImportant && item.isWeak -> Pair(Color.White, Color(0xFFE53935)) // Red bg
                        !item.isImportant && item.isWeak -> Pair(Color.Black, Color(0xFFFB8C00)) // Orange bg
                        else -> Pair(Color.White, Color(0xFF43A047)) // Green bg
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onImprove(item.subject, item.chapter) },
                        colors = CardDefaults.cardColors(containerColor = containerColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.statusLabel.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = statusColor.copy(alpha = 0.8f))
                                Text(item.subject + " - " + item.chapter, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = statusColor)
                                Text("Avg PYQ: ${String.format("%.1f", item.pyqAvg)} | Accuracy: ${item.accuracy?.let { String.format("%.0f%%", it) } ?: "N/A"}", style = MaterialTheme.typography.bodySmall, color = statusColor.copy(alpha = 0.8f))
                            }
                            
                            Icon(Icons.Default.ArrowForwardIos, contentDescription = "Improve", tint = statusColor, modifier = Modifier.size(16.dp))
                        }
                    }"""

pattern2 = r'\s*val \(statusColor, statusIcon\) = when \{.*?(?=if \(index < items\.size - 1\) HorizontalDivider)if \(index < items\.size - 1\) HorizontalDivider\(modifier = Modifier\.padding\(vertical = 4\.dp\)\)'

new_content = re.sub(pattern2, replacement2, new_content, flags=re.MULTILINE | re.DOTALL)


with open('app/src/main/java/com/example/ui/screens/GuidanceScreen.kt', 'w') as f:
    f.write(new_content)
